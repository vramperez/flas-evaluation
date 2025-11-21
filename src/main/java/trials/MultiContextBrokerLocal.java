package trials;

import java.io.IOException;

public class MultiContextBrokerLocal extends AbstractBroker {

	public MultiContextBrokerLocal() {

		super("haproxy", "LOAD_BALANCER", 1026, "LOAD_BALANCER", 1026);
	}
	
	@Override
	public String toString() {
		return "MULTI_CONTEXT_BROKER";
	}
}