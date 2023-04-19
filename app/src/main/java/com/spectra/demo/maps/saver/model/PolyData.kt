package com.spectra.demo.maps.saver.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class PolyData(
    var points: List<Point> = emptyList(),
    var type: String = PolyMode.GON.name,
    var title: String = "Snap Title" ,
    var desc: String = ""
)

@Serializable
data class Point(
    var lat: Double = 0.0,
    var lng: Double = 0.0
)

fun PolyData.getAsString() = Json.encodeToString(this)

fun String.toPolyData(): PolyData = Json.decodeFromString(this)