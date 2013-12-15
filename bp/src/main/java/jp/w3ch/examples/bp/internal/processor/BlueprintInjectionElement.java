package jp.w3ch.examples.bp.internal.processor;

public abstract class BlueprintInjectionElement {
	
	public static class BundleContextInjection extends BlueprintInjectionElement {
	}
	
	public static class ServiceInjection extends BlueprintInjectionElement {
		public final String serviceInterface;

		public ServiceInjection(String serviceInterface) {
			this.serviceInterface = serviceInterface;
		}
	}
}
