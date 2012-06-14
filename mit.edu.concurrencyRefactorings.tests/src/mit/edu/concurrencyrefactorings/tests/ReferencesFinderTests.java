package mit.edu.concurrencyrefactorings.tests;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import mit.edu.concurrencyrefactorings.refactorings.ConvertToAtomicIntegerRefactoring;
import mit.edu.concurrencyrefactorings.util.ReferencesFinder;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.structure.ASTNodeSearchUtil;
import org.eclipse.jdt.ui.tests.refactoring.infra.AbstractSelectionTestCase;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class ReferencesFinderTests extends AbstractSelectionTestCase {

	private static ReferencesFinderTestSetup fgTestSetup;
	
	public ReferencesFinderTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		fgTestSetup= new ReferencesFinderTestSetup(new TestSuite(ReferencesFinderTests.class));
		return fgTestSetup;
	}
	
	public static Test setUpTest(Test test) {
		fgTestSetup= new ReferencesFinderTestSetup(test);
		return fgTestSetup;
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		fIsPreDeltaTest= true;
	}

	protected String getResourceLocation() {
		return "ReferencesFinder/";
	}
	
	protected String adaptName(String name) {
		return Character.toUpperCase(name.charAt(0)) + name.substring(1) + ".java";
	}	
	
	protected void performTest(IPackageFragment packageFragment, String id, String fieldName, String methodName, int expectedReads, int expectedWrites) throws Exception {
		ICompilationUnit unit= createCU(packageFragment, id);
		
		initializePreferences();

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setProject(fgTestSetup.getProject());
		parser.setResolveBindings(true);
		parser.setSource(unit);
		
		CompilationUnit cuNode = (CompilationUnit) parser.createAST(null);
		MethodDeclaration methodDeclaration = getMethod(cuNode, methodName);
		ReferencesFinder finder = new ReferencesFinder();
		
		FieldDeclaration fieldDeclaration = ASTNodeSearchUtil.getFieldDeclarationNode(getField(unit, fieldName), cuNode);
		
		int startPosition = methodDeclaration.getStartPosition();
		int endPosition = startPosition + methodDeclaration.getLength();
		SimpleName nameToSearchFor = ((VariableDeclarationFragment)fieldDeclaration.fragments().get(0)).getName();
		finder.findReferences(methodDeclaration, nameToSearchFor, startPosition, endPosition);
		
		List<ASTNode> readReferences = finder.getReadReferences();
		List<ASTNode> writeReferences = finder.getWriteReferences();
		assertEquals(expectedWrites, writeReferences.size());
		assertEquals(expectedReads, readReferences.size());
	}
	
	private MethodDeclaration getMethod(CompilationUnit unit, String methodName) throws Exception {
		MethodDeclaration result= null;
		 List types = unit.types();
		 for (Object object : types) {
			AbstractTypeDeclaration type = (AbstractTypeDeclaration) object;
			if (type instanceof TypeDeclaration) {
				TypeDeclaration typeDecl = (TypeDeclaration) type;
				MethodDeclaration[] methodDeclarations = typeDecl.getMethods();
				for (MethodDeclaration methodDeclaration : methodDeclarations) {
					if (methodDeclaration.getName().getIdentifier().equals(methodName))
						return methodDeclaration;
				}
			}
		}
		return result;
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

	
	
	//=====================================================================================
	// Basic Object Test
	//=====================================================================================
	
	public void test1Read() throws Exception {
		performTest(fgTestSetup.getObjectPackage(), getName(), "i", "method", 1, 0);
	}
	
	public void test1ReadMethodInvocation() throws Exception {
		performTest(fgTestSetup.getObjectPackage(), getName(), "hm", "method", 1, 0);
	}
	
	public void test2Reads() throws Exception {
		performTest(fgTestSetup.getObjectPackage(), getName(), "i", "method", 2, 1);
	}
	
	public void testReadWriteInPrefix() throws Exception {
		performTest(fgTestSetup.getObjectPackage(), getName(), "i", "method", 1, 1);
	}
	
	public void test1Write() throws Exception {
		performTest(fgTestSetup.getObjectPackage(), getName(), "hm", "method", 0, 1);
	}
	
}
