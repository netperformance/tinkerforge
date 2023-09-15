package com.opitzconsulting.ebmpapst;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttClientConnectionEvents;
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class Config {

    @Value("classpath:/root-CA.crt")
    private Resource rootCertificate;

    @Value("classpath:/device2.cert.pem")
    private Resource certificate;

    @Value("classpath:/device2.public.key")
    private Resource publicKey;

    @Value("classpath:/device2.private.key")
    private Resource privateKey;

    private static final String ENDPOINT = "a2p6ctsf8kybe-ats.iot.eu-central-1.amazonaws.com";

    private static final String CLIENT_ID = "sdk-java";

    private static final int PORT = 8883;

    @Bean(destroyMethod = "close")
    public MqttClientConnection mqttClientConnection() throws IOException {
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

        AwsIotMqttConnectionBuilder builder = AwsIotMqttConnectionBuilder.newMtlsBuilder(certificate.getContentAsByteArray(), privateKey.getContentAsByteArray());
        if (rootCertificate != null) {
            builder.withCertificateAuthority(rootCertificate.getContentAsString(StandardCharsets.UTF_8));
        }
        builder.withConnectionEventCallbacks(callbacks)
                .withClientId(CLIENT_ID)
                .withEndpoint(ENDPOINT)
                .withPort((short) PORT)
                .withCleanSession(true)
                .withProtocolOperationTimeoutMs(60000);

        MqttClientConnection connection = builder.build();
        builder.close();
        return connection;
    }


}
