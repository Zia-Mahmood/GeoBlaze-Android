package com.zarkhub.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText register_vehicleno,register_email,register_name,register_password,confirm_password;

    private String vehicleno,email,name,con_password,password;

    private FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        register_vehicleno = findViewById(R.id.register_vehicleno);
        register_email = findViewById(R.id.register_email);
        register_name = findViewById(R.id.register_name);
        register_password = findViewById(R.id.register_password);
        confirm_password = findViewById(R.id.confirm_password);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);
    }



    public void Login(View v){
        RegisterActivity.this.finish();
    }


    public void Sign_Up(View v){
        vehicleno = register_vehicleno.getText().toString().trim();
        email = register_email.getText().toString().trim();
        name = register_name.getText().toString().trim();
        password = register_password.getText().toString().trim();
        con_password = confirm_password.getText().toString().trim();

        if(name.isEmpty() || email.isEmpty() || vehicleno.isEmpty() || password.isEmpty() || con_password.isEmpty()){
            Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(con_password)) {
            Toast.makeText(RegisterActivity.this, "Passwords are not matching", Toast.LENGTH_SHORT).show();
        }
        else {
            DocumentReference dr = firebaseFirestore.collection("User").document(vehicleno);
            dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Toast.makeText(RegisterActivity.this, "Vehicle is already registered", Toast.LENGTH_SHORT).show();
                        } else {
                            DocumentReference dr2 = firebaseFirestore.collection("Emails").document(email);
                            dr2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task1) {
                                    if(task1.isSuccessful()){
                                        DocumentSnapshot documentSnapshot = task1.getResult();
                                        if(documentSnapshot.exists()){
                                            Toast.makeText(RegisterActivity.this, "Email already used", Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            firebaseAuth.createUserWithEmailAndPassword(email,password)
                                                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                                        @Override
                                                        public void onSuccess(AuthResult authResult) {
                                                            startActivity(new Intent(RegisterActivity.this,MainActivity.class));
                                                            Toast.makeText(RegisterActivity.this, "User registered Successfully", Toast.LENGTH_SHORT).show();
                                                            firebaseFirestore.collection("User")
                                                                    .document(vehicleno)
                                                                    .set(new UserObject(vehicleno,email,name));
                                                            firebaseFirestore.collection("Emails")
                                                                    .document(email)
                                                                    .set(new Emails(email));
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(RegisterActivity.this,"Can't process your request.\nTry after some time",Toast.LENGTH_LONG).show();
                                                            e.printStackTrace();
                                                            System.out.println(e.toString());
                                                        }
                                                    });

                                        }
                                    }
                                    else {
                                        Toast.makeText(RegisterActivity.this, "No such email registered", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                    else {
                        Toast.makeText(RegisterActivity.this, "No such vehicle registered", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        //firebaseFirestore.collection("User").document(vehicleno).set(new UserObject(vehicleno,email,name));

        /*DocumentReference dr = firebaseFirestore.collection("User").document(vehicleno);
        dr.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserObject uo = documentSnapshot.toObject(UserObject.class);
                System.out.println(uo.getName()+" "+uo.getEmail()+" "+uo.getVehicleno());
            }
        });*/


    }
}