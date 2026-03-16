package com.white.notepilot.ui.state

import com.white.notepilot.data.ml.SmartTagEngine

sealed class SmartTagState {
    data object Idle : SmartTagState()
    data object Analysing : SmartTagState()
    data class Ready(val suggestions: List<SmartTagEngine.TagSuggestion>) : SmartTagState()
}
