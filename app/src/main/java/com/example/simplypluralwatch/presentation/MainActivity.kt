/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:OptIn(
    ExperimentalHorologistApi::class,
    ExperimentalWearFoundationApi::class,
    ExperimentalStdlibApi::class ,
    ExperimentalSerializationApi::class
)

package com.example.simplypluralwatch.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.example.simplypluralwatch.R
import com.example.simplypluralwatch.presentation.theme.WearAppTheme
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.ItemType
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.AlertContent
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.ListHeaderDefaults.firstItemPadding
import com.google.android.horologist.compose.material.ResponsiveListHeader
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.math.pow
import kotlin.math.sqrt

var systemID = ""
var allAlters = listOf<Alter>()
var allCustomFronts = listOf<Alter>()
var currentFronters = listOf<Alter>()

/**
 * Simple "Hello, World" app meant as a starting point for a new project using Compose for Wear OS.
 *
 * Displays a centered [Text] composable and a list built with Horologist
 * (https://github.com/google/horologist).
 *
 * Use the Wear version of Compose Navigation. You can carry
 * over your knowledge from mobile and it supports the swipe-to-dismiss gesture (Wear OS's
 * back action). For more information, go here:
 * https://developer.android.com/reference/kotlin/androidx/wear/compose/navigation/package-summary
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread{
            systemID = getUserID()
            allAlters = getAllAlters(systemID)
            allCustomFronts = getAllCustomFronts(systemID)
            currentFronters = getFronters()
            getFrontHistory(systemID)

            // TODO: Refresh?
        }.start()

        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    val navController = rememberSwipeDismissableNavController()

    WearAppTheme {
        AppScaffold {
            SwipeDismissableNavHost(navController = navController, startDestination = "menu") {
                composable("menu") {
                    GreetingScreen(
                        getAlterNames(currentFronters),
                        onShowAlterList = { navController.navigate("alterList") },
                        onShowCustomList = { navController.navigate("customList") }
                    )
                }
                composable("alterList") {
                    AlterScreen()
                }
                composable("customList") {
                    CustomScreen()
                }
            }
        }
    }
}

@Composable
fun GreetingScreen(greetingName: String, onShowAlterList: () -> Unit, onShowCustomList: () -> Unit) {
    val scrollState = rememberScrollState()
    var color = MaterialTheme.colors.primary
    if (!currentFronters.isEmpty()) {
        color = currentFronters[0].color
    }

    /*
     * Specifying the types of items that appear at the start and end of the list ensures that the
     * appropriate padding is used.
     */
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Text,
            last = ItemType.Chip
        )
    )

    ScreenScaffold(scrollState = scrollState) {
        /*
         * The Horologist [ScalingLazyColumn] takes care of the horizontal and vertical
         * padding for the list, so there is no need to specify it, as in the [GreetingScreen]
         * composable.
         */
        ScalingLazyColumn(
            columnState = columnState
        ) {
            item {
                Greeting(greetingName = greetingName, color = color)
            }
            item {
                Chip(
                    label = "Show Alters",
                    onClick = onShowAlterList,
                    colors = ChipDefaults.primaryChipColors(color)
                )
            }
            item {
                Chip(
                    label = "Show Custom Fronts",
                    onClick = onShowCustomList,
                    colors = ChipDefaults.primaryChipColors(color)
                )
            }
        }
    }
}

fun getBestTextColor(backgroundColor: Color): Color {

    val luminance = sqrt(backgroundColor.red.pow(2) * 0.241 + backgroundColor.green.pow(2) * 0.691 + backgroundColor.blue.pow(2) * 0.068)
    return if (luminance > 0.65) Color.DarkGray else Color.White

}


@Composable
fun AlterScreen() {
    var showDialog by remember { mutableStateOf(false) }

    /*
     * Specifying the types of items that appear at the start and end of the list ensures that the
     * appropriate padding is used.
     */
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Text,
            last = ItemType.Chip
        )
    )

    ScreenScaffold(scrollState = columnState) {
        /*
         * The Horologist [ScalingLazyColumn] takes care of the horizontal and vertical
         * padding for the list, so there is no need to specify it, as in the [GreetingScreen]
         * composable.
         */
        ScalingLazyColumn(
            columnState = columnState
        ) {
            item {
                ResponsiveListHeader(contentPadding = firstItemPadding()) {
                    Text(text = "Alters")
                }
            }
            for (alter in allAlters) {
                if (currentFronters.contains(alter)) {
                    item {
                        Chip(
                            colors = ChipDefaults.primaryChipColors(alter.color, getBestTextColor(alter.color)),
                            label = "Remove " + alter.name + " from front",
                            onClick = { removeAlterFromFront(alter) }
                        )
                    }
                } else {
                    item {
                        Chip(
                            colors = ChipDefaults.primaryChipColors(alter.color, getBestTextColor(alter.color)),
                            label = "Add " + alter.name + " to front",
                            onClick = { addAlterToFront(alter) }
                        )
                    }
                }

            }
        }
    }

    SampleDialog(
        showDialog = showDialog,
        onDismiss = { showDialog = false },
        onCancel = {},
        onOk = {}
    )
}

@Composable
fun CustomScreen() {
    var showDialog by remember { mutableStateOf(false) }

    /*
     * Specifying the types of items that appear at the start and end of the list ensures that the
     * appropriate padding is used.
     */
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Text,
            last = ItemType.Chip
        )
    )

    ScreenScaffold(scrollState = columnState) {
        /*
         * The Horologist [ScalingLazyColumn] takes care of the horizontal and vertical
         * padding for the list, so there is no need to specify it, as in the [GreetingScreen]
         * composable.
         */
        ScalingLazyColumn(
            columnState = columnState
        ) {
            item {
                ResponsiveListHeader(contentPadding = firstItemPadding()) {
                    Text(text = "Custom Fronts")
                }
            }
            for (alter in allCustomFronts) {
                if (currentFronters.contains(alter)) {
                    item {
                        Chip(
                            colors = ChipDefaults.primaryChipColors(alter.color,getBestTextColor(alter.color)),
                            label = "Remove " + alter.name + " from front",
                            onClick = { removeAlterFromFront(alter) }
                        )
                    }
                } else {
                    item {
                        Chip(
                            colors = ChipDefaults.primaryChipColors(alter.color,getBestTextColor(alter.color)),
                            label = "Add " + alter.name + " to front",
                            onClick = { addAlterToFront(alter) }
                        )
                    }
                }

            }
        }
    }

    SampleDialog(
        showDialog = showDialog,
        onDismiss = { showDialog = false },
        onCancel = {},
        onOk = {}
    )
}

@Composable
fun Greeting(greetingName: String, color: Color) {
    ResponsiveListHeader(contentPadding = firstItemPadding()) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = color,
            text = stringResource(R.string.hello_world, greetingName)
        )
    }
}

@Composable
fun SampleDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onOk: () -> Unit
) {
    val state = rememberResponsiveColumnState()

    Dialog(
        showDialog = showDialog,
        onDismissRequest = onDismiss,
        scrollState = state.state
    ) {
        SampleDialogContent(onCancel, onDismiss, onOk)
    }
}

@Composable
fun SampleDialogContent(
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
    onOk: () -> Unit
) {
    AlertContent(
        icon = {},
        title = "Title",
        onCancel = {
            onCancel()
            onDismiss()
        },
        onOk = {
            onOk()
            onDismiss()
        }
    ) {
        item {
            Text(text = "An unknown error occurred during the request.")
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun GreetingScreenPreview() {
    GreetingScreen("Preview Name", onShowAlterList = {}, onShowCustomList = {})
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun AlterScreenPreview() {
    AlterScreen()
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun CustomScreenPreview() {
    CustomScreen()
}
