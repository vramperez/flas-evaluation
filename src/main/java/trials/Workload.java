package trials;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import payloads.Condition;
import payloads.Datum;
import payloads.DatumProperty;
import payloads.Entity;
import payloads.Expression;
import payloads.Http;
import payloads.Notification;
import payloads.NotificationEvent;
import payloads.Subject;
import payloads.Subscription;

public class Workload {

	private final int totalSubscriptions;
	private final int totalNotifications;

	private List<Subscription> subscriptions;
	private List<NotificationEvent> notifications;

	private String notificationURL;
	private Random randomizer;

	public Workload(int totalSubscriptions, int totalNotifications,
			String notificationURL, Random randomizer) {

		this.totalSubscriptions = totalSubscriptions;
		this.totalNotifications = totalNotifications;
		this.notificationURL = notificationURL;
		this.randomizer = randomizer;

		this.subscriptions = new ArrayList<>(totalSubscriptions);
		this.notifications = new ArrayList<>(totalNotifications);
	}

	public void createNotificationsAndSubscriptions() {

		while (subscriptions.size() < totalSubscriptions) {

			boolean match = true;
			int nConstraints = randomizer.nextInt(1); // TODO: not hardcoded
														// (see Workload from
														// pubsub-mr)
			Subscription subscription = new Subscription();
			NotificationEvent notification = new NotificationEvent();

			while (nConstraints >= 0) {

				String attributeName = String.valueOf(randomizer.nextInt(100));
				Double giant = randomizer.nextDouble() * 1_000;
				String value = giant.toString();

				List<Entity> entities = new ArrayList<>();
				entities.add(new Entity().withId("Room1"));

				List<String> attributes = new ArrayList<>();
				attributes.add(attributeName);
				Subject subject = new Subject().withEntities(entities)
						.withCondition(new Condition().withAttrs(attributes)
								.withExpression(new Expression()
										.withQ(attributeName + "<" + value)));

				// Notifications URLs must be different for each subscriber
				Http http = new Http()
						.withUrl(notificationURL + "/" + randomizer.nextInt(2));
				Notification notificationPayload = new Notification()
						.withHttp(http).withAttrs(new ArrayList<>());

				subscription.withNotification(notificationPayload)
						.withSubject(subject);

				if (notifications.size() < totalNotifications) {
					// Generate notification
					List<Datum> data = new ArrayList<>();
					String entityId = "Room1";

					Datum datum = new Datum().withId(entityId).withType("Room")
							.withAdditionalProperty(attributeName,
									new DatumProperty().withValue(giant - 1)
											.withType("number"));

					data.add(datum);
					notification.withData(data);
				}
				
				nConstraints--;
			}

			subscriptions.add(subscription);
			
			if (notifications.size() < totalNotifications) {
				notifications.add(notification);
			}
		}

		fillMessageArray(notifications, totalNotifications);
	}

	private void fillMessageArray(List<NotificationEvent> notifications,
			int size) {

		int remaining = size - notifications.size();

		for (int i = 0; i < remaining; i++) {

			notifications.add(notifications.get(i));
		}

		Map<String, DatumProperty> props = notifications.get(0).getData().get(0)
				.getAdditionalProperties();

		props.forEach((prop, value) -> {

			List<Datum> data = new ArrayList<>();
			String entityId = "Room1";

			Datum datum = new Datum().withId(entityId).withType("Room")
					.withAdditionalProperty(prop,
							new DatumProperty().withValue(value.getValue())
									.withType(value.getType()))
					.withAdditionalProperty("special_attribute",
							new DatumProperty().withType("nothing")
									.withValue(""));

			data.add(datum);
			NotificationEvent notification = new NotificationEvent();
			notification.withData(data);

			notifications.set(notifications.size() - 1, notification);
		});
	}

	public List<Subscription> getSubscriptions() {

		return subscriptions;
	}

	public List<NotificationEvent> getNotifications() {

		return notifications;
	}
}
