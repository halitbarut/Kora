package com.barutdev.kora.ui.navigation

import android.util.Log
import android.view.Choreographer
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.barutdev.kora.BuildConfig

private const val TAG = "KoraScaffoldChrome"

@Stable
data class TopBarAction(
    val icon: ImageVector,
    val contentDescription: String?,
    val onClick: () -> Unit
)

@Stable
data class TopBarConfig(
    val title: String,
    val navigationIcon: TopBarAction? = null,
    val actions: List<TopBarAction> = emptyList()
)

@Stable
data class FabConfig(
    val icon: ImageVector,
    val contentDescription: String?,
    val onClick: () -> Unit,
    val containerColor: Color? = null,
    val contentColor: Color? = null
)

@Stable
class KoraScaffoldController internal constructor(
    val snackbarHostState: SnackbarHostState
) {
    private val choreographer = Choreographer.getInstance()

    private val _topBarConfig = mutableStateOf<TopBarConfig?>(null)
    val topBarConfig: State<TopBarConfig?> = _topBarConfig

    private var topBarOwnerId: Any? = null
    private var topBarOwnerLabel: String? = null
    private var topBarMutationVersion = 0
    private var pendingTopBarClearToken = 0

    private var activeChromeOwnerId: Any? = null
    private var activeChromeOwnerLabel: String? = null

    private val _fabConfig = mutableStateOf<FabConfig?>(null)
    val fabConfig: State<FabConfig?> = _fabConfig

    private var fabOwnerId: Any? = null
    private var fabOwnerLabel: String? = null

    fun setTopBar(ownerId: Any, ownerLabel: String, config: TopBarConfig?) {
        if (!isActiveOwner(ownerId)) {
            debugLog(
                "TopBar update ignored from $ownerLabel; active owner=" +
                    activeChromeOwnerLabel.orUnknown()
            )
            return
        }
        if (topBarOwnerId !== ownerId) {
            debugLog("TopBar owner -> $ownerLabel (was ${topBarOwnerLabel.orUnknown()})")
        } else {
            debugLog("TopBar update from $ownerLabel")
        }
        topBarOwnerId = ownerId
        topBarOwnerLabel = ownerLabel
        topBarMutationVersion++
        _topBarConfig.value = config
    }

    fun clearTopBar(ownerId: Any) {
        if (!isActiveOwner(ownerId)) {
            debugLog(
                "TopBar clear ignored for ${topBarOwnerLabel.orUnknown()}; owner not active " +
                    "(active=${activeChromeOwnerLabel.orUnknown()})"
            )
            return
        }
        if (topBarOwnerId === ownerId) {
            val clearingLabel = topBarOwnerLabel.orUnknown()
            val expectedMutationVersion = topBarMutationVersion
            val requestToken = ++pendingTopBarClearToken
            choreographer.postFrameCallback {
                if (pendingTopBarClearToken != requestToken) {
                    return@postFrameCallback
                }
                val ownerUnchanged = topBarOwnerId === ownerId
                val mutationUnchanged = topBarMutationVersion == expectedMutationVersion
                if (ownerUnchanged && mutationUnchanged) {
                    debugLog("TopBar cleared by $clearingLabel")
                    topBarOwnerId = null
                    topBarOwnerLabel = null
                    topBarMutationVersion++
                    _topBarConfig.value = null
                } else {
                    debugLog(
                        "TopBar clear for $clearingLabel skipped; " +
                            "ownerUnchanged=$ownerUnchanged mutationUnchanged=$mutationUnchanged"
                    )
                }
            }
        } else {
            debugLog(
                "TopBar clear ignored for ownerId=$ownerId; current owner=${topBarOwnerLabel.orUnknown()}"
            )
        }
    }

    private fun forceClearTopBar() {
        if (topBarOwnerId != null) {
            debugLog("TopBar force cleared from ${topBarOwnerLabel.orUnknown()}")
        }
        topBarOwnerId = null
        topBarOwnerLabel = null
        topBarMutationVersion++
        _topBarConfig.value = null
    }

    fun setFab(ownerId: Any, ownerLabel: String, config: FabConfig?) {
        if (!isActiveOwner(ownerId)) {
            debugLog(
                "FAB update ignored from $ownerLabel; active owner=" +
                    activeChromeOwnerLabel.orUnknown()
            )
            return
        }
        if (fabOwnerId !== ownerId) {
            debugLog("FAB owner -> $ownerLabel (was ${fabOwnerLabel.orUnknown()})")
        } else {
            debugLog("FAB update from $ownerLabel")
        }
        fabOwnerId = ownerId
        fabOwnerLabel = ownerLabel
        _fabConfig.value = config
    }

    fun clearFab(ownerId: Any) {
        if (!isActiveOwner(ownerId)) {
            debugLog(
                "FAB clear ignored for ownerId=$ownerId; active owner=" +
                    activeChromeOwnerLabel.orUnknown()
            )
            return
        }
        if (fabOwnerId === ownerId) {
            debugLog("FAB cleared by ${fabOwnerLabel.orUnknown()}")
            fabOwnerId = null
            fabOwnerLabel = null
            _fabConfig.value = null
        } else {
            debugLog(
                "FAB clear ignored for ownerId=$ownerId; current owner=${fabOwnerLabel.orUnknown()}"
            )
        }
    }

    private fun forceClearFab() {
        if (fabOwnerId != null) {
            debugLog("FAB force cleared from ${fabOwnerLabel.orUnknown()}")
        }
        fabOwnerId = null
        fabOwnerLabel = null
        _fabConfig.value = null
    }

    fun reset() {
        debugLog("Scaffold reset requested")
        activeChromeOwnerId = null
        activeChromeOwnerLabel = null
        forceClearTopBar()
        forceClearFab()
    }

    fun claimChrome(ownerId: Any, ownerLabel: String) {
        if (activeChromeOwnerId !== ownerId) {
            debugLog("Chrome owner -> $ownerLabel (was ${activeChromeOwnerLabel.orUnknown()})")
        }
        activeChromeOwnerId = ownerId
        activeChromeOwnerLabel = ownerLabel
    }

    fun releaseChrome(ownerId: Any) {
        if (activeChromeOwnerId === ownerId) {
            val releasedBy = activeChromeOwnerLabel.orUnknown()
            debugLog("Chrome released by $releasedBy")
            activeChromeOwnerId = null
            activeChromeOwnerLabel = null
        } else {
            debugLog(
                "Chrome release ignored for ownerId=$ownerId; active owner=" +
                activeChromeOwnerLabel.orUnknown()
            )
        }
    }

    fun ownsChrome(ownerId: Any): Boolean = activeChromeOwnerId === ownerId

    fun dropChrome(ownerId: Any) {
        if (!ownsChrome(ownerId)) {
            debugLog(
                "Chrome drop ignored for ownerId=$ownerId; active owner=" +
                    activeChromeOwnerLabel.orUnknown()
            )
            return
        }
        clearFab(ownerId)
        if (topBarOwnerId === ownerId) {
            debugLog("TopBar ownership released by ${topBarOwnerLabel.orUnknown()} without clearing content")
            topBarOwnerId = null
            topBarOwnerLabel = null
        }
        releaseChrome(ownerId)
    }

    private fun isActiveOwner(ownerId: Any): Boolean = activeChromeOwnerId === ownerId

    private fun String?.orUnknown(): String = this ?: "unknown"

    private fun debugLog(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }
}

@Composable
fun rememberKoraScaffoldController(): KoraScaffoldController {
    return remember { KoraScaffoldController(SnackbarHostState()) }
}

val LocalKoraScaffoldController = staticCompositionLocalOf {
    KoraScaffoldController(SnackbarHostState())
}
