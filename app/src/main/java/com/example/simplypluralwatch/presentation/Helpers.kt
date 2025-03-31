package com.example.simplypluralwatch.presentation

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.core.content.edit
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.math.pow
import kotlin.math.sqrt

fun getBestTextColor(backgroundColor: Color): Color {
    val luminance = sqrt(backgroundColor.red.pow(2) * 0.241 + backgroundColor.green.pow(2) * 0.691 + backgroundColor.blue.pow(2) * 0.068)
    return if (luminance > 0.65) Color.DarkGray else Color.White
}

@ExperimentalStdlibApi
@ExperimentalSerializationApi
fun spAlterContainerToAlter(spAlterContainer : List<SPAlterContainer>) : List<Alter> {
    var allMembers = listOf<Alter>()
    for (a in spAlterContainer) {
        if (!a.content.archived) {
            var color = Color(
                a.content.color.substring(1, 3).hexToInt(),
                a.content.color.substring(3, 5).hexToInt(),
                a.content.color.substring(5).hexToInt(),
                255)
            allMembers = allMembers.plus(Alter(a.content.name, a.id, color))
        }
    }
    return allMembers
}

// Note this function modifies the param allFronters
@ExperimentalSerializationApi
fun spFrontContainerToAlter(spFrontContainer : Array<SPFrontContainer>, allFronters : List<Alter>) : List<Alter> {
    var currentFronters = listOf<Alter>()
    for (a in spFrontContainer) {
        for (alter in allFronters) {
            if (a.content.member == alter.id) {
                // Found it
                alter.startTime = a.content.startTime
                alter.docID = a.id
                currentFronters = currentFronters.plus(alter)
                // Move to next
                break
            }
        }
    }
    return currentFronters
}

// Note this function modifies the param allFronters
@ExperimentalSerializationApi
fun saveEndTime(spFrontContainer : List<SPFrontContainer>, allFronters : List<Alter>) {
    for (alter in allFronters) {
        for (a in spFrontContainer) {
            if (a.content.member == alter.id) {
                // Found it
                alter.endTime = a.content.endTime
                // Move to next
                break
            }
        }
        // If not found leave as null
    }
}

fun getAlterNames(al : List<Alter>) : String {
    var names = ""
    for (a in al) {
        if (names != "") { names += ", "}
        names += a.name
    }
    return names
}

fun sortFrontList(list : List<Alter>) : List<Alter> {
    // Sort by who fronted most recently
    // If your currently fronting, bop to top
    return list.sortedByDescending { it.endTime }.sortedByDescending { it.startTime }
}

fun writeString(context: Context, key: String?, property: String?) {
    context.getSharedPreferences("spw_preference_file_key", Context.MODE_PRIVATE).edit() {
        putString(key, property)
    }
}

fun readString(context: Context, key: String?): String? {
    return context.getSharedPreferences("spw_preference_file_key", Context.MODE_PRIVATE)
        .getString(key, null)
}
