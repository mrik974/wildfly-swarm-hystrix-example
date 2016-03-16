package org.wildfly.swarm.payroll.api;

public class Utils {
	public static String getSysPropOrEnvVar(String key, String difault) {
		String value = System.getProperty(key);
		
		if (value == null || value.trim().length() == 0) {
			value = System.getenv(key);
		}
		
		if (value == null || value.trim().length() == 0) {
			value = difault;
		} 
		
		return value;
	}
}
