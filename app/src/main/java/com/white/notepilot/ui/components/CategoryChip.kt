package com.white.notepilot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.white.notepilot.R
import com.white.notepilot.data.model.Category

@Composable
fun CategoryChip(
    category: Category,
    isSelected: Boolean = false,
    showRemoveIcon: Boolean = false,
    onClick: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val backgroundColor = try {
        Color(category.color.toColorInt())
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }
    
    val chipModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }
    
    Row(
        modifier = chipModifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) {
                    backgroundColor.copy(alpha = 0.2f)
                } else {
                    backgroundColor.copy(alpha = 0.1f)
                }
            )
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) backgroundColor else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category icon
        Icon(
            painter = painterResource(getCategoryIcon(category.icon)),
            contentDescription = category.name,
            tint = backgroundColor,
            modifier = Modifier.size(16.dp)
        )
        
        // Category name
        Text(
            text = category.name,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = backgroundColor
        )
        
        // Remove icon
        if (showRemoveIcon && onRemove != null) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = backgroundColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

private fun getCategoryIcon(iconName: String): Int {
    return when (iconName) {
        "person" -> R.drawable.person
        "work" -> R.drawable.work
        "lightbulb" -> R.drawable.lightbulb
        "star" -> R.drawable.star
        "checklist" -> R.drawable.checklist
        "label" -> R.drawable.label
        else -> R.drawable.label
    }
}
