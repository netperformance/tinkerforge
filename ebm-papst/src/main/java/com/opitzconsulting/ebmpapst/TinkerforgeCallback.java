package com.opitzconsulting.ebmpapst;

import com.tinkerforge.IPConnection;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.mqtt.MqttClientConnectionEvents;
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder;

import java.util.concurrent.ExecutionException;

import com.tinkerforge.BrickletTemperature;

public class TinkerforgeCallback {

    private static final String HOST = "localhost";
    private static final int PORT = 4223;

    // Change XYZ to the UID of your Temperature Bricklet
    private static final String UID = "dGW";

    // Note: To make the example code cleaner we do not handle exceptions. Exceptions
    //       you might normally want to catch are described in the documentation
    public static void main(String args[]) throws Exception {
    	
    	// MQTT Connector
    	MqttClientConnectionEvents callbacks = new MqttClientConnectionEvents() {
            @Override
            public void onConnectionInterrupted(int errorCode) {
                if (errorCode != 0) {
                    System.out.println("Connection interrupted: " + errorCode + ": " + CRT.awsErrorString(errorCode));
                }
            }

            @Override
            public void onConnectionResumed(boolean sessionPresent) {
                System.out.println("Connection resumed: " + (sessionPresent ? "existing session" : "clean session"));
            }
        };
        

    	
        IPConnection ipcon = new IPConnection(); // Create IP connection
        BrickletTemperature t = new BrickletTemperature(UID, ipcon); // Create device object

        ipcon.connect(HOST, PORT); // Connect to brickd
        // Don't use device before ipcon is connected

        // Add temperature listener
        t.addTemperatureListener(new BrickletTemperature.TemperatureListener() {
            public void temperature(short temperature) {
            	
            	double currentTemperature = temperature/100.0;
            	
            	// send temperature to AWS
                try {
                	
                	AwsIotMqttConnectionBuilder builder = AwsIotMqttConnectionBuilder.newMtlsBuilderFromPath("/ebm-papst/src/main/resources/device2.cert.pem", "/ebm-papst/src/main/resources/device2.private.key");
                	
                	
                }  catch (CrtRuntimeException ex) {
                    System.out.println(ex);
                }
                
            }
        });

        // Set period for temperature callback to 1s (1000ms)
        // Note: The temperature callback is only called every second
        //       if the temperature has changed since the last call!
        t.setTemperatureCallbackPeriod(1000);

        System.out.println("Press key to exit"); System.in.read();
        ipcon.disconnect();
    }
}