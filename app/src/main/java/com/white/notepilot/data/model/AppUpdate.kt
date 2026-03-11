package com.white.notepilot.data.model

import com.google.firebase.firestore.PropertyName

data class AppUpdate(
    @PropertyName("latestVersion") val latestVersion: String = "",
    @PropertyName("minimumVersion") val minimumVersion: String = "",
    @PropertyName("forceUpdate") val forceUpdate: Boolean = false,
    @PropertyName("updateTitle") val updateTitle: String = "New Update Available!",
    @PropertyName("updateMessage") val updateMessage: String = "We've added new features and fixed some bugs to make your experience as smooth as possible",
    @PropertyName("playStoreUrl") val playStoreUrl: String = "",
    @PropertyName("isEnabled") val isEnabled: Boolean = true
)