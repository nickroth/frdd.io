package io.frdd.blogs.mustache.codegen;

import java.io.File;
import java.util.Locale;

/**
 * WTF does this class do?
 */
public abstract class OverrideMe {

	// Methods that can't be overridden.
	public static String aStaticMethod() {
		return "Can't override me. I'm static.";
	}
	
	private void privateMethod() {}
	
	public final String finaMethod() {
		return "That's my final answer Regis";
	}
	
	//Methods we can override
	abstract File myAbstractMethod();
	
	protected void takeInStrings(String s1, String s2, String s3) {}
	
	public abstract Locale getLocale(File file);
}
