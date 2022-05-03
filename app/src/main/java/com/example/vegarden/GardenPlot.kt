package com.example.vegarden

import java.io.Serializable
import java.util.Date

class GardenPlot(
    val cropID: Int = 0,
    val sowingDate: Date?,
    val numberOfPlants: Int?,
    val userNotes: String?,
) : Serializable {
    fun toMap(): MutableMap<String, Any?> {
        return mutableMapOf(
            "cropID" to cropID,
            "sowingDate" to sowingDate,
            "numberOfPlants" to numberOfPlants,
            "userNotes" to userNotes
        )
    }
}