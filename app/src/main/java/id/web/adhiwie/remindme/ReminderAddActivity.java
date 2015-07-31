package id.web.adhiwie.remindme;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.util.ArrayList;
import java.util.List;


public class ReminderAddActivity extends ActionBarActivity
        implements  OnMapReadyCallback,
                    GoogleApiClient.ConnectionCallbacks,
                    GoogleApiClient.OnConnectionFailedListener,
                    GoogleMap.OnMarkerDragListener {

    public static final String TAG = "ReminderAddActivity";

    /** Initialization **/
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private String name, place;
    double latitude, longitude;
    private ArrayList<Geofence> geofences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_add);

        // Building the GoogleApi client
        buildGoogleApiClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onStart();
        mGoogleApiClient.disconnect();
    }
    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.d(TAG, "Location found");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // TODO
        Log.d(TAG, "Connection suspended!");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // TODO
        Log.d(TAG, "Connection failed!");
    }

    @Override
    public void onMapReady(GoogleMap map) {
        LatLng loc = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());

        latitude = loc.latitude;
        longitude = loc.longitude;

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
        map.getUiSettings().setZoomControlsEnabled(true);

        map.addMarker(new MarkerOptions()
                .position(loc)
                .draggable(true)
                .title("Remind me at this place"));

        map.setOnMarkerDragListener(this);
    }

    @Override
    public void onMarkerDrag(Marker marker){
    }

    @Override
    public void onMarkerDragStart(Marker marker){
    }

    @Override
    public void onMarkerDragEnd(Marker marker){
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;

        Log.d(TAG, "Marker position: "+Double.toString(latitude)+","+Double.toString(longitude));
    }

    public void saveReminder(View view){
        try {
            DB snappyDB = DBFactory.open(getApplicationContext(), "remindersDB"); //create or open an existing database using the default name

            EditText editTextName   = (EditText) findViewById(R.id.activity_reminder_add_edittext_reminder_name);
            EditText editTextPlace  = (EditText) findViewById(R.id.activity_reminder_add_edittext_place);

            name = editTextName.getText().toString();
            place = editTextPlace.getText().toString();

            String[] reminderArray = {name, place,
                    Double.toString(latitude),
                    Double.toString(longitude)};

            /* Insert Reminder into database */
            int countReminder = snappyDB.countKeys("reminder");

            if(countReminder > 0){
                int nextIndex = ++countReminder;
                String nextIndexKey = Integer.toString(nextIndex);
                String index = "reminder:"+nextIndexKey;
                snappyDB.put(index, reminderArray);
            }else{
                snappyDB.put("reminder:1", reminderArray);
            }
            /* End of inserting reminder into database */


            snappyDB.close();

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("message", "New reminder has been added");
            startActivity(intent);

        } catch (SnappydbException e) {
        }
    }
}
