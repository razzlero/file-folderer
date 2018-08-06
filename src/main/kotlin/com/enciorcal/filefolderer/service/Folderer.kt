package com.enciorcal.filefolderer.service

import com.enciorcal.filefolderer.model.PathTransformation
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.streams.toList


class Folderer(val fs: FileSystem = FileSystems.getDefault()) {
    /**
     * Get all files and folders in the specified directory.
     * Returned as a transformation list with no newPath set.
     */
    fun getDirContent(pathString: String) : List<PathTransformation> {
        val path = fs.getPath(pathString)
        val pathList = Files.list(path).toList()

        val transformationList = ArrayList<PathTransformation>()

        for (curPath in pathList) {
            val newTransformation = PathTransformation(curPath)
            transformationList.add(newTransformation)
        }

        return transformationList
    }

    /**
     * Returns the transformations of files and folders grouped by the given regex.
     * Any files matching the regex will be grouped.
     * Files are grouped based on capture groups.
     * All files with the same capture group will be grouped into folders with names based on the capture groups.
     * Files with no transformation will have a newPath value of null.
     */
    fun getTransformationList(pathString: String, regex: Regex): List<PathTransformation> {
        val transformationList = getDirContent(pathString)

        // We go through each original path can determine their transformed path.
        for (transformation in transformationList) {
            val path = transformation.originalPath
            // First we assume there is no transformation and we set the new path the same as the current path.
            var newPath = path
            // We only group files, so we ignore directories.
            if(!Files.isDirectory(path)) {
                val matchResult = regex.matchEntire(path.fileName.toString())

                if (matchResult != null && matchResult.groupValues.size > 1) {

                    // Determine the name of the grouped directory based on the capturing groups.
                    val groupValues = matchResult.groupValues
                    var groupDir = ""
                    // The first value is the full match so we ignore it
                    for (i in 1..groupValues.lastIndex) {
                        val groupValue = groupValues[i]
                        if (groupDir.isNotEmpty()) {
                            groupDir += "_"
                        }
                        groupDir += groupValue

                    }
                    // Add the group directory between the current directory and the file name.
                    val groupedPath = fs.getPath(path.parent.toString(), groupDir.trim(), path.fileName.toString())
                    newPath = groupedPath
                    if (Files.exists(groupedPath)) {
                        transformation.existsInDestination = true
                        transformation.ignore = true
                    } else {
                        transformation.existsInDestination = false
                        transformation.ignore = false
                    }
                }
            }
            transformation.newPath = newPath
        }
        return transformationList
    }

    /**
     * Performs the given transformations
     */
    fun transform(transformationList: List<PathTransformation>) {

        for (transformation in transformationList) {
            val originalPath = transformation.originalPath.normalize()
            val newPath = transformation.newPath?.normalize()
            val ignore = transformation.ignore == true
            if (newPath != null && originalPath != newPath && !ignore) {
                if (Files.notExists(newPath.parent)) {
                    Files.createDirectories(newPath.parent)
                }
                Files.move(originalPath, newPath, StandardCopyOption.REPLACE_EXISTING)
                // NOTE: It's possible that an unexpected IOException could be thrown here.
                // We'll just use TornadoFX's default exception handling to show an error.
            }
        }
    }
}