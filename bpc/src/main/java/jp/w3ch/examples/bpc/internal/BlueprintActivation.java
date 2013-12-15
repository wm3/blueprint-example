package jp.w3ch.examples.bpc.internal;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import jp.w3ch.examples.bp.BlueprintActivator;
import jp.w3ch.examples.bp.BlueprintInjections;
import jp.w3ch.examples.bp.Parent;
import jp.w3ch.examples.bp.Service;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@BlueprintActivator
public class BlueprintActivation {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Service
	private Parent p;
	@Service
	private BundleContext bundleContext;
	private Run run;
	private Thread thread;

	public BlueprintActivation(BlueprintInjections injection) {
		injection.inject(this);
	}
	
	public void start() {
		this.run = new Run();
		this.thread = new Thread(run);
		this.thread.start();
	}
	
	public void stop() throws InterruptedException {
		run.active.set(false);
		thread.join(1000);
	}
	
	private class Run implements Runnable {
		public AtomicBoolean active = new AtomicBoolean(true);
		
		public void run() {
			try {
				for (; active.get(); Thread.sleep(500)) {
					logger.info(Arrays.asList(p, bundleContext).toString());
				}
			} catch (InterruptedException e) {
				logger.warn("Interrupted", e);
			}
		}
	}
}
