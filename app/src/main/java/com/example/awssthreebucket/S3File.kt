package com.example.awssthreebucket

data class S3File(
    val path: String,
    val key: String,
    val origin: String
)
