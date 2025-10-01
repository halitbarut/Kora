package com.barutdev.kora.ui.screens.dashboard

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.barutdev.kora.R
import com.barutdev.kora.navigation.KoraDestination

private data class DashboardNavItem(
    val destination: KoraDestination,
    val icon: ImageVector,
    @StringRes val labelRes: Int
)

@Composable
fun DashboardScreen(
    currentRoute: String?,
    onNavigateToDestination: (KoraDestination) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navigationItems = remember {
        listOf(
            DashboardNavItem(
                destination = KoraDestination.Dashboard,
                icon = Icons.Outlined.Dashboard,
                labelRes = R.string.dashboard_title
            ),
            DashboardNavItem(
                destination = KoraDestination.Calendar,
                icon = Icons.Outlined.CalendarMonth,
                labelRes = R.string.calendar_title
            ),
            DashboardNavItem(
                destination = KoraDestination.Homework,
                icon = Icons.Outlined.Assignment,
                labelRes = R.string.homework_title
            )
        )
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                navigationItems.forEach { item ->
                    val label = stringResource(id = item.labelRes)
                    NavigationBarItem(
                        selected = currentRoute == item.destination.route,
                        onClick = { onNavigateToDestination(item.destination) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = label
                            )
                        },
                        label = { Text(text = label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(id = R.string.dashboard_title))
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToSettings) {
                Text(text = stringResource(id = R.string.navigate_to_settings))
            }
        }
    }
}
