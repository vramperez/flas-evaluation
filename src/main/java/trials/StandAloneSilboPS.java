package trials;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandAloneSilboPS extends AbstractBroker{
	
	private static final Logger logger = LoggerFactory
			.getLogger(StandAloneSilboPS.class);
	
	private int accessPoints = 1;
	private int matchers = 1;
	private int exitPoints = 2;

	public StandAloneSilboPS() {

		super("silbops", "", 0, "", 0);
	}
	
	public StandAloneSilboPS(int accessPoints, int matchers, int exitPoints) {

		super("silbops", "", 0, "", 0);
		
		this.accessPoints = accessPoints;
		this.matchers = matchers;
		this.exitPoints = exitPoints;
	}
	
	@Override
	public String toString() {
		return "E-SilboPS_" + accessPoints + "-" + matchers + "-" + exitPoints;
	}
	
	@Override
	public void deployContainers() {

		try {

			logger.debug("Deploying...");
			
			Process process = new ProcessBuilder("docker", "service", "create",
					"--constraint=node.role==manager", 
					"--mount", "type=bind,src=/var/run/docker.sock,dst=/var/run/docker.sock",
					"--mount", "type=bind,src=/usr/lib/x86_64-linux-gnu/libltdl.so.7,dst=/usr/lib/x86_64-linux-gnu/libltdl.so.7",
					"--mount", "type=bind,src=/usr/bin/docker,dst=/bin/docker",
					"--network", "ejemplo", "--name", "ORCHESTRATOR", 
					"vramperez/silbops:test",
					"orchestrator", Integer.toString(accessPoints), Integer.toString(matchers), Integer.toString(exitPoints)).start();
			
			Utils.redirectIO(process, "ORCHESTRATOR");

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Wait for the containers deployment
		Utils.sleepTime(40);
	}
	
	@Override
	public void stopContainers() {

		String [] args = {"docker", "service", "rm", "ORCHESTRATOR"};
		List<String> argsList = new ArrayList<>(Arrays.asList(args));
		
		for(int i = 0; i<accessPoints; i++) {
			argsList.add("ACCESS_POINT_" + i);
		}
		
		for(int i = 0; i<matchers; i++) {
			argsList.add("MATCHER_" + i);
		}
		
		for(int i = 0; i<exitPoints; i++) {
			argsList.add("EXIT_POINT_" + i);
		}
		
		// It is not possible to use docker-compose down because SilboPS
		// operators will not be removed since they are created by their own
		try {

			Process process = new ProcessBuilder(argsList).start();
			process.waitFor();
			
			Utils.redirectIO(process, "ORCHESTRATOR");

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		Utils.sleepTime(30);
	}
}
