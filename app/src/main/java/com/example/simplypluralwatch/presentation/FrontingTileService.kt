package com.example.simplypluralwatch.presentation

import android.content.ComponentName
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.Text
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.Chip
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.example.simplypluralwatch.R
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService

private const val RESOURCES_VERSION = "0"

@ExperimentalHorologistApi
class FrontingTileService : SuspendingTileService() {
    var currentFrontersString : String? = ""

    override fun onCreate() {
        super.onCreate()
        // Load data
        currentFrontersString = readString(this.applicationContext, "currentFronters")
        if (currentFrontersString.isNullOrEmpty()) { currentFrontersString = "" }
        getUpdater(this.applicationContext).requestUpdate(FrontingTileService::class.java)
    }

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .build()
    }

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val singleTileTimeline = TimelineBuilders.Timeline.Builder()
            .addTimelineEntry(
                TimelineBuilders.TimelineEntry.Builder()
                    .setLayout(
                        LayoutElementBuilders.Layout.Builder()
                            .setRoot(tileLayout(currentFrontersString!!))
                            .build()
                    )
                    .build()
            )
            .build()

        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(singleTileTimeline)
            .build()
    }

    private fun tileLayout(currentFrontersString : String) : LayoutElementBuilders.LayoutElement {
        val text = getString(R.string.hello, currentFrontersString)
        val deviceParams = DeviceParameters.Builder().build()
        return PrimaryLayout.Builder(deviceParams)
            .setResponsiveContentInsetEnabled(true)
            .setContent(
                Text.Builder().setText(text).build()
            )
            .setPrimaryChipContent(
                Chip.Builder(
                    this.applicationContext,
                    Clickable.Builder().setOnClick(
                        ActionBuilders.launchAction(
                            ComponentName("com.example.simplypluralwatch.presentation", "MainActivity")
                        )
                    ).build(),
                    deviceParams
                ).build()
            ).build()
    }
}
