package locationtrail.msk.com.locationtrail;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private ArrayList<LatLng> locTrails;
    private LocationListener locationListener;
    private LocationManager locationManager;

    private final long MIN_TIME = 5000;        //time to update location = 5 seconds
    private final long MIN_DISTANCE = 10;    //in meters displacement

    Polyline polyline = null;

    //Bluetooth
    Button nearby;
    ArrayList<BluetoothDevice> nearbyDevices;
    RelativeLayout header;
    ListView nearbyDevicesList;
    DeviceListAdapter deviceListAdapter;
    BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        nearbyDevices = new ArrayList<>();


        nearbyDevicesList = new ListView(this);
        View headerView = ((LayoutInflater)this.getSystemService(this.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.nearby_list_container, null, false);

        nearbyDevicesList.addHeaderView(headerView);
        locTrails = new ArrayList<>();


        nearby = findViewById(R.id.nearby);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        deviceListAdapter = new DeviceListAdapter(this, R.layout.device_adapter_view, nearbyDevices);
        nearbyDevicesList.setAdapter(deviceListAdapter);

        nearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Register for broadcasts when a device is discovered.
//                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//
                showNearbyDevices();

            }
        });


        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 900);
        startActivity(discoverableIntent);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        bluetoothAdapter.startDiscovery();

    }


    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d("DEBUG", "RECIEVED");

            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address


                Log.d("DEBUG", deviceName + deviceHardwareAddress);

                if (nearbyDevices.indexOf(device) == -1) {
                    nearbyDevices.add(device);
                }

                deviceListAdapter.notifyDataSetChanged();
                }

            }
        };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }
//


        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera. In this case,
         * we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to install
         * it inside the SupportMapFragment. This method will only be triggered once the user has
         * installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            // adding polylines
            final PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(locTrails).clickable(true);

            polyline = googleMap.addPolyline(polylineOptions);
            polyline.setWidth(10f);

            Log.d("DEBUG", "MAP READY");

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    Log.d("DEBUG", "UPDATED");

                    LatLng updated = new LatLng(location.getLatitude(), location.getLongitude());

                    // adding new updated location
                    locTrails.add(updated);

                    //adding marker
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String timeStamp = dateFormat.format(new Date());

                    mMap.addMarker(new MarkerOptions().position(updated).title("Timestamp: " + timeStamp));
                    //mMap.animateCamera(CameraUpdateFactory.newLatLng(updated));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(updated, 16.0f));

                    //adding new polyline
                    polylineOptions.add(updated);
                    polyline = mMap.addPolyline(polylineOptions);
                    polyline.setColor(Color.rgb(74, 137, 243));

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            //requesting location updates for location listener
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, "GPS is Enabled in your device", Toast.LENGTH_SHORT).show();

            } else {
                // GPS not enabled
                Toast.makeText(this, "GPS is not enabled in your device", Toast.LENGTH_SHORT).show();
                showGPSDisabledAlertToUser();
            }

            //setting location update time and distance
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);


                Log.d("DEBUG", "first");

                // first marker
                Location loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                LatLng updated = new LatLng(loc.getLatitude(), loc.getLongitude());

                // adding new updated location
                locTrails.add(updated);

                //adding marker
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timeStamp = dateFormat.format(new Date());

                mMap.addMarker(new MarkerOptions().position(updated).title("Timestamp: " + timeStamp));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(updated, 16.0f));

                //adding new polyline
                polylineOptions.add(updated);
                polyline = mMap.addPolyline(polylineOptions);
                polyline.setColor(Color.rgb(74, 137, 243));

            } catch (SecurityException | NullPointerException e) {
                e.printStackTrace();
            }

        }


        private void showNearbyDevices() {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setPositiveButton("OK", null);
            if (nearbyDevicesList.getParent() != null) {
                ((ViewGroup) nearbyDevicesList.getParent()).removeView(nearbyDevicesList);
            }
//            LayoutInflater inflater = this.getLayoutInflater();
//            View dialogView = inflater.inflate(R.layout.nearby_list_container, null);
            builder.setView(nearbyDevicesList);
            AlertDialog alert = builder.create();
            alert.show();
        }


        private void showGPSDisabledAlertToUser() {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("GPS is disabled in your device please enable it.")
                    .setCancelable(false)
                    .setPositiveButton("Goto Settings Page To Enable GPS",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(callGPSSettingIntent);
                                }
                            });

            alertDialogBuilder.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }
}


