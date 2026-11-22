package com.example.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.home.NavDestination
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.MutedNavText
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryText

private data class NavItemSpec(
    val destination: NavDestination,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val krishiNavItems = listOf(
    NavItemSpec(
        destination = NavDestination.HOME,
        labelRes = R.string.nav_home,
        selectedIcon = Icons.Rounded.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_item_home"
    ),
    NavItemSpec(
        destination = NavDestination.MONITORING,
        labelRes = R.string.nav_monitoring,
        selectedIcon = Icons.Rounded.Sensors,
        unselectedIcon = Icons.Outlined.Sensors,
        testTag = "nav_item_monitoring"
    ),
    NavItemSpec(
        destination = NavDestination.REPORTS,
        labelRes = R.string.nav_reports,
        selectedIcon = Icons.Rounded.Assessment,
        unselectedIcon = Icons.Outlined.Assessment,
        testTag = "nav_item_reports"
    ),
    NavItemSpec(
        destination = NavDestination.PROFILE,
        labelRes = R.string.nav_profile,
        selectedIcon = Icons.Rounded.Person,
        unselectedIcon = Icons.Outlined.PersonOutline,
        testTag = "nav_item_profile"
    )
)

/**
 * SECTION G: Fixed translucent glassmorphism bottom navigation bar for smartphones & tablets.
 * Strictly 4 items: Home (selected by default), Monitoring, Reports, Profile.
 */
@Composable
fun KrishiBottomNavBar(
    activeDestination: NavDestination,
    modifier: Modifier = Modifier,
    onPlaceholderNavClick: (NavDestination) -> Unit = {}
) {
    val barShape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = barShape,
                ambientColor = PrimaryText.copy(alpha = 0.12f),
                spotColor = PrimaryText.copy(alpha = 0.12f)
            )
            .clip(barShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xEBFFFFFF),
                        Color(0xF5F3F9FF)
                    )
                )
            )
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xCCFFFFFF),
                            Color(0xFFEBF5FF),
                            Color(0xCCFFFFFF)
                        )
                    )
                ),
                shape = barShape
            )
            .navigationBarsPadding()
            .testTag("bottom_navigation_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            krishiNavItems.forEach { item ->
                val isSelected = item.destination == activeDestination
                val label = stringResource(item.labelRes)
                val interactionSource = remember { MutableInteractionSource() }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .semantics { selected = isSelected }
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(bounded = true, color = PrimaryGreen.copy(alpha = 0.18f)),
                            role = Role.Tab,
                            onClick = { onPlaceholderNavClick(item.destination) }
                        )
                        .padding(vertical = 4.dp)
                        .testTag(item.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = label,
                        tint = if (isSelected) PrimaryGreen else MutedNavText,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) PrimaryGreen else MutedNavText,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Small glowing green active indicator bar underneath active Home item
                    Box(
                        modifier = Modifier
                            .width(if (isSelected) 22.dp else 0.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(PrimaryGreen, AccentGreen)
                                )
                            )
                    )
                }
            }
        }
    }
}

/**
 * Responsive Navigation Rail for Expanded (large tablet / desktop) viewports.
 */
@Composable
fun KrishiNavigationRail(
    activeDestination: NavDestination,
    modifier: Modifier = Modifier,
    onPlaceholderNavClick: (NavDestination) -> Unit = {}
) {
    Column(
        modifier = modifier
            .width(88.dp)
            .fillMaxHeight()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xE8FFFFFF),
                        Color(0xE0EFF7FF)
                    )
                )
            )
            .border(
                border = BorderStroke(1.dp, Color(0xCCFFFFFF)),
                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(vertical = 20.dp, horizontal = 8.dp)
            .testTag("desktop_navigation_rail"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        krishiNavItems.forEach { item ->
            val isSelected = item.destination == activeDestination
            val label = stringResource(item.labelRes)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) PrimaryGreen.copy(alpha = 0.12f) else Color.Transparent)
                    .semantics { selected = isSelected }
                    .clickable(
                        role = Role.Tab,
                        onClick = { onPlaceholderNavClick(item.destination) }
                    )
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .testTag(item.testTag),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = label,
                    tint = if (isSelected) PrimaryGreen else MutedNavText,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) PrimaryGreen else MutedNavText,
                    maxLines = 1
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(50))
                            .background(PrimaryGreen)
                    )
                }
            }
        }
    }
}
