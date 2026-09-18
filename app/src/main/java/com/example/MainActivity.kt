package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.ui.theme.VibraSetTheme
import com.example.vibraset.ui.VibraSetMainScreen
import com.example.vibraset.ui.VibraSetViewModel
import com.example.vibraset.ui.components.VibraSetSplashScreen

class MainActivity : ComponentActivity() {

    private val viewModel: VibraSetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var showSplash by remember { mutableStateOf(true) }

            // Check & request RECORD_AUDIO permission for hardware Visualizer FFT capture
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* Audio engine seamlessly adapts if granted or denied */ }

            LaunchedEffect(Unit) {
                val hasAudioPerm = ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasAudioPerm) {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }

            VibraSetTheme {
                if (showSplash) {
                    VibraSetSplashScreen(
                        onSplashFinished = { showSplash = false }
                    )
                } else {
                    VibraSetMainScreen(viewModel = viewModel)
                }
            }
        }
    }
}
