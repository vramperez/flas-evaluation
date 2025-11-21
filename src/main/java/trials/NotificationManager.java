package trials;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import payloads.NotificationEvent;

/**
 * This class receives and manages the Context Broker notifications.
 * 
 * @author victor
 *
 */
public class NotificationManager extends HttpServlet {

	private static final Logger logger = LoggerFactory
			.getLogger(NotificationManager.class);
	
	private String beaconEntityId;
	

	private AtomicLong counter;
	private List<Long> delays;
	private int slot;
	private CountDownLatch latch;

	public NotificationManager(int slot, CountDownLatch latch, String beaconEntityId) {

		super();

		this.slot = slot;
		this.latch = latch;

		this.delays = new ArrayList<>();
		this.counter = new AtomicLong();
		this.beaconEntityId = beaconEntityId;
	}
	
	public void setLatch(CountDownLatch latch) {
		this.latch = latch;
	}

	private String getBody(HttpServletRequest req) {

		StringBuilder buffer = new StringBuilder();

		try {

			BufferedReader reader = req.getReader();

			String line;

			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return buffer.toString();
	}

	private boolean isLastNotification(NotificationEvent notification) {

		return notification.getData().get(0).getId().equals(beaconEntityId);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		long count = counter.getAndIncrement();
		
		String body = getBody(req);

		//System.out.println("Received: " + body);
		
		ObjectMapper mapper = new ObjectMapper();
		NotificationEvent notification = mapper.readValue(body,
				NotificationEvent.class);

		if (isLastNotification(notification)) {

			// Unlock
			System.err.println(count);
			latch.countDown();
			System.out.println("Total number of notifications received: " + count);

		} else {

			if (count % slot == 0) {

				double timeStamp = (double) notification.getData().get(0)
						.getAdditionalProperties().get("timestamp").getValue();

				Long delay = System.nanoTime() - (long) timeStamp;
				delays.add(delay);
			}
		}
	}

	/**
	 * 
	 * @return the average notification delay in microseconds
	 */
	public double getAverageDelay() {

		Long sum = 0L;

		for (Long delay : delays) {

			sum += delay;
		}

		return TimeUnit.NANOSECONDS.toMicros(sum / delays.size());
	}
}
