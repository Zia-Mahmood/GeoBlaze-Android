package com.zarkhub.myapplication;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private Boolean started = false;
    private double lat, lng, d1=999999999f;
    private EditText input_search;
    private LatLng home,dest;
    private Myobject[] objects;
    private int objects_length;
    private int iterator;
    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice esp;
    BluetoothSocket espSocket;
    boolean turn_left=false,turn_right=false,horn=false,light=false,turn = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        System.out.println("working fine");
        input_search = findViewById(R.id.input_search);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            Location mylocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            lat = mylocation.getLatitude();
            lng = mylocation.getLongitude();
        } catch (SecurityException | IllegalArgumentException e) {
            lat = 17.340564;
            lng = 78.459693;
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, this);
        init();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        esp = bluetoothAdapter.getRemoteDevice("00:00:00:00:00:02");

        espSocket = null;
        int counter = 0;
        do { try {
            espSocket = esp.createRfcommSocketToServiceRecord(mUUID);
            System.out.println(espSocket);
            espSocket.connect();
            System.out.println(espSocket.isConnected());

        }catch (SecurityException | IOException e){
            e.printStackTrace();
        } counter++; } while(!(espSocket != null && espSocket.isConnected()) && counter<10);

    }



    private void init(){
        started = false;
        input_search.setOnEditorActionListener((textView, actionid, keyEvent) -> {
            if(actionid == EditorInfo.IME_ACTION_SEARCH
                    || actionid == EditorInfo.IME_ACTION_DONE
                    || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                    || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){
                geoLocate();
            }
            return false;
        });
    }

    private void geoLocate(){
        started = false;
        String searchString = input_search.getText().toString();

        Geocoder geocoder1 = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder1.getFromLocationName(searchString,1);
        }catch ( IOException e){
            e.printStackTrace();
        }
        if(list.size()>0){
            mMap.clear();
            Address address = list.get(0);
            dest = new LatLng(address.getLatitude(),address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(dest).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dest,18f));
        }
    }

    public void onGPS(View v){
        started = false;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(home,18f));
    }

    public void getDirections(View v){
        started = false;
        String searchString = input_search.getText().toString();

        if(searchString.isEmpty()){
            Toast.makeText(this, "Enter a Place or City in Search Box", Toast.LENGTH_SHORT).show();
        }
        else{
            Geocoder geocoder1 = new Geocoder(MapsActivity.this);
            List<Address> list = new ArrayList<>();
            try {
                list = geocoder1.getFromLocationName(searchString,1);
            }catch ( IOException e){
                e.printStackTrace();
            }
            if(list.size()>0){
                mMap.clear();
                Address address = list.get(0);
                dest = new LatLng(address.getLatitude(),address.getLongitude());
                LatLngBounds bounds = new LatLngBounds.Builder().include(home).include(dest).build();
                mMap.addMarker(new MarkerOptions().position(dest).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,120));
                String url = getRequestUrl(home,dest);
                TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                taskRequestDirections.execute(url);
            }
        }

    }

    public void Start(View v){
        String searchString = input_search.getText().toString();

        if(searchString.isEmpty()){
            Toast.makeText(this, "Enter a Place or City in Search Box", Toast.LENGTH_SHORT).show();
        }
        else{
            Geocoder geocoder1 = new Geocoder(MapsActivity.this);
            List<Address> list = new ArrayList<>();
            try {
                list = geocoder1.getFromLocationName(searchString,1);
            }catch ( IOException e){
                e.printStackTrace();
            }
            if(list.size()>0){

                String url = getRequestUrl(home,dest);
                iterator = 0;
                mMap.clear();
                Address address = list.get(0);
                dest = new LatLng(address.getLatitude(),address.getLongitude());
                TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                taskRequestDirections.execute(url);
                UploadRequestDirections uploadRequestDirections = new UploadRequestDirections();
                uploadRequestDirections.execute(url);
                mMap.addMarker(new MarkerOptions().position(dest).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(home,20f));
                started = true;
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        started = false;
        mMap = googleMap;


        //mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);



        // Add a marker in Sydney and move the camera
        home = new LatLng(lat, lng);
        //mMap.addMarker(new MarkerOptions().position(home).title("Marker at home"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(home));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(home,18f));

        /*mMap.setOnMapLongClickListener(latLng -> {
            if(latLngArrayList.size() == 2){
                latLngArrayList.clear();
                mMap.clear();
            }

            latLngArrayList.add(latLng);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            if(latLngArrayList.size()==1){
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            }
            else {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }
            mMap.addMarker(markerOptions);
            if(latLngArrayList.size()==2){
                String url = getRequestUrl(latLngArrayList.get(0),latLngArrayList.get(1));
                TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                taskRequestDirections.execute(url);
            }
        });*/
    }

    public String getRequestUrl(LatLng from,LatLng to){
        String str_from = "origin=" + from.latitude + "," + from.longitude;
        String str_to = "destination=" + to.latitude + "," + to.longitude;
        String sensor = "sensor=false";
        String mode = "mode=bike";
        String avoid = "avoid=tolls";
        String params = str_from+"&"+str_to+"&"+sensor+"&"+avoid+"&"+mode+"&key=AIzaSyDP3fJIO-c-Gwtlc2OLtLIZGLD0OjZI3mI";
        System.out.println("https://maps.googleapis.com/maps/api/directions/json?"+params);
        return "https://maps.googleapis.com/maps/api/directions/json?"+params;
    }

    private String requestDirection(String reqUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line=bufferedReader.readLine())!=null){
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            if(inputStream != null){

                inputStream.close();

            }
            assert httpURLConnection != null;
            httpURLConnection.disconnect();
        }

        return  responseString;
    }


    public void taketurn(String inst){
        if(inst.contains("left")) {
            OutputStream outputStream;
            try {
                outputStream = espSocket.getOutputStream();
                outputStream.write(48);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(inst.contains("right")) {
            OutputStream outputStream;
            try {
                outputStream = espSocket.getOutputStream();
                outputStream.write(50);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopTurn(String inst){
        if(inst.contains("left")) {
            OutputStream outputStream;
            try {
                outputStream = espSocket.getOutputStream();
                outputStream.write(49);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(inst.contains("right")) {
            OutputStream outputStream;
            try {
                outputStream = espSocket.getOutputStream();
                outputStream.write(51);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean check(String inst){
        return inst.contains("right") || inst.contains("left");
    }
    public void onLocationChanged(@NonNull Location location) {
        home = new LatLng(location.getLatitude(),location.getLongitude());
        if(started)
        {

            if(iterator<objects_length && check(objects[iterator].maneuver)){
                Location l = new Location("");
                l.setLatitude(objects[iterator].str_lat);
                l.setLongitude(objects[iterator].str_long);
                double dist = location.distanceTo(l);
                double speed = 0f;
                if(location.hasSpeed()){
                    speed = location.getSpeed()*3.6f;
                }
                if(speed>=80 && dist<=300 ) {
                    turn = true;
                    if(d1<dist){
                        turn = false;
                    }
                    if(turn)
                    { taketurn(objects[iterator].maneuver); }
                    else{
                        stopTurn(objects[iterator].maneuver);
                        iterator++;
                    }
                    turn = false;
                    Toast.makeText(this, objects[iterator].maneuver, Toast.LENGTH_SHORT).show();
                }
                else if(speed>=40 && speed<80 && dist<=135){
                    turn = true;
                    if(d1<dist){
                        turn = false;
                    }
                    if(turn)
                    { taketurn(objects[iterator].maneuver); }
                    else{
                        stopTurn(objects[iterator].maneuver);
                        iterator++;
                    }
                    turn = false;
                    Toast.makeText(this, objects[iterator].maneuver, Toast.LENGTH_SHORT).show();
                }
                else if(speed>=20 && speed<40 && dist<=70){
                    turn = true;
                    if(d1<dist){
                        turn = false;
                    }
                    if(turn)
                    { taketurn(objects[iterator].maneuver); }
                    else{
                        stopTurn(objects[iterator].maneuver);
                        iterator++;
                    }
                    turn = false;
                    Toast.makeText(this, objects[iterator].maneuver, Toast.LENGTH_SHORT).show();
                } else if (speed>=5 && speed<20 && dist<=30) {
                    turn = true;
                    if(d1<dist){
                        turn = false;
                    }
                    if(turn)
                    { taketurn(objects[iterator].maneuver); }
                    else{
                        stopTurn(objects[iterator].maneuver);
                        iterator++;
                    }
                    turn = false;
                    Toast.makeText(this, objects[iterator].maneuver, Toast.LENGTH_SHORT).show();
                } else if (speed<5 && dist<=30) {
                    turn = true;
                    if(d1<dist){
                        turn = false;
                    }
                    if(turn)
                    { taketurn(objects[iterator].maneuver); }
                    else{
                        stopTurn(objects[iterator].maneuver);
                        iterator++;
                    }
                    turn = false;
                    Toast.makeText(this, objects[iterator].maneuver, Toast.LENGTH_SHORT).show();
                }
                //Toast.makeText(this, d1+" "+dist, Toast.LENGTH_SHORT).show();
                d1 = dist;
            }else{
                d1 = 999999999;
                iterator++;
            }
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(dest).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(home,20f));
            String url = getRequestUrl(home,dest);
            TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
            taskRequestDirections.execute(url);
            started = true;
        }
        //mMap.addMarker(new MarkerOptions().position(home).title("Current Location"));
        //mMap.clear();
        //mMap.addCircle(new CircleOptions().center(home).radius(10).fillColor(R.color.map_transparent_blue));
        }

    public class UploadRequestDirections extends AsyncTask<String,Void, String> {

        protected String doInBackground(String... strings){
            String responseString = "";
            try{
                responseString = requestDirection(strings[0]);
            }catch (IOException e){
                e.printStackTrace();
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            UploadParser uploadParser = new UploadParser();
            uploadParser.execute(s);

        }
    }
    public class UploadParser extends AsyncTask<String,Void,JSONObject>{

        protected JSONObject doInBackground(String... strings) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(strings[0]);
            }catch (JSONException e){
                e.printStackTrace();
            }
            return jsonObject;
        }

        protected void onPostExecute(JSONObject data){
            try {
                JSONArray steps = data.getJSONArray("routes").getJSONObject(0)
                        .getJSONArray("legs").getJSONObject(0)
                        .getJSONArray("steps");

                // Print out the directions
                System.out.println(steps.length());
                objects = new Myobject[steps.length()];
                objects_length = steps.length();
                for (int i = 0; i < steps.length(); i++) {
                    JSONObject obj = steps.getJSONObject(i);
                    if (!obj.has("distance")) {
                        if (!obj.has("maneuver")) {
                            objects[i] = new Myobject(obj.getJSONObject("duration").getString("text"),
                                    obj.getString("html_instructions").replaceAll("<.*?>", " ")
                                            .replaceAll("\\s+", " ").trim());
                        } else {
                            objects[i] = new Myobject(obj.getJSONObject("duration").getString("text"),
                                    obj.getString("maneuver"),
                                    obj.getString("html_instructions").replaceAll("<.*?>", " ")
                                            .replaceAll("\\s+", " ").trim(),
                                    0);
                        }
                    } else {
                        if (!obj.has("maneuver")) {
                            objects[i] = new Myobject(obj.getJSONObject("duration").getString("text"),
                                    obj.getJSONObject("distance").getString("text"),
                                    obj.getString("html_instructions").replaceAll("<.*?>", " ")
                                            .replaceAll("\\s+", " ").trim());
                        } else {
                            objects[i] = new Myobject(obj.getJSONObject("duration").getString("text"),
                                    obj.getJSONObject("distance").getString("text"), obj.getString("maneuver"),
                                    obj.getString("html_instructions").replaceAll("<.*?>", " ")
                                            .replaceAll("\\s+", " ").trim());
                        }
                    }
                    objects[i].setStartingPoint(obj.getJSONObject("start_location").getDouble("lat"),
                            obj.getJSONObject("start_location").getDouble("lng"));
                    objects[i].setEndPoint(obj.getJSONObject("end_location").getDouble("lat"),
                            obj.getJSONObject("end_location").getDouble("lng"));
                    objects[i].printDetailsofObject();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
    @SuppressLint("StaticFieldLeak")
    public class TaskRequestDirections extends AsyncTask<String,Void, String> {

        protected String doInBackground(String... strings){
            String responseString = "";
            try{
                responseString = requestDirection(strings[0]);
            }catch (IOException e){
                e.printStackTrace();
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);

        }
    }



    @SuppressLint("StaticFieldLeak")
    public class TaskParser extends AsyncTask<String,Void,List<List<HashMap<String, String>>>>{

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
            }catch (JSONException e){
                e.printStackTrace();
            }
            return routes;
        }

        protected void onPostExecute(List<List<HashMap<String, String>>> lists){
            ArrayList<LatLng> points;
            PolylineOptions polylineOptions = null;
            System.out.println(home.latitude+" "+home.longitude);
            for(List<HashMap<String, String>> path: lists){
                points = new ArrayList<>();
                polylineOptions = new PolylineOptions();

                for(HashMap<String, String> point :path){
                    double lat2 = Double.parseDouble(Objects.requireNonNull(point.get("lat")));
                    double lng2 = Double.parseDouble(Objects.requireNonNull(point.get("lon")));

                    points.add(new LatLng(lat2,lng2));

                }
                System.out.println(points.size());
                polylineOptions.addAll(points);
                polylineOptions.width(15);
                polylineOptions.color(Color.rgb(61,147,217));
                polylineOptions.geodesic(true);
            }
            if(polylineOptions != null){
                mMap.addPolyline(polylineOptions);
            }
            else {
                Toast.makeText(MapsActivity.this, "Directions not found", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
