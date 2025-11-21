package trials;

import java.io.File;
import java.util.concurrent.ExecutorService;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

/**
 * This class deploys an embedded Tomcat as a service callback for the Context 
 * Broker notifications
 * 
 * @author victor
 *
 */
public class Listener {
	
	private Tomcat tomcat;
	private NotificationManager notificationManager;
	private ExecutorService executor;

	public Listener(Tomcat tomcat, NotificationManager notificationManager, ExecutorService executor) {
		
		this.tomcat = tomcat;
		this.notificationManager = notificationManager;
		this.executor = executor;
	}
	
	public void start(String host, int port, String path) {
		
		executor.submit(() -> {
			
			tomcat.setHostname(host);
			tomcat.setPort(port);
			
			Context ctx = tomcat.addContext("/", new File(".").getAbsolutePath());

			Tomcat.addServlet(ctx, "accumulate", notificationManager);

			ctx.addServletMappingDecoded(path, "accumulate");

			try {
				
				tomcat.start();
				tomcat.getServer().await();
				
			} catch (LifecycleException e) {
				e.printStackTrace();
			}
		});
	}
	
	public void stop() {
		
		try {
			
			executor.shutdownNow();
			tomcat.getServer().stop();
			tomcat.destroy();
			
		} catch (LifecycleException e) {
			e.printStackTrace();
		}
	}
}
