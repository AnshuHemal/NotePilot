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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.white.notepilot.R
import com.white.notepilot.ui.theme.Dimens

@Composable
fun CustomPopupDialog(
    message: String,
    negativeButtonText: String,
    positiveButtonText: String,
    onNegativeClick: () -> Unit,
    onPositiveClick: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(Dimens.PaddingLarge),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(Dimens.PaddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.info),
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
                ) {
                    Button(
                        onClick = onNegativeClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        ),
                        shape = RoundedCornerShape(Dimens.PaddingSmall)
                    ) {
                        Text(
                            text = negativeButtonText,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = Dimens.PaddingSmall)
                        )
                    }
                    
                    Button(
                        onClick = onPositiveClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(Dimens.PaddingSmall)
                    ) {
                        Text(
                            text = positiveButtonText,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = Dimens.PaddingSmall)
                        )
                    }
                }
            }
        }
    }
}
