package trials;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import payloads.EntityCreation;
import payloads.NotificationEvent;
import payloads.DatumProperty;
import payloads.EntityAttribute;

/**
 * This class offers common functionalities for the trials
 * 
 * @author victor
 *
 */
public class Utils {

	private static final Logger logger = LoggerFactory.getLogger(Utils.class);

	private Client client;
	private ObjectMapper mapper;

	public Utils(Client client, ObjectMapper mapper) {

		this.client = client;
		this.mapper = mapper;
	}

	public EntityCreation createEntity(String entityId, String type) {

		return new EntityCreation().withType(type).withId(entityId)
				.withAdditionalProperty("temperature", createTemperature(0.0))
				.withAdditionalProperty("humidity", createHumidity(0.0))
				.withAdditionalProperty("timestamp", createTimestamp(0L));
	}

	public EntityAttribute createTemperature(double value) {

		return new EntityAttribute().withType("number").withValue(value);
	}

	public EntityAttribute createHumidity(double value) {

		return new EntityAttribute().withType("percentage").withValue(value);
	}

	public EntityAttribute createTimestamp(long value) {

		return new EntityAttribute().withType("long").withValue(value);
	}

	public void sendRequest(String payload, String url, int expectedStatus) {
		
		WebResource resource = client.resource(url);
		ClientResponse response = resource.type("application/json")
				.post(ClientResponse.class, payload);

		if (response.getStatus() != expectedStatus) {
			logger.error("Error sending request (" + response.getStatus()
					+ "); expected status: " + expectedStatus);
			logger.error(response.getStatusInfo().getReasonPhrase());
		}
		
		response.close();
	}

	public void sendNotification(NotificationWrapper notification,
			int expectedStatus) {

		Long timestamp = System.nanoTime();
		notification.getEntityUpdate().setAdditionalProperty("timestamp",
				createTimestamp(timestamp));

		try {

			String payload = mapper
					.writeValueAsString(notification.getEntityUpdate());
//			System.out.println("Sending to: " + notification.getNotificationURL() + " payload: "+ payload);
			sendRequest(payload, notification.getNotificationURL(),
					expectedStatus);

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	public void sendCBNotification(NotificationEvent notification, String url,
			int expectedStatus) {

		Long timestamp = System.nanoTime();
		notification.getData().get(0).setAdditionalProperty("timestamp",
				new DatumProperty().withType("double").withValue(new Double(timestamp)));
		
		try {
			
			String payload = mapper.writeValueAsString(notification);
//			System.out.println(payload);
			sendRequest(payload, url, expectedStatus);
			
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
	
	public static void sleepTime(long seconds) {
		
		try {
			logger.debug("sleeping " + seconds + " seconds...");
			Thread.sleep(seconds * 1000L);
			logger.debug("exiting sleep.");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void redirectIO(Process process, String procesID)
			throws IOException {

		BufferedReader stdInput = new BufferedReader(
				new InputStreamReader(process.getInputStream()));

		BufferedReader stdError = new BufferedReader(
				new InputStreamReader(process.getErrorStream()));

		// read the output from the command
		String s = null;
		if ((s = stdInput.readLine()) != null) {

			logger.debug("{} stdout: {}", procesID, s);

			while ((s = stdInput.readLine()) != null) {
				logger.debug(s);
			}
		}

		// read any errors from the attempted command
		if ((s = stdError.readLine()) != null) {

			logger.error("{} stderror: {}", procesID, s);

			while ((s = stdInput.readLine()) != null) {
				logger.error(s);
			}
		}
	}
}
