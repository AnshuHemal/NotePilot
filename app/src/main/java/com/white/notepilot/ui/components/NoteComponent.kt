package com.white.notepilot.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.white.notepilot.data.model.Category
import com.white.notepilot.ui.theme.Dimens
import com.white.notepilot.utils.ColorUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteComponent(
    title: String,
    colorCode: String,
    categories: List<Category> = emptyList(),
    onClick: () -> Unit = {}
) {
    // Parse color once and remember it
    val color = remember(colorCode) { 
        Color(ColorUtils.colorCodeToInt(colorCode))
    }
    
    // Calculate text color based on background luminance and remember it
    val textColor = remember(color) {
        if (color.luminance() > 0.5f) {
            Color.Black // Use black text for light backgrounds
        } else {
            Color.White // Use white text for dark backgrounds
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
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            if (categories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { category ->
                        CategoryChip(
                            category = category,
                            modifier = Modifier
                        )
                    }
                    
                    if (categories.size > 3) {
                        Text(
                            text = "+${categories.size - 3}",
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
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
        categories = listOf(
            Category(id = 1, name = "Work", color = "#2196F3", icon = "work"),
            Category(id = 2, name = "Important", color = "#F44336", icon = "star")
        )
    )
}