package trials;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.catalina.startup.Tomcat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;

import payloads.EntityCreation;
import payloads.EntityUpdate;
import payloads.Subscription;

@RunWith(Parameterized.class)
public class PerformanceTest {

	private static final Logger logger = LoggerFactory
			.getLogger(PerformanceTest.class);

	private static final int repetitions = 1;

	private PrintWriter writer;
	private URL notificationUrl;

	private final String beaconEntityId = "BeaconEntity";
	private final String beaconEntityType = "Beacon";

	private Listener listener;
	private NotificationManager notificationManager;
	private CountDownLatch latch;

	private String entitiesUrl;
	private String subscriptionUrl;

	private Random random;
	private ObjectMapper mapper;
	private Utils utils;

	@Parameter(0)
	public AbstractBroker broker;
	@Parameter(1)
	public int numSubscriptions;
	@Parameter(2)
	public int numNotifications;
	@Parameter(3)
	public int notificationSpeed;
	@Parameter(4)
	public int entities;

	@Parameters(name = "Broker= {0}, Number of subscriptions={1}, Number of notifications={2}, Notification speed={3}, Number of CB entites= {4}")
	public static Collection<Object[]> data() {

		List<Object[]> testCases = new ArrayList<>();
		List<Object[]> parameters = new ArrayList<>();
		List<AbstractBroker> brokers = new ArrayList<>();

		//brokers.add(new ContextBroker());
		brokers.add(new MultiContextBrokerLocal());
		// brokers.add(new SilboPSBroker());
		// brokers.add(new MockedContextBroker());

		brokers.forEach(broker -> {

			testCases.add(new Object[] { broker, 1_000, 100_000, 100_000, 1 });
		});

		// Repeat each test case
		testCases.forEach(testCase -> {

			for (int i = 0; i < repetitions; i++) {
				parameters.add(testCase);
			}
		});

		return parameters;
	}

	private void waitFor(int seconds) {

		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Before
	public void setUp() throws MalformedURLException {

		try {

			String fileName = "/home/victor/workspace/silbops-benchmark/data/results/"
					+ broker.toString() + "_" + System.currentTimeMillis()
					+ ".csv";
			File file = new File(fileName);

			// Print header
			this.writer = new PrintWriter(file, "UTF-8");
			writer.println(
					"broker, subs, notifs, notif_speed, entities, throughput");
			writer.flush();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		notificationUrl = new URL("http://172.17.0.1:1028");

		// Stop previous containers
		logger.info("Stopping and removing previous containers...");
		broker.stopContainers();

		// Deploy the broker
		logger.info("Deploying " + broker + "...");
		broker.deployContainers();

		random = new Random();
		mapper = new ObjectMapper();
		utils = new Utils(Client.create(), mapper);
	}

	@After
	public void tearDown() {

		logger.info("Stopping embedded tomcat and containers...");
		// listener.stop();
		broker.stopContainers();
	}

	/**
	 * Creates and registers the number of entities of the test and a beacon
	 * entity for test management
	 */
	private void registerEntities(WorkloadGenerator workloadGenerator) {

		try {

			EntityCreation entity = workloadGenerator.createEntity();

			utils.sendRequest(mapper.writeValueAsString(entity), entitiesUrl,
					201);

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		try {

			// Register an extra entity for test management
			EntityCreation beaconEntity = utils.createEntity(beaconEntityId,
					beaconEntityType);

			utils.sendRequest(mapper.writeValueAsString(beaconEntity),
					entitiesUrl, 201);

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		logger.info(entities + " registered entities");
	}

	private void sendSubscriptions(List<Subscription> subscriptions) {

		subscriptions.forEach(subscription -> {

			try {

				utils.sendRequest(mapper.writeValueAsString(subscription),
						subscriptionUrl, 201);

			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		});
	}

	private void printTestResults(long subscriptions, double throughput) {

		writer.println(broker + "," + subscriptions + "," + numNotifications
				+ "," + notificationSpeed + "," + entities + "," + throughput);
		writer.flush();
	}

	@Test(timeout = 45 * 60 * 1_000) // 45 min timeout for each test case
	public void shouldMeasurePerformance() {

		latch = new CountDownLatch(1);
		notificationManager = new NotificationManager(20, latch,
				beaconEntityId);

		logger.info("Starting notification endpoint...");

		// Deploy the notification endpoint
		listener = new Listener(new Tomcat(), notificationManager,
				Executors.newSingleThreadExecutor());
		listener.start(notificationUrl.getHost(), notificationUrl.getPort(),
				"/");

		// Waiting for notification endpoint deployment
		waitFor(10);

		String serviceBaseUrl = broker.getBaseUrl();// "172.17.0.1:1027";
		this.entitiesUrl = broker.getEntitiesUrl() + "/v2/entities";
		this.subscriptionUrl = broker.getBaseUrl() + "/v2/subscriptions";

		// Send subscriptions
		WorkloadGenerator workloadGenerator = new WorkloadGenerator(
				notificationUrl.toString(), beaconEntityId, beaconEntityType,
				utils, numSubscriptions, numNotifications, entitiesUrl);

		// Register entities in the CB
		logger.info("Registering entities...");
		registerEntities(workloadGenerator);

		logger.info("Creating workload...");
		workloadGenerator.createNotificationsAndSubscriptions();

		List<Subscription> subscriptions = workloadGenerator.getSubscriptions();
		List<NotificationWrapper> notifications = workloadGenerator
				.getNotifications();

		logger.info(
				"Workload generated. {} subscriptions and {} notifications.",
				subscriptions.size(), notifications.size());

		int toSkip = 0;
		int size = subscriptions.size();

		for (Long subs : new LogaritmicSequence(1, size, 9)) {
			
			NotificationSender notificationSender = new NotificationSender(
					notifications, notificationSpeed, utils);

			int limit = subs.intValue() - toSkip;
			List<Subscription> toSend = subscriptions.subList(0, limit);

			if (subs == 1) {
				toSend.add(workloadGenerator.generateManagementSubscription());
			}

			logger.info("Sending additional {} subscriptions...",
					toSend.size());

			sendSubscriptions(toSend);
			toSkip = subs.intValue();
			toSend.clear();
			waitFor(60);
			System.gc();
			waitFor(1);

			logger.info("Sending {} notifications...", notifications.size());

			Long startTime = System.nanoTime();
			notificationSender.sendNotifications();
			Long endSendTime = System.nanoTime();

			double sendTime = TimeUnit.NANOSECONDS
					.toMillis(endSendTime - startTime);

			logger.info("Waiting notification arrvival...");

			try {

				latch.await();

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Long endTime = System.nanoTime();

			logger.info("Waiting for all the notifications...");
			waitFor(10);

			// Print the test results
			double averageDelay = notificationManager.getAverageDelay();

			double totalTime = TimeUnit.NANOSECONDS
					.toMillis(endTime - startTime);
			double throughput = tuneThroughput(sendTime, totalTime);
			logger.info("TotalTime: " + totalTime + " ms ; Avg. Delay: "
					+ averageDelay + " microseconds ; Throughput: " + throughput
					+ " notf/s");

			System.err.println("Send time: " + sendTime + " ms");
			printTestResults(subs, throughput);

			String beaconURL = entitiesUrl + "/" + beaconEntityId
					+ "/attrs?type=" + beaconEntityType;

			// Add management notifications at the end for finalization
			EntityUpdate endBeaconUpdate = new EntityUpdate()
					.withAdditionalProperty("timestamp",
							utils.createTimestamp(0L));

			NotificationWrapper endManagementNotification = new NotificationWrapper(
					endBeaconUpdate, beaconURL);
			
			this.latch = new CountDownLatch(1);
			notificationManager.setLatch(this.latch);
			
			try {
				
				utils.sendRequest(
						mapper.writeValueAsString(
								endManagementNotification.getEntityUpdate()),
						endManagementNotification.getNotificationURL(), 204);
				
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
	}

	private double tuneThroughput(double sendingTime, double totalTime) {

		double maxRealThroughput = 1000 * (numNotifications / sendingTime);
		double actualThroughput = 1000 * (numNotifications / totalTime);

		return (notificationSpeed * actualThroughput) / maxRealThroughput;
	}
}
