package com.example.lab12.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapTypeScreen() {
    val arequipaLocation = LatLng(-16.4040102, -71.559611)
    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
            arequipaLocation, 12f
        )
    }

    val selectedMapType = remember { mutableStateOf(MapType.NORMAL) }
    val uiSettings = remember { MapUiSettings(zoomControlsEnabled = true) }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapType = selectedMapType.value),
            uiSettings = uiSettings
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(8.dp)
        ) {
            Button(onClick = { selectedMapType.value = MapType.NORMAL }) { Text("Normal") }
            Spacer(modifier = Modifier.size(8.dp))
            Button(onClick = { selectedMapType.value = MapType.HYBRID }) { Text("Hybrid") }
            Spacer(modifier = Modifier.size(8.dp))
            Button(onClick = { selectedMapType.value = MapType.TERRAIN }) { Text("Terrain") }
            Spacer(modifier = Modifier.size(8.dp))
            Button(onClick = { selectedMapType.value = MapType.SATELLITE }) { Text("Satellite") }
        }
    }
}

