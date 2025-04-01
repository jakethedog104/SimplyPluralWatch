package com.example.simplypluralwatch.presentation

import android.content.ComponentName
import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.LayoutElementBuilders.Text
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders.Tile
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

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): Tile {
        val text = getString(R.string.hello, currentFrontersString)
        val layoutElement = tileLayout(text, this.applicationContext, requestParams.deviceConfiguration)
        return Tile.Builder().setResourcesVersion(RESOURCES_VERSION).setTileTimeline(
            Timeline.fromLayoutElement(layoutElement)
        ).build()
    }

    internal fun tileLayout(
        currentFrontersString: String,
        context: Context,
        deviceParameters: DeviceParameters
    ) = PrimaryLayout.Builder(deviceParameters)
            .setResponsiveContentInsetEnabled(true)
            .setContent(
                Text.Builder().setText(currentFrontersString).build()
            )
            .setPrimaryChipContent(
                CompactChip.Builder(
                    context,
                    "Change",
                    Clickable.Builder().setOnClick(
                        ActionBuilders.launchAction(
                            ComponentName("com.example.simplypluralwatch.presentation", "MainActivity")
                        )
                    ).build(),
                    deviceParameters
                )
                .build()
            ).build()
}
