package trials;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBroker {
	
	private static final Logger logger = LoggerFactory
			.getLogger(AbstractBroker.class);

	protected String service;
	protected String baseContainerName;
	protected String entitiesContainerName;
	protected int basePort;
	protected int entitiesPort;
	
	public AbstractBroker(String service, String baseContainerName, int basePort, String entitiesContainerName, int entitiesPort) {
		
		this.service = service;
		this.baseContainerName = baseContainerName;
		this.entitiesContainerName = entitiesContainerName;
		this.basePort = basePort;
		this.entitiesPort = entitiesPort;
	}
	
	// TODO: improve
	private String getIp(String containerName) {

		String ip = "";

		try {

			Process process = new ProcessBuilder("docker", "inspect", "-f",
					"{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}",
					containerName).start();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));

			ip = reader.readLine();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return ip;
	}
	
	public String getBaseUrl() {
		
		String protocol = "http://";
		String ip = getIp(baseContainerName);
		
		return protocol + ip + ":" + basePort;
	}
	
	public String getEntitiesUrl() {
		
		String protocol = "http://";
		String ip = getIp(entitiesContainerName);
		
		return protocol + ip + ":" + entitiesPort;
	}
	
	/**
	 * Deploy the containers for the selected broker
	 * 
	 * @param service	docker-compose service name
	 *
	 */
	public void deployContainers() {

		try {

			new ProcessBuilder("docker-compose", "up", service).start();
			
			// Wait for the containers deployment
			Thread.sleep(10 * 1000);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Stop all the docker containers
	 */
	public void stopContainers() {

		try {

			// It is not possible to use docker-compose down because SilboPS
			//  operators will not be removed since they are created by their own
			Process p = new ProcessBuilder("/bin/bash", "-C",
					"./stop_containers.sh").start();
			p.waitFor();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	protected void redirectIO(Process process, String procesID)
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
