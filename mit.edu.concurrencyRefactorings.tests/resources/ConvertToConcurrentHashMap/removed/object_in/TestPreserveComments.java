package object_in;

import java.util.HashMap;
import java.lang.String;

public class TestPreserveComments {
	
	private HashMap /*< String, ModuleConfig >*/ _registeredModules = new HashMap /*< String, ModuleConfig >*/();
	private static final NONEXISTANT_MODULE_CONFIG = "";

	public String getModuleConfig(String namespace) {
		assert namespace != null;

		// Before_pattern
		String mc = (String) _registeredModules.get(namespace);

		// Right before pattern
		if (mc == null) {
			// Before_put
			_registeredModules.put(namespace, NONEXISTANT_MODULE_CONFIG);
			// After_put
		}
		// Right after pattern
		// After_pattern
		return mc;
	}
}