package trials;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import payloads.Condition;
import payloads.Datum;
import payloads.DatumProperty;
import payloads.Entity;
import payloads.EntityAttribute;
import payloads.EntityCreation;
import payloads.EntityUpdate;
import payloads.Expression;
import payloads.Http;
import payloads.Notification;
import payloads.NotificationEvent;
import payloads.Subject;
import payloads.Subscription;

/**
 * This class generates the specified number of subscriptions and notifications.
 * 
 * @author victor
 *
 */
public class WorkloadGenerator {

	private final String[] operators = { "<=" };

	private final String ENTITY_ID = "Room0";
	private final String ENTITY_TYPE = "Room";

	private String beaconEntityId;
	private String beaconEntityType;

	private String notificationURL;

	private Random random;
	private Utils utils;
	private String baseURL;

	private int totalSubscriptions;
	private int totalNotifications;

	List<Subscription> subscriptions;
	List<NotificationWrapper> notifications;

	public List<NotificationWrapper> getNotifications() {
		return this.notifications;
	}

	public List<Subscription> getSubscriptions() {
		return this.subscriptions;
	}

	public WorkloadGenerator(String notificationURL, String beaconEntityId,
			String beaconEntityType, Utils utils, int totalSubscriptions,
			int totalNotifications, String baseURL) {

		this.notificationURL = notificationURL;
		this.random = new Random();
		this.beaconEntityId = beaconEntityId;
		this.beaconEntityType = beaconEntityType;
		this.utils = utils;
		this.baseURL = baseURL;

		this.totalSubscriptions = totalSubscriptions;
		this.totalNotifications = totalNotifications;
		
		subscriptions = new ArrayList<>(totalSubscriptions);
		notifications = new ArrayList<>(totalNotifications);
	}

	public EntityCreation createEntity() {

		EntityCreation entity = new EntityCreation().withType(ENTITY_TYPE)
				.withId(ENTITY_ID);

		for (int i = 0; i < 100; i++) {
			EntityAttribute attribute = new EntityAttribute().withType("number")
					.withValue(random.nextDouble() * 1_1000);
			entity.withAdditionalProperty(Integer.toString(i), attribute);
		}

		return entity;
	}

	public void createNotificationsAndSubscriptions() {

		List<Entity> entities = new ArrayList<>();
		entities.add(new Entity().withId(ENTITY_ID).withType(ENTITY_TYPE));

		while (subscriptions.size() < totalSubscriptions) {

			Subscription subscription = new Subscription();
			EntityUpdate notification = new EntityUpdate();

			String attributeName = String.valueOf(random.nextInt(100));
			double value = random.nextDouble() * 1_000;

			Subject subject = new Subject().withEntities(entities)
					.withCondition(generateSubscriptionCondition(attributeName, value));

			// Notifications URLs must be different for each subscriber
			Http http = new Http()
					.withUrl(notificationURL + "/" + random.nextInt(2));
			Notification notificationPayload = new Notification().withHttp(http)
					.withAttrs(new ArrayList<>());

			subscription = new Subscription()
					.withNotification(notificationPayload).withSubject(subject);
			
			subscriptions.add(subscription);
			
			if (notifications.size() < totalNotifications) {
				
				EntityUpdate entityUpdate = new EntityUpdate();

				entityUpdate.setAdditionalProperty(attributeName, new EntityAttribute().withType("number").withValue(value));
				String url = baseURL + "/" + ENTITY_ID + "/attrs?type=Room";
				
				notifications.add(new NotificationWrapper(entityUpdate, url));
			}
		}
		
		fillNotifications(notifications, totalNotifications);

		// Add a special subscription and notification to manage the test
		this.notifications = addManagementNotification(notifications, baseURL);
		
		return;
	}

	private void fillNotifications(List<NotificationWrapper> notifications, int size) {
		
		int remaining = size - notifications.size();
		
		for (int i = 0; i < remaining; i++) {
			
			notifications.add(notifications.get(i));
		}
	}
	
	private Condition generateSubscriptionCondition(String attribute, double value) {


		String operator = operators[random.nextInt(operators.length)];

		// Timestamp condition is needed in order to avoid spontaneous
		// notifications
		String q = attribute + operator + Double.toString(value)
				+ ";timestamp>0.0";

		List<String> attributes = new ArrayList<>();
		attributes.add(attribute);
		attributes.add("timestamp");

		return new Condition().withAttrs(attributes)
				.withExpression(new Expression().withQ(q));
	}

	public Subscription generateManagementSubscription() {

		List<Entity> entities = new ArrayList<>();
		entities.add(
				new Entity().withId(beaconEntityId).withType(beaconEntityType));

		List<String> attributes = new ArrayList<>();
		attributes.add("timestamp");
		Expression expression = new Expression().withQ("timestamp>0.0");
		Condition condition = new Condition().withAttrs(attributes)
				.withExpression(expression);
		Subject subject = new Subject().withCondition(condition)
				.withEntities(entities);

		Http http = new Http().withUrl(notificationURL);
		Notification notification = new Notification().withHttp(http)
				.withAttrs(new ArrayList<>());

		return new Subscription().withNotification(notification)
				.withSubject(subject);
	}

//	public List<Subscription> generateSubscriptions(int numSubscriptions,
//			int numEntities) {
//
//		List<Subscription> subscriptions = new ArrayList<>();
//
//		// this is necessary because the cb-silbops adapter does not support
//		// idPattern field
//		List<Entity> entities = new ArrayList<>();
//		entities.add(new Entity().withId(ENTITY_ID).withType(ENTITY_TYPE));
//
//		for (int i = 0; i < numSubscriptions; i++) {
//
//			Subject subject = new Subject().withEntities(entities)
//					.withCondition(generateSubscriptionCondition());
//
//			// Notifications URLs must be different for each subscriber
//			Http http = new Http()
//					.withUrl(notificationURL + "/" + random.nextInt(2));
//			Notification notification = new Notification().withHttp(http)
//					.withAttrs(new ArrayList<>());
//
//			Subscription subscription = new Subscription()
//					.withNotification(notification).withSubject(subject);
//
//			subscriptions.add(subscription);
//		}
//
//		// Add a special subscription to manage the test
//		subscriptions.add(generateManagementSubscription());
//
//		return subscriptions;
//	}

	public Subscription generateGlobalSubscription() {

		List<Entity> entities = new ArrayList<>();
		entities.add(new Entity().withIdPattern(".*"));

		Condition condition = new Condition().withAttrs(new ArrayList<>());
		Subject subject = new Subject().withEntities(entities)
				.withCondition(condition);

		Notification notification = new Notification()
				.withHttp(new Http()
						.withUrl("http://172.17.0.1:1028/notifications"))
				.withAttrs(new ArrayList<>());

		Subscription contextBrokerSubs = new Subscription().withSubject(subject)
				.withNotification(notification);

		return contextBrokerSubs;
	}

	private List<NotificationWrapper> addManagementNotification(
			List<NotificationWrapper> notifications, String baseURL) {

		String beaconURL = baseURL + "/" + beaconEntityId + "/attrs?type="
				+ beaconEntityType;

		// Add management notifications at the end for finalization
		EntityUpdate endBeaconUpdate = new EntityUpdate()
				.withAdditionalProperty("timestamp", utils.createTimestamp(0L));

		NotificationWrapper endManagementNotification = new NotificationWrapper(
				endBeaconUpdate, beaconURL);
		notifications.add(notifications.size(), endManagementNotification);

		return notifications;
	}

	public List<NotificationWrapper> generateNotifications(int numNotifications,
			int numEntities, String baseURL) {

		List<NotificationWrapper> notifications = new ArrayList<>();

		for (int i = 0; i < numNotifications; i++) {

			// Timestamp should be added just before being sent
			EntityUpdate entityUpdate = new EntityUpdate();

			String attribute = Integer.toString(random.nextInt(100));

			entityUpdate.setAdditionalProperty(attribute,
					utils.createTemperature(random.nextDouble()));

			String entityId = "Room0";
			String url = baseURL + "/" + entityId + "/attrs?type=Room";

			notifications.add(new NotificationWrapper(entityUpdate, url));
		}

		return addManagementNotification(notifications, baseURL);
	}

	public List<NotificationEvent> generateCBNotifications(int numNotifications,
			int numEntities) {

		List<NotificationEvent> notifications = new ArrayList<>();

		for (int i = 0; i < numNotifications; i++) {

			NotificationEvent notification = new NotificationEvent();
			List<Datum> data = new ArrayList<>();

			String entityId = "Room" + random.nextInt(numEntities);
			String attribute = (i % 2 == 0) ? "temperature" : "humidity";

			Datum datum = new Datum().withId(entityId).withType("Room")
					.withAdditionalProperty(attribute, new DatumProperty()
							.withValue(random.nextDouble()).withType("number"));

			data.add(datum);
			notification.withData(data);
			notifications.add(notification);
		}

		// Add management notificatin
		NotificationEvent notification = new NotificationEvent();
		List<Datum> data = new ArrayList<>();

		Datum datum = new Datum().withId(beaconEntityId)
				.withType(beaconEntityType).withAdditionalProperty("timestamp",
						new DatumProperty().withValue(0.0).withType("double"));

		data.add(datum);
		notification.withData(data);
		notifications.add(notification);

		return notifications;
	}
}
