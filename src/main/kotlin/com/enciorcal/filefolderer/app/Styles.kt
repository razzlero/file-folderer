package com.enciorcal.filefolderer.app

import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val masterStyle by cssclass()
    }

    init {
        masterStyle {
            prefWidth = 900.px
            prefHeight = 650.px
        }
    }
}