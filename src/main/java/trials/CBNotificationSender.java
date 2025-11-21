package trials;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import payloads.NotificationEvent;

public class CBNotificationSender {

	private BlockingQueue<NotificationEvent> queue;
	private int msgSlot;
	private AtomicBoolean running;

	private Utils utils;
	private ExecutorService executor;
	private String url;
	
	private final int poolSize = 10;

	public CBNotificationSender(List<NotificationEvent> notifications, String url, int msgPerSec, Utils utils) {

		this.queue = new ArrayBlockingQueue<>(notifications.size());
		this.queue.addAll(notifications);

		// We assume a 10ms granularity
		msgSlot = msgPerSec / 100;

		running = new AtomicBoolean(true);

		this.url = url;
		this.utils = utils;
		
		this.executor = Executors.newFixedThreadPool(poolSize);
	}

	private void sendSlot(int slot, BlockingQueue<NotificationEvent> queue) {

		Collection<NotificationEvent> toSend = new ArrayList<>(slot);
		queue.drainTo(toSend, slot);

		for (NotificationEvent notification : toSend) {

			executor.execute(new Runnable() {
				
				@Override
				public void run() {
					
					utils.sendCBNotification(notification, url, 204);
				}
			});
		}
	}

	public long sendNotifications() {

		Long startTime = System.nanoTime();
		
		try {

			while (running.get() && !queue.isEmpty()) {

				sendSlot(msgSlot, queue);

				Thread.sleep(10L);
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Long endTime = System.nanoTime();
		
		return TimeUnit.NANOSECONDS.toMillis(endTime-startTime);
	}
}
