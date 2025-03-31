package com.example.simplypluralwatch.presentation

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

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
data class User (val id: String)

@Serializable
@JsonIgnoreUnknownKeys
@ExperimentalSerializationApi
data class SPAlterContainer (val id: String, val content: SPAlter)
@Serializable
@JsonIgnoreUnknownKeys
@ExperimentalSerializationApi
data class SPAlter (val name: String, val color: String, val lastOperationTime : Long?, val archived : Boolean = false)

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