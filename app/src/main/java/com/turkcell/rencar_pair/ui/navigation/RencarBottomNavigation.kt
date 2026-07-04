package com.turkcell.rencar_pair.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.turkcell.rencar_pair.R
import com.turkcell.rencar_pair.ui.theme.BackgroundLight
import com.turkcell.rencar_pair.ui.theme.BorderSubtleDark
import com.turkcell.rencar_pair.ui.theme.BorderSubtleLight
import com.turkcell.rencar_pair.ui.theme.Primary
import com.turkcell.rencar_pair.ui.theme.PrimaryOnDark
import com.turkcell.rencar_pair.ui.theme.TopBarDark
import com.turkcell.rencar_pair.ui.theme.TopBarLight
import com.turkcell.rencar_pair.ui.theme.labelMicro

enum class NavigationTab {
    HARITA,
    GECMIS,
    CUZDAN,
    PROFIL,
}

private data class TabItem(
    val tab: NavigationTab,
    val label: String,
    val iconRes: Int,
)

private val tabs = listOf(
    TabItem(NavigationTab.HARITA, "Harita", R.drawable.ic_nav_map),
    TabItem(NavigationTab.GECMIS, "Geçmiş", R.drawable.ic_nav_history),
    TabItem(NavigationTab.CUZDAN, "Cüzdan", R.drawable.ic_nav_wallet),
    TabItem(NavigationTab.PROFIL, "Profil", R.drawable.ic_nav_profile),
)

@Composable
fun RencarBottomNavigation(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.background != Color(BackgroundLight.value)
    val containerColor = if (isDark) TopBarDark else TopBarLight
    val borderColor = if (isDark) BorderSubtleDark else BorderSubtleLight
    val activeColor = if (isDark) PrimaryOnDark else Primary
    val inactiveColor = if (isDark) Color(0xFF6B7480) else Color(0xFF9AA3AE)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
            .drawBehind {
                drawLine(
                    color = borderColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { item ->
                val selected = selectedTab == item.tab
                val tint = if (selected) activeColor else inactiveColor

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(item.tab) },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = item.iconRes),
                            contentDescription = item.label,
                            tint = tint,
                            modifier = Modifier.size(30.dp),
                        )
                        Text(
                            text = item.label,
                            style = labelMicro,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                            color = tint,
                        )
                    }
                }
            }
        }
    }
}
