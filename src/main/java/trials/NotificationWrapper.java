package trials;

import payloads.EntityUpdate;

public class NotificationWrapper {

	private EntityUpdate notification;
	private String notificationURL;
	
	public NotificationWrapper(EntityUpdate notification, String notificationURL) {
		
		this.notification = notification;
		this.notificationURL = notificationURL;
	}

	public EntityUpdate getEntityUpdate() {
		return notification;
	}

	public String getNotificationURL() {
		return notificationURL;
	}
}