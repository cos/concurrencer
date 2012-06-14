package mit.edu.concurrencyrefactorings.tests;

import junit.framework.Test;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.ui.tests.refactoring.infra.RefactoringTestSetup;

public class ReferencesFinderTestSetup extends RefactoringTestSetup {
	
	private IPackageFragment fBaseTypes;
	private IPackageFragment fObjectTypes;
	private IPackageFragment fInvalid;
	private IPackageFragment fStatic;
	private IPackageFragment fStaticRef;
	private IPackageFragment fExistingMethod;
	
	public ReferencesFinderTestSetup(Test test) {
		super(test);
	}	
	
	protected void setUp() throws Exception {
		super.setUp();

		IPackageFragmentRoot root= getDefaultSourceFolder();
		
		fBaseTypes= root.createPackageFragment("base_in", true, null);
		fObjectTypes= root.createPackageFragment("object_in", true, null);
		fInvalid= root.createPackageFragment("invalid", true, null);
		fStatic= root.createPackageFragment("static_in", true, null);
		fStaticRef= root.createPackageFragment("static_ref_in", true, null);
		fExistingMethod= root.createPackageFragment("existingmethods_in", true, null);
	}

	public IPackageFragment getBasePackage() {
		return fBaseTypes;
	}	

	public IPackageFragment getObjectPackage() {
		return fObjectTypes;
	}	

	public IPackageFragment getInvalidPackage() {
		return fInvalid;
	}
	
	public IPackageFragment getStaticPackage() {
		return fStatic;
	}
	
	public IPackageFragment getStaticRefPackage() {
		return fStaticRef;
	}
	
	public IPackageFragment getExistingMethodPackage(){
		return fExistingMethod;
	}
}