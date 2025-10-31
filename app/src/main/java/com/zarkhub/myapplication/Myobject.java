package com.zarkhub.myapplication;

public class Myobject {
    public double duration;
    public double distance;
    public String maneuver;
    public double str_lat, str_long;
    public double end_lat, end_long;
    public String instruction;

    Myobject(String iduration, String idistance, String imaneuver, String iinstruction) {
        duration = getDuration(iduration);
        distance = getDistance(idistance);
        maneuver = imaneuver;
        instruction = iinstruction;
    }

    Myobject(String iduration, String imaneuver, String iinstruction, int i) {
        duration = getDuration(iduration);
        distance = -1;
        maneuver = imaneuver;
        instruction = iinstruction;
    }

    Myobject(String iduration, String idistance, String iinstruction) {
        duration = getDuration(iduration);
        distance = getDistance(idistance);
        maneuver = "unknown";
        instruction = iinstruction;
    }

    Myobject(String iduration, String iinstruction) {
        duration = getDuration(iduration);
        distance = -1;
        maneuver = "unknown";
        instruction = iinstruction;
    }

    public double getDistance(String dis) {
        String[] s = dis.split("\\s");
        if (s[1].equalsIgnoreCase("m")) {
            return Double.valueOf(s[0]) / 1000;
        }
        return Double.valueOf(s[0]);
    }

    public double getDuration(String dur) {
        String[] s = dur.split("\\s");
        if (s.length == 4) {
            return Double.valueOf(s[0]) * 60 + Double.valueOf(s[2]);
        }
        return Double.valueOf(s[0]);
    }

    void setStartingPoint(double lat, double lon) {
        str_lat = lat;
        str_long = lon;
    }

    void setEndPoint(double lat, double lon) {
        end_lat = lat;
        end_long = lon;
    }

    void printDetailsofObject() {
        System.out.println("\nDuration: " + duration + " mins");
        System.out.println("Distance " + distance + " km");
        System.out.println("Maneuver " + maneuver);
        System.out.println("Start latitude: " + str_lat + " , Start longitude: " + str_long);
        System.out.println("End latitude: " + end_lat + " , End longitude: " + end_long);
        System.out.println("Instruction: " + instruction + "\n\n");
    }

}
