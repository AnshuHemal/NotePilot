package com.white.notepilot.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

object HapticFeedbackHelper {
    
    enum class HapticType {
        LIGHT_CLICK,
        MEDIUM_CLICK,
        HEAVY_CLICK,
        SUCCESS,
        WARNING,
        ERROR,
        SELECTION,
        LONG_PRESS
    }
    
    fun performHapticFeedback(context: Context, type: HapticType) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (!vibrator.hasVibrator()) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = when (type) {
                HapticType.LIGHT_CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                HapticType.MEDIUM_CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                HapticType.HEAVY_CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                HapticType.SUCCESS -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                HapticType.WARNING -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                HapticType.ERROR -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                HapticType.SELECTION -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                HapticType.LONG_PRESS -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
            }
            vibrator.vibrate(effect)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val duration = when (type) {
                HapticType.LIGHT_CLICK, HapticType.SELECTION -> 10L
                HapticType.MEDIUM_CLICK, HapticType.SUCCESS -> 20L
                HapticType.HEAVY_CLICK, HapticType.ERROR, HapticType.LONG_PRESS -> 50L
                HapticType.WARNING -> 30L
            }
            val amplitude = when (type) {
                HapticType.LIGHT_CLICK, HapticType.SELECTION -> 50
                HapticType.MEDIUM_CLICK, HapticType.SUCCESS -> 100
                HapticType.HEAVY_CLICK, HapticType.ERROR, HapticType.LONG_PRESS -> 200
                HapticType.WARNING -> 150
            }
            vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } else {
            @Suppress("DEPRECATION")
            val duration = when (type) {
                HapticType.LIGHT_CLICK, HapticType.SELECTION -> 10L
                HapticType.MEDIUM_CLICK, HapticType.SUCCESS -> 20L
                HapticType.HEAVY_CLICK, HapticType.ERROR, HapticType.LONG_PRESS -> 50L
                HapticType.WARNING -> 30L
            }
            vibrator.vibrate(duration)
        }
    }
    
    fun performViewHapticFeedback(view: View, type: HapticType) {
        val feedbackConstant = when (type) {
            HapticType.LIGHT_CLICK, HapticType.SELECTION -> HapticFeedbackConstants.CLOCK_TICK
            HapticType.MEDIUM_CLICK, HapticType.SUCCESS -> HapticFeedbackConstants.CONTEXT_CLICK
            HapticType.HEAVY_CLICK, HapticType.ERROR -> HapticFeedbackConstants.LONG_PRESS
            HapticType.WARNING -> HapticFeedbackConstants.CONTEXT_CLICK
            HapticType.LONG_PRESS -> HapticFeedbackConstants.LONG_PRESS
        }
        view.performHapticFeedback(feedbackConstant)
    }
}

@Composable
fun rememberHapticFeedback(): (HapticFeedbackHelper.HapticType) -> Unit {
    val context = LocalContext.current
    val view = LocalView.current
    
    return remember(context, view) {
        { type ->
            try {
                HapticFeedbackHelper.performViewHapticFeedback(view, type)
            } catch (e: Exception) {
                HapticFeedbackHelper.performHapticFeedback(context, type)
            }
        }
    }
}
