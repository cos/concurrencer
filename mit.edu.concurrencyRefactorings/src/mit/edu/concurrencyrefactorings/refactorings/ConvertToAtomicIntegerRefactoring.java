package mit.edu.concurrencyrefactorings.refactorings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.ModifierRewrite;
import org.eclipse.jdt.internal.corext.dom.NodeFinder;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringScopeFactory;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringSearchEngine;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.TextChangeCompatibility;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.corext.refactoring.util.ResourceUtil;
import org.eclipse.jdt.internal.corext.refactoring.util.TextChangeManager;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

public class ConvertToAtomicIntegerRefactoring extends Refactoring {

	private static final String NO_NAME = "";
	private IField fField;
	private CompilationUnit fRoot;
	private VariableDeclarationFragment fFieldDeclarationFragment;
	private ASTRewrite fRewriter;
	private TextChangeManager fChangeManager;
	private ImportRewrite fImportRewrite;
	private boolean initializeDeclaration;
	private static final String LINKED_NAME= "name"; //$NON-NLS-1$

	public ConvertToAtomicIntegerRefactoring(IField field){
		fChangeManager= new TextChangeManager();
		fField= field;
		initializeDeclaration = true;
	}
	
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		
		RefactoringStatus result= new RefactoringStatus();
		fChangeManager.clear();
		pm.beginTask("", 12);
		pm.setTaskName("Convert to AtomicInteger checking preconditions");
		pm.worked(1);
		if (result.hasFatalError())
			return result;
		pm.setTaskName("ConvertToAtomicInteger searching for cunits"); 
		final SubProgressMonitor subPm= new SubProgressMonitor(pm, 5);
		ICompilationUnit[] affectedCUs= RefactoringSearchEngine.findAffectedCompilationUnits(
			SearchPattern.createPattern(fField, IJavaSearchConstants.ALL_OCCURRENCES),
			RefactoringScopeFactory.create(fField, true),
			subPm,
			result, true);
		
		if (result.hasFatalError())
			return result;
			
		pm.setTaskName("Analyzing the field");	 
		IProgressMonitor sub= new SubProgressMonitor(pm, 5);
		sub.beginTask("", affectedCUs.length);
		IVariableBinding fieldIdentifier= fFieldDeclarationFragment.resolveBinding();
		ITypeBinding declaringClass= 
			((AbstractTypeDeclaration)ASTNodes.getParent(fFieldDeclarationFragment, AbstractTypeDeclaration.class)).resolveBinding();
		List ownerDescriptions= new ArrayList();
		ICompilationUnit owner= fField.getCompilationUnit();
		
		fImportRewrite= StubUtility.createImportRewrite(fRoot, true);
		
		for (int i= 0; i < affectedCUs.length; i++) {
			ICompilationUnit unit= affectedCUs[i];
			sub.subTask(unit.getElementName());
			CompilationUnit root= null;
			ASTRewrite rewriter= null;
			ImportRewrite importRewrite;
			List descriptions;
			if (owner.equals(unit)) {
				root= fRoot;
				rewriter= fRewriter;
				importRewrite= fImportRewrite;
				descriptions= ownerDescriptions;
			} else {
				root= new RefactoringASTParser(AST.JLS3).parse(unit, true);
				rewriter= ASTRewrite.create(root.getAST());
				descriptions= new ArrayList();
				importRewrite= StubUtility.createImportRewrite(root, true);
			}
			checkCompileErrors(result, root, unit);
			AccessAnalyzerForAtomicInteger analyzer= new AccessAnalyzerForAtomicInteger(this, unit, fieldIdentifier, declaringClass, rewriter, importRewrite);
			root.accept(analyzer);
			result.merge(analyzer.getStatus());
			if (result.hasFatalError()) {
				fChangeManager.clear();
				return result;
			}
			descriptions.addAll(analyzer.getGroupDescriptions());
			if (!owner.equals(unit))
				createEdits(unit, rewriter, descriptions, importRewrite);
			sub.worked(1);
			if (pm.isCanceled())
				throw new OperationCanceledException();
		}
		
		ownerDescriptions.addAll(addChangeDeclaringType(fRoot));
		
		createEdits(owner, fRewriter, ownerDescriptions, fImportRewrite);
		
		sub.done();
		IFile[] filesToBeModified= ResourceUtil.getFiles(fChangeManager.getAllCompilationUnits());
		result.merge(Checks.validateModifiesFiles(filesToBeModified, getValidationContext()));
		if (result.hasFatalError())
			return result;
		ResourceChangeChecker.checkFilesToBeChanged(filesToBeModified, new SubProgressMonitor(pm, 1));
		return result;
		
	}
	
	private Collection addChangeDeclaringType(CompilationUnit root) {
		
		Type newType= null;
		newType= root.getAST().newSimpleType(ASTNodeFactory.newName(root.getAST(), "AtomicInteger"));
		
		FieldDeclaration oldFieldDeclaration = (FieldDeclaration)ASTNodes.getParent(fFieldDeclarationFragment, FieldDeclaration.class);
		ASTNode typeToReplace = oldFieldDeclaration.getType();
		TextEditGroup gd = new TextEditGroup("ChangeType");
		
		List fragments = oldFieldDeclaration.fragments();
		
		AST ast = root.getAST();
		VariableDeclarationFragment newVariableDeclarationFragment = ast.newVariableDeclarationFragment();
		SimpleName newSimpleName = ast.newSimpleName(fField.getElementName());
		newVariableDeclarationFragment.setName(newSimpleName);
		
		if (initializeDeclaration) {
			Expression initializer = fFieldDeclarationFragment.getInitializer();
			ClassInstanceCreation atomicInstanceCreation = root.getAST().newClassInstanceCreation();;
			atomicInstanceCreation.setType(root.getAST().newSimpleType(ASTNodeFactory.newName(root.getAST(), "AtomicInteger")));
			if (initializer != null) {
				atomicInstanceCreation.arguments().add(fRewriter.createCopyTarget(initializer));
			}
			newVariableDeclarationFragment.setInitializer((Expression) atomicInstanceCreation);
		}

		FieldDeclaration newFieldDeclaration = ast.newFieldDeclaration(newVariableDeclarationFragment);
		newFieldDeclaration.setType(newType);
		
		ModifierRewrite.create(fRewriter, newFieldDeclaration).copyAllModifiers(oldFieldDeclaration, gd);
		
		if (fragments.size() > 1) {

			fRewriter.remove(fFieldDeclarationFragment, gd);
			
			ChildListPropertyDescriptor descriptor= getBodyDeclarationsProperty(oldFieldDeclaration.getParent());
	
			
			fRewriter.getListRewrite(oldFieldDeclaration.getParent(), descriptor).insertAfter(newFieldDeclaration, oldFieldDeclaration, gd);
		} else {
			// case of only 1 declaration fragment
			fRewriter.replace(oldFieldDeclaration, newFieldDeclaration, gd);
		}
		
		ArrayList<TextEditGroup> group = new ArrayList<TextEditGroup>();
		group.add(gd);
		return group;
	}

	private ChildListPropertyDescriptor getBodyDeclarationsProperty(ASTNode declaration) {
		if (declaration instanceof AnonymousClassDeclaration)
			return AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY;
		else if (declaration instanceof AbstractTypeDeclaration)
			return ((AbstractTypeDeclaration) declaration).getBodyDeclarationsProperty();
		Assert.isTrue(false);
		return null;
	}
	
	private void createEdits(ICompilationUnit unit, ASTRewrite rewriter, List groups, ImportRewrite importRewrite) throws CoreException {
		TextChange change= fChangeManager.get(unit);
		MultiTextEdit root= new MultiTextEdit();
		change.setEdit(root);
		
		TextEdit importEdit = importRewrite.rewriteImports(null);
		TextChangeCompatibility.addTextEdit(fChangeManager.get(unit), "Update Imports", importEdit);
		
		root.addChild(rewriter.rewriteAST());
		for (Iterator iter= groups.iterator(); iter.hasNext();) {
			change.addTextEditGroup((TextEditGroup)iter.next());
		}
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus result=  new RefactoringStatus();
		result.merge(Checks.checkAvailability(fField));
		
		if (result.hasFatalError())
			return result;
		
		fRoot= new RefactoringASTParser(AST.JLS3).parse(fField.getCompilationUnit(), true, pm);
		ISourceRange sourceRange= fField.getNameRange();
		ASTNode node= NodeFinder.perform(fRoot, sourceRange.getOffset(), sourceRange.getLength());
		if (node == null) {
			return mappingErrorFound(result, node);
		}
		fFieldDeclarationFragment= (VariableDeclarationFragment)ASTNodes.getParent(node, VariableDeclarationFragment.class);
		if (fFieldDeclarationFragment == null) {
			return mappingErrorFound(result, node);
		}
		if (fFieldDeclarationFragment.resolveBinding() == null) {
			if (!processCompilerError(result, node))
				result.addFatalError("type not resolveable"); 
			return result;
		}
		fRewriter= ASTRewrite.create(fRoot.getAST());
		return result;
	}
	
	private RefactoringStatus mappingErrorFound(RefactoringStatus result, ASTNode node) {
		if (node != null && (node.getFlags() & ASTNode.MALFORMED) != 0 && processCompilerError(result, node))
			return result;
		result.addFatalError(getMappingErrorMessage());
		return result;
	}
	
	private String getMappingErrorMessage() {
		return Messages.format(
			"Convert to AtomicInteger cannot analyze selected field", 
			new String[] {fField.getElementName()});
	}

	private boolean processCompilerError(RefactoringStatus result, ASTNode node) {
		Message[] messages= ASTNodes.getMessages(node, ASTNodes.INCLUDE_ALL_PARENTS);
		if (messages.length == 0)
			return false;
		result.addFatalError(Messages.format(
			"Compiler errors with the field to be refactored",  
			new String[] { fField.getElementName(), messages[0].getMessage()}));
		return true;
	}
	
	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		String project= null;
		IJavaProject javaProject= fField.getJavaProject();
		if (javaProject != null)
			project= javaProject.getElementName();
		int flags= JavaRefactoringDescriptor.JAR_MIGRATION | JavaRefactoringDescriptor.JAR_REFACTORING | RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE;
		final IType declaring= fField.getDeclaringType();
		try {
			if (declaring.isAnonymous() || declaring.isLocal())
				flags|= JavaRefactoringDescriptor.JAR_SOURCE_ATTACHMENT;
		} catch (JavaModelException exception) {
			JavaPlugin.log(exception);
		}

		//TODO need to properly initialize the arguments so that this refactoring becomes recordable
		final Map arguments= new HashMap();
		String description = "Convert int to AtomicInteger";
		String comment = "Convert int to AtomicInteger";
		
		final JavaRefactoringDescriptor descriptor= new JavaRefactoringDescriptor(IJavaRefactorings.ENCAPSULATE_FIELD, project, description, comment, arguments, flags) {};
		
		//JDTRefactoringDescriptor(IJavaRefactorings.ENCAPSULATE_FIELD, project, description, comment, arguments, flags);
		
		final DynamicValidationRefactoringChange result= new DynamicValidationRefactoringChange(descriptor, getName());
		TextChange[] changes= fChangeManager.getAllChanges();
		pm.beginTask(NO_NAME, changes.length);
		pm.setTaskName("ConvertToAtomicInteger create changes");
		for (int i= 0; i < changes.length; i++) {
			result.add(changes[i]);
			pm.worked(1);
		}
		pm.done();
		return result;
	}

	@Override
	public String getName() {
		return "Convert to AtomicInteger";
	}

	public void setField(IField field) {
		this.fField = field;
	}

	public RefactoringStatus setFieldName(String text) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public RefactoringStatus setInitializeDeclaration(boolean initializeDeclaration) {
		this.initializeDeclaration = initializeDeclaration;
		return new RefactoringStatus();
	}

	public IField getField() {
		return fField;
	}

	public String getFieldName() {
		return fField.getElementName();
	}

	
	private boolean isIgnorableProblem(IProblem problem) {
		if (problem.getID() == IProblem.NotVisibleField)
			return true;
		return false;
	}
	
	private void checkCompileErrors(RefactoringStatus result, CompilationUnit root, ICompilationUnit element) {
		IProblem[] messages= root.getProblems();
		for (int i= 0; i < messages.length; i++) {
			IProblem problem= messages[i];
			if (!isIgnorableProblem(problem)) {
				result.addError(Messages.format(
						"ConvertToAtomicInteger: Compiler errors", 
						element.getElementName()), JavaStatusContext.create(element));
				return;
			}
		}
	}	
}
