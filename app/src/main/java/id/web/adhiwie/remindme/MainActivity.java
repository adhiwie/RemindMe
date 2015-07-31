package id.web.adhiwie.remindme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.util.ArrayList;
import java.util.List;

import id.web.adhiwie.remindme.id.web.adhiwie.remindme.model.ReminderModel;


public class MainActivity extends ActionBarActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public static final String TAG = "MainActivity";

    /** Initialization **/
    private GoogleApiClient mGoogleApiClient = null;
    private Location mLastLocation = null;
    private ArrayList<ReminderModel> reminderArrayList = null;
    private List<Geofence> geofencesList = new ArrayList<>();
    private GeofencingRequest geofencingRequest = null;
    private ReminderModel reminderModel = null;
    private String name, place;
    private Double latitude, longitude;
    private Bundle bundle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Building the GoogleApi client
        buildGoogleApiClient();

        try {
            DB snappyDB = DBFactory.open(getApplicationContext(), "remindersDB"); //create or open an existing database using the default name

            reminderArrayList = new ArrayList<>();

            String [] keys = snappyDB.findKeys("reminder");
            for(int i=0; i<keys.length; i++){
                String[] array = snappyDB.getObjectArray(keys[i], String.class);
                name = array[0];
                place = array[1];
                latitude = Double.parseDouble(array[2]);
                longitude = Double.parseDouble(array[3]);

                reminderModel = new ReminderModel();
                reminderModel.setKey(keys[i]);
                reminderModel.setName(name);
                reminderModel.setPlace(place);
                reminderModel.setLatitude(latitude);
                reminderModel.setLongitude(longitude);
                reminderArrayList.add(reminderModel);
            }
            snappyDB.close();

        } catch (SnappydbException e) {
        }

        bundle = getIntent().getExtras();
        if(bundle != null){
            Toast.makeText(getApplicationContext(), bundle.getString("message"),
                    Toast.LENGTH_SHORT).show();
        }

        ReminderListAdapter adapter = new ReminderListAdapter(this, reminderArrayList);
        ListView listview = (ListView) findViewById(R.id.activity_main_listview);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(view.getContext(), ReminderDetailActivity.class);
                intent.putExtra("key",reminderArrayList.get(position).getKey());
                intent.putExtra("name",reminderArrayList.get(position).getName());
                intent.putExtra("place",reminderArrayList.get(position).getPlace());
                intent.putExtra("latitude", reminderArrayList.get(position).getLatitude());
                intent.putExtra("longitude", reminderArrayList.get(position).getLongitude());
                startActivity(intent);
            }

        });

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
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "connected!");

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        TextView textView = (TextView) findViewById(R.id.activity_main_textview_empty);
        if(reminderArrayList.size() > 0){
            for(ReminderModel reminder : reminderArrayList){
                createGeofences(reminder.getLatitude(),
                        reminder.getLongitude(),
                        reminder.getName(),
                        reminder.getPlace(),
                        reminder.getKey());
            }
        } else {
            textView.setText("No reminder set. Please add reminder");
        }

        if(geofencesList.size() > 0){
            createGeofencingRequest(geofencesList);
        }

        if(reminderArrayList != null){
            //Log.d(TAG, "Reminder size : "+Integer.toString(reminderArrayList.size()));
            //Log.d(TAG, "Geofence size : "+Integer.toString(geofencingRequest.getGeofences().size()));
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // TODO
        Log.d(TAG, "connection suspended!");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // TODO
        Log.d(TAG, "connection failed!");
    }

    public void addReminder(View view){
        Intent intent = new Intent(this, ReminderAddActivity.class);
        startActivity(intent);
    }

    public void createGeofences(double latitude, double longitude, String name, String place, String key) {
        // create a Geofence around the location
        Geofence geofence = new Geofence.Builder()
                .setRequestId(key)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setCircularRegion(latitude, longitude, 1000)
                .build();

        geofencesList.add(geofence);
    }

    public void createGeofencingRequest(List<Geofence> geofencesList){
        geofencingRequest = new GeofencingRequest.Builder()
                .addGeofences(geofencesList)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();

        // Create an Intent pointing to the IntentService
        Intent intent = new Intent(this,
                ReceiveGeoFenceTransitionService.class);

        PendingIntent pi = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, geofencingRequest, pi);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class ReminderListAdapter extends ArrayAdapter<ReminderModel> {
        ArrayList<ReminderModel> reminderArrayList;
        public ReminderListAdapter(Context context, ArrayList<ReminderModel> reminderArrayList) {
            super(context, 0, reminderArrayList);
            this.reminderArrayList = reminderArrayList;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {

            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.adapter_activity_main_listview, parent, false);
            }
            TextView name = (TextView) view.findViewById(R.id.adapter_activity_main_listview_name);
            TextView place = (TextView) view.findViewById(R.id.adapter_activity_main_listview_place);

            name.setText(reminderArrayList.get(position).getName());
            place.setText(reminderArrayList.get(position).getPlace());

            return view;
        }
    }
}
