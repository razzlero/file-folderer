package com.enciorcal.filefolderer.model

import java.nio.file.Path

data class PathTransformation(
        val originalPath: Path,
        var newPath: Path? = null,
        var ignore: Boolean? = null,
        var existsInDestination: Boolean? = null
)