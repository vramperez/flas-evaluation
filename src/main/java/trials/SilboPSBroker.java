package trials;

public class SilboPSBroker extends AbstractBroker {

	public SilboPSBroker() {
		super("adapter", "ADAPTER", 1027, "CONTEXT_BROKER1", 1024);
	}
	
	@Override
	public String toString() {
		return "SILBOPS";
	}
}
