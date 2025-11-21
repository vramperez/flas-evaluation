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

public class NotificationSender {

	private BlockingQueue<NotificationWrapper> queue;
	private int msgSlot;
	private AtomicBoolean running;

	private Utils utils;
	private ExecutorService executor;
	
	private final int poolSize = 3;

	public NotificationSender(List<NotificationWrapper> notifications, int msgPerSec, Utils utils) {

		this.queue = new ArrayBlockingQueue<>(notifications.size());
		this.queue.addAll(notifications);

		// We assume a 10ms granularity
		msgSlot = msgPerSec / 100;

		running = new AtomicBoolean(true);

		this.utils = utils;
		
		this.executor = Executors.newFixedThreadPool(poolSize);
	}

	private void sendSlot(int slot, BlockingQueue<NotificationWrapper> queue) {

		Collection<NotificationWrapper> toSend = new ArrayList<>(slot);
		queue.drainTo(toSend, slot);

		for (NotificationWrapper notification : toSend) {

			executor.execute(new Runnable() {
				
				@Override
				public void run() {

					utils.sendNotification(notification, 204);
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
