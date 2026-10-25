package com.kampplus.havali.data.local.dto

import com.google.gson.annotations.SerializedName

/** assets/map/turkiye_map.json dosyasının JSON biçimi. */
data class MapFileDto(
    @SerializedName("source") val source: String?,
    @SerializedName("layers") val layers: List<MapLayerDto>?
)

data class MapLayerDto(
    @SerializedName("id") val id: String?,
    @SerializedName("closed") val closed: Boolean?,
    @SerializedName("rings") val rings: List<List<Float>>?
)
