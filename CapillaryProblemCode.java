import java.util.concurrent.atomic.AtomicReference;

public class Foo {
    private AtomicReference<State> state = new AtomicReference<>(State.CLOSED);

    private enum State {
        CLOSED,
        OPEN,
        PARTIALLY_OPEN
    }

    public void setState(State newState) {
        state.set(newState);
    }

    public boolean serviceRequest() {
        State currentState = state.get();
        switch (currentState) {
            case CLOSED:
                return true;
            case PARTIALLY_OPEN:
                if (state.compareAndSet(currentState, State.CLOSED)) {
                    return true;
                } else {
                    return false;
                }
            default:
                return false;
        }
    }
}

interface Service {
    boolean isHealthy();
    void serveRequest();
}

class ServiceImpl implements Service {
    private boolean healthy = true;

    public boolean isHealthy() {
        return healthy;
    }

    public void serveRequest() {
        healthy = !healthy;
    }
}

public class Driver {
    private static final int NUM_REQUESTS = 10;

    public static void main(String[] args) {
        Foo foo = new Foo();
        Service service = new ServiceImpl();

        for (int i = 0; i < NUM_REQUESTS; i++) {
            if (service.isHealthy()) {
                foo.setState(Foo.State.CLOSED);
            } else {
                foo.setState(Foo.State.OPEN);
            }

            if (foo.serviceRequest()) {
                service.serveRequest();
                System.out.println("Request served. Service state: " + (service.isHealthy() ? "Healthy" : "Unhealthy"));
                System.out.println("Foo state: " + foo.state.get());
            } else {
                System.out.println("Request rejected. Service state: " + (service.isHealthy() ? "Healthy" : "Unhealthy"));
                System.out.println("Foo state: " + foo.state.get());
            }
        }
    }
}
