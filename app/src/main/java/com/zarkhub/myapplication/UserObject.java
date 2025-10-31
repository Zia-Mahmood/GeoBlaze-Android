package com.zarkhub.myapplication;

public class UserObject {
    private String vehicleno,email,name;

    public UserObject() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserObject(String vehicleno, String email, String name){
        this.email = email;
        this.vehicleno = vehicleno;
        this.name = name;
    }

    public String getVehicleno() {
        return vehicleno;
    }

    public void setVehicleno(String vehicleno) {
        this.vehicleno = vehicleno;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
