package com.enciorcal.filefolderer.view

import com.enciorcal.filefolderer.app.Styles
import com.enciorcal.filefolderer.controller.MainController
import com.enciorcal.filefolderer.model.PathTransformation
import javafx.beans.property.StringProperty
import javafx.scene.control.Alert
import javafx.scene.layout.Priority
import tornadofx.*
import tornadofx.FX.Companion.messages

class MainView : View(messages["FileFolderer"]) {
    private val controller: MainController by inject()
    val state: StringProperty = controller.state
    val regexProperty: StringProperty = controller.filterRegex

    override val root = borderpane() {
        addClass(Styles.masterStyle)
        top = form {
            fieldset {
                field(messages["Directory"]) {
                    textfield(controller.selectedFile) {
                        isEditable = false
                    }
                    button(messages["Select"]) {
                        action {
                            controller.selectAction()
                        }
                    }
                }
            }
        }

        center = tableview(controller.transformationList) {
            vgrow = Priority.ALWAYS
            smartResize()
            enableCellEditing()
            regainFocusAfterEdit()

            column(messages["Original"], PathTransformation::originalPath).weightedWidth(1)
            column(messages["New"], PathTransformation::newPath).weightedWidth(1)
            column(messages["Ignore"], PathTransformation::ignore).useCheckbox()
            column(messages["Exists"], PathTransformation::existsInDestination).useCheckbox(editable = false)
        }

        bottom = form {
            fieldset {
                field(messages["Regex"]) {
                    textfield(regexProperty)
                    button(messages["Preview"]) {
                        enableWhen(
                                state.isEqualTo(State.DirectoryLoaded.toString())
                                        .or(state.isEqualTo(State.PreviewLoaded.toString()))
                        )
                        action {
                            controller.previewAction()
                        }
                    }
                    button(messages["Process"]) {
                        enableWhen(state.isEqualTo(
                                State.PreviewLoaded.toString())
                                .and(regexProperty.isNotEmpty)
                        )
                        action {
                            controller.processAction()
                            alert(Alert.AlertType.CONFIRMATION, messages["FolderingComplete"])
                        }
                    }
                }
            }
        }
    }

    enum class State {
        Init,
        DirectoryLoaded,
        PreviewLoaded
    }
}