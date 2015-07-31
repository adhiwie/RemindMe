package id.web.adhiwie.remindme;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.text.DecimalFormat;

/**
 * Created by adhiwie on 03/03/15.
 */
public class DirectionActivity extends ActionBarActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final String TAG = "DirectionActivity";

    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LatLng fromLatLng, toLatLng;
    private double latitude, longitude, distance;
    private String place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            latitude = bundle.getDouble("latitude");
            longitude = bundle.getDouble("longitude");
            place = bundle.getString("place");
            toLatLng = new LatLng(latitude, longitude);
        }

        createLocationRequest();
        // Building the GoogleApi client
        buildGoogleApiClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
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

        //mLastLocation = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,)

        fromLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

        startLocationUpdates();
        calculateDistance();
        updateUI();

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
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(fromLatLng, 15));
        map.getUiSettings().setZoomControlsEnabled(true);

        map.addMarker(new MarkerOptions()
                .position(toLatLng)
                .title("Remind me at this place"));
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        fromLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        calculateDistance();
        updateUI();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    private void updateUI() {
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        TextView textView = (TextView) findViewById(R.id.activity_direction_textview_distance);
        DecimalFormat df = new DecimalFormat("0");
        String distanceFormatted = df.format(distance);
        String str = distanceFormatted+" meters to "+place;
        textView.setText(str);

        Log.d(TAG, "Distance : "+Double.toString(distance));
    }

    private void calculateDistance(){
        distance = SphericalUtil.computeDistanceBetween(fromLatLng, toLatLng);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }
}

