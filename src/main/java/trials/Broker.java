package trials;

public enum Broker {

	CONTEXT_BROKER("orion", "CONTEXT_BROKER"), 
	SILBOPS("adapter", "ACCESS_POINT_0");

	private String service;
	private String containerName;

	private Broker(String service, String containerName) {

		this.service = service;
		this.containerName = containerName;
	}
}
