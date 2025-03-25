package com.example.simplypluralwatch.presentation

import androidx.compose.ui.graphics.Color
import com.example.simplypluralwatch.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.serialization.*
import kotlinx.serialization.json.*

data class Alter (val name: String, val id: String, val color: Color, var startTime: Long?)

@Serializable
@JsonIgnoreUnknownKeys
@ExperimentalSerializationApi
data class SPAlterContainer (val id: String, val content: SPAlter)
@Serializable
@JsonIgnoreUnknownKeys
@ExperimentalSerializationApi
data class SPAlter (val name: String, val color: String)

@Serializable
@ExperimentalSerializationApi
data class SPFrontContainer (val exists : Boolean, val id: String, val content: SPFront)
@Serializable
@JsonIgnoreUnknownKeys
@ExperimentalSerializationApi
data class SPFront (
    val custom : Boolean,
    val live : Boolean,
    val startTime : Long,
    val endTime : Int?,
    val member : String
) {
    constructor(pMember: String, pStartTime: Long) : this (
        custom = false,
        live = true,
        startTime = pStartTime,
        endTime = null,
        member = pMember
    )
}

@ExperimentalStdlibApi
@ExperimentalSerializationApi
fun spAlterContainerToAlter(spAlterContainer : Array<SPAlterContainer>) : ArrayList<Alter> {
    var allMembers = ArrayList<Alter>()
    for (a in spAlterContainer) {
        var color = Color(
            a.content.color.substring(1, 3).hexToInt(),
            a.content.color.substring(3, 5).hexToInt(),
            a.content.color.substring(5).hexToInt(),
            255)
        allMembers.add(Alter(a.content.name, a.id, color, null))
    }
    return allMembers
}

@ExperimentalSerializationApi
fun spFrontContainerToAlter(spFrontContainer : Array<SPFrontContainer>) : ArrayList<Alter> {
    var allFronters = ArrayList<Alter>()
    for (a in spFrontContainer) {
        for (alter in allAlters) {
            if (a.content.member == alter.id) {
                alter.startTime = a.content.startTime
                allFronters.add(alter)
            }
        }
    }
    return allFronters
}

fun getAlterNames(al : ArrayList<Alter>) : String {
    var names = ""
    for (a in al) {
        if (names != "") { names += ", "}
        names += a.name
    }
    return names
}

@ExperimentalStdlibApi
@ExperimentalSerializationApi
fun getAllAlters(systemID : String) : ArrayList<Alter> {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(BuildConfig.spURI + "members/" + systemID)
        .addHeader("Authorization", BuildConfig.apiKey)
        .build()
    val response = client.newCall(request).execute()

    if (response.code == 404) {
        error("System Not Found")
    } else {
        // Response Code 200
        var json : String = response.body!!.string()
        var convertedJson = Json.decodeFromString<Array<SPAlterContainer>>(json)
        return spAlterContainerToAlter(convertedJson)
    }
}

@ExperimentalSerializationApi
fun getFronters() : ArrayList<Alter> {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(BuildConfig.spURI + "fronters/")
        .addHeader("Authorization", BuildConfig.apiKey)
        .build()
    val response = client.newCall(request).execute()

    if (response.code == 404) {
        error("System Not Found")
    } else {
        // Response Code 200
        var json : String = response.body!!.string()
        var convertedJson = Json.decodeFromString<Array<SPFrontContainer>>(json)
        return spFrontContainerToAlter(convertedJson)
    }
}

@ExperimentalSerializationApi
fun addAlterToFront(alter: Alter) {
    // TODO: Add via API
}

@ExperimentalSerializationApi
fun removeAlterFromFront(alter: Alter) {
    // TODO: Remove via API
}
