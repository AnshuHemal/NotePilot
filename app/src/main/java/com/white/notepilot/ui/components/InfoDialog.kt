package com.white.notepilot.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.white.notepilot.R
import com.white.notepilot.ui.theme.Dimens

@Composable
fun InfoDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.width(350.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = Dimens.PaddingExtraLarge,
                        horizontal = Dimens.PaddingExtraLarge
                    ),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)
            ) {
                Text(
                    text = stringResource(R.string.designed_by),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = stringResource(R.string.redesigned_by),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = stringResource(R.string.illustrations_flaticon),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = stringResource(R.string.icons_flaticon),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = stringResource(R.string.font_nunito),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(Dimens.PaddingLarge))

                Text(
                    text = stringResource(R.string.made_by_hemal_katariya),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}