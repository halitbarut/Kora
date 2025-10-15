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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavBackStackEntry

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
    val chromeOwner = remember(lifecycleOwner) { createChromeOwner(lifecycleOwner) }
    var hasChromeOwnership by remember(lifecycleOwner) { mutableStateOf(false) }
    var isResumed by remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }

    DisposableEffect(scaffoldController, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ ->
            val nowResumed = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            isResumed = nowResumed
            if (nowResumed) {
                if (!hasChromeOwnership) {
                    scaffoldController.claimChrome(chromeOwner.id, chromeOwner.label)
                    hasChromeOwnership = true
                }
                scaffoldController.setTopBar(chromeOwner.id, chromeOwner.label, latestTopBarConfig)
                scaffoldController.setFab(chromeOwner.id, chromeOwner.label, latestFabConfig)
            } else if (hasChromeOwnership) {
                scaffoldController.dropChrome(chromeOwner.id)
                hasChromeOwnership = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (isResumed) {
            scaffoldController.claimChrome(chromeOwner.id, chromeOwner.label)
            scaffoldController.setTopBar(chromeOwner.id, chromeOwner.label, latestTopBarConfig)
            scaffoldController.setFab(chromeOwner.id, chromeOwner.label, latestFabConfig)
            hasChromeOwnership = true
        } else {
            hasChromeOwnership = false
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (hasChromeOwnership || scaffoldController.ownsChrome(chromeOwner.id)) {
                scaffoldController.dropChrome(chromeOwner.id)
                hasChromeOwnership = false
            }
        }
    }

    LaunchedEffect(isResumed, latestTopBarConfig) {
        if (isResumed && scaffoldController.ownsChrome(chromeOwner.id)) {
            scaffoldController.setTopBar(chromeOwner.id, chromeOwner.label, latestTopBarConfig)
        }
    }

    LaunchedEffect(isResumed, latestFabConfig) {
        if (isResumed && scaffoldController.ownsChrome(chromeOwner.id)) {
            scaffoldController.setFab(chromeOwner.id, chromeOwner.label, latestFabConfig)
        }
    }
}

private data class ChromeOwner(val id: Any, val label: String)

private fun createChromeOwner(lifecycleOwner: LifecycleOwner): ChromeOwner {
    val label = when (lifecycleOwner) {
        is NavBackStackEntry -> lifecycleOwner.destination.route
            ?: "NavEntry@${lifecycleOwner.id}"
        else -> lifecycleOwner.javaClass.simpleName ?: "LifecycleOwner"
    }
    return ChromeOwner(id = Any(), label = label)
}
