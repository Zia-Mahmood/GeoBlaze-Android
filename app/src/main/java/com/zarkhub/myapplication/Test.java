package com.zarkhub.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class Test extends AppCompatActivity {


    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice esp;
    BluetoothSocket espSocket;
    boolean turn_left=false,turn_right=false,horn=false,light=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

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

    public void go_back(View v){
        try {
            espSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Test.this.finish();
    }

    public void Turn_Left(View v){
        turn_left = !turn_left;
        if(espSocket!=null){
            if(turn_left)
            { OutputStream outputStream;
            try {
                outputStream = espSocket.getOutputStream();
                outputStream.write(48);
            } catch (IOException  e) {
                e.printStackTrace();
            }
            }else {
                OutputStream outputStream;
                try {
                    outputStream = espSocket.getOutputStream();
                    outputStream.write(49);
                } catch (IOException  e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            Toast.makeText(this, "Bluetooth Connection Interrupted", Toast.LENGTH_SHORT).show();
        }
    }

    public void Turn_Right(View v){
        turn_right = !turn_right;
        if(espSocket!=null){
            if(turn_right)
            { OutputStream outputStream;
                try {
                    outputStream = espSocket.getOutputStream();
                    outputStream.write(50);
                } catch (IOException  e) {
                    e.printStackTrace();
                }
            }else {
                OutputStream outputStream;
                try {
                    outputStream = espSocket.getOutputStream();
                    outputStream.write(51);
                } catch (IOException  e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            Toast.makeText(this, "Bluetooth Connection Interrupted", Toast.LENGTH_SHORT).show();
        }
    }

    public void Horn(View v){
        horn = !horn;
        if(espSocket!=null){
            if(horn)
            { OutputStream outputStream;
                try {
                    outputStream = espSocket.getOutputStream();
                    outputStream.write(52);
                } catch (IOException  e) {
                    e.printStackTrace();
                }
            }else {
                OutputStream outputStream;
                try {
                    outputStream = espSocket.getOutputStream();
                    outputStream.write(53);
                } catch (IOException  e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            Toast.makeText(this, "Bluetooth Connection Interrupted", Toast.LENGTH_SHORT).show();
        }
    }

    public void Light(View v){
        light = !light;
        if(espSocket!=null){
            if(light)
            { OutputStream outputStream;
                try {
                    outputStream = espSocket.getOutputStream();
                    outputStream.write(54);
                } catch (IOException  e) {
                    e.printStackTrace();
                }
            }else {
                OutputStream outputStream;
                try {
                    outputStream = espSocket.getOutputStream();
                    outputStream.write(55);
                } catch (IOException  e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            Toast.makeText(this, "Bluetooth Connection Interrupted", Toast.LENGTH_SHORT).show();
        }
    }
}