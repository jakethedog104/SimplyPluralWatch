@file:OptIn(
    ExperimentalHorologistApi::class,
    ExperimentalWearFoundationApi::class,
    ExperimentalStdlibApi::class ,
    ExperimentalSerializationApi::class
)

package com.example.simplypluralwatch.presentation

import android.content.Context
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
import java.time.Instant

var systemID = ""
var allAlters = listOf<Alter>()
var allCustomFronts = listOf<Alter>()
var currentFronters = listOf<Alter>()
var reloadTime = arrayOf<Instant?>(null, null, null)

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
            reloadTime[1] = Instant.now()
            allCustomFronts = getAllCustomFronts(systemID)
            reloadTime[2] = Instant.now()
            currentFronters = getFronters(this.applicationContext, allAlters + allCustomFronts)
            reloadTime[0] = Instant.now()
            // TODO: When does this need to be reloaded?
            getFrontHistory(systemID, allAlters + allCustomFronts)
            // Sort
            allAlters = sortFrontList(allAlters)
            allCustomFronts = sortFrontList(allCustomFronts)
        }.start()

        setContent {
            WearApp(this.applicationContext)
        }
    }
}

@Composable
fun WearApp(context : Context) {
    val navController = rememberSwipeDismissableNavController()

    WearAppTheme {
        AppScaffold {
            SwipeDismissableNavHost(navController = navController, startDestination = "menu") {
                composable("menu") {
                    GreetingScreen(
                        context = context,
                        getAlterNames(currentFronters),
                        onShowAlterList = { navController.navigate("alterList") },
                        onShowCustomList = { navController.navigate("customList") }
                    )
                }
                composable("alterList") {
                    AlterScreen(context)
                }
                composable("customList") {
                    CustomScreen(context)
                }
            }
        }
    }
}

@Composable
fun GreetingScreen(context : Context, greetingName: String, onShowAlterList: () -> Unit, onShowCustomList: () -> Unit) {
    if (reloadTime[0] != null &&
        // and its been more than 30 seconds
        reloadTime[0]!!.toEpochMilli() + 30000 <= Instant.now().toEpochMilli()) {
        // Reload fronters
        // Put the request in a thread since we are calling from Main
        Thread {
            currentFronters = getFronters(context, allAlters + allCustomFronts)
            allAlters = sortFrontList(allAlters)
            allCustomFronts = sortFrontList(allCustomFronts)
            reloadTime[0] = Instant.now()
        }.start()
    }

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

@Composable
fun AlterScreen(context : Context) {
    if (reloadTime[1] != null &&
        // its been more than an hour
        reloadTime[1]!!.toEpochMilli() + 3600000 <= Instant.now().toEpochMilli()) {
        // Reload alters
        // Put the request in a thread since we are calling from Main
        Thread {
            allAlters = sortFrontList(getAllAlters(systemID))
            reloadTime[1] = Instant.now()
        }.start()
    }

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
                            onClick = { removeAlterFromFront(context, alter); allAlters = sortFrontList(allAlters) }
                        )
                    }
                } else {
                    item {
                        Chip(
                            colors = ChipDefaults.primaryChipColors(alter.color, getBestTextColor(alter.color)),
                            label = "Add " + alter.name + " to front",
                            onClick = { addAlterToFront(context, alter); allAlters = sortFrontList(allAlters) }
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
fun CustomScreen(context : Context) {
    if (reloadTime[2] != null &&
        // its been more than an hour
        reloadTime[2]!!.toEpochMilli() + 3600000 <= Instant.now().toEpochMilli()) {
        // Reload custom
        // Put the request in a thread since we are calling from Main
        Thread {
            allCustomFronts = sortFrontList(getAllCustomFronts(systemID))
            reloadTime[2] = Instant.now()
        }.start()
    }

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
                            onClick = { removeAlterFromFront(context, alter); allCustomFronts = sortFrontList(allCustomFronts) }
                        )
                    }
                } else {
                    item {
                        Chip(
                            colors = ChipDefaults.primaryChipColors(alter.color,getBestTextColor(alter.color)),
                            label = "Add " + alter.name + " to front",
                            onClick = { addAlterToFront(context, alter); allCustomFronts = sortFrontList(allCustomFronts) }
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
            text = stringResource(R.string.hello, greetingName)
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
//    GreetingScreen("Preview Name", onShowAlterList = {}, onShowCustomList = {})
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun AlterScreenPreview() {
//    AlterScreen()
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun CustomScreenPreview() {
//    CustomScreen()
}
