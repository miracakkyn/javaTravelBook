package com.miracakkoyun.travelbookjava.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.miracakkoyun.travelbookjava.R;
import com.miracakkoyun.travelbookjava.databinding.ActivityMapsBinding;
import com.miracakkoyun.travelbookjava.model.Place;
import com.miracakkoyun.travelbookjava.roomdb.PlaceDao;
import com.miracakkoyun.travelbookjava.roomdb.PlaceDatabase;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback , GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    LocationManager locationManager;
    LocationListener locationListener;
    SharedPreferences sharedPreferences;
    boolean info;
    PlaceDatabase db;
    PlaceDao placeDao;
    double selectedLatitude;
    double selectedLongitude;
    Place selectedPlace;
    private CompositeDisposable compositeDisposable=new CompositeDisposable();
    ActivityResultLauncher<String> permissionLauncher;//izin launcherimiz izinkleri bunun ile isteyeceğiz aşağıda metodu var
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        registerLauncher();
        sharedPreferences=this.getSharedPreferences("com.miracakkoyun.travelbookjava",MODE_PRIVATE);
        info=false;
        db= Room.databaseBuilder(getApplicationContext(),PlaceDatabase.class,"Places").build();
        placeDao=db.placeDao();
        selectedLatitude=0.0;
        selectedLongitude=0.0;
        binding.saveButton.setEnabled(false);

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
        mMap.setOnMapLongClickListener(this);
        Intent intent=getIntent();
        String intentInfo=intent.getStringExtra("info");
        if(intentInfo.equals("new")){
            binding.saveButton.setVisibility(View.VISIBLE);
            binding.deleteButton.setVisibility(View.GONE);
            LocationManager locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            //location listener location managerdan gelen bilgileri alıp bize iletilmesini sağlıyor
            LocationListener locationListener=new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    info=sharedPreferences.getBoolean("info",false);

                    if(!info){
                        LatLng userLocation=new LatLng(location.getLatitude(),location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,17));
                        mMap.addMarker(new MarkerOptions().position(userLocation));
                        sharedPreferences.edit().putBoolean("info",true).apply();
                    }




                }
            };

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                    Snackbar.make(binding.getRoot(),"Permission Need For Maps",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //request permission
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                        }
                    }).show();
                }else{
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                    //request permission
                }
            }else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(lastLocation!=null){
                    LatLng lastUserLocation=new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,18));
                }
                mMap.setMyLocationEnabled(true);
            }
        }else{
            mMap.clear();
            selectedPlace=(Place)intent.getSerializableExtra("place");
            LatLng latLng=new LatLng(selectedPlace.latitude,selectedPlace.longitude);
            mMap.addMarker(new MarkerOptions().position(latLng).title(selectedPlace.name));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
            binding.placeName.setText(selectedPlace.name);
            binding.saveButton.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.VISIBLE);
        }
        // location manager ile konumumuzu uygulamada almasını sağlayacağız,(lcoationManager) ile döndürmek istediğimiz objenin bir lokasyon servisi olduğunu doğrukuyoruz


        // konum güncellemesini lcoationmanagerdan location listener ile alıp kaç metrede ve kaç milisaniyede alacağımızı aşağıdaki satır
        // --- ile belirtiyoruz ancak öncesinde izinlerimizi vermemiz gerekiyor , bu iznin adı ise access fine location
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);



        //Etli ekmekci konumu 37.8686148,32.4535635

        //LatLng etliEkmek=new LatLng(37.8686148,32.4535635);

        //map objemizin adı mMap
        //mMap.addMarker(new MarkerOptions().position(etliEkmek).title("ET EKMEK ET"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(etliEkmek));// kamerayı götüreceğimiz konum, işaretleme marker yapmaz ama kamerayı o tarafa çevirir

        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(etliEkmek,18));// bu ise bir üst satırdakinden farklı olarak zoom yaparak git anlamına gelir
        // bir üstteki gibi o bölgeyi uzaktan göstermiyor daha pratik


    }
    private void registerLauncher(){
        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
              if(result){
                  if(ContextCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION )==PackageManager.PERMISSION_GRANTED){
                      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                      Location lastLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                      if(lastLocation!=null){
                          LatLng lastUserLocation=new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                          mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,18));
                      }
                  }

                  //permission granted

              }else{
                  Toast.makeText(MapsActivity.this,"Permission needed",Toast.LENGTH_LONG).show();
                  //permission denied
              }
            }
        });
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));
        selectedLatitude=latLng.latitude;
        selectedLongitude=latLng.longitude;
        binding.saveButton.setEnabled(true);
    }
    public void save(View view){
        Place place=new Place(binding.placeName.getText().toString(),selectedLatitude,selectedLongitude);
        // placeDao.insert(place).subscribeOn(Schedulers.io()).subscribe(); bu bir yöntem

        // biz disposable oalrak yapacağız yani kullan at böylece veriler hafızada yer işgal etmekeycek
        compositeDisposable.add(placeDao.insert(place)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(MapsActivity.this::handleResponse));

    }
    public void delete(View view){
        if(selectedPlace!=null){
            compositeDisposable.add(placeDao.delete(selectedPlace).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(MapsActivity.this::handleResponse));
        }
    }
    private void handleResponse(){
        Intent intent=new Intent(MapsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}