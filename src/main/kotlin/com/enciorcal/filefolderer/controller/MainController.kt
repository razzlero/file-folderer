package com.enciorcal.filefolderer.controller

import com.enciorcal.filefolderer.model.PathTransformation
import com.enciorcal.filefolderer.service.Folderer
import com.enciorcal.filefolderer.view.MainView
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import tornadofx.*

class MainController: Controller() {
    private val folderer = Folderer()

    val state: StringProperty = SimpleStringProperty(MainView.State.Init.toString())

    val transformationList = ArrayList<PathTransformation>().observable()
    val selectedFile: StringProperty = SimpleStringProperty("")
    val filterRegex: StringProperty = SimpleStringProperty("")

    fun selectAction() {
        val directory = chooseDirectory(
                title = "Select Directory"
        )

        if (directory != null) {
            selectedFile.value = directory.canonicalPath
            loadCurrentDirectoryData()
        }
    }

    fun previewAction() {
        val path = selectedFile.value
        val regex = filterRegex.value.toRegex()
        val previewData = folderer.getTransformationList(path, regex)

        updateTableData(previewData)
        state.set(MainView.State.PreviewLoaded.toString())
    }

    fun processAction() {
        folderer.transform(transformationList)
        loadCurrentDirectoryData()
    }

    private fun loadCurrentDirectoryData(){
        val newData = folderer.getDirContent(selectedFile.value)
        updateTableData(newData)
        state.set(MainView.State.DirectoryLoaded.toString())
    }

    private fun updateTableData(newData: List<PathTransformation>) {
        transformationList.clear()
        transformationList.addAll(newData)
    }
}