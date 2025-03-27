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


@ExperimentalSerializationApi
fun getUserID() : String {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(BuildConfig.spURI + "me/")
        .addHeader("Authorization", BuildConfig.apiKey)
        .build()
    val response = client.newCall(request).execute()

    if (response.code == 200) {
        var json : String = response.body!!.string()
        var convertedJson = Json.decodeFromString<User>(json)
        return convertedJson.id
    } else {
        error("Call failed: " + response.code.toString())
    }
}

@Serializable
@JsonIgnoreUnknownKeys
@ExperimentalSerializationApi
data class User (val id: String)

data class Alter (
    val name: String,
    val id: String,
    val color: Color,
    var startTime: Long?,
    var endTime: Long?,
    var docID: String?) {
    constructor(pName: String, pID: String, pColor: Color) : this (
        name = pName,
        id = pID,
        color = pColor,
        startTime = null,
        endTime = null,
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
data class SPFrontRead (val startTime : Long, val endTime : Long?, val member : String)

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


@ExperimentalStdlibApi
@ExperimentalSerializationApi
fun getFrontHistory(systemID : String) {
    val client = OkHttpClient()

    var startTime: Long = Instant.now().toEpochMilli() - 604800000 // one week ago
    var endTime: Long = Instant.now().toEpochMilli()

    val request = Request.Builder()
        .url(BuildConfig.spURI + "frontHistory/" + systemID + "?startTime=" + startTime + "&endTime=" + endTime)
        .addHeader("Authorization", BuildConfig.apiKey)
        .build()
    val response = client.newCall(request).execute()

    if (response.code == 200) {
        var json : String = response.body!!.string()
        var convertedJson = Json.decodeFromString<List<SPFrontContainer>>(json)
        convertedJson.sortedByDescending { it.content.endTime }
        saveEndTime(convertedJson, allAlters + allCustomFronts)
        // Sort
        allAlters = allAlters.sortedByDescending { it.endTime }
        allCustomFronts = allCustomFronts.sortedByDescending { it.endTime }
    } else {
        error("Call failed: " + response.code.toString())
    }
}

@ExperimentalStdlibApi
@ExperimentalSerializationApi
fun getAllAlters(systemID : String) : List<Alter> {
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
fun getAllCustomFronts(systemID : String) : List<Alter> {
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
fun getFronters() : List<Alter> {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(BuildConfig.spURI + "fronters/")
        .addHeader("Authorization", BuildConfig.apiKey)
        .build()
    val response = client.newCall(request).execute()

    if (response.code == 200) {
        var json : String = response.body!!.string()
        var convertedJson = Json.decodeFromString<Array<SPFrontContainer>>(json)
        return spFrontContainerToAlter(convertedJson, allAlters + allCustomFronts)
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
        alter.endTime = startTime + 1
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
                currentFronters = currentFronters.plus(alter)
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
                currentFronters = currentFronters.minus(alter)
            }
        }.start()
    }
}
