package trials;

import java.io.IOException;

public class MultiContextBrokerCluster extends AbstractBroker{
	
	public MultiContextBrokerCluster() {

		super("haproxy", "LOAD_BALANCER", 1026, "LOAD_BALANCER", 1026);
	}

	@Override
	public String toString() {
		return "MULTI_CONTEXT_BROKER";
	}

	@Override
	public String getBaseUrl() {

		String protocol = "http://";
		String ip = "192.168.1.137";

		return protocol + ip + ":" + basePort;
	}

	@Override
	public String getEntitiesUrl() {

		String protocol = "http://";
		String ip = "192.168.1.137";

		return protocol + ip + ":" + entitiesPort;
	}

	@Override
	public void deployContainers() {

		try {

			Process p1 = new ProcessBuilder("docker", "service", "create",
					"--name", "mongo", "--constraint", "node.labels.type == worker1", "--network", "ejemplo", "mongo:3.2", "--nojournal").start();
			redirectIO(p1, "mongo");
			
			p1.waitFor();
			
			Process p2 = new ProcessBuilder("docker", "service", "create",
					"--name", "CONTEXT_BROKER1", "--constraint", "node.labels.type == worker1", "--network", "ejemplo", "--publish", "1024:1024", "fiware/orion", "-subCacheIval", "10", "-port", "1024" , "-dbhost", "mongo", "-notificationMode", "threadpool:10000000:60", "-logLevel", "ERROR", "-disableMetrics").start();
			redirectIO(p2, "CONTEXT_BROKER1");
			
			p2.waitFor();
			
			Process p3 = new ProcessBuilder("docker", "service", "create",
					"--name", "CONTEXT_BROKER2", "--constraint", "node.labels.type == worker1", "--network", "ejemplo", "--publish", "1025:1025", "fiware/orion", "-subCacheIval", "10", "-port", "1024" , "-dbhost", "mongo", "-notificationMode", "threadpool:10000000:60", "-logLevel", "ERROR", "-disableMetrics").start();
			redirectIO(p3, "CONTEXT_BROKER2");
			
			p3.waitFor();
			
			Process p4 = new ProcessBuilder("docker", "service", "create",
					"--name", "LOAD_BALANCER", "--constraint", "node.role==manager", "--network", "ejemplo", "--publish", "1026:1026","--mount", "type=bind,src=/home/victor/workspace/silbops-benchmark/haproxy_confg,dst=/usr/local/etc/haproxy", "haproxy").start();
			redirectIO(p4, "LOAD_BALANCER");
			
			p4.waitFor();

			// Wait for the containers deployment
			Thread.sleep(10 * 1_000);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
