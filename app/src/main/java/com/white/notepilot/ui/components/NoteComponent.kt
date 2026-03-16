package com.white.notepilot.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.white.notepilot.R
import com.white.notepilot.data.model.Category
import com.white.notepilot.ui.theme.Dimens
import com.white.notepilot.utils.ColorUtils

@Composable
fun NoteComponent(
    title: String,
    colorCode: String,
    isPinned: Boolean = false,
    isLocked: Boolean = false,
    categories: List<Category> = emptyList(),
    onClick: () -> Unit = {}
) {
    val color = remember(colorCode) {
        Color(ColorUtils.colorCodeToInt(colorCode))
    }
    
    val textColor = remember(color) {
        if (color.luminance() > 0.5f) {
            Color.Black
        } else {
            Color.White
        }
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.PaddingMedium),
        shape = RoundedCornerShape(Dimens.PaddingMedium),
        color = color,
        shadowElevation = Dimens.PaddingSmall,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = Dimens.PaddingExtraLarge,
                vertical = Dimens.PaddingMedium
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLocked) {
                        Icon(
                            painter = painterResource(R.drawable.lock),
                            contentDescription = "Locked",
                            tint = textColor.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(24.dp)
                                .padding(start = 4.dp)
                        )
                    }
                    
                    if (isPinned) {
                        Icon(
                            painter = painterResource(R.drawable.pin),
                            contentDescription = "Pinned",
                            tint = textColor.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(28.dp)
                                .padding(start = 4.dp)
                        )
                    }
                }
            }
            
            // Always show category section if categories exist
            if (categories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Show up to 3 categories
                    categories.take(3).forEach { category ->
                        CategoryChip(
                            category = category,
                            modifier = Modifier,
                            isSelected = false,
                            showRemoveIcon = false
                        )
                    }
                    
                    // Show "+N" indicator if there are more than 3 categories
                    if (categories.size > 3) {
                        Text(
                            text = "+${categories.size - 3}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = textColor.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoteComponentPreview() {
    NoteComponent(
        title = "Book Review : The Design of Everyday Things by Don Norman",
        colorCode = "72FE3E",
        isPinned = true,
        isLocked = true,
        categories = listOf(
            Category(id = 1, name = "Work", color = "#2196F3", icon = "work"),
            Category(id = 2, name = "Important", color = "#F44336", icon = "star")
        )
    )
}