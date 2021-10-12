package com.aaron.cs.geocacheassessment

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location;
    private lateinit var fusedLocationClient : FusedLocationProviderClient

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 100
    }

    private val positiveButtonClick = { dialog: DialogInterface, which: Int ->
        Toast.makeText(applicationContext, "OK", Toast.LENGTH_SHORT).show();
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        setUpMap()
    }

    /**
     *
     * Set up map, asks users for their required permissions, once granted it sets the fused location
     * to the user's current location then places the marker, animating the camera to the current
     * user's location
     *
     */
    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_ACCESS_FINE_LOCATION)
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->

            if(location != null) {
                lastLocation = location
                val currentLatLong = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLong)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 12f))
            }
        }
    }

    /**
     *
     * Places a marker on a map, giving it the title of it's current lat/long values
     *
     */
    private fun placeMarkerOnMap(currentLatLong: LatLng) {
        val markerOptions = MarkerOptions().position(currentLatLong)
        markerOptions.title("$currentLatLong")
        mMap.addMarker(markerOptions)
    }

    /**
     * onRequestPermissionResult() for permission, if it has been granted, set up the map
     * if not use a dummy action dialogue which pops up prompting the user the permission is required
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> setUpMap()
                PackageManager.PERMISSION_DENIED -> dialogPermissionDenied(window.decorView)
            }
        }
    }

    /**
     * Inflates the toolbar's menu items
     */

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_buttons, menu)
        return true
    }

    /**
     * If the add maker menu item is selected, give the updated location and place the marker
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId){
        R.id.action_add -> {
            mMap.clear()

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_ACCESS_FINE_LOCATION)
            }
            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->

                if(location != null) {
                    lastLocation = location
                    val currentLatLong = LatLng(location.latitude, location.longitude)
                    placeMarkerOnMap(currentLatLong)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 12f))
                }
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun dialogPermissionDenied(view: View) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Location Access")
        builder.setMessage("Allow your location to continue with the app")
        builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = positiveButtonClick))
        builder.show()
    }

    override fun onMarkerClick(p0: Marker?) = false

}