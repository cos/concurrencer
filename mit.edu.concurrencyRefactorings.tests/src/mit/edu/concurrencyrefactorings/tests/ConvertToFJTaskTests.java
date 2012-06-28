package mit.edu.concurrencyrefactorings.tests;

import junit.framework.Test;
import junit.framework.TestSuite;
import mit.edu.concurrencyrefactorings.refactorings.ConvertToFJTaskRefactoring;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.tests.refactoring.infra.AbstractSelectionTestCase;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class ConvertToFJTaskTests extends AbstractSelectionTestCase {

	private static ConvertToFJTaskTestSetup fgTestSetup;
	
	public ConvertToFJTaskTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		fgTestSetup= new ConvertToFJTaskTestSetup(new TestSuite(ConvertToFJTaskTests.class));
		return fgTestSetup;
	}
	
	public static Test setUpTest(Test test) {
		fgTestSetup= new ConvertToFJTaskTestSetup(test);
		return fgTestSetup;
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		fIsPreDeltaTest= false;  //TODO Why does this fail when true
	}

	protected String getResourceLocation() {
		return "ConvertToFJTask/";
	}
	
	protected String adaptName(String name) {
		return Character.toUpperCase(name.charAt(0)) + name.substring(1) + ".java";
	}	
	
	protected void performTest(IPackageFragment packageFragment, String id, String outputFolder, String methodName, String sequentialThresholdCheck) throws Exception {
		ICompilationUnit unit= createCU(packageFragment, id);
		IMethod method= getMethod(unit, methodName);
		assertNotNull(method);
		
		initializePreferences();

		ConvertToFJTaskRefactoring refactoring= new ConvertToFJTaskRefactoring(method);
		refactoring.setSequentialThreshold(sequentialThresholdCheck);
		performTest(unit, refactoring, COMPARE_WITH_OUTPUT, getProofedContent(outputFolder, id), true);
	}


	protected void performInvalidTest(IPackageFragment packageFragment, String id, String methodName) throws Exception {
		ICompilationUnit unit= createCU(packageFragment, id);
		IMethod method= getMethod(unit, methodName);
		assertNotNull(method);
		
		initializePreferences();

		ConvertToFJTaskRefactoring refactoring= new ConvertToFJTaskRefactoring(method);
		if (refactoring != null) {
			RefactoringStatus status= refactoring.checkAllConditions(new NullProgressMonitor());
			assertTrue(status.hasError());
		}
	}	
	
	private void initializePreferences() {
		Preferences preferences= JavaCore.getPlugin().getPluginPreferences();
		preferences.setValue(JavaCore.CODEASSIST_FIELD_PREFIXES, "");
		preferences.setValue(JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES, "");
		preferences.setValue(JavaCore.CODEASSIST_FIELD_SUFFIXES, "");
		preferences.setValue(JavaCore.CODEASSIST_STATIC_FIELD_SUFFIXES, "");
	}
	
	private IMethod getMethod(ICompilationUnit unit, String methodName) throws JavaModelException {
		IType[] types= unit.getAllTypes();
		for (int i= 0; i < types.length; i++) {
			IType type= types[i];
			IMethod[] methods = type.getMethods();
			for (IMethod method : methods) {
				if (method.getElementName().equals(methodName))
					return method;
			}
		}
		return null;
	}

	private void objectTest(String methodName, String sequentialThresholdCheck) throws Exception {
		performTest(fgTestSetup.getObjectPackage(), getName(), "object_out", methodName, sequentialThresholdCheck);
	}
	
//	private void baseTest(String methodName) throws Exception {
//		performTest(fgTestSetup.getBasePackage(), getName(), "base_out", methodName);
//	}
	
	private void invalidTest(String methodName) throws Exception {
		performInvalidTest(fgTestSetup.getInvalidPackage(), getName(), methodName);
	}
	
//	private void existingTest(String methodName) throws Exception {
//		performTest(fgTestSetup.getExistingMethodPackage(), getName(), "existingmethods_out", methodName);
//	}
	
	//=====================================================================================
	// Basic Object Test
	//=====================================================================================
	
	public void testCreateTypeDeclaration() throws Exception {
		objectTest("method", "array.length < 10");
	}
	
	public void testCreateResultField() throws Exception {
		objectTest("method", "array.length < 10");
	}
	
	public void testMaxConsecutiveSum() throws Exception {
		objectTest("maxSumRec", "right -left < 4");
	}
	
	public void testSequentialMergeSort() throws Exception {
		objectTest("sort", "whole.length < 10");
	}
	
	public void testQuickSort() throws Exception {
		objectTest("quicksort", "right - left < 10");
	}
	
//	public void testReimplementRecursiveMethod() throws Exception {
//		objectTest("method");
//	}
	
	public void testFibonacci() throws Exception {
		objectTest("fibonacci", "end < 10");
	}
	
	public void testFibonacciCombination() throws Exception {
		objectTest("fibonacciCombination", "end < 10");
	}
	
	public void testSum() throws Exception {
		objectTest("recursionSum", "end < 5");
	}
	
	public void testSumCombination() throws Exception {
		objectTest("recursionSumCombination", "end < 5");
	}
	
	public void testCreateMultipleTasks() throws Exception {
		objectTest("method", "num < 10");
	}
	
	public void testReturnMultipleTasks() throws Exception {
		objectTest("method", "num < 10");
	}
	
	public void testMethodMultipleTasks() throws Exception {
		objectTest("method", "num < 10");
	}
	
	public void testBaseCaseDoesNotHaveReturn() throws Exception {
		invalidTest("method");
	}
	
	public void testBaseCaseHasRecursiveCall() throws Exception {
		invalidTest("method");
	}
}
