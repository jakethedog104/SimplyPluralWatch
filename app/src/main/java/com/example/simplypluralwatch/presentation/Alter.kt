package com.example.simplypluralwatch.presentation

import androidx.compose.ui.graphics.Color
import com.example.simplypluralwatch.BuildConfig
import java.time.Instant
import java.util.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

data class Alter (
    val name: String,
    val id: String,
    val color: Color,
    var startTime: Long?,
    var docID: String?) {
    constructor(pName: String, pID: String, pColor: Color) : this (
        name = pName,
        id = pID,
        color = pColor,
        startTime = null,
        docID = null
    )
}

@Serializable
@JsonIgnoreUnknownKeys
@ExperimentalSerializationApi
data class SPAlterContainer (val id: String, val content: SPAlter)
@Serializable
@JsonIgnoreUnknownKeys
@ExperimentalSerializationApi
data class SPAlter (val name: String, val color: String, val lastOperationTime : Long?)

@Serializable
@JsonIgnoreUnknownKeys
@ExperimentalSerializationApi
data class SPFrontContainer (val id: String, val content: SPFrontRead)
@Serializable
@JsonIgnoreUnknownKeys
@ExperimentalSerializationApi
data class SPFrontRead (val startTime : Long, val member : String)

@Serializable
@JsonIgnoreUnknownKeys
@ExperimentalSerializationApi
data class SPFrontStart (
    val custom : Boolean,
    val live : Boolean,
    val startTime : Long,
    val member : String
) {
    constructor(pMember: String, pStartTime: Long) : this (
        custom = false,
        live = true,
        startTime = pStartTime,
        member = pMember
    )
}
@Serializable
@JsonIgnoreUnknownKeys
@ExperimentalSerializationApi
data class SPFrontEnd (
    val custom : Boolean,
    val live : Boolean,
    val startTime : Long,
    val endTime : Long,
    val member : String
) {
    constructor(pMember: String, pStartTime: Long, pEndTime: Long) : this (
        custom = false,
        live = false,
        startTime = pStartTime,
        endTime = pEndTime,
        member = pMember
    )
}

@ExperimentalStdlibApi
@ExperimentalSerializationApi
fun spAlterContainerToAlter(spAlterContainer : List<SPAlterContainer>) : ArrayList<Alter> {
    var allMembers = ArrayList<Alter>()
    for (a in spAlterContainer) {
        var color = Color(
            a.content.color.substring(1, 3).hexToInt(),
            a.content.color.substring(3, 5).hexToInt(),
            a.content.color.substring(5).hexToInt(),
            255)
        allMembers.add(Alter(a.content.name, a.id, color))
    }
    return allMembers
}

// Note this function modifies the param allAlters
@ExperimentalSerializationApi
fun spFrontContainerToAlter(spFrontContainer : Array<SPFrontContainer>, allAlters : ArrayList<Alter>) : ArrayList<Alter> {
    var allFronters = ArrayList<Alter>()
    for (a in spFrontContainer) {
        for (alter in allAlters) {
            if (a.content.member == alter.id) {
                alter.startTime = a.content.startTime
                alter.docID = a.id
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

    if (response.code == 200) {
        var json : String = response.body!!.string()
        var convertedJson = Json.decodeFromString<List<SPAlterContainer>>(json)
        // Sort response by most recent interaction
        var sortedJson = convertedJson.sortedBy { it.content.lastOperationTime }
        return spAlterContainerToAlter(sortedJson)
    } else {
        error("Call failed: " + response.code.toString())
    }
}

@ExperimentalStdlibApi
@ExperimentalSerializationApi
fun getAllCustomFronts(systemID : String) : ArrayList<Alter> {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(BuildConfig.spURI + "customFronts/" + systemID)
        .addHeader("Authorization", BuildConfig.apiKey)
        .build()
    val response = client.newCall(request).execute()

    if (response.code == 200) {
        var json : String = response.body!!.string()
        var convertedJson = Json.decodeFromString<List<SPAlterContainer>>(json)
        // Sort response by most recent interaction
        var sortedJson = convertedJson.sortedBy { it.content.lastOperationTime }
        return spAlterContainerToAlter(sortedJson)
    } else {
        error("Call failed: " + response.code.toString())
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

    if (response.code == 200) {
        var json : String = response.body!!.string()
        var convertedJson = Json.decodeFromString<Array<SPFrontContainer>>(json)
        return spFrontContainerToAlter(convertedJson, allAlters)
    } else {
        error("Call failed: " + response.code.toString())
    }
}

@ExperimentalSerializationApi
fun addAlterToFront(alter: Alter) {
    if (!currentFronters.contains(alter)) {
        val client = OkHttpClient()

        var startTime: Long = Instant.now().toEpochMilli()
        alter.startTime = startTime
        val mediaType = "application/json".toMediaType()
        var postBody =
            Json.encodeToString(SPFrontStart(alter.id, startTime)).toRequestBody(mediaType)

        val request = Request.Builder()
            .url(BuildConfig.spURI + "frontHistory/")
            .addHeader("Authorization", BuildConfig.apiKey)
            .addHeader("Content-Type", "application/json")
            .post(postBody)
            .build()

        // Put the request in a thread since we are calling from Main
        Thread {
            val response = client.newCall(request).execute()

            if (response.code == 200) {
                var json: String = response.body!!.string()
                alter.docID = json.replace("\"", "")
                currentFronters.add(alter)
            }
        }.start()
    }
}

@ExperimentalSerializationApi
fun removeAlterFromFront(alter: Alter) {
    if (currentFronters.contains(alter)) {
        val client = OkHttpClient()

        var endTime: Long = Instant.now().toEpochMilli()
        val mediaType = "application/json".toMediaType()
        var postBody =
            Json.encodeToString(SPFrontEnd(alter.id, alter.startTime!!, endTime)).toRequestBody(mediaType)

        val request = Request.Builder()
            .url(BuildConfig.spURI + "frontHistory/" + alter.docID)
            .addHeader("Authorization", BuildConfig.apiKey)
            .addHeader("Content-Type", "application/json")
            .patch(postBody)
            .build()

        // Put the request in a thread since we are calling from Main
        Thread {
            val response = client.newCall(request).execute()

            if (response.code == 200) {
                alter.startTime = null
                alter.docID = null
                currentFronters.remove(alter)
            }
        }.start()
    }
}
