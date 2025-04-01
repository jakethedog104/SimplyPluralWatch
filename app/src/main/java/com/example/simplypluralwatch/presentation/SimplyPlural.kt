package com.example.simplypluralwatch.presentation

import android.content.Context
import com.example.simplypluralwatch.BuildConfig
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant

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

@ExperimentalStdlibApi
@ExperimentalSerializationApi
fun getFrontHistory(systemID : String,  allFronts : List<Alter>) {
    val client = OkHttpClient()

    var startTime: Long = Instant.now().toEpochMilli() - 604800000 // one week ago
    var endTime: Long = Instant.now().toEpochMilli()

    val request = Request.Builder()
        .url(BuildConfig.spURI + "frontHistory/" + systemID +
                "?startTime=" + startTime +
                "&endTime=" + endTime +
                // Ask for the list sorted by
                // who started fronting first
                "&sortBy=startTime&sortOrder=-1")
        .addHeader("Authorization", BuildConfig.apiKey)
        .build()
    val response = client.newCall(request).execute()

    if (response.code == 200) {
        var json : String = response.body!!.string()
        var convertedJson = Json.decodeFromString<List<SPFrontContainer>>(json)
        saveEndTime(convertedJson, allFronts)
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
        return sortFrontList(spAlterContainerToAlter(convertedJson))
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
        return sortFrontList(spAlterContainerToAlter(convertedJson))
    } else {
        error("Call failed: " + response.code.toString())
    }
}

@ExperimentalSerializationApi
fun getFronters(context : Context, allFronts : List<Alter>) : List<Alter> {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(BuildConfig.spURI + "fronters/")
        .addHeader("Authorization", BuildConfig.apiKey)
        .build()
    val response = client.newCall(request).execute()

    if (response.code == 200) {
        var json : String = response.body!!.string()
        var convertedJson = Json.decodeFromString<Array<SPFrontContainer>>(json)
        var currentFronters = spFrontContainerToAlter(convertedJson, allFronts)
        writeString(context, "currentFronters", getAlterNames(currentFronters))
        return currentFronters
    } else {
        error("Call failed: " + response.code.toString())
    }
}

@ExperimentalSerializationApi
fun addAlterToFront(context : Context, alter: Alter, currentFronters: List<Alter>) : List<Alter> {
    if (!currentFronters.contains(alter)) {
        val client = OkHttpClient()

        var startTime: Long = Instant.now().toEpochMilli()
        val mediaType = "application/json".toMediaType()
        var postBody =
            Json.encodeToString(SPFrontStart(alter.id, startTime)).toRequestBody(mediaType)

        val request = Request.Builder()
            .url(BuildConfig.spURI + "frontHistory/")
            .addHeader("Authorization", BuildConfig.apiKey)
            .addHeader("Content-Type", "application/json")
            .post(postBody)
            .build()

        val response = client.newCall(request).execute()

        if (response.code == 200) {
            var json: String = response.body!!.string()
            alter.docID = json.replace("\"", "")
            alter.startTime = startTime
            alter.endTime = startTime + 1
            var newCurrentFronters = currentFronters.plus(alter)
            writeString(context, "currentFronters", getAlterNames(newCurrentFronters))
            return newCurrentFronters
        }
    }
    return currentFronters
}

@ExperimentalSerializationApi
fun removeAlterFromFront(context : Context, alter: Alter, currentFronters : List<Alter>) : List<Alter> {
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

        val response = client.newCall(request).execute()

        if (response.code == 200) {
            alter.startTime = null
            alter.endTime = endTime
            alter.docID = null
            var newCurrentFronters = currentFronters.minus(alter)
            writeString(context, "currentFronters", getAlterNames(newCurrentFronters))
            return newCurrentFronters
        }
    }
    return currentFronters
}
