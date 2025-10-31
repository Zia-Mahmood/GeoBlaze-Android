package com.zarkhub.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ResetPassword extends AppCompatActivity {

    private EditText reset_vehicleno;
    private Button reset;
    private String vehicleno;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        reset_vehicleno = findViewById(R.id.reset_vehicleno);
        reset = findViewById(R.id.reset);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    public void Go_Back(View v){
        ResetPassword.this.finish();
    }
    public void Reset(View v){
        reset.setText("Resend");
        vehicleno = reset_vehicleno.getText().toString().trim();
        DocumentReference dr = firebaseFirestore.collection("User").document(vehicleno);
        dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot.exists()){
                        UserObject uo = documentSnapshot.toObject(UserObject.class);
                        firebaseAuth.sendPasswordResetEmail(uo.getEmail())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(ResetPassword.this, "Email Sent", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ResetPassword.this,"Try after some time",Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                    else {
                        Toast.makeText(ResetPassword.this, "Vehicle is not registered", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(ResetPassword.this,"Vehicle is not registered",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}