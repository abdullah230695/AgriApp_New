package com.shivaconsulting.agriapp.Home;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.shivaconsulting.agriapp.Adapter.AreaAdapter;
import com.shivaconsulting.agriapp.Adapter.PlacesAutoCompleteAdapter;
import com.shivaconsulting.agriapp.Adapter.TimeAdapterNew;
import com.shivaconsulting.agriapp.History.BookingHistoryActivity;
import com.shivaconsulting.agriapp.Models.TimeAmPm;
import com.shivaconsulting.agriapp.Profile.LoginActivity;
import com.shivaconsulting.agriapp.Profile.ProfileActivity;
import com.shivaconsulting.agriapp.R;
import com.vivekkaushik.datepicker.DatePickerTimeline;
import com.vivekkaushik.datepicker.OnDateSelectedListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener,
        AreaAdapter.OnAreaItemSelectedListener, PlacesAutoCompleteAdapter.ClickListener {

    //Const
    private static final String TAG = "MapsActivity";
    private Context mContext = MapsActivity.this;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final int LOCATION_SETTINGS_REQUEST = 4548;
    private static final float DEFAULT_ZOOM = 19f;

    //Vars
    private GoogleMap mMap;
    private Boolean mLocationPermissionsGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationManager locationManager;
    private LatLng mCenterLatLong;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String selectedDate;
    private AreaAdapter areaAdapter;
    private List<Integer> areaList;
    private PlacesAutoCompleteAdapter mAutoCompleteAdapter;



    //Id's
    private ImageView home, booking_history, profile;
    private Button booking_button;
    private ImageView gps_button;
    private ConstraintLayout bookContraint;
    private CardView cardView1, cardView2, cardView3;
    public static TextView combine_text, pick_time_text, pick_date_text, pick_area_text, tot_Type, belt_Type;
    public static RecyclerView time_picker_recyclerview, area_picker_recyclerview, map_search_recyler;
    private ImageView tot_image_1, tot_image_2, belt_image_1, belt_image_2, belt_image_3,
            combine_image_3, combine_image_2, combine_image_1;
    private EditText autoCompleteTextView;
    public static DatePickerTimeline datePickerTimeline;
    private AreaAdapter.OnAreaItemSelectedListener areaItemSelectedListener;
    private ProgressBar progressBar;
    String phone,custName;
    public static String time;
    public static String area;
    String address;
    String ServiceType;
    String ServiceID;
    double lat, lon;
    Random rnd = new Random(); //To generate random booking id
    final Long ID = (long) rnd.nextInt(99999999); //To generate random booking id
    String UUID = FirebaseAuth.getInstance().getUid();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference dr = db.collection("Users").document(UUID);
    final Map<String, Object> post = new HashMap<>();
    private static final String myTAG="FCM check";

    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        enableData();

        setupID();

        dr.addSnapshotListener(new EventListener<DocumentSnapshot>() {

            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if (error != null) {
                    Log.d(TAG, error.getMessage());
                    return;
                }
                if (value != null && value.exists()) {
                    phone = value.getData().get("phone_number").toString();
                    custName=value.getData().get("user_name").toString();
                }
            }
        });


        Places.initialize(this, getResources().getString(R.string.api_key));

        String apiKey = getString(R.string.api_key);

        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

// Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        autoCompleteTextView.addTextChangedListener(filterTextWatcher);

        mAutoCompleteAdapter = new PlacesAutoCompleteAdapter(this);
        map_search_recyler.setLayoutManager(new LinearLayoutManager(this));
        mAutoCompleteAdapter.setClickListener(this);
        map_search_recyler.setAdapter(mAutoCompleteAdapter);
        mAutoCompleteAdapter.notifyDataSetChanged();

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        datePickerTimeline.setInitialDate(year, month, day);


        datePickerTimeline.setDateTextColor(Color.RED);
        datePickerTimeline.setDayTextColor(Color.RED);
        datePickerTimeline.setMonthTextColor(Color.RED);


        home.setOnClickListener(this);
        booking_history.setOnClickListener(this);
        profile.setOnClickListener(this);

        home.setImageResource(R.drawable.ic_baseline_home);

        setupFirebaseAuth();


        getLocationPermission();

        this.setFinishOnTouchOutside(true);
        locationManager = (LocationManager) MapsActivity.this.getSystemService(Context.LOCATION_SERVICE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        gps_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Clicked when gps is turned off");
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Toast.makeText(mContext, "Please Enable GPS First", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onClick: Gps not enabled");
                    enableLoc();
                    //TODO:NEED TO IMPLEMENT LIKE SWIGGY ONCE GPS TURNED ON

                } else {
                    Log.d(TAG, "onClick: Clicked after Gps Is On");
                    getDeviceLocation();
                }


            }
        });
        tot_Type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Toast toast = Toast.makeText(mContext, "Selected service Type is TOT", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();*/

                ServiceType = "TOT Type";
                ServiceID = "TOT";
                cardView1.setVisibility(View.GONE);
                cardView2.setVisibility(View.GONE);
                cardView3.setVisibility(View.GONE);
                tot_image_1.setVisibility(View.GONE);
                tot_image_2.setVisibility(View.GONE);
                belt_image_1.setVisibility(View.GONE);
                belt_image_2.setVisibility(View.GONE);
                belt_image_3.setVisibility(View.GONE);

                bookContraint.setVisibility(View.VISIBLE);

                datePickerTimeline.setVisibility(View.VISIBLE);
                area_picker_recyclerview.setVisibility(View.INVISIBLE);
                time_picker_recyclerview.setVisibility(View.INVISIBLE);
                pick_time_text.setTextSize(14);
                pick_date_text.setTextSize(18);
                pick_area_text.setTextSize(14);
            }
        });

        belt_Type.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               /* Toast toast = Toast.makeText(mContext, "Selected service Type is Belt", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();*/

                ServiceType = "Belt Type";
                ServiceID = "BLT";
                cardView1.setVisibility(View.GONE);
                cardView2.setVisibility(View.GONE);
                cardView3.setVisibility(View.GONE);
                tot_image_1.setVisibility(View.GONE);
                tot_image_2.setVisibility(View.GONE);
                belt_image_1.setVisibility(View.GONE);
                belt_image_2.setVisibility(View.GONE);
                belt_image_3.setVisibility(View.GONE);

                bookContraint.setVisibility(View.VISIBLE);

                datePickerTimeline.setVisibility(View.VISIBLE);
                area_picker_recyclerview.setVisibility(View.INVISIBLE);
                time_picker_recyclerview.setVisibility(View.INVISIBLE);
                pick_time_text.setTextSize(14);
                pick_date_text.setTextSize(18);
                pick_area_text.setTextSize(14);
            }
        });


        combine_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Clicked");
               /* Toast toast = Toast.makeText(mContext, "Selected service Type is Combined", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();*/
                ServiceType = "Combined Type";
                ServiceID = "CMB";
                cardView1.setVisibility(View.GONE);
                cardView2.setVisibility(View.GONE);
                cardView3.setVisibility(View.GONE);
                tot_image_1.setVisibility(View.GONE);
                tot_image_2.setVisibility(View.GONE);
                belt_image_1.setVisibility(View.GONE);
                belt_image_2.setVisibility(View.GONE);
                belt_image_3.setVisibility(View.GONE);

                bookContraint.setVisibility(View.VISIBLE);

                datePickerTimeline.setVisibility(View.VISIBLE);
                area_picker_recyclerview.setVisibility(View.INVISIBLE);
                time_picker_recyclerview.setVisibility(View.INVISIBLE);
                pick_time_text.setTextSize(14);
                pick_date_text.setTextSize(18);
                pick_area_text.setTextSize(14);
            }
        });


        datePickerTimeline.setOnDateSelectedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(int year, int month, int day, int dayOfWeek) {
                month = month + 1;
                selectedDate = day + "/" + month + "/" + year;
                Log.d(TAG, "onDateSelected: date: " + year + month + day);
                Log.d(TAG, "onDateSelected: SelectedDate reform: " + selectedDate);

                datePickerTimeline.setVisibility(View.INVISIBLE);
                area_picker_recyclerview.setVisibility(View.INVISIBLE);
                time_picker_recyclerview.setVisibility(View.VISIBLE);
                pick_time_text.setTextSize(18);
                pick_date_text.setTextSize(14);
                pick_area_text.setTextSize(14);
            }

            @Override
            public void onDisabledDateSelected(int year, int month, int day, int dayOfWeek, boolean isDisabled) {

            }
        });
         Log.d(TAG, "onClick: Booked Date :" + selectedDate);

        final ArrayList<TimeAmPm> ArList = new ArrayList<>();
        ArList.add(new TimeAmPm("6", "AM"));
        ArList.add(new TimeAmPm("7", "AM"));
        ArList.add(new TimeAmPm("8", "AM"));
        ArList.add(new TimeAmPm("9", "AM"));
        ArList.add(new TimeAmPm("10", "AM"));
        ArList.add(new TimeAmPm("11", "AM"));
        ArList.add(new TimeAmPm("12", "PM"));
        ArList.add(new TimeAmPm("1", "PM"));
        ArList.add(new TimeAmPm("2", "PM"));
        ArList.add(new TimeAmPm("3", "PM"));
        ArList.add(new TimeAmPm("4", "PM"));
        ArList.add(new TimeAmPm("5", "PM"));
        ArList.add(new TimeAmPm("6", "PM"));
        ArList.add(new TimeAmPm("7", "PM"));
        ArList.add(new TimeAmPm("8", "PM"));

        TimeAdapterNew adapter = new TimeAdapterNew(ArList, getApplicationContext());
        time_picker_recyclerview.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        time_picker_recyclerview.setLayoutManager(linearLayoutManager);




        //Booking Event
        booking_button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {

                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) |
                        selectedDate == null | time == null | area == null |
                        autoCompleteTextView.length() == 0) {

                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Toast.makeText(mContext, "Please Enable GPS First", Toast.LENGTH_SHORT).show();
                        try {
                            enableLoc();
                        } catch (Exception e) {
                           Log.d(TAG,e.getMessage());
                        }
                    } else if (selectedDate == null | time == null | area == null) {
                        Toast.makeText(mContext, "Please select Date,Time,Area", Toast.LENGTH_SHORT).show();
                        try {
                            getDeviceLocation();
                        } catch (Exception e) {
                            Toast.makeText(mContext, "Unable to get device location", Toast.LENGTH_SHORT).show();
                        }
                    } else if (autoCompleteTextView.length() == 0) {
                        Toast.makeText(mContext, "Please check your delivery address above", Toast.LENGTH_SHORT).show();
                        try {
                            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                Toast.makeText(mContext, "Please Enable GPS First", Toast.LENGTH_SHORT).show();
                                enableLoc();

                            } else {
                                getDeviceLocation();
                                getAddress();

                            }
                        } catch (Exception e) {

                        }
                    }
                } else {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Confirm Booking");
                    builder.setMessage("Selected service type is " + ServiceType + ". Do you want to proceed ?");
                    builder.setCancelable(false);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //Checking Booking id Existance
                            db.collection("All Booking ID").document(ServiceID+ID).get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if(documentSnapshot.exists()){
                                                Log.d(TAG,"Booking ID Already Exist");
                                                try {
                                                    InsertData1();
                                                    sendNotification();
                                                }catch(Exception e) {
                                                    Log.d(TAG,e.getMessage());
                                                }
                                            } else {
                                                Log.d(TAG,"This is new Booking ID");
                                                try {
                                                    InsertData2();
                                                    sendNotification();
                                                }catch(Exception e) {
                                                    Log.d(TAG,e.getMessage());
                                                }
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG,e.getMessage());
                                }
                            });
                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(mContext, "Booking Cancelled", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        }
                    }).setIcon(R.drawable.ic_baseline_commute_24);
                    //Creating dialog box
                    AlertDialog alert = builder.create();
                    //Setting the title manually
                    alert.show();

                }
            }


        });


        pick_time_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pick_time_text.setTextSize(18);
                pick_date_text.setTextSize(14);
                pick_area_text.setTextSize(14);
                datePickerTimeline.setVisibility(View.INVISIBLE);
                area_picker_recyclerview.setVisibility(View.INVISIBLE);
                time_picker_recyclerview.setVisibility(View.VISIBLE);
            }
        });


        pick_date_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pick_time_text.setTextSize(14);
                pick_area_text.setTextSize(14);
                pick_date_text.setTextSize(18);
                datePickerTimeline.setVisibility(View.VISIBLE);
                time_picker_recyclerview.setVisibility(View.INVISIBLE);
                area_picker_recyclerview.setVisibility(View.INVISIBLE);
            }
        });


        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        area_picker_recyclerview.setLayoutManager(linearLayoutManager1);
        areaList = new ArrayList<>();
        areaAdapter = new AreaAdapter(areaList, mContext, this);
        area_picker_recyclerview.setAdapter(areaAdapter);
        areaList.add(1);
        areaList.add(2);
        areaList.add(3);
        areaList.add(4);
        areaList.add(5);
        areaList.add(6);
        areaList.add(7);
        areaList.add(8);
        pick_area_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pick_time_text.setTextSize(14);
                pick_date_text.setTextSize(14);
                pick_area_text.setTextSize(18);
                datePickerTimeline.setVisibility(View.INVISIBLE);
                time_picker_recyclerview.setVisibility(View.INVISIBLE);
                area_picker_recyclerview.setVisibility(View.VISIBLE);
            }
        });


    }



   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100 && resultCode=RESULT_OK) {
            //if success
            //Initialize place
                Place place=Autocomplete.getPlaceFromIntent(data);
                //set address in autoCompleteView
            autoCompleteTextView.setText(place.getAddress());
            //Get Locality name String locality=place.getName;
            //Get LatLng id double LatLng=place.getLatLng;
        } else if (resultCode==AutocompleteActivity.RESULT_ERROR) {
           Status status=Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getApplicationContext(),status.getStatusMessage(),Toast.LENGTH_SHORT).show();
        }
    }*/


    private void moveCamera(LatLng latLng, float zoom, String tittle) {
        Log.d(TAG, "location: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(tittle);
        mMap.addMarker(options);
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setTiltGesturesEnabled(true);


            final Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    mCenterLatLong = mMap.getCameraPosition().target;
                    mMap.clear();

                    try {

                        Location mLocation = new Location("");
                        mLocation.setLatitude(mCenterLatLong.latitude);
                        mLocation.setLongitude(mCenterLatLong.longitude);

//                        mLocationMarkerText.setText("Lat : " + mCenterLatLong.latitude + "," + "Long : " + mCenterLatLong.longitude);


                        List<Address> myAddress = geocoder.getFromLocation(mCenterLatLong.latitude, mCenterLatLong.longitude, 1);
                        String address = myAddress.get(0).getAddressLine(0);
                        String city = myAddress.get(0).getSubLocality();
//                       mLocationCity.setText(city);
//                        mLocationAddress.setText(address);
//
//                        ConfirmLocation(city,address,mCenterLatLong.latitude,mCenterLatLong.longitude);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    bookContraint.setVisibility(View.GONE);
                    cardView1.setVisibility(View.VISIBLE);
                    cardView2.setVisibility(View.VISIBLE);
                    cardView3.setVisibility(View.VISIBLE);
                }
            });
        }
    }
    private TextWatcher filterTextWatcher = new TextWatcher() {
        public void afterTextChanged(@NotNull Editable s) {
            if (!s.toString().equals("")) {
                mAutoCompleteAdapter.getFilter().filter(s.toString());
                if (map_search_recyler.getVisibility() == View.GONE) {
                    map_search_recyler.setVisibility(View.VISIBLE);
                }
            } else {
                if (map_search_recyler.getVisibility() == View.VISIBLE) {
                    map_search_recyler.setVisibility(View.GONE);
                }
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    };


    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }

    private void enableLoc() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        //builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {


            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        MapsActivity.this,
                                        LOCATION_SETTINGS_REQUEST);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });
    }


    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            try {
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                        DEFAULT_ZOOM, "My Location");
                            } catch (Exception e) {
                               Log.d(TAG,"OnCreate :"+e.getMessage());
                            }
                            try {
                                lat = currentLocation.getLatitude();
                                lon = currentLocation.getLongitude();
                            } catch (Exception e) {
                                Log.d(TAG, "error is " + e.getMessage());
                            }


                            /*tot_image_1.setVisibility(View.VISIBLE);
                            tot_image_2.setVisibility(View.VISIBLE);
                            combine_image_1.setVisibility(View.VISIBLE);
                            combine_image_2.setVisibility(View.VISIBLE);
                            combine_image_3.setVisibility(View.VISIBLE);
                            belt_image_1.setVisibility(View.VISIBLE);
                            belt_image_2.setVisibility(View.VISIBLE);
                            belt_image_3.setVisibility(View.VISIBLE);*/

                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }


    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }


        /*
    ---------------------------------------BottomNavBar-------------------------------------------------
     */

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.booking_history:
                Intent intent = new Intent(mContext, BookingHistoryActivity.class);
                startActivity(intent);
                break;

            case R.id.profile:
                Intent intent1 = new Intent(mContext, ProfileActivity.class);
                startActivity(intent1);
                break;
        }

    }


      /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * checks to see if the @param 'user' is logged in
     *
     * @param user
     */
    private void checkCurrentUser(FirebaseUser user) {
        Log.d(TAG, "checkCurrentUser: checking if user is logged in.");

        if (user == null) {
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Setup the firebase auth object
     */


    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if the user is logged in
                checkCurrentUser(user);

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        checkCurrentUser(mAuth.getCurrentUser());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    //Alert Dialogue Box on Exit

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builderExit = new AlertDialog.Builder(mContext);
        builderExit.setTitle("Exit ?");
        builderExit.setMessage("Do you want to exit ?");
        builderExit.setCancelable(false);

        builderExit.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();
            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).setIcon(R.drawable.ic_baseline_commute_24).show();
    }





    @Override
    public void click(Place place) {
        moveCamera(new LatLng(place.getLatLng().latitude, place.getLatLng().longitude), DEFAULT_ZOOM, "Selected Location");
    }

    //ID setups
    public void setupID() {
        home = findViewById(R.id.home);
        booking_history = findViewById(R.id.booking_history);
        profile = findViewById(R.id.profile);
        gps_button = findViewById(R.id.gps_button);
        booking_button = findViewById(R.id.book_button);
        datePickerTimeline = findViewById(R.id.datePickerTimeline);
        bookContraint = findViewById(R.id.booking_constraint);
        cardView1 = findViewById(R.id.tot_type_cardview);
        cardView2 = findViewById(R.id.belt_type_cardview);
        cardView3 = findViewById(R.id.combine_type_cardview);
        combine_text = findViewById(R.id.combine_text);
        pick_time_text = findViewById(R.id.pick_time_text);
        time_picker_recyclerview = findViewById(R.id.time_picker_recyclerview);
        area_picker_recyclerview = findViewById(R.id.area_picker_recyclerview);
        pick_date_text = findViewById(R.id.pick_date_text);
        pick_area_text = findViewById(R.id.pick_area_text);
        combine_image_1 = findViewById(R.id.combine_image_1);
        combine_image_2 = findViewById(R.id.combine_image_2);
        combine_image_3 = findViewById(R.id.combine_image_3);
        tot_image_1 = findViewById(R.id.tot_image_1);
        tot_image_2 = findViewById(R.id.tot_image_2);
        belt_image_1 = findViewById(R.id.belt_image_1);
        belt_image_2 = findViewById(R.id.belt_image_2);
        belt_image_3 = findViewById(R.id.belt_image_3);
        map_search_recyler = findViewById(R.id.map_search_recyler);
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        tot_Type = findViewById(R.id.totType);
        belt_Type = findViewById(R.id.beltType);
        progressBar = findViewById(R.id.pb1);
    }//ID setups


    private void getAddress() {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        try {

            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            address =addresses.get(0).getAddressLine(0);
            autoCompleteTextView.setText(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Adding Booking details to firestore if Booking id exist
    private void InsertData1() {

        Random r = new Random();
        Long id = (long) r.nextInt(999999999);

        cardView1.setVisibility(View.VISIBLE);
        cardView2.setVisibility(View.VISIBLE);
        cardView3.setVisibility(View.VISIBLE);
        bookContraint.setVisibility(View.GONE);
        String UUID1 = FirebaseAuth.getInstance().getUid();

        post.put("Booking_Date", new Timestamp(new Date()));
        post.put("Delivery_Date", selectedDate);
        post.put("Booking_Id", ServiceID + id);
        post.put("Contact_Number", phone);
        post.put("Customer_Name", custName);
        post.put("Delivery_Time", time);
        post.put("Area", area);
        post.put("Service_Type", ServiceType);
        post.put("Latitude", lat);
        post.put("Longitude", lon);
        post.put("Address", address);
        post.put("PicUrl", "https://i.pinimg.com/originals/c9/f5/fb/c9f5fba683ab296eb94c62de0b0e703c.png");
        post.put("Status", "Pending");
        post.put("Service_Provider", "Not Assigned");
        post.put("Unique_ID", UUID1);


        ProgeressDialog();
        progressDialog.show();

        // storing booking id in seperate place for checking id redundancy
        Map<String, Object> ids = new HashMap<>();
        ids.put("ID",ServiceID+id);
        db.collection("All Booking ID").document(ServiceID+id).set(post)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG,"New Booking id added");
                    }
                }); // storing booking id in seperate place for checking id redundancy


        db.collection("Bookings").document(UUID1).collection("Booking Details")
                .document(ServiceID + id)
                .set(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
               progressDialog.dismiss();
                Toast.makeText(MapsActivity.this, "Service Booked, Please check history tab for vehicle confirmation", Toast.LENGTH_SHORT).show();
                selectedDate = null;
                time = null;
                area = null;
                autoCompleteTextView.setText(null);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(MapsActivity.this, "Failed to book!", Toast.LENGTH_SHORT).show();
            }
        });
    } //Adding Booking details to firestore if Bookig id exist


    //Adding Booking details to firestore
    private void InsertData2() {

        Toast.makeText(MapsActivity.this, "Processing", Toast.LENGTH_SHORT).show();
        cardView1.setVisibility(View.VISIBLE);
        cardView2.setVisibility(View.VISIBLE);
        cardView3.setVisibility(View.VISIBLE);
        bookContraint.setVisibility(View.GONE);
        String UUID1 = FirebaseAuth.getInstance().getUid();

        post.put("Booking_Date", new Timestamp(new Date()));
        post.put("Delivery_Date", selectedDate);
        post.put("Booking_Id", ServiceID + ID);
        post.put("Contact_Number", phone);
        post.put("Delivery_Time", time);
        post.put("Area", area);
        post.put("Service_Type", ServiceType);
        post.put("Customer_Name", custName);
        post.put("Latitude", lat);
        post.put("Longitude", lon);
        post.put("PicUrl", "https://i.pinimg.com/originals/c9/f5/fb/c9f5fba683ab296eb94c62de0b0e703c.png");
        post.put("Status", "Pending");
        post.put("Service_Provider", "Not Assigned");
        post.put("Unique_ID", UUID1);

        ProgeressDialog();
        progressDialog.show();

// storing booking id in seperate place for checking id redundancy
        Map<String, Object> ids = new HashMap<>();
        ids.put("ID",ServiceID+ID);
        db.collection("All Booking ID").document(ServiceID+ID).set(post)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG,"New Booking id added");

                    }
                }); // storing booking id in separate place for checking id redundancy

        db.collection("Bookings").document(UUID1).collection("Booking Details")
                .document(ServiceID + ID)
                .set(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                Toast.makeText(MapsActivity.this, "Service has been 'Booked' , Please check history tab for vehicle confirmation", Toast.LENGTH_SHORT).show();
                Log.d(TAG,"Service Booked, Please check history tab for vehicle confirmation");
                selectedDate = null;
                time = null;
                area = null;
                autoCompleteTextView.setText(null);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(MapsActivity.this, "Failed to book!", Toast.LENGTH_SHORT).show();
                Log.d(TAG,"Failed to book!");
            }
        });
    } //Adding Booking details to firestore

    //Prompting user to enable data connection
    public boolean isOnline() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo datac = cm
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        android.net.NetworkInfo wifi = cm
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

if((wifi != null & cm != null)
        && (wifi.isConnected()| datac.isConnected())){
            return true;
        } else {
            return false;

        }

    }   //Prompting user to enable data connection
    public void enableData() {
        final AlertDialog.Builder builderExit = new AlertDialog.Builder(mContext);

        if(!isOnline()==true){
            LayoutInflater factory = LayoutInflater.from(MapsActivity.this);
            final View view = factory.inflate(R.layout.image_for_dialog, null);
            builderExit.setTitle("No Data Connection Available");
            builderExit.setMessage("Please Enable Internet or Wifi Connection To Continue.");
            builderExit.setCancelable(false);
            builderExit.setView(view);
            builderExit.setIcon(R.drawable.no_wifi_foreground);
            builderExit.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
        finishAffinity();
                }
            });
            builderExit.show();
        }

    }

       private void ProgeressDialog(){
           progressDialog=new ProgressDialog(mContext);
           progressDialog.setCancelable(false);
           progressDialog.setMessage("Processing please wait");

       }

    @Override
    public void OnSelectedAreaListener(Integer area_number) {
                    //Empty Listener
    }

    //Sending notification after a booking has made
    private void sendNotification() {

        Intent intent = new Intent(this, BookingHistoryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        String channelId = "Default";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new  NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Your Booking Has Been Received")
                .setContentText("Please check booking history for vehicle confirmation")
                .setSound(defaultSoundUri).setAutoCancel(true).setContentIntent(pendingIntent);;
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        manager.notify(0, builder.build());
    }  //Sending notification after a booking has made
}







