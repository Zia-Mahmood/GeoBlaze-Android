package com.zarkhub.myapplication;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.INTERNET;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE = 3;
    private static final String TAG = "MainActivity";
    private EditText login_vehicleno,login_password;
    private String vehicleno,password;

    FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login_vehicleno = findViewById(R.id.login_vehicleno);
        login_password = findViewById(R.id.login_password);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        if(!checkPermissions()){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{ACCESS_FINE_LOCATION,ACCESS_COARSE_LOCATION,INTERNET,BLUETOOTH,BLUETOOTH_ADMIN,BLUETOOTH_CONNECT},REQ_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQ_CODE){
            if(grantResults.length>0){
                for(int i=0;i<grantResults.length;i++){
                    if( grantResults[i] == PackageManager.PERMISSION_DENIED ){
                        if(ActivityCompat.shouldShowRequestPermissionRationale(this,permissions[i])){
                            new AlertDialog.Builder(this)
                                    .setTitle("Permission Needed")
                                    .setMessage("This permission is necessary for the app to work smoothly.")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{permissions[i]},REQ_CODE);
                                        }
                                    })
                                    .setNegativeButton("Dismis", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                            new AlertDialog.Builder(MainActivity.this)
                                                    .setTitle("Permission denied")
                                                    .setMessage("Restart the app and grant the permission to proceed further.")
                                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.dismiss();
                                                            MainActivity.this.finish();
                                                        }
                                                    }).create().show();
                                        }
                                    }).create().show();
                        }
                        else {
                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{permissions[i]},REQ_CODE);
                        }
                    }
                }

            }
        }
    }

    public boolean checkPermissions(){
        int result_fine_location = ActivityCompat.checkSelfPermission(this,ACCESS_FINE_LOCATION);
        int result_coarse_location = ActivityCompat.checkSelfPermission(this,ACCESS_COARSE_LOCATION);
        int result_internet = ActivityCompat.checkSelfPermission(this,INTERNET);
        int result_bluetooth = ActivityCompat.checkSelfPermission(this,BLUETOOTH);
        int result_bluetooth_admin = ActivityCompat.checkSelfPermission(this,BLUETOOTH_ADMIN);
        int result_bluetooth_connect = ActivityCompat.checkSelfPermission(this,BLUETOOTH_CONNECT);

        return (result_internet == PackageManager.PERMISSION_GRANTED)
                && (result_coarse_location == PackageManager.PERMISSION_GRANTED)
                && (result_fine_location == PackageManager.PERMISSION_GRANTED)
                && (result_bluetooth == PackageManager.PERMISSION_GRANTED)
                && (result_bluetooth_admin == PackageManager.PERMISSION_GRANTED)
                && (result_bluetooth_connect == PackageManager.PERMISSION_GRANTED);

    }

    public void Destroy(){
        this.finish();
    }
    public void ForgotPassword(View v){
        Intent intent = new Intent(MainActivity.this,ResetPassword.class);
        MainActivity.this.startActivity(intent);
    }

    public void Login(View v){
        vehicleno = login_vehicleno.getText().toString().trim();
        password = login_password.getText().toString().trim();
        Intent intent = new Intent(MainActivity.this,HomeActivity.class);//remove this line afterwards
        MainActivity.this.startActivity(intent); // remove this line afterwards
        if(vehicleno.isEmpty() || password.isEmpty()){
            Toast.makeText(MainActivity.this, "Please enter your Vehicle Number and Password", Toast.LENGTH_SHORT).show();
        }
        else {
            DocumentReference dr = firebaseFirestore.collection("User").document(vehicleno);
            dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if(documentSnapshot.exists()){
                            UserObject uo = documentSnapshot.toObject(UserObject.class);
                            firebaseAuth.signInWithEmailAndPassword(uo.getEmail(),password)
                                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                        @Override
                                        public void onSuccess(AuthResult authResult) {
                                            Intent intent = new Intent(MainActivity.this,HomeActivity.class);
                                            MainActivity.this.startActivity(intent);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(MainActivity.this,"Incorrect Password",Toast.LENGTH_LONG).show();
                                            System.out.println(e.getMessage());
                                        }
                                    });
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Vehicle is not registered", Toast.LENGTH_SHORT).show();
                            System.out.println(task.getException());
                        }
                    }
                    else {
                        Toast.makeText(MainActivity.this,"Error while authentication",Toast.LENGTH_SHORT).show();
                        System.out.println(task.getException()+" "+vehicleno);
                    }
                }
            });

        }
    }
    public void SignUp(View v){
        Intent intent = new Intent(MainActivity.this,RegisterActivity.class);
        MainActivity.this.startActivity(intent);
    }
}