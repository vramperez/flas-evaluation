package trials;

public class ContextBroker extends AbstractBroker {
	
	public ContextBroker() {
		
		super("orion1", "CONTEXT_BROKER1", 1024, "CONTEXT_BROKER1", 1024);
	}
	
	@Override
	public String toString() {
		return "CONTEXT_BROKER";
	}
}