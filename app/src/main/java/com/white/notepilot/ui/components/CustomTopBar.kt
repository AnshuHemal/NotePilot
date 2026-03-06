package com.white.notepilot.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.white.notepilot.R
import com.white.notepilot.ui.theme.Dimens

@Composable
fun CustomTopBar(
    modifier: Modifier = Modifier,
    title: String = "",
    leftIconRes: Int? = null,
    onLeftIconClick: () -> Unit = {},
    rightIconRes: Int? = null,
    onRightIconClick: () -> Unit = {},
    customContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .systemBarsPadding()
            .padding(horizontal = Dimens.PaddingLarge)
            .padding(top = Dimens.PaddingSmall, bottom = 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leftIconRes != null) {
            RoundedImageCard(
                imageRes = leftIconRes,
                onClick = onLeftIconClick
            )
        } else {
            Row(modifier = Modifier.weight(1f)) {}
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.Center
        )

        if (customContent != null) {
            customContent()
        } else if (rightIconRes != null) {
            RoundedImageCard(
                imageRes = rightIconRes,
                onClick = onRightIconClick
            )
        } else {
            Row(modifier = Modifier.weight(1f)) {}
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun CustomTopBarPreview() {
    CustomTopBar(
        title = "Title",
        leftIconRes = R.drawable.back_arrow,
        rightIconRes = R.drawable.save
    )
}