package ru.netology.nework.dto

import java.io.File

data class Media(val id: String)

data class MediaUpload(val file: File)

data class MediaResponse(val url: String)