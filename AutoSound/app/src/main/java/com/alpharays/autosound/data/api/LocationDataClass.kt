package com.alpharays.autosound.data.api

data class PlaceData(
    val html_attributions: List<String>,
    val results: List<Place>,
    val status: String
)

data class Place(
    val geometry: Geometry,
    val icon: String,
    val icon_background_color: String,
    val icon_mask_base_uri: String,
    val name: String,
    val photos: List<Photo>?,
    val place_id: String,
    val reference: String,
    val scope: String,
    val types: List<String>,
    val vicinity: String
)

data class Geometry(
    val location: Location,
    val viewport: Viewport
)

data class Location(
    val lat: Double,
    val lng: Double
)

data class Viewport(
    val northeast: Location,
    val southwest: Location
)

data class Photo(
    val height: Int,
    val html_attributions: List<String>,
    val photo_reference: String,
    val width: Int
)
