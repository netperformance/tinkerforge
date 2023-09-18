package com.opitzconsulting.ebmpapst;

import com.tinkerforge.BrickletTemperature;
import com.tinkerforge.IPConnection;
import com.tinkerforge.TinkerforgeException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SendingEventsService {

    private static final Logger LOG = LoggerFactory.getLogger(SendingEventsService.class);

    private static final String HOST = "localhost";
    private static final int PORT = 4223;

    // Change XYZ to the UID of your Temperature Bricklet
    private static final String UID = "dGW";

    private final AwsEventSender awsEventSender;

    public SendingEventsService(AwsEventSender awsEventSender) {
        this.awsEventSender = awsEventSender;
    }

    @PostConstruct
    public void connectAndSendEvents() {
        LOG.info("connectAndSendEvents: ...");
        try {
            connectToDevice();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void connectToDevice() throws TinkerforgeException {
        LOG.info("connect to device: host={}, port={}", HOST, PORT);
        IPConnection ipcon = new IPConnection(); // Create IP connection
        BrickletTemperature t = new BrickletTemperature(UID, ipcon); // Create device object

        ipcon.connect(HOST, PORT); // Connect to brickd
        // Don't use device before ipcon is connected

        // Add temperature listener
        t.addTemperatureListener(new BrickletTemperature.TemperatureListener() {
            public void temperature(short temperature) {
                LOG.info("getting temperature: {}", temperature/100.0);
                awsEventSender.sendTemperatureToAws(temperature);
            }
        });

        // Set period for temperature callback to 1s (1000ms)
        // Note: The temperature callback is only called every second
        //       if the temperature has changed since the last call!
        t.setTemperatureCallbackPeriod(1000);
        ipcon.setAutoReconnect(true);
//        ipcon.disconnect();
    }

}
