package id.web.adhiwie.remindme;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.util.ArrayList;
import java.util.List;

public class ReceiveGeoFenceTransitionService extends IntentService {

    /** Initialization **/
    private List<Geofence> geofenceList = new ArrayList<>();
    private String name = null;
    private String place = null;
    private Double latitude = null;
    private Double longitude = null;

    public ReceiveGeoFenceTransitionService() {
        super("ReceiveGeoFenceTransitionService");
    }

    /**
     * Handles incoming intents
     *
     * @param intent
     *            The Intent sent by Location Services. This Intent is provided
     *            to Location Services (inside a PendingIntent) when you call
     *            addGeofences()
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if (event.hasError()) {
            // TODO: Handle error
        } else {
            int transition = event.getGeofenceTransition();

            if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {

                geofenceList = event.getTriggeringGeofences();

                for(Geofence geofence : geofenceList){
                    AudioManager aManager=(AudioManager)getSystemService(AUDIO_SERVICE);
                    aManager.setRingerMode(aManager.RINGER_MODE_SILENT);

                    try {
                        DB snappyDB = DBFactory.open(getApplicationContext(), "remindersDB"); //create or open an existing database using the default name

                        String [] keys = snappyDB.findKeys(geofence.getRequestId());
                        for(int i=0; i<keys.length; i++){
                            String[] array = snappyDB.getObjectArray(keys[i], String.class);
                            name = array[0];
                            place = array[1];
                            latitude = Double.parseDouble(array[2]);
                            longitude = Double.parseDouble(array[3]);
                        }

                        snappyDB.close();
                    } catch (SnappydbException e) {
                    }

                    // Send a notification, when clicked, open website
                    //String url = "http://www.theelectric.co.uk/programme.php";
                    Intent notificationIntent = new Intent(this, DirectionActivity.class);
                    notificationIntent.putExtra("latitude", latitude);
                    notificationIntent.putExtra("longitude", longitude);
                    notificationIntent.putExtra("place", place);
                    //notificationIntent.setData(Uri.parse(url));

                    PendingIntent contentIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, notificationIntent, 0);

                    Notification notification = new Notification.Builder(this.getApplicationContext())
                            .setContentTitle("Near "+place)
                            .setContentText(name)
                            .setContentIntent(contentIntent)
                            .setSmallIcon(R.drawable.stat_sys_gps_on)
                            .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_launcher))
                            .build();

                    Log.d(MainActivity.TAG, "Notification created");

                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.notify(1, notification);
                }

                Log.d(MainActivity.TAG, "Notified!");
            } else if(transition == Geofence.GEOFENCE_TRANSITION_EXIT){
                AudioManager aManager=(AudioManager)getSystemService(AUDIO_SERVICE);
                aManager.setRingerMode(aManager.RINGER_MODE_NORMAL);
            } else {
                // Handle invalid transition
            }
        }
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType A transition type constant defined in Geofence
     * @return A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);

            default:
                return getString(R.string.geofence_transition_unknown);
        }
    }
}
