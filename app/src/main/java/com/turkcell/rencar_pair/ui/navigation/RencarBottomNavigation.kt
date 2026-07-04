package com.turkcell.rencar_pair.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkcell.rencar_pair.R
import com.turkcell.rencar_pair.ui.theme.Primary
import com.turkcell.rencar_pair.ui.theme.TextHintLight
import com.turkcell.rencar_pair.ui.theme.labelS

enum class NavigationTab {
    HARITA,
    GECMIS,
    CUZDAN,
    PROFIL
}

private data class TabItem(
    val tab: NavigationTab,
    val label: String,
    val iconRes: Int,
)

@Composable
fun RencarBottomNavigation(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf(
        TabItem(NavigationTab.HARITA,  "Harita",  R.drawable.ic_nav_map),
        TabItem(NavigationTab.GECMIS,  "Geçmiş",  R.drawable.ic_nav_history),
        TabItem(NavigationTab.CUZDAN,  "Cüzdan",  R.drawable.ic_nav_wallet),
        TabItem(NavigationTab.PROFIL,  "Profil",  R.drawable.ic_nav_profile),
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = modifier,
    ) {
        tabs.forEach { item ->
            val selected = selectedTab == item.tab
            NavigationBarItem(
                selected = selected,
                onClick  = { onTabSelected(item.tab) },
                icon = {
                    Icon(
                        imageVector        = ImageVector.vectorResource(id = item.iconRes),
                        contentDescription = item.label,
                        modifier           = Modifier.size(24.dp),
                    )
                },
                label = {
                    Text(
                        text     = item.label,
                        style    = labelS,
                        fontSize = 11.sp,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = Primary,
                    selectedTextColor   = Primary,
                    unselectedIconColor = TextHintLight,
                    unselectedTextColor = TextHintLight,
                    indicatorColor      = Color.White,   // no pill highlight
                ),
            )
        }
    }
}
