package com.barutdev.kora.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

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
    private val _topBarConfig = mutableStateOf<TopBarConfig?>(null)
    val topBarConfig: State<TopBarConfig?> = _topBarConfig

    private val _fabConfig = mutableStateOf<FabConfig?>(null)
    val fabConfig: State<FabConfig?> = _fabConfig

    fun setTopBar(config: TopBarConfig?) {
        _topBarConfig.value = config
    }

    fun clearTopBar() {
        _topBarConfig.value = null
    }

    fun setFab(config: FabConfig?) {
        _fabConfig.value = config
    }

    fun clearFab() {
        _fabConfig.value = null
    }

    fun reset() {
        clearTopBar()
        clearFab()
    }
}

@Composable
fun rememberKoraScaffoldController(): KoraScaffoldController {
    return remember { KoraScaffoldController(SnackbarHostState()) }
}

val LocalKoraScaffoldController = staticCompositionLocalOf {
    KoraScaffoldController(SnackbarHostState())
}
