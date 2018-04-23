package com.example.fazlulhoque.iiucbususer;

import android.*;
import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.fazlulhoque.iiucbususer.Common.Common;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserMap extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, android.location.LocationListener,GoogleMap.OnInfoWindowClickListener{

    private GoogleMap mMap;
    //GoogleApiClient mGoogleApiClient;

    //private LocationRequest mLocationRequest;
    //private Marker CurrentLocationMarker;
    public static final int REQUEST_LOCATION_CODE=99;
    private Location mLastLocation;
    private LocationManager mLocationManager;
    private Button mLogout,distance;
    private ToggleButton mRequest;
    private LatLng pickupLocation;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private static final int RP_ACCESS_LOCATION = 1;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    private static final long MIN_TIME_BW_UPDATES = 500;
    private Boolean isLoggingout = false;

    private List<LatLng> latLngs;

    HashMap<String, LatLng> hashMap;


    ArrayList<String>pokeDriverid;
    String driverIDforPoke="";

    String getName;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        latLngs = new ArrayList<>();
        hashMap = new HashMap<>();
        pokeDriverid=new ArrayList<>();

     //   userRef=FirebaseDatabase.getInstance().getReference().child("StudentUsers").child(CurrentUser);

        distance=(Button)findViewById(R.id.distance);
        mRequest = (ToggleButton) findViewById(R.id.request);
        mRequest.setText("Show Me");
        getDriversLocation();

        getName=getIntent().getExtras().getString("gender");

        distance.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent=new Intent(UserMap.this,MapDistance.class);
            Toast.makeText(UserMap.this, "go to distance ", Toast.LENGTH_SHORT).show();
            startActivity(intent);
            finish();
            return;

        }
        });

        mRequest.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    //show user
                    compoundButton.setChecked(b);
                    mRequest.setTextOn("Hide Me");
                    getLocation();
                }
                else {
                    //hide user
                    compoundButton.setChecked(b);
                    mRequest.setTextOff("Show Me");
                    if (ActivityCompat.checkSelfPermission(UserMap.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UserMap.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    else{
                        mLocationManager.removeUpdates(UserMap.this);
                    }
                    mLocationManager.removeUpdates(UserMap.this);
                    String  userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref= FirebaseDatabase.getInstance().getReference("studentRequest").child(userId);
                    ref.removeValue();
                }
            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navigationHeaderView =  navigationView.getHeaderView(0);
        final TextView userName = (TextView)navigationHeaderView.findViewById(R.id.userName);
        CircleImageView userImage = (CircleImageView)navigationHeaderView.findViewById(R.id.imageUser);

        userName.setText(Common.currentUser.getName());

        //But with userimage ,we just chaeck it with null or empty

        if(Common.currentUser.getImageUrl() !=null && !TextUtils.isEmpty(Common.currentUser.getImageUrl())){
            Picasso.with(this).load(Common.currentUser.getImageUrl()).into(userImage);
        }

    }
         private void signOut() {
             isLoggingout=true;
             disconnectDriver();
             FirebaseAuth.getInstance().signOut();
             Intent intent = new Intent(UserMap.this, Login.class);
             startActivity(intent);
             finish();
             return;
    }

        private  int radius=1;
        private  Boolean driverFound=false;
        private String driverFoundID;
        private void getCloseatDriver() {
                DatabaseReference driverLocation=FirebaseDatabase.getInstance().getReference().child("DriverAvailable");
                GeoFire geoFire=new GeoFire(driverLocation);
                GeoQuery geoQuery=geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude,pickupLocation.longitude),radius);
                geoQuery.removeAllListeners();
                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
        @Override
        public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound)
                {
                    driverFound=true;
                    driverFoundID=key;
                    getDriversLocation();
                    mRequest.setText("Looking for Driver location......");
                }

        }

        @Override
        public void onKeyExited(String key) {

                }

        @Override
        public void onKeyMoved(String key, GeoLocation location) {

                }

        @Override
        public void onGeoQueryReady() {
                if(!driverFound)
                {
                    radius++;
                    getCloseatDriver();
                }
        }

        @Override
        public void onGeoQueryError(DatabaseError error) {

        }
                });
        }

        private String realtype;
        private void getDriversLocation() {
        DatabaseReference getAssignedPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("DriverAvailable");
        getAssignedPickupLocationRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mMap.clear();

                    for (final DataSnapshot eachcustomers : dataSnapshot.getChildren()) {
                        final String driverId = eachcustomers.getKey();
                        final String studentid=FirebaseAuth.getInstance().getCurrentUser().getUid();

                        DatabaseReference studenttype = FirebaseDatabase.getInstance().getReference("DriverAvailable").child(driverId).child("type");


                        studenttype.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists())
                                {
                                    realtype=dataSnapshot.getValue(String.class);
                                    Log.d("realtype","realtypefind"+ realtype);

                                    if(getName.equals(realtype))
                                    {

                                        DatabaseReference refundopoke = FirebaseDatabase.getInstance().getReference("undopoke").child(studentid).child(driverId);
                                        refundopoke.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists())
                                                {
                                                    List<Object> map = (List<Object>) eachcustomers.child("l").getValue();
                                                    double LocationLat = 0;
                                                    double LocationLng = 0;

                                                    if (map.get(0) != null) {
                                                        LocationLat = Double.parseDouble(map.get(0).toString());
                                                    }
                                                    if (map.get(1) != null) {
                                                        LocationLng = Double.parseDouble(map.get(1).toString());
                                                    }
                                                    final LatLng undopokedriver = new LatLng(LocationLat, LocationLng);
                                                    mMap.addMarker(new MarkerOptions().position(new LatLng(undopokedriver.latitude, undopokedriver.longitude)).snippet(driverId).title("poke").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                                                }

                                                else
                                                {


                                                    Log.d("realtypefound","realtypefound"+ realtype);
                                                    List<Object> map = (List<Object>) eachcustomers.child("l").getValue();
                                                    double LocationLat = 0;
                                                    double LocationLng = 0;

                                                    if (map.get(0) != null) {
                                                        LocationLat = Double.parseDouble(map.get(0).toString());
                                                    }
                                                    if (map.get(1) != null) {
                                                        LocationLng = Double.parseDouble(map.get(1).toString());
                                                    }
                                                    final LatLng studentLatLng = new LatLng(LocationLat, LocationLng);
                                                    //   hashMap.put(driverId,studentLatLng);
                                                    DatabaseReference pokeref=FirebaseDatabase.getInstance().getReference("poke").child(driverId).child(studentid);
                                                    pokeref.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if(dataSnapshot.exists())
                                                            {
                                                                String type=dataSnapshot.child("Reply").getValue(String.class);

                                                                if(type.equals("A"))
                                                                    mMap.addMarker(new MarkerOptions().position(new LatLng(studentLatLng.latitude, studentLatLng.longitude)).snippet(driverId).title("poke").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

                                                                else if(type.equals("D"))
                                                                    mMap.addMarker(new MarkerOptions().position(new LatLng(studentLatLng.latitude, studentLatLng.longitude)).snippet(driverId).title("poke").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                                                else
                                                                    mMap.addMarker(new MarkerOptions().position(new LatLng(studentLatLng.latitude, studentLatLng.longitude)).snippet(driverId).title("poke").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));


                                                            }
                                                            else
                                                            {
                                                                mMap.addMarker(new MarkerOptions().position(new LatLng(studentLatLng.latitude, studentLatLng.longitude)).snippet(driverId).title("poke").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });


                                                }

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });



                                        //  mMap.addMarker(new MarkerOptions().position(new LatLng(studentLatLng.latitude, studentLatLng.longitude)).snippet(driverId).title("poke"));
                                    }
                                }


                                //  Log.d("realtype",realtype);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });






                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


        public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;



                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
                {
                    mMap.setMyLocationEnabled(true);
                    mMap.setOnInfoWindowClickListener(this);

                }
            mMap.setOnInfoWindowClickListener(this);

                }

        public boolean checkLocationPermission() {

                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
                {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_CODE );
                }
                else
                {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_CODE );
                }
                return false;
                }
                else
                return true;

                }


        public void onLocationChanged(Location location) {
        Log.d("locUpdate", String.valueOf(location.getLatitude()));
        mLastLocation = location;
        getDriversLocation();


        String  userId= FirebaseAuth.getInstance().getCurrentUser().getUid();

        if(getName.equals("male"))
        {
            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("studentRequest");

            GeoFire geoFire=new GeoFire(ref);
            geoFire.setLocation(userId,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
            ref.child(userId).child("type").setValue(getName);
            createNdUpdatePoke();
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }

        else if(getName.equals("female"))
        {
            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("studentRequest");

            GeoFire geoFire=new GeoFire(ref);
            geoFire.setLocation(userId,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
            ref.child(userId).child("type").setValue(getName);
            createNdUpdatePoke();
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        }
        else if(getName.equals("teacher"))
        {
            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("studentRequest");
            GeoFire geoFire=new GeoFire(ref);
            geoFire.setLocation(userId,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
            ref.child(userId).child("type").setValue(getName);
            createNdUpdatePoke();
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }




    }


        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

                }

        @Override
        public void onProviderEnabled(String s) {

                }

        @Override
        public void onProviderDisabled(String s) {

                }

        public void getLocation() {

                mLocationManager = (LocationManager) UserMap.this.getSystemService(LOCATION_SERVICE);
                isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                Location location;

                if (!isGPSEnabled && !isNetworkEnabled) {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserMap.this);
                    alertDialog.setTitle("GPS Settings");
                    alertDialog.setMessage("PS is not enabled. Do you want to go to settings menu?");
                    alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            UserMap.this.startActivity(intent);
                        }
                    });

                    alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                alertDialog.show();
                } else {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) &&
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                String[] perm = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
                ActivityCompat.requestPermissions(this, perm,
                RP_ACCESS_LOCATION);
                } else {
                String[] perm = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
                ActivityCompat.requestPermissions(this, perm,
                RP_ACCESS_LOCATION);
                }
                } else {
                if (isNetworkEnabled) {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, (android.location.LocationListener) UserMap.this);

                if (mLocationManager != null) {
                location = mLocationManager
                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if(location!=null){
                onLocationChanged(location);
                }
                }
                }

                if (isGPSEnabled) {
                if (mLastLocation== null) {
                mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, (android.location.LocationListener) UserMap.this);

                if (mLocationManager != null) {
                location = mLocationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(location!=null){
                onLocationChanged(location);
                }
                }
                }
                }
                }
                }
                }

        private void disconnectDriver() {


                if (ActivityCompat.checkSelfPermission(UserMap.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UserMap.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }else{
                    mLocationManager.removeUpdates(UserMap.this);

                }

                mLocationManager.removeUpdates(UserMap.this);
                String  userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("studentRequest").child(userId);
                DatabaseReference ref1=FirebaseDatabase.getInstance().getReference("poke");
                DatabaseReference ref2=FirebaseDatabase.getInstance().getReference("undopoke");
                ref2.removeValue();
                ref1.removeValue();
                ref.removeValue();


            }



       @Override
        protected void onStop() {
            super.onStop();
            if(!isLoggingout)
            {
                disconnectDriver();
            }
        }

        @Override
        public void onBackPressed() {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.user_map, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            /*if (id == R.id.action_settings) {
                return true;
            }*/

            return super.onOptionsItemSelected(item);
        }

        @SuppressWarnings("StatementWithEmptyBody")
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            // Handle navigation view item clicks here.
            int id = item.getItemId();

            if (id == R.id.nav_updateinfo){
                showDialogUpdateInfo();
            }
            else if (id == R.id.nav_passchange) {

                showDialogChangePwd();

            } else if (id == R.id.nav_logout) {
                signOut();

            } else if (id == R.id.nav_help) {

            }

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }

        private void showDialogUpdateInfo() {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("UPDATE INFORMATION");
            dialog.setMessage("Please fill all information");

            LayoutInflater inflater = LayoutInflater.from(this);
            View layout_info = inflater.inflate(R.layout.layout_update_information,null);

            final MaterialEditText edtName = layout_info.findViewById(R.id.edtName);
            final ImageView image_update = layout_info.findViewById(R.id.image_update);

            image_update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    chooseImage();
                }
            });

            dialog.setView(layout_info);

            //set button
            dialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    final android.app.Dialog waitingDialog = new SpotsDialog(UserMap.this);
                    waitingDialog.show();

                    String name = edtName.getText().toString();

                    Map<String,Object> updateInfo = new HashMap<>();
                    if(!TextUtils.isEmpty(name))
                        updateInfo.put("name",name);

                    DatabaseReference userinformation = FirebaseDatabase.getInstance().getReference(Common.user_tbl);
                    userinformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .updateChildren(updateInfo)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                        Toast.makeText(UserMap.this, "Information  Update !", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(UserMap.this, "Information Update Failed !", Toast.LENGTH_SHORT).show();

                                    waitingDialog.dismiss();
                                }
                            });
                }
            });

            dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            dialog.show();
        }

        private void chooseImage() {
            Intent intent =new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"select picture: "),Common.PICK_IMAGE_REQUEST);
            /*startActivityForResult(intent,Gallery_Pick);*/
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if(requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
            /*if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null && data.getData() != null)*/
            {
                Uri saveUri = data.getData();
                if(saveUri != null){
                    final ProgressDialog mDialog = new ProgressDialog(this);
                    mDialog.setMessage("Upoloading ...");
                    mDialog.show();

                    String imageName = UUID.randomUUID().toString(); //rendom name image upload
                    final StorageReference imageFolder = storageReference.child("image/"+imageName);
                    imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(UserMap.this, "Uploaded !", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Map<String,Object> imageUpdate = new HashMap<>();
                                    imageUpdate.put("imageUrl",uri.toString());

                                    DatabaseReference userinformation = FirebaseDatabase.getInstance().getReference(Common.user_tbl);
                                    userinformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .updateChildren(imageUpdate)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful())
                                                        Toast.makeText(UserMap.this, "Uploaded !", Toast.LENGTH_SHORT).show();
                                                    else
                                                        Toast.makeText(UserMap.this, "Upload Error !", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                        }
                    })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                                    mDialog.setMessage("Uploaded"+progress+"%");
                                }
                            });
                }
            }
        }

        private void showDialogChangePwd() {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("CHANGE PASSWORD");
            dialog.setMessage("Please fill all information");

            LayoutInflater inflater = LayoutInflater.from(this);
            View layout_pwd = inflater.inflate(R.layout.layout_change_password,null);

            final MaterialEditText edtOldPassword = layout_pwd.findViewById(R.id.edtOldPassword);
            final MaterialEditText edtNewPassword = layout_pwd.findViewById(R.id.edtNewPassword);
            final MaterialEditText edtRepeatPassword = layout_pwd.findViewById(R.id.edtRepeatPassword);

            dialog.setView(layout_pwd);

            dialog.setPositiveButton("CHANGE PASSWORD", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    final AlertDialog waitingDialog = new SpotsDialog(UserMap.this);

                    if(edtNewPassword.getText().toString().equals(edtRepeatPassword.getText().toString()))
                    {
                        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                        AuthCredential credential = EmailAuthProvider.getCredential(email,edtOldPassword.getText().toString());

                        FirebaseAuth.getInstance().getCurrentUser().
                                reauthenticate(credential).
                                addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            FirebaseAuth.getInstance().getCurrentUser().
                                                    updatePassword(edtRepeatPassword.getText().toString()).
                                                    addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful())
                                                            {
                                                                //update driver password column
                                                                Map<String,Object> password = new HashMap<>();

                                                                password.put("password",edtRepeatPassword.getText().toString());

                                                                DatabaseReference driverInformation = FirebaseDatabase.getInstance().getReference(Common.user_tbl);

                                                                driverInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(password)
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if(task.isSuccessful())
                                                                                    Toast.makeText(UserMap.this, "Password was changed", Toast.LENGTH_SHORT).show();
                                                                                else
                                                                                    Toast.makeText(UserMap.this, "Password was changed but not update in Driver Information", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                            }
                                                            else
                                                            {
                                                                Toast.makeText(UserMap.this, "Password Doesn`t Change", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        }
                                        else
                                        {
                                            waitingDialog.dismiss();
                                            Toast.makeText(UserMap.this, "Wrong Old Password", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                    else {
                        waitingDialog.dismiss();
                        Toast.makeText(UserMap.this, "Password doesn`t match", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            //show dialog
            dialog.show();
        }

        @Override
        public void onInfoWindowClick(Marker marker) {
            driverIDforPoke=marker.getSnippet();
            Log.d("markerDebugSTD", driverIDforPoke);
            createNdUpdatePoke();

        }

        private void createNdUpdatePoke() {

            Log.d("markerDebugSTD", driverIDforPoke);
            String studentid=FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (!driverIDforPoke.isEmpty()){
                if(pokeDriverid.contains(driverIDforPoke) && (pokeDriverid.contains(studentid)) )
                {
                    // Toast.makeText(this, "once more msg from driver poke id", Toast.LENGTH_SHORT).show();
                }
                else
                {

                    pokeDriverid.add(driverIDforPoke);
                    pokeDriverid.add(studentid);

                    DatabaseReference ref= FirebaseDatabase.getInstance().getReference("poke").child(driverIDforPoke);
                    String  userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Log.d("pokestudentid", studentid);
                    GeoFire geoFire=new GeoFire(ref);
                    geoFire.setLocation(userId,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
                    ref.child(userId).child("Reply").setValue("P");

                    ref.child(userId).child("Reply").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String reply= dataSnapshot.getValue(String.class);
                            if(reply!=null){
                                if(reply.equals("A")){
                                    //Toast.makeText(StudentMapActivity.this, "Accept", Toast.LENGTH_SHORT).show();

                                    AlertDialog.Builder notifyPoke= new AlertDialog.Builder(UserMap.this);
                                    notifyPoke.setTitle("Accepted");
                                    notifyPoke.setPositiveButton("okk", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    });
                                    notifyPoke.show();
                                }
                                if(reply.equals("D")){
                                    //  Toast.makeText(StudentMapActivity.this, "Deny", Toast.LENGTH_SHORT).show();

                                    AlertDialog.Builder notifyPoke= new AlertDialog.Builder(UserMap.this);
                                    notifyPoke.setTitle("Denied");
                                    notifyPoke.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    });
                                    notifyPoke.show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }


            }

        }
}
