package jp.w3ch.examples.bp.internal.processor;

import java.io.Writer;
import java.nio.channels.IllegalSelectorException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jp.w3ch.examples.bp.BlueprintInjections;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class OutputXml {
	
	private String activatorClass;
	private boolean hasDestroy;
	private boolean hasStart;
	private Map<String, BlueprintInjectionElement> injection;
	
	public void write(Writer out) {
		try {
			DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			
			Document root = b.newDocument();
			process(root);
	
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(root);
			StreamResult result = new StreamResult(out);
	 
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
	 
			transformer.transform(source, result);
		} catch (ParserConfigurationException | TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void process(Document root) {
		Element blueprint = root.createElement("blueprint");
		blueprint.setAttribute("xmlns", "http://www.osgi.org/xmlns/blueprint/v1.0.0");
		blueprint.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		blueprint.setAttribute("xsi:schemaLocation", ""
				+ "http://www.osgi.org/xmlns/blueprint/v1.0.0 http://www.osgi.org/xmlns/blueprint/v1.0.0/blueprint.xsd");

		root.appendChild(blueprint);
		blueprint.appendChild(root.createComment("hello"));
		
		Element activator = root.createElement("bean");
		activator.setAttribute("class", activatorClass);
		if (hasDestroy) activator.setAttribute("destroy-method", "stop");
		if (hasStart) activator.setAttribute("init-method", "start");
		blueprint.appendChild(activator);

		// create arguments to inject
		// <argument><bean><argument><map>...</map></argument></bean></argument>
		Node activatorArgument = root.createElement("argument");
		activator.appendChild(activatorArgument);
		Element injections = root.createElement("bean");
		injections.setAttribute("class", BlueprintInjections.class.getName());
		activatorArgument.appendChild(injections);
		Element injectionArg = root.createElement("argument");
		injections.appendChild(injectionArg);
		Element injectElementMap = root.createElement("map");
		injectionArg.appendChild(injectElementMap);
		for (Map.Entry<String, BlueprintInjectionElement> e : injection.entrySet()) {
			Element entry = root.createElement("entry");
			injectElementMap.appendChild(entry);
			// <key><value>fieldName</value></key>
			Element key = root.createElement("key");
			Element keyValue = root.createElement("value");
			key.appendChild(keyValue);
			keyValue.appendChild(root.createTextNode(e.getKey()));
			
			entry.appendChild(key);
			Element ref;
			BlueprintInjectionElement value = e.getValue();
			ref = getDependencyElem(root, value);
			entry.appendChild(ref);
		}
	}

	private Element getDependencyElem(Document root, BlueprintInjectionElement value) {
		if (value instanceof BlueprintInjectionElement.ServiceInjection) {
			Element ref = root.createElement("reference");
			ref.setAttribute("interface", ((BlueprintInjectionElement.ServiceInjection)value).serviceInterface);
			return ref;
		}
		if (value instanceof BlueprintInjectionElement.BundleContextInjection) {
			Element ref = root.createElement("ref");
			ref.setAttribute("component-id", "blueprintBundleContext");
			return ref;
		}
		throw new IllegalSelectorException();
	}

	public void activator(String className) {
		activatorClass = className;
	}

	public void hasStop() {
		hasDestroy = true;
	}

	public void hasStart() {
		hasStart = true;
	}

	public void setInjection(Map<String, BlueprintInjectionElement> fields) {
		injection = fields;
	}
}
