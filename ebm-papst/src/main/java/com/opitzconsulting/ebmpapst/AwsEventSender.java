package com.opitzconsulting.ebmpapst;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;

import java.util.concurrent.CompletableFuture;

@Component
public class AwsEventSender {

    private static final String TOPIC = "sdk/test/java";

    private final MqttClientConnection mqttClientConnection;

    public AwsEventSender(MqttClientConnection mqttClientConnection) {
        this.mqttClientConnection = mqttClientConnection;
    }

    public void sendTemperatureToAws(short temperature) {
        final String msg = String.format("{ \"time\": \"%s\"; \"value\": \"%s\" }", System.currentTimeMillis(), temperature);

        // Connect the MQTT client
        CompletableFuture<Boolean> connected = mqttClientConnection.connect();
        try {
            boolean sessionPresent = connected.get();
            System.out.println("Connected to " + (!sessionPresent ? "new" : "existing") + " session!");

            CompletableFuture<Integer> published = mqttClientConnection.publish(new MqttMessage(TOPIC, msg.getBytes(), QualityOfService.AT_LEAST_ONCE, false));
            published.get();

            // Disconnect
            CompletableFuture<Void> disconnected = mqttClientConnection.disconnect();
            disconnected.get();

        } catch (Exception ex) {
            throw new RuntimeException("Exception occurred during connect", ex);
        }
    }
}
