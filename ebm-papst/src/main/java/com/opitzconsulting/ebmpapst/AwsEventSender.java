package com.opitzconsulting.ebmpapst;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;

import java.util.concurrent.CompletableFuture;

@Component
public class AwsEventSender {

    private static final Logger LOG = LoggerFactory.getLogger(AwsEventSender.class);

    private static final String TOPIC = "sdk/test/java";

    private final MqttClientConnection mqttClientConnection;

    public AwsEventSender(MqttClientConnection mqttClientConnection) {
        this.mqttClientConnection = mqttClientConnection;
        // Connect the MQTT client
        CompletableFuture<Boolean> connected = mqttClientConnection.connect();
        try {
            boolean sessionPresent = connected.get();
            LOG.info("Connected to {} session!", (!sessionPresent ? "new" : "existing"));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void sendTemperatureToAws(short temperature) {
        final String msg = String.format("{ \"time\": \"%s\", \"value\": %s }", System.currentTimeMillis(), temperature / 100.0);

        try {
            CompletableFuture<Integer> published = mqttClientConnection.publish(new MqttMessage(TOPIC, msg.getBytes(), QualityOfService.AT_LEAST_ONCE, false));
            published.get();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @PreDestroy
    private void disconnect() {
        try {
            // Disconnect
            CompletableFuture<Void> disconnected = mqttClientConnection.disconnect();
            disconnected.get();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
