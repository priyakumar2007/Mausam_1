package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.data.model.AppLanguage
import com.example.data.model.AppStrings
import com.example.ui.theme.SkyBlueLight

data class NavItem(
    val tabId: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun MausamBottomBar(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    language: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem("home", AppStrings.getTabLabel("home", language), Icons.Default.Home),
        NavItem("lifestyle", AppStrings.getTabLabel("lifestyle", language), Icons.Default.AutoAwesome),
        NavItem("forecast", AppStrings.getTabLabel("forecast", language), Icons.Default.CalendarMonth),
        NavItem("agriculture", AppStrings.getTabLabel("agriculture", language), Icons.Default.Agriculture),
        NavItem("travel", AppStrings.getTabLabel("travel", language), Icons.Default.FlightTakeoff),
        NavItem("beach", AppStrings.getTabLabel("beach", language), Icons.Default.Waves)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("mausam_bottom_navigation_bar")
    ) {
        items.forEach { item ->
            val isSelected = currentTab == item.tabId
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.tabId) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) SkyBlueLight else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = SkyBlueLight.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_item_${item.tabId}")
            )
        }
    }
}
