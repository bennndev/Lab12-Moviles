package com.example.lab12.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.example.lab12.R
import com.google.android.gms.maps.CameraUpdateFactory
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.RoundCap
import com.google.android.gms.maps.model.SquareCap
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationRequest
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.compose.MapProperties
import kotlinx.coroutines.launch

@Composable
@SuppressLint("MissingPermission")
fun MapScreen() {
    // Crear scope de corrutinas para animaciones de cámara
    val scope = rememberCoroutineScope()

    val arequipaLocation = LatLng(-16.4040102, -71.559611)
    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
            arequipaLocation, 12f
        )
    }

    // Estado de permisos y ubicación del usuario
    val context = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    // Launcher de permisos
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasLocationPermission = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasLocationPermission) {
            val fused = LocationServices.getFusedLocationProviderClient(context)
            fused.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    val latLng = LatLng(loc.latitude, loc.longitude)
                    userLocation = latLng
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                    )
                }
            }
        }
    }

    // Solicitud de permisos al iniciar
    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        hasLocationPermission = fineGranted || coarseGranted
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            val fused = LocationServices.getFusedLocationProviderClient(context)
            fused.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    val latLng = LatLng(loc.latitude, loc.longitude)
                    userLocation = latLng
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                    )
                }
            }
        }
    }

    // Callback para actualizaciones en tiempo real
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                val latLng = LatLng(loc.latitude, loc.longitude)
                userLocation = latLng
                // Usar corrutina para la función suspend animate(...)
                scope.launch {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLng(latLng))
                }
            }
        }
    }

    // Iniciar/detener actualizaciones de ubicación mientras el Composable esté activo
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val fused = LocationServices.getFusedLocationProviderClient(context)
            val request = LocationRequest.Builder(2000L)
                .setMinUpdateIntervalMillis(1000L)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()
            fused.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        }
    }

    // Render del mapa y marcadores (fallback y en tiempo real)
    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            // Marcador de fallback en Arequipa (azul) si aún no hay ubicación del usuario
            if (userLocation == null) {
                Marker(
                    state = rememberMarkerState(position = arequipaLocation),
                    title = "Arequipa, Perú",
                    icon = BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_BLUE
                    )
                )
            }

            // Marcador rojo en la ubicación del usuario cuando esté disponible
            userLocation?.let { latLng ->
                Marker(
                    state = rememberMarkerState(position = latLng),
                    title = "Mi ubicación",
                    icon = BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_RED
                    )
                )
            }
        }
    }
}






















