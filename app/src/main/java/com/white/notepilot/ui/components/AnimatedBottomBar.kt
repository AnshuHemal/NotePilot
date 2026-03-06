package com.white.notepilot.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.white.notepilot.ui.navigation.BottomNavDestination
import com.white.notepilot.ui.theme.Black
import com.white.notepilot.ui.theme.Dimens
import com.white.notepilot.ui.theme.LightGray
import com.white.notepilot.ui.theme.NotesTheme

@Composable
fun AnimatedBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onFabClick: () -> Unit,
    hasUnreadNotifications: Boolean = false,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    val onHomeClick = remember(onNavigate) { { onNavigate(BottomNavDestination.Home.route) } }
    val onNotificationsClick = remember(onNavigate) { { onNavigate(BottomNavDestination.Notifications.route) } }
    val onSettingsClick = remember(onNavigate) { { onNavigate(BottomNavDestination.Settings.route) } }
    val onAccountClick = remember(onNavigate) { { onNavigate(BottomNavDestination.Account.route) } }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(95.dp)
                .align(Alignment.BottomCenter)
        ) {
            val width = size.width
            val height = size.height

            val fabRadius = 32.dp.toPx()
            val curveRadius = fabRadius + 5.dp.toPx()
            val centerX = width / 2f
            val barTop = 30.dp.toPx()

            val path = Path().apply {
                moveTo(0f, barTop)
                lineTo(centerX - curveRadius, barTop)

                arcTo(
                    rect = Rect(
                        left = centerX - curveRadius,
                        top = barTop - curveRadius,
                        right = centerX + curveRadius,
                        bottom = barTop + curveRadius
                    ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = -180f,
                    forceMoveTo = false
                )

                lineTo(width, barTop)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            drawPath(
                path = path,
                color = surfaceColor
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .align(Alignment.BottomCenter)
                .padding(horizontal = Dimens.PaddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedBottomNavItem(
                destination = BottomNavDestination.Home,
                isSelected = currentRoute == BottomNavDestination.Home.route,
                onClick = onHomeClick
            )

            AnimatedBottomNavItem(
                destination = BottomNavDestination.Notifications,
                isSelected = currentRoute == BottomNavDestination.Notifications.route,
                onClick = onNotificationsClick,
                showBadge = hasUnreadNotifications
            )

            Box(modifier = Modifier.size(64.dp))

            AnimatedBottomNavItem(
                destination = BottomNavDestination.Settings,
                isSelected = currentRoute == BottomNavDestination.Settings.route,
                onClick = onSettingsClick
            )

            AnimatedBottomNavItem(
                destination = BottomNavDestination.Account,
                isSelected = currentRoute == BottomNavDestination.Account.route,
                onClick = onAccountClick
            )
        }

        FloatingActionButton(
            onClick = onFabClick,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .size(56.dp)
                .align(Alignment.TopCenter)
                .offset(y = (0).dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Note",
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun AnimatedBottomNavItem(
    destination: BottomNavDestination,
    isSelected: Boolean,
    onClick: () -> Unit,
    showBadge: Boolean = false
) {
    val selectedColor = MaterialTheme.colorScheme.onSurface
    val unselectedColor = LightGray
    
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else unselectedColor,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "iconColor"
    )
    
    val iconRes = remember(isSelected, destination) {
        if (isSelected) destination.selectedIcon else destination.unselectedIcon
    }

    Box {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(46.dp)
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = destination.label,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
        }
        
        // Red dot badge
        if (showBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-10).dp, y = 8.dp)
                    .size(10.dp)
                    .background(
                        color = androidx.compose.ui.graphics.Color.Red,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun AnimatedBottomBarPreview() {
    NotesTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Black)
        ) {
            AnimatedBottomBar(
                currentRoute = BottomNavDestination.Home.route,
                onNavigate = {},
                onFabClick = {}
            )
        }
    }
}
