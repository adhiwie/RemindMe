package id.web.adhiwie.remindme;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adhiwie on 01/03/15.
 */
public class ReminderDetailActivity extends ActionBarActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    public static final String TAG = "ReminderDetailActivity";

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    String key, name, place;
    Double latitude ,longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_detail);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            TextView textViewName = (TextView) findViewById(R.id.activity_reminder_detail_textview_reminder_name);
            TextView textViewPlace = (TextView) findViewById(R.id.activity_reminder_detail_textview_place);

            textViewName.setText(bundle.getString("name"));
            textViewPlace.setText(bundle.getString("place"));

            key = bundle.getString("key");
            name = bundle.getString("name");
            place = bundle.getString("place");
            latitude = bundle.getDouble("latitude");
            longitude = bundle.getDouble("longitude");
        }

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
        MapFragment mapFragment = (MapFragment) getFragmentManager()
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
        LatLng loc = new LatLng(latitude, longitude);

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
        map.getUiSettings().setZoomControlsEnabled(true);

        map.addMarker(new MarkerOptions()
                .position(loc)
                .title("Remind me at this place"));
    }

    public void editReminder(View view){
        Intent intent = new Intent(this, ReminderEditActivity.class);
        intent.putExtra("key", key);
        intent.putExtra("name", name);
        intent.putExtra("place", place);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        startActivity(intent);
    }

    public void deleteReminder(View view){
        try {
            DB snappyDB = DBFactory.open(getApplicationContext(), "remindersDB"); //create or open an existing database using the default name
            snappyDB.del(key);
            snappyDB.close();

            List<String> geofenceRequestIds = new ArrayList<String>();
            geofenceRequestIds.add(key);
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, geofenceRequestIds);

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("message", "Reminder has been deleted");
            startActivity(intent);

        } catch (SnappydbException e) {
        }
    }
}
