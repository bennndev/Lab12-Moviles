// En este archivo: MapPlacesScreen() composable
package com.example.lab12.ui.screens

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch
import com.google.android.gms.common.api.ApiException

@Composable
@SuppressLint("MissingPermission")
fun MapPlacesScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val placesClient = remember {
        if (!Places.isInitialized()) {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName, PackageManager.GET_META_DATA
            )
            val apiKey = appInfo.metaData?.getString("com.google.android.geo.API_KEY") ?: ""
            if (apiKey.isNotBlank()) {
                Places.initialize(context.applicationContext, apiKey)
            }
        }
        Places.createClient(context)
    }

    val token = remember { AutocompleteSessionToken.newInstance() }

    var query by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }
    var placePhoto by remember { mutableStateOf<Bitmap?>(null) }

    val arequipa = LatLng(-16.4040102, -71.559611)
    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(arequipa, 12f)
    }

    fun searchPredictions(q: String) {
        if (q.length < 2) {
            predictions = emptyList()
            return
        }
        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(q)
            .setCountries(listOf("PE")) // limita a Perú para resultados locales
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                predictions = response.autocompletePredictions
                Log.d("MapPlaces", "Autocomplete resultados: ${predictions.size}")
            }
            .addOnFailureListener { e ->
                Log.e("MapPlaces", "Autocomplete error: ${e.message}", e)
                val apiException = e as? ApiException
                if (apiException?.statusCode == 9011) {
                    Toast.makeText(
                        context,
                        "Places requires billing enabled on your Google Cloud project.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Autocomplete failed: ${e.localizedMessage ?: "Unknown error"}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                predictions = emptyList()
            }
    }


    fun fetchPlaceDetails(placeId: String) {
        val fields = listOf(
            com.google.android.libraries.places.api.model.Place.Field.LOCATION,
            com.google.android.libraries.places.api.model.Place.Field.DISPLAY_NAME,
            com.google.android.libraries.places.api.model.Place.Field.ADR_FORMAT_ADDRESS,
            com.google.android.libraries.places.api.model.Place.Field.PHOTO_METADATAS
        )
        val request = com.google.android.libraries.places.api.net.FetchPlaceRequest
            .builder(placeId, fields)
            .build()
    
        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place: com.google.android.libraries.places.api.model.Place = response.place

                selectedLatLng = place.location      // LatLng?
                selectedName = place.displayName
                placePhoto = null
    
                place.location?.let { loc: com.google.android.gms.maps.model.LatLng ->
                    scope.launch {
                        cameraPositionState.animate(
                            com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(loc, 15f)
                        )
                    }
                }
    
                val photoMetadata: com.google.android.libraries.places.api.model.PhotoMetadata? =
                    place.photoMetadatas?.firstOrNull()
                if (photoMetadata != null) {
                    val photoRequest = com.google.android.libraries.places.api.net.FetchPhotoRequest
                        .builder(photoMetadata)
                        .setMaxWidth(800)
                        .setMaxHeight(600)
                        .build()
                    placesClient.fetchPhoto(photoRequest)
                        .addOnSuccessListener { photoResponse ->
                            placePhoto = photoResponse.bitmap
                        }
                        .addOnFailureListener {
                            placePhoto = null
                        }
                }
            }
            .addOnFailureListener {
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = query,
                onValueChange = { q ->
                    query = q
                    searchPredictions(q)
                },
                modifier = Modifier.weight(1f),
                label = { Text("Buscar lugares (Places)") }
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = { searchPredictions(query) }) {
                Text("Buscar")
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = 8.dp)
        ) {
            items(predictions) { prediction ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Text(text = prediction.getPrimaryText(null).toString())
                    Text(text = prediction.getSecondaryText(null).toString())
                    Spacer(Modifier.height(6.dp))
                    Button(onClick = { fetchPlaceDetails(prediction.placeId) }) {
                        Text("Ver en mapa")
                    }
                }
                Divider()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                if (selectedLatLng == null) {
                    Marker(
                        state = rememberMarkerState(position = arequipa),
                        title = "Arequipa, Perú",
                        icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                            com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_BLUE
                        )
                    )
                }
                selectedLatLng?.let { latLng ->
                    Marker(
                        state = rememberMarkerState(position = latLng),
                        title = selectedName ?: "Lugar seleccionado",
                        icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                            com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
                        )
                    )
                }
            }

            placePhoto?.let { bmp ->
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text(text = selectedName ?: "")
                    Spacer(Modifier.height(4.dp))
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Foto del lugar",
                        modifier = Modifier.size(width = 200.dp, height = 120.dp)
                    )
                }
            }
        }
    }
}
