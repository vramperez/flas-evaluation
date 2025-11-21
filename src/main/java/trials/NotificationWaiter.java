package trials;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicLong;

import com.conwet.silbops.api.AdvertiseListener;
import com.conwet.silbops.api.NotificationListener;
import com.conwet.silbops.model.Advertise;
import com.conwet.silbops.model.Notification;
import com.conwet.silbops.model.basic.Type;

public class NotificationWaiter implements NotificationListener, AdvertiseListener {
	
	private Notification expected;
	private Phaser phaser;
	
	private List<Long> delays;
	private final int SLOT = 200;
	private AtomicLong counter;
	
	public NotificationWaiter(Notification expected) {
		
		this.expected = Objects.requireNonNull(expected);
		this.phaser = new Phaser(2);
		
		this.counter = new AtomicLong();
		this.delays = new ArrayList<>();
	}
	
	@Override
	public void receiveNotification(Notification notification) {
		
		if (expected.equals(notification)) {
			
			phaser.arrive();
		}
		
		long count = counter.getAndIncrement();
		
		if (count % SLOT == 0) {
			
			long current = System.nanoTime();
			long delay = current - notification.getValue("timestamp", Type.LONG).getLong();
			
			delays.add(delay);
		}
	}
	
	public void awaitLastNotification() {
		
		phaser.arriveAndAwaitAdvance();
	}
	
	public long getAverageDelay() {

		long sum = 0;

		for (Long delay : delays) {
			sum += delay;
		}

		return sum / delays.size();
	}

	@Override
	public void receiveAdvertise(Advertise advertise) {
		// Nothing to do here
		
	}

	@Override
	public void receiveUnadvertise(Advertise advertise) {
		// Nothing to do here
	}
}
