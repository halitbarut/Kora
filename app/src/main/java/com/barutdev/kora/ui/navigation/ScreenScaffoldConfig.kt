package com.barutdev.kora.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Keeps the scaffold's top bar and FAB aligned with the lifecycle state of the current
 * navigation entry so off-screen destinations cannot override the visible UI chrome.
 */
@Composable
fun ScreenScaffoldConfig(
    topBarConfig: TopBarConfig?,
    fabConfig: FabConfig? = null
) {
    val scaffoldController = LocalKoraScaffoldController.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestTopBarConfig by rememberUpdatedState(topBarConfig)
    val latestFabConfig by rememberUpdatedState(fabConfig)
    var isResumed by remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }

    DisposableEffect(scaffoldController, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ ->
            val nowResumed = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            isResumed = nowResumed
            if (nowResumed) {
                scaffoldController.setTopBar(latestTopBarConfig)
                scaffoldController.setFab(latestFabConfig)
            } else if (scaffoldController.fabConfig.value == latestFabConfig) {
                scaffoldController.clearFab()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (isResumed) {
            scaffoldController.setTopBar(latestTopBarConfig)
            scaffoldController.setFab(latestFabConfig)
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (scaffoldController.topBarConfig.value == latestTopBarConfig) {
                scaffoldController.clearTopBar()
            }
            if (scaffoldController.fabConfig.value == latestFabConfig) {
                scaffoldController.clearFab()
            }
        }
    }

    LaunchedEffect(isResumed, latestTopBarConfig) {
        if (isResumed) {
            scaffoldController.setTopBar(latestTopBarConfig)
        }
    }

    LaunchedEffect(isResumed, latestFabConfig) {
        if (isResumed) {
            scaffoldController.setFab(latestFabConfig)
        } else if (scaffoldController.fabConfig.value == latestFabConfig) {
            scaffoldController.clearFab()
        }
    }
}
