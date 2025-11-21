package trials;

import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class Prueba {
	
	public static void main(String [] args) {
		
		ObjectMapper mapper = new ObjectMapper();
		
		String url = "http://172.17.0.1:1028/";
		
		Client client = new Client();
		WebResource resource = client.resource(url);
		
		long start = System.nanoTime();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String payload = "";
				try {
					payload = mapper.writeValueAsString("{89:{value:633.3938182623399,type:number}}");
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for (int i = 0; i < 100_000; i++) {
					resource.type("application/json")
							.post(payload);
					System.out.println(i);
				}
			}
		}).start();
		
		long end = System.nanoTime();
		System.out.println("Time: " + TimeUnit.NANOSECONDS.toMillis(end-start));
	}
}