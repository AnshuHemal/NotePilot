package com.white.notepilot.ui.components.ads

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.white.notepilot.R

@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    adUnitId: String = stringResource(R.string.banner_ad_unit_id),
    showSpacerBelow: Boolean = true
) {
    val context = LocalContext.current
    val adView = remember {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            this.adUnitId = adUnitId
        }
    }
    
    DisposableEffect(Unit) {
        adView.loadAd(AdRequest.Builder().build())
        onDispose {
            adView.destroy()
        }
    }
    
    Column {
        AndroidView(
            modifier = modifier
                .fillMaxWidth(),
            factory = { adView }
        )
        
        if (showSpacerBelow) {
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}
