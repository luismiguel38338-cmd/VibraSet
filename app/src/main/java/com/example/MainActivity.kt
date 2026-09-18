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
import androidx.core.content.ContextCompat
import com.example.ui.theme.VibraSetTheme
import com.example.vibraset.ui.VibraSetMainScreen
import com.example.vibraset.ui.VibraSetViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: VibraSetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
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
                VibraSetMainScreen(viewModel = viewModel)
            }
        }
    }
}
