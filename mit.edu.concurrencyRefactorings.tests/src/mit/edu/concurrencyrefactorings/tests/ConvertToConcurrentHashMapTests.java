package mit.edu.concurrencyrefactorings.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import mit.edu.concurrencyrefactorings.refactorings.ConvertToConcurrentHashMapRefactoring;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.ui.tests.refactoring.infra.AbstractSelectionTestCase;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class ConvertToConcurrentHashMapTests extends AbstractSelectionTestCase {

	private static ConvertToConcurrentHashMapTestSetup fgTestSetup;
	
	public ConvertToConcurrentHashMapTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		fgTestSetup= new ConvertToConcurrentHashMapTestSetup(new TestSuite(ConvertToConcurrentHashMapTests.class));
		return fgTestSetup;
	}
	
	public static Test setUpTest(Test test) {
		fgTestSetup= new ConvertToConcurrentHashMapTestSetup(test);
		return fgTestSetup;
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		fIsPreDeltaTest= true;
	}

	protected String getResourceLocation() {
		return "ConvertToConcurrentHashMap/";
	}
	
	protected String adaptName(String name) {
		return Character.toUpperCase(name.charAt(0)) + name.substring(1) + ".java";
	}	
	
	protected void performTest(IPackageFragment packageFragment, String id, String outputFolder, String fieldName) throws Exception {
		ICompilationUnit unit= createCU(packageFragment, id);
		IField field= getField(unit, fieldName);
		assertNotNull(field);
		
		initializePreferences();

		ConvertToConcurrentHashMapRefactoring refactoring= new ConvertToConcurrentHashMapRefactoring(field);
		performTest(unit, refactoring, COMPARE_WITH_OUTPUT, getProofedContent(outputFolder, id), true);
	}

	protected void performInvalidTest(IPackageFragment packageFragment, String id, String fieldName) throws Exception {
		ICompilationUnit unit= createCU(packageFragment, id);
		IField field= getField(unit, fieldName);
		assertNotNull(field);

		initializePreferences();

		ConvertToConcurrentHashMapRefactoring refactoring= new ConvertToConcurrentHashMapRefactoring(field);
		if (refactoring != null) {
			RefactoringStatus status= refactoring.checkAllConditions(new NullProgressMonitor());
			assertTrue("should haves raised error message, since precondition is not met", status.hasError());
		}
		//assertTrue(refactoring!=null);
		//performTest(unit, refactoring, INVALID_SELECTION, null, true);
	}	
	
	private void initializePreferences() {
		Preferences preferences= JavaCore.getPlugin().getPluginPreferences();
		preferences.setValue(JavaCore.CODEASSIST_FIELD_PREFIXES, "");
		preferences.setValue(JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES, "");
		preferences.setValue(JavaCore.CODEASSIST_FIELD_SUFFIXES, "");
		preferences.setValue(JavaCore.CODEASSIST_STATIC_FIELD_SUFFIXES, "");
	}
	
	private static IField getField(ICompilationUnit unit, String fieldName) throws Exception {
		IField result= null;
		IType[] types= unit.getAllTypes();
		for (int i= 0; i < types.length; i++) {
			IType type= types[i];
			result= type.getField(fieldName);
			if (result != null && result.exists())
				break;
		}
		return result;
	}

	private void objectTest(String fieldName) throws Exception {
		performTest(fgTestSetup.getObjectPackage(), getName(), "object_out", fieldName);
	}
	
	private void baseTest(String fieldName) throws Exception {
		performTest(fgTestSetup.getBasePackage(), getName(), "base_out", fieldName);
	}
	
	private void invalidTest(String fieldName) throws Exception {
		performInvalidTest(fgTestSetup.getInvalidPackage(), getName(), fieldName);
	}
	
	private void existingTest(String fieldName) throws Exception {
		performTest(fgTestSetup.getExistingMethodPackage(), getName(), "existingmethods_out", fieldName);
	}
	
	//=====================================================================================
	// Basic Object Test
	//=====================================================================================
	
	public void testCreateValueMethodNameWithOneArgument() throws Exception {
		objectTest("map2");
	}
	
	public void testCreateValueMethodNameWithTwoArguments() throws Exception {
		objectTest("map2");
	}
	
	public void testDeclaredCreateValue() throws Exception {
		objectTest("map");
	}
	
	public void testCommonMethods() throws Exception {
		objectTest("hm");
	}
	
	public void testPrivateModifier() throws Exception {
		objectTest("hm");
	}
	
	public void testParameterizedForm1() throws Exception {
		objectTest("hm");
	}
	
	public void testParameterizedForm2() throws Exception {
		objectTest("hm");
	}
	
	public void testParameterizedForm3() throws Exception {
		objectTest("hm");
	}
	
	public void testParameterizedForm4() throws Exception {
		objectTest("hm");
	}
	
	public void testRemoveSyncBlockWithSameLock() throws Exception {
		objectTest("hm");
	}
	
	public void testRemoveSyncBlockWithThisLock() throws Exception {
		objectTest("hm");
	}
	
	public void testRemoveSyncMethodModifier() throws Exception {
		objectTest("hm");
	}
	
	public void testNotRemoveSyncBlockWithSameLock() throws Exception {
		objectTest("hm");
	}
	
	public void testNotRemoveSyncBlockWithThisLock() throws Exception {
		objectTest("hm");
	}
	
	public void testNotRemoveSyncMethodModifier() throws Exception {
		objectTest("hm");
	}
	
	public void testMultipleFields() throws Exception {
		objectTest("hm");
	}
	
	public void testDeclarationTypeMap() throws Exception {
		objectTest("hm");
	}
	
	// Tests for putIfAbsent forms --------------------------------------------------------
	
	public void testPutIfAbsentFormUsingGet() throws Exception {
		objectTest("hm");
	}
	
	public void testPutIfAbsentFormUsingGetWithSyncBlock() throws Exception {
		objectTest("hm");
	}
	
	public void testPutIfAbsentFormUsingGetWithSyncMethod() throws Exception {
		objectTest("hm");
	}
	
	public void testPutIfAbsentFormUsingContainsKey() throws Exception {
		objectTest("hm");
	}
	
	public void testPutIfAbsentFormUsingContainsKeyWithSyncBlock() throws Exception {
		objectTest("hm");
	}
	
	public void testPutIfAbsentFormUsingContainsKeyWithSyncMethod() throws Exception {
		objectTest("hm");
	}
	
	public void testPutIfAbsentFormUsingContainsKey_WithCreateValue () throws Exception {
		objectTest("map");
	} 
	
// We decided this form for putIfAbsent is an uncommon case, so we're not testing for it.
//
//	public void testPutIfAbsentForm3() throws Exception {
//		objectTest("hm");
//	}
//	
//	public void testPutIfAbsentForm3WithSyncBlock() throws Exception {
//		objectTest("hm");
//	}
//	
//	public void testPutIfAbsentForm3WithSyncMethod() throws Exception {
//		objectTest("hm");
//	}
	
	
	// Tests for replace forms --------------------------------------------------------
	
	public void testReplaceFormWithBooleanReturn() throws Exception {
		objectTest("hm");
	}
	
	public void testReplaceFormWithValueTypeReturn() throws Exception {
		objectTest("hm");
	}
	
	
	// Tests for remove forms --------------------------------------------------------

	public void testRemoveFormWithBooleanReturn() throws Exception {
	objectTest("hm");
}
	
	public void testRemoveFormWithValueTypeReturn() throws Exception {
		objectTest("hm");
	}
	
	
	// Miscellaneous tests --------------------------------------------------------

	// TODO Requires analysis. May or may not decide to implement.  
//	public void testReturnType() throws Exception {
//		objectTest("hm");
//	}
	
	public void testInitializeInConstructor() throws Exception {
		objectTest("hm");
	}
	
	public void testInitializeFieldAccesses() throws Exception {
		objectTest("hm");
	}
	
	public void testSuperFieldAccess() throws Exception {
		objectTest("hm");
	}
	
	public void testParenthesizedExpressionUnraveling() throws Exception {
		objectTest("hm");
	}
	
	public void testStatementsBeforeAndAfterPut_AfterDoesNotUseCreateValue() throws Exception {
		objectTest("hm");
	}
	
	public void testStatementsBeforeAndAfterPut_AfterUsesCreateValue() throws Exception {
		objectTest("hm");
	}
	
	public void testStatementsBeforePutWithCreateValue() throws Exception {
		objectTest("hm");
	}
	
	public void testStatementsBeforePut_NoCreateValue() throws Exception {
		objectTest("hm");
	}
	
	public void testStatementsAfterPut() throws Exception {
		objectTest("hm");
	}
	
	public void testStatementsBeforePut_NeverUsed() throws Exception {
		objectTest("hm");
	}
	
	public void testCastingOnGet() throws Exception {
		objectTest("_classCaches");
	}
	
	public void testPreserveInitialization() throws Exception {
		objectTest("appScope");
	}
	
	public void testLeaveAfterPattern() throws Exception {
		objectTest("_classCaches");
	}
	
	public void testMapInitialization() throws Exception {
		objectTest("appScope");
	}
	
//	public void testPreserveComments() throws Exception {
//		objectTest("_registeredModules");
//	}
	
//	public void testMultipleIfConditions() throws Exception {
//		objectTest("_registeredModules");
//	}
//	
	public void testDifferentCreateValueLengths() throws Exception {
		objectTest("_classCaches");
	}
	
	public void testAfterIfUsesCreatedValue() throws Exception {
		objectTest("appScope");
	}
	
	public void testPreserveMap() throws Exception {
		objectTest("appScope");
	}
	
	public void testPreserveFormAndOrdering() throws Exception {
		objectTest("_classCaches");
	}
	
	//------------------------- Cases below do not meet preconditions - therefore refactoring should not proceed
	
	public void testCannotRefactorCloneMethod() throws Exception {
		invalidTest("hm");
	}
	

	public void testSideEffectsFieldAssignment() throws Exception {
		invalidTest("hm");
	}

	// TODO
	/*public void testFailDueToCreateValueSideEffects() throws Exception {
		invalidTest("hm");
	}*/
	
}
