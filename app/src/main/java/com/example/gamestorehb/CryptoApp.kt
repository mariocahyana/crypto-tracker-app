package com.example.gamestorehb

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class annotated with @HiltAndroidApp to trigger Hilt's code generation.
 * This serves as the root component for Hilt's dependency injection graph.
 */
@HiltAndroidApp
class CryptoApp : Application()
