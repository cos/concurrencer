package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.String;

public class TestPreserveComments {
	
	private ConcurrentHashMap /*< String, ModuleConfig >*/ _registeredModules = new ConcurrentHashMap /*< String, ModuleConfig >*/();
	private static final NONEXISTANT_MODULE_CONFIG = "";

	public String getModuleConfig(String namespace) {
		assert namespace != null;
		// Before_pattern
		// Right before pattern
		
		if(_registeredModules.putIfAbsent(namespace, NONEXISTANT_MODULE_CONFIG) != null) {
			// Before_put
			// After_put
		}
		// Right after pattern
		// After_pattern
		
		return mc;
	}
}