package org.soundcloud

data class TrackResponse(
    val id: Long,
    val title: String,
    val artist: String,
    val artworkUrl: String?,
    val duration: Long,
    val permalinkUrl: String
)
