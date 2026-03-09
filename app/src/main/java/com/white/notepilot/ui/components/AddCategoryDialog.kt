package com.white.notepilot.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.toColorInt
import com.white.notepilot.R
import com.white.notepilot.ui.theme.Blue
import kotlin.math.roundToInt

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, color: String, icon: String) -> Unit,
    existingCategoryNames: List<String> = emptyList()
) {
    var categoryName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#4CAF50") }
    var showCustomColorPicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    var hue by remember { mutableFloatStateOf(120f) }
    var saturation by remember { mutableFloatStateOf(0.69f) }
    var value by remember { mutableFloatStateOf(0.69f) }
    
    fun hsvToColor(h: Float, s: Float, v: Float): String {
        val color = Color.hsv(h, s, v)
        val red = (color.red * 255).roundToInt()
        val green = (color.green * 255).roundToInt()
        val blue = (color.blue * 255).roundToInt()
        return String.format("#%02X%02X%02X", red, green, blue)
    }
    
    fun hexToHsv(hex: String): Triple<Float, Float, Float> {
        return try {
            val color = Color(hex.toColorInt())
            val hsv = FloatArray(3)
            android.graphics.Color.RGBToHSV(
                (color.red * 255).toInt(),
                (color.green * 255).toInt(),
                (color.blue * 255).toInt(),
                hsv
            )
            Triple(hsv[0], hsv[1], hsv[2])
        } catch (_: Exception) {
            Triple(120f, 0.69f, 0.69f)
        }
    }
    
    val availableColors = listOf(
        "#4CAF50" to "Green",
        "#2196F3" to "Blue",
        "#FF9800" to "Orange",
        "#F44336" to "Red",
        "#9C27B0" to "Purple",
        "#00BCD4" to "Cyan",
        "#FFEB3B" to "Yellow",
        "#795548" to "Brown"
    )
    
    val arrowRotation by animateFloatAsState(
        targetValue = if (showCustomColorPicker) 90f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "arrow_rotation"
    )
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Category",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            val scrollState = rememberScrollState()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp)
                        .padding(top = 32.dp)
                ) {
                    OutlinedTextField(
                        value = categoryName,
                        onValueChange = {
                            categoryName = it
                            errorMessage = null
                        },
                        label = { Text("Category Name", style = MaterialTheme.typography.bodyMedium) },
                        placeholder = { Text("e.g., Shopping, Travel", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        singleLine = true,
                        isError = errorMessage != null,
                        supportingText = if (errorMessage != null) {
                            { Text(errorMessage!!, color = MaterialTheme.colorScheme.error) }
                        } else null
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = "Select Color",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        availableColors.take(4).forEach { (colorHex, _) ->
                            ColorOption(
                                color = colorHex,
                                isSelected = selectedColor == colorHex,
                                onClick = { 
                                    selectedColor = colorHex
                                    showCustomColorPicker = false
                                    val (h, s, v) = hexToHsv(colorHex)
                                    hue = h
                                    saturation = s
                                    value = v
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        availableColors.drop(4).forEach { (colorHex, _) ->
                            ColorOption(
                                color = colorHex,
                                isSelected = selectedColor == colorHex,
                                onClick = { 
                                    selectedColor = colorHex
                                    showCustomColorPicker = false
                                    val (h, s, v) = hexToHsv(colorHex)
                                    hue = h
                                    saturation = s
                                    value = v
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { 
                                showCustomColorPicker = !showCustomColorPicker
                                if (showCustomColorPicker) {
                                    val (h, s, v) = hexToHsv(selectedColor)
                                    hue = h
                                    saturation = s
                                    value = v
                                }
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        try {
                                            Color(selectedColor.toColorInt())
                                        } catch (_: Exception) {
                                            MaterialTheme.colorScheme.primary
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            )
                            
                            Column {
                                Text(
                                    text = "Custom Color",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = if (showCustomColorPicker) "Tap to collapse" else "Tap to expand",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        
                        Icon(
                            painter = painterResource(R.drawable.arrow_right),
                            contentDescription = if (showCustomColorPicker) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer { rotationZ = arrowRotation }
                        )
                    }
                    
                    AnimatedVisibility(
                        visible = showCustomColorPicker,
                        enter = expandVertically(
                            animationSpec = tween(durationMillis = 300)
                        ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                        exit = shrinkVertically(
                            animationSpec = tween(durationMillis = 300)
                        ) + fadeOut(animationSpec = tween(durationMillis = 300))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Selected Color",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = selectedColor,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                try {
                                                    Color(selectedColor.toColorInt())
                                                } catch (_: Exception) {
                                                    MaterialTheme.colorScheme.primary
                                                }
                                            )
                                            .border(
                                                width = 2.dp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            ColorSlider(
                                label = "Hue",
                                value = hue,
                                onValueChange = { 
                                    hue = it
                                    selectedColor = hsvToColor(hue, saturation, value)
                                },
                                valueRange = 0f..360f,
                                colors = listOf(
                                    Color.Red,
                                    Color.Yellow,
                                    Color.Green,
                                    Color.Cyan,
                                    Color.Blue,
                                    Color.Magenta,
                                    Color.Red
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            ColorSlider(
                                label = "Saturation",
                                value = saturation,
                                onValueChange = { 
                                    saturation = it
                                    selectedColor = hsvToColor(hue, saturation, value)
                                },
                                valueRange = 0f..1f,
                                colors = listOf(
                                    Color.White,
                                    Color.hsv(hue, 1f, value)
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            ColorSlider(
                                label = "Brightness",
                                value = value,
                                onValueChange = { 
                                    value = it
                                    selectedColor = hsvToColor(hue, saturation, value)
                                },
                                valueRange = 0f..1f,
                                colors = listOf(
                                    Color.Black,
                                    Color.hsv(hue, saturation, 1f)
                                )
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp, top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Blue
                        )
                    }
                    
                    Button(
                        onClick = {
                            when {
                                categoryName.isBlank() -> {
                                    errorMessage = "Category name cannot be empty"
                                }
                                existingCategoryNames.any { it.equals(categoryName.trim(), ignoreCase = true) } -> {
                                    errorMessage = "Category already exists"
                                }
                                else -> {
                                    onAdd(categoryName.trim(), selectedColor, "category_label")
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Add",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorOption(
    color: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorInt = try {
        Color(color.toColorInt())
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }
    
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colorInt)
            .border(
                width = if (isSelected) 4.dp else 0.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                painter = painterResource(R.drawable.check),
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    colors: List<Color>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = when (label) {
                    "Hue" -> "${value.roundToInt()}°"
                    else -> "${(value * 100).roundToInt()}%"
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                drawRect(
                    brush = Brush.horizontalGradient(colors = colors),
                    size = size
                )
            }
            
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                ),
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(
                                width = 3.dp,
                                color = Blue,
                                shape = CircleShape
                            )
                    )
                }
            )
        }
    }
}
