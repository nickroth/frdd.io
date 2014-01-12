package io.frdd.blogs.mustache.codegen;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;


public class App {
	
	public static class MustacheMethod {
		
		private String visibility;
		private String name;
		private String returnType;
		private List<String> arguments = new LinkedList<String>();
		
		public MustacheMethod(String vis, String name, String returnType) {
			this.visibility = vis;
			this.name = name;
			this.returnType = returnType;
		}
		
		public void addArgument(String argType) {
			String argName = " arg"+arguments.size();
			arguments.add(argType+argName);
		}

		public String visibility() {
			return visibility;
		}

		public String name() {
			return name;
		}

		public String returnType() {
			return returnType;
		}

		public String arguments() {
			if (arguments.isEmpty()) { return null;}
			String argsString = arguments.toString();
			return argsString.substring(1, argsString.length()-1);
		}
	}
	
	public static class Import {
		
		private String packageToImport;
		
		public Import(String packageToImport) {
			this.packageToImport = packageToImport;
		}
		
		public String packageToImport() {
			return this.packageToImport;
		}
		
		@Override public boolean equals(Object other) {
			return (other instanceof Import) && (packageToImport.equals(((Import)other).packageToImport));
		}
		
		@Override public int hashCode() {
			return packageToImport.hashCode();
		}
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {

		// Load up the the parent class.
		Class<?> inheritedType = Class.forName("io.frdd.blogs.mustache.codegen.OverrideMe");
		
		// Initialize the set of Imports and Methods
		Set<Import> imports = new HashSet<Import>();
		imports.add(new Import(inheritedType.getName()));
		
		List<MustacheMethod> methods = new LinkedList<App.MustacheMethod>();

		// Create the top level scope for the template
		Map<String, Object> scope = new HashMap<String, Object>();
		scope.put("package", "io.frdd");
		
		if (Modifier.isInterface(inheritedType.getModifiers())) {
			scope.put("inheritenceType", "implements");			
		} else {
			scope.put("inheritenceType", "extends");
		}
		
		scope.put("superType", inheritedType.getSimpleName());

		Method[] parentMethods = inheritedType.getDeclaredMethods();
		for(Method m : parentMethods) {
			// We're not interested in overriding methods inherited from Object.
			if(isObjectMethod(m)) {continue;}
			
			// Check for methods we can't override.
			if (!canOverride(m)) {continue;}

			String vis = getVisibility(m);
			String name = m.getName();
			String returnType = (m.getReturnType().equals(Void.TYPE)) ? null : m.getReturnType().getSimpleName();
			MustacheMethod method = new MustacheMethod(vis, name, returnType);
			
			if (returnType != null) imports.add(new Import(m.getName()));
			
			Class<?>[] paramTypes = m.getParameterTypes();
			for (Class<?> paramType : paramTypes) {
				method.addArgument(paramType.getSimpleName());
				imports.add(new Import(paramType.getName()));
			}
			methods.add(method);
		}

		scope.put("methods", methods);
		scope.put("imports", imports);
		
		DefaultMustacheFactory mf = new DefaultMustacheFactory();
		Mustache template = mf.compile("LoggingImpl.mustache");
		
		template.execute(new PrintWriter(System.out), scope).flush();
	}
	
	private static String getVisibility(Method m) {
		int mod = m.getModifiers();
		if (Modifier.isPublic(mod)) return "public";
		if (Modifier.isProtected(mod)) return "protected";
		if (Modifier.isPrivate(mod)) return "private";
		return ""; //Default visibility
	}

	/**
	 * Determines if the passed method could be overridden by a subclass. Static, private and final
	 * methods can not be overridden.
	 * @param m The Method to test
	 * @return true iff the passed method can be overridden by a subclass, false otherwise.
	 */
	private static boolean canOverride(Method m) {
		int mod = m.getModifiers();
		return !(Modifier.isStatic(mod) || Modifier.isPrivate(mod) || Modifier.isFinal(mod));
	}

	// Create a Set of Methods that all objects inherit from Object 
	private static final Set<Method> OBJECT_METHODS = new HashSet<Method>(Arrays.asList(Object.class.getMethods()));
	
	/**
	 * Determines if the passed Method is one that is inherited from the Object class.
	 * @param m The Method to test
	 * @return true iff the passed Method is one that is inherited from the Object class, false otherwise.
	 */
	private static boolean isObjectMethod(Method m) {
		return OBJECT_METHODS.contains(m);
	}
}
