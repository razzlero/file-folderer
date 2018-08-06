package com.enciorcal.filefolderer

import com.enciorcal.filefolderer.service.Folderer
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import org.junit.jupiter.api.Test
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path

internal class FoldererTest {
    private val fs: FileSystem = Jimfs.newFileSystem(Configuration.unix())
    private val baseDir = "dir"
    private val folderer = Folderer(fs)

    init {
        val basePath = fs.getPath(baseDir)
        Files.createDirectories(basePath)
    }

    @Test
    fun testGetDirContent() {
        println("[testGetDirContent]")
        createBaseDirSubDir("a random dir")
        createBaseDirSubDir("test")
        createBaseDirFile("test 1.txt")
        createBaseDirFile("test 2.txt")
        createBaseDirFile("test 3.txt")

        val result = folderer.getDirContent(baseDir)

        val resultPathList = ArrayList<Path>()
        for (transformation in result) {
            resultPathList.add(transformation.originalPath)
        }
        assert(resultPathList.containsAll(getDirPathTreeAsList()))
    }

    /**
     * Test that getTransformationList generates the expected transformation list.
     */
    @Test
    fun testGetTransformationList() {
        println("[testGetTransformationList]")
        createBaseDirSubDir("a random dir")
        createBaseDirSubDir("test")
        createBaseDirFile("test 1.txt")
        createBaseDirFile("test 2.txt")
        createBaseDirFile("test 3.txt")
        createBaseDirFile("File which doesn't match regex")
        createBaseDirFile("A file 1.txt")
        createBaseDirFile("A file 2.txt")
        createBaseDirFile("A file 3.txt")

        val regex = "(.*)\\s+\\d+\\.txt".toRegex()

        val expectedPaths = ArrayList<Path>()
        expectedPaths.add(fs.getPath(baseDir, "a random dir"))
        expectedPaths.add(fs.getPath(baseDir, "test"))
        expectedPaths.add(fs.getPath(baseDir, "test", "test 1.txt"))
        expectedPaths.add(fs.getPath(baseDir, "test", "test 2.txt"))
        expectedPaths.add(fs.getPath(baseDir, "test", "test 3.txt"))
        expectedPaths.add(fs.getPath(baseDir, "File which doesn't match regex"))
        expectedPaths.add(fs.getPath(baseDir, "A file", "A file 1.txt"))
        expectedPaths.add(fs.getPath(baseDir, "A file", "A file 2.txt"))
        expectedPaths.add(fs.getPath(baseDir, "A file", "A file 3.txt"))

        checkTransformedPaths(regex, expectedPaths)
    }

    /**
     * Test that getTransformationList generates the expected transformation list when multiple capture groups are used.
     */
    @Test
    fun testGetTransformationListMultiCapture() {
        println("[testGetTransformationListMultiCapture]")
        createBaseDirSubDir("a random dir")
        createBaseDirSubDir("test")
        createBaseDirFile("test 1.txt")
        createBaseDirFile("test 2.txt")
        createBaseDirFile("A file 1.txt")
        createBaseDirFile("A file 2.txt")
        createBaseDirFile("A file 3.txt")

        val regex = "(.+)\\s(.+)\\s+\\d+\\.txt".toRegex()

        val expectedPaths = ArrayList<Path>()
        expectedPaths.add(fs.getPath(baseDir, "a random dir"))
        expectedPaths.add(fs.getPath(baseDir, "test"))
        expectedPaths.add(fs.getPath(baseDir, "test 1.txt"))
        expectedPaths.add(fs.getPath(baseDir, "test 2.txt"))
        expectedPaths.add(fs.getPath(baseDir, "A_file", "A file 1.txt"))
        expectedPaths.add(fs.getPath(baseDir, "A_file", "A file 2.txt"))
        expectedPaths.add(fs.getPath(baseDir, "A_file", "A file 3.txt"))

        checkTransformedPaths(regex, expectedPaths)
    }

    private fun checkTransformedPaths(regex: Regex, expectedPaths: List<Path>) {
        val result = folderer.getTransformationList(baseDir, regex)

        val resultPathList = ArrayList<Path>()

        println("- New Paths -")
        for (transformation in result) {
            val newPath = transformation.newPath
            if (newPath != null) {
                resultPathList.add(newPath)
                println(newPath)
            }
        }

        assert(expectedPaths.containsAll(resultPathList))
    }

    @Test
    fun testTransform() {
        println("[testTransform]")
        createBaseDirSubDir("a random dir")
        createBaseDirSubDir("test")
        createBaseDirFile("test 1.txt")
        createBaseDirFile("test 2.txt")
        createBaseDirFile("File which doesn't match regex")
        createBaseDirFile("A file 1.txt")
        createBaseDirFile("A file 2.txt")

        val regex = "(.*)\\s+\\d+\\.txt".toRegex()

        val expectedPaths = ArrayList<Path>()
        // Get the expected paths from getTransformationList.
        val transformationList = folderer.getTransformationList(baseDir, regex)
        for (transformation in transformationList) {
            val newPath = transformation.newPath
            if (newPath != null) {
                expectedPaths.add(newPath)
                println(newPath)
            }
        }

        folderer.transform(transformationList)

        // Get the current actual paths from the directory.
        val newPaths = getDirPathTreeAsList()

        newPaths.containsAll(expectedPaths)
    }

    /**
     * Test that duplicates in the destination are ignored by default.
     */
    @Test
    fun testTransformDuplicate() {
        createBaseDirFile("test 1.txt")
        createBaseDirSubDir("test")
        createBaseDirFile("test/test 1.txt")
        val originalBaseDirPaths = getDirPathTreeAsList()

        val regex = "(.*)\\s+\\d+\\.txt".toRegex()
        val transformationList = folderer.getTransformationList(baseDir, regex)
        folderer.transform(transformationList)
        val newPaths = getDirPathTreeAsList()

        assert(originalBaseDirPaths == newPaths)
    }

    /**
     * Gets the content of the current directory and sub directories.
     */
    private fun getDirPathTreeAsList(): List<Path> {
        println("- path list -")
        val dirContent = folderer.getDirContent(baseDir)
        val paths = ArrayList<Path>()
        for (transformation in dirContent) {
            val path = transformation.originalPath
            println(path.toString())
            paths.add(path)
            if (Files.isDirectory(path)) {

                for (subPath in Files.list(path)) {
                    println(subPath.toString())
                    paths.add(subPath)
                }
            }
        }
        return paths
    }

    private fun createBaseDirFile(pathString: String){
        val path = fs.getPath(baseDir, pathString)
        Files.createFile(path)
    }

    private fun createBaseDirSubDir(pathString: String){
        val path = fs.getPath(baseDir, pathString)
        Files.createDirectories(path)
    }
}