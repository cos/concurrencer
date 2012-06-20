package mit.edu.concurrencyrefactorings.refactorings;

import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 */
public final class ConcurrencyRefactorings extends NLS {

	private static final String BUNDLE_NAME= ConcurrencyRefactorings.class.getName();

	private ConcurrencyRefactorings() {
		// Do not instantiate
	}
	
	public static String ConcurrencyRefactorings_empty_string;
	
	public static String ConvertToFJTaskRefactoring_check_preconditions;
	public static String ConvertToFJTaskRefactoring_name;
	public static String ConvertToFJTaskRefactoring_recursive_method;
	public static String ConvertToFJTaskRefactoring_recursive_action;
	public static String ConvertToFJTaskRefactoring_generate_compute;
	public static String ConvertToFJTaskRefactoring_recursion_error_1;
	public static String ConvertToFJTaskRefactoring_recursion_error_2;
	public static String ConvertToFJTaskRefactoring_scenario_error;
	public static String ConvertToFJTaskRefactoring_update_imports;
	public static String ConvertToFJTaskRefactoring_type_error;
	public static String ConvertToFJTaskRefactoring_analyze_error;
	public static String ConvertToFJTaskRefactoring_compile_error;
	public static String ConvertToFJTaskRefactoring_compile_error_update;
	public static String ConvertToFJTaskRefactoring_name_user;
	public static String ConvertToFJTaskRefactoring_create_changes;
	public static String ConvertToFJTaskRefactoring_name_official;
	public static String ConvertToFJTaskRefactoring_sequential_req;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, ConcurrencyRefactorings.class);
	}
}