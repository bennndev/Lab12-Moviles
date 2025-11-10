package com.example.lab12

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lab12.ui.screens.MapTypeScreen
import com.example.lab12.ui.screens.MapScreen
import com.example.lab12.ui.screens.MapPlacesScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val selected = remember { mutableStateOf("places") }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(onClick = { selected.value = "places" }) { Text("Places") }
                    Button(onClick = { selected.value = "types" }) { Text("Tipos de mapa") }
                    Button(onClick = { selected.value = "user" }) { Text("Mi ubicación") }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    when (selected.value) {
                        "places" -> MapPlacesScreen()
                        "types" -> MapTypeScreen()
                        "user" -> MapScreen()
                    }
                }
            }
        }
    }
}
