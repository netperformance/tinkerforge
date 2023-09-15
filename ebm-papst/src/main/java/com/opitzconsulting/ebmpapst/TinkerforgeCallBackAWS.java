package com.opitzconsulting.ebmpapst;

import com.tinkerforge.IPConnection;
import com.tinkerforge.BrickletTemperature;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iotdata.IotDataClient;
import software.amazon.awssdk.services.iotdata.model.PublishRequest;

public class TinkerforgeCallBackAWS {

	private static final String HOST = "localhost";
	private static final int PORT = 4223;
	private static final String UID = "dGW";
	private static final String AWS_IOT_ENDPOINT = "YOUR_AWS_IOT_ENDPOINT"; // Replace with your AWS IoT endpoint
	private static final String AWS_REGION = "YOUR_AWS_REGION"; // Replace with your AWS region
	private static final String AWS_ACCESS_KEY = "YOUR_AWS_ACCESS_KEY"; // Replace with your AWS access key
	private static final String AWS_SECRET_KEY = "YOUR_AWS_SECRET_KEY"; // Replace with your AWS secret key
	private static final String AWS_IOT_TOPIC = "YOUR_TOPIC"; // Replace with your AWS IoT topic

	public static void main(String args[]) throws Exception {
		IPConnection ipcon = new IPConnection();
		BrickletTemperature t = new BrickletTemperature(UID, ipcon);

		ipcon.connect(HOST, PORT);

		t.addTemperatureListener(new BrickletTemperature.TemperatureListener() {
			public void temperature(short temperature) {
				System.out.println("Temperature: " + temperature / 100.0 + " °C");

				// Send temperature data to AWS IoT Core
				sendTemperatureToAWSIoT(temperature);
			}
		});

		t.setTemperatureCallbackPeriod(1000);

		System.out.println("Press key to exit");
		System.in.read();
		ipcon.disconnect();
	}

	private static void sendTemperatureToAWSIoT(short temperature) {
		IotDataClient iotDataClient = IotDataClient.builder().region(Region.of(AWS_REGION))
				.credentialsProvider(
						StaticCredentialsProvider.create(AwsBasicCredentials.create(AWS_ACCESS_KEY, AWS_SECRET_KEY)))
				.endpointOverride(URI.create(AWS_IOT_ENDPOINT)).build();

		String payload = "{\"temperature\": " + (temperature / 100.0) + "}";
		SdkBytes payloadBytes = SdkBytes.fromUtf8String(payload);

		PublishRequest publishRequest = PublishRequest.builder().topic(AWS_IOT_TOPIC).payload(payloadBytes).qos(1)
				.build();

		iotDataClient.publish(publishRequest);
	}
}