package id.web.adhiwie.remindme;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

/**
 * Created by adhiwie on 01/03/15.
 */
public class ReminderEditActivity extends ActionBarActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener {

    public static final String TAG = "ReminderEditActivity";

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    String key, name, place;
    Double latitude ,longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_edit);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            EditText editTextName = (EditText) findViewById(R.id.activity_reminder_edit_edittext_reminder_name);
            EditText editTextPlace = (EditText) findViewById(R.id.activity_reminder_edit_edittext_place);

            editTextName.setText(bundle.getString("name"));
            editTextPlace.setText(bundle.getString("place"));

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
            EditText editTextName   = (EditText) findViewById(R.id.activity_reminder_edit_edittext_reminder_name);
            EditText editTextPlace  = (EditText) findViewById(R.id.activity_reminder_edit_edittext_place);

            String[] reminderArray = {editTextName.getText().toString(),
                    editTextPlace.getText().toString(),
                    Double.toString(mLastLocation.getLatitude()),
                    Double.toString(mLastLocation.getLongitude())};

            snappyDB.del(key);
            snappyDB.put(key, reminderArray);
            snappyDB.close();

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("message", "Reminder has been edited");
            startActivity(intent);

        } catch (SnappydbException e) {
        }
    }
}
