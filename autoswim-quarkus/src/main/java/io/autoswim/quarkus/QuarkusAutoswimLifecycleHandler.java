package io.autoswim.quarkus;

import io.autoswim.runtime.AutoswimRuntime;
import io.quarkus.runtime.ShutdownEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class QuarkusAutoswimLifecycleHandler {
	
	void onStop(@Observes ShutdownEvent ev, AutoswimRuntime runtime) {
		runtime.stop();
	}
}
