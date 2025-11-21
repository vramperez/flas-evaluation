package trials;

public class MockedContextBroker extends AbstractBroker{

	public MockedContextBroker() {

		super("adapter", "ADAPTER", 1027, "ADAPTER", 1027);
	}
	
	@Override
	public String toString() {
		return "SILBOPS_MOCKED_CB";
	}
}
