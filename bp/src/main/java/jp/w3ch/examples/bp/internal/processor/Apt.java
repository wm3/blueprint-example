package jp.w3ch.examples.bp.internal.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import jp.w3ch.examples.bp.BlueprintActivator;
import jp.w3ch.examples.bp.Service;

import org.osgi.framework.BundleContext;


@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("jp.w3ch.examples.bp.BlueprintActivator")
public class Apt extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		writex(annotations, roundEnv);
		return true;
	}

	private void writex(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		try {
			OutputXml outputXml = new OutputXml();
			for (Element element : roundEnv.getElementsAnnotatedWith(BlueprintActivator.class)) {
				TypeElement activator = (TypeElement)element;
				outputXml.activator(className(activator));
				
				Map<String, BlueprintInjectionElement> fields = new LinkedHashMap<>();
				for (VariableElement f : fields(activator)) {
					if (f.getAnnotation(Service.class) != null) {
						String className = className(typeOfParameter(f));
						BlueprintInjectionElement inject;
						if (className.equals(BundleContext.class.getName())) {
							inject = new BlueprintInjectionElement.BundleContextInjection();
						} else {
							inject = new BlueprintInjectionElement.ServiceInjection(className);
						}
						fields.put(f.getSimpleName().toString(), inject);
					}
				}
				outputXml.setInjection(fields);
				List<ExecutableElement> methods = methods(activator);
				if (hasMethodNamed("start", methods)) {
					outputXml.hasStart();
				}
				if (hasMethodNamed("stop", methods)) {
					outputXml.hasStop();
				}
				warn("XYZ: " + activator.toString());
				
				FileObject res = processingEnv.getFiler().createResource(
						StandardLocation.CLASS_OUTPUT,
						"",
						"OSGI-INF/blueprint/context.xml"
				);
				try (PrintWriter x = new PrintWriter(res.openWriter())) {
					outputXml.write(x);
				}
			}
			
		} catch (IOException ex) {
			processingEnv.getMessager().printMessage(Kind.ERROR, ex.toString());
		}
	}

	private boolean hasMethodNamed(String name, List<ExecutableElement> methods) {
		for (ExecutableElement m : methods) {
			warn(m.getSimpleName().toString());
			if (m.getSimpleName().toString().equals(name)) {
				return true;
			}
		}
		return false;
	}

	private List<ExecutableElement> methods(TypeElement activator) {
		return ElementFilter.methodsIn(processingEnv.getElementUtils().getAllMembers(activator));
	}

	private List<VariableElement> fields(TypeElement activator) {
		return ElementFilter.fieldsIn(processingEnv.getElementUtils().getAllMembers(activator));
	}

	private TypeElement typeOfParameter(VariableElement p) {
		return (TypeElement)processingEnv.getTypeUtils().asElement(p.asType());
	}

	private String className(TypeElement ptype) {
		String qualifiedName = ptype.getQualifiedName().toString();
		return qualifiedName;
	}

	private ExecutableElement constructor(TypeElement element) {
		ExecutableElement constructor = null;
		List<? extends Element> allMembers = processingEnv.getElementUtils().getAllMembers(element);
		for (Element member : ElementFilter.constructorsIn(allMembers)) {
			warn("E: " + element.toString());
			if (member instanceof ExecutableElement) {
				ExecutableElement ex = (ExecutableElement)member;
				if (ex.getKind().equals(ElementKind.CONSTRUCTOR)) {
					if (constructor != null) throw new IllegalStateException("MULTIPLE CONSTRUCTOR");
					constructor = ex;
				}
			}
		}
		
		if (constructor == null) throw new IllegalStateException("CONSTRUCTOR NOTFOUND");
		return constructor;
	}

	private void warn(String warn) {
		System.err.println(warn);
//		processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, warn);
	}

}
