package com.opitzconsulting.ebmpapst;

import com.tinkerforge.IPConnection;
import com.tinkerforge.BrickletTemperature;

public class TinkerforgeController {

    private static final String HOST = "localhost";
    private static final int PORT = 4223;

    // Change XYZ to the UID of your Temperature Bricklet
    private static final String UID = "dGW";

    // Note: To make the example code cleaner we do not handle exceptions. Exceptions
    //       you might normally want to catch are described in the documentation
    public static void main(String args[]) throws Exception {
        IPConnection ipcon = new IPConnection(); // Create IP connection
        BrickletTemperature t = new BrickletTemperature(UID, ipcon); // Create device object

        ipcon.connect(HOST, PORT); // Connect to brickd
        // Don't use device before ipcon is connected

        // Get current temperature
        short temperature = t.getTemperature(); // Can throw com.tinkerforge.TimeoutException
        System.out.println("Temperature: " + temperature/100.0 + " °C");

        System.out.println("Press key to exit"); System.in.read();
        ipcon.disconnect();
    }
}