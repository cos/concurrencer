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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.dom.ModifierRewrite;
import org.eclipse.jdt.internal.corext.dom.NodeFinder;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.TextChangeCompatibility;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.corext.refactoring.util.ResourceUtil;
import org.eclipse.jdt.internal.corext.refactoring.util.TextChangeManager;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

public class ConvertToFJTaskRefactoring extends Refactoring {

//	private final class StatementVisitor extends ASTVisitor {  TODO What is the point of this class?
//		private final List<Statement> statements;
//
//		private StatementVisitor(List<Statement> statementsInOriginalMethod) {
//			this.statements= statementsInOriginalMethod;
//		}
//
//		public boolean visit(ExpressionStatement node) {
//			statements.add(node);
//			return false;
//		}
//
//		public boolean visit(VariableDeclarationStatement node) {
//			statements.add(node);
//			return false;
//		}
//		
//		public boolean visit(ReturnStatement node) {
//			statements.add(node);
//			return false;
//		}
//	}

	private static final String NO_NAME= ""; //$NON-NLS-1$
	private IMethod fMethod;
	private CompilationUnit fRoot;
	private MethodDeclaration fMethodDeclaration;
	private ASTRewrite fRewriter;
	private TextChangeManager fChangeManager;
	private ImportRewrite fImportRewrite;
//	private static final String LINKED_NAME= "name"; //$NON-NLS-1$
	private String nameForFJTaskSubtype= ""; //$NON-NLS-1$
	private String sequentialThreshold;
	private boolean fInfixExpressionFlag= false;
	private boolean fMethodInvocationFlag= false;

	

	public ConvertToFJTaskRefactoring(IMethod method){
		fChangeManager= new TextChangeManager();
		this.fMethod= method;
		nameForFJTaskSubtype= suggestTaskName();
	}
	
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		
		RefactoringStatus result= new RefactoringStatus();
		fChangeManager.clear();
		pm.beginTask("", 12); //$NON-NLS-1$
		pm.setTaskName("Convert to FJTask checking preconditions"); //$NON-NLS-1$
		pm.worked(1);
			
		pm.setTaskName("ConvertToFJTask analyze preconditions");	//$NON-NLS-1$ 
		
	//	ITypeBinding declaringClass= 
			((AbstractTypeDeclaration)ASTNodes.getParent(fMethodDeclaration, AbstractTypeDeclaration.class)).resolveBinding();
		List<TextEditGroup> ownerDescriptions= new ArrayList<TextEditGroup>();
		ICompilationUnit owner= fMethod.getCompilationUnit();
		
		fImportRewrite= StubUtility.createImportRewrite(fRoot, true);
		
		checkCompileErrors(result, fRoot, owner);
		
		ownerDescriptions.addAll(addCreateTaskClass(fRoot, result));
		if (result.hasFatalError())
			return result;
		
		ownerDescriptions.addAll(reimplementOriginalRecursiveFunction());
		
		addImports(fImportRewrite);
		
		
		createEdits(owner, fRewriter, ownerDescriptions, fImportRewrite);
		
		IFile[] filesToBeModified= ResourceUtil.getFiles(fChangeManager.getAllCompilationUnits());
		result.merge(Checks.validateModifiesFiles(filesToBeModified, getValidationContext()));
		if (result.hasFatalError())
			return result;
		ResourceChangeChecker.checkFilesToBeChanged(filesToBeModified, new SubProgressMonitor(pm, 1));
		return result;
		
	}
	
	private Collection<TextEditGroup> reimplementOriginalRecursiveFunction() {
		TextEditGroup gd= new TextEditGroup("Reimplement recursive method to invoke the FJTask framework"); //$NON-NLS-1$
		
		AST ast= fRoot.getAST();
		
		Block originalBody= fMethodDeclaration.getBody();
		Block newMethodBody= ast.newBlock();
		List<ASTNode> newStatements= newMethodBody.statements();
		
		String declareNumOfAvailableResource= "int processorCount = Runtime.getRuntime().availableProcessors();"; //$NON-NLS-1$
		ASTNode declNumOfAvailableResources= fRewriter.createStringPlaceholder(declareNumOfAvailableResource, ASTNode.EXPRESSION_STATEMENT);
		newStatements.add(declNumOfAvailableResources);
		
		String pool= new String("ForkJoinPool pool = new ForkJoinPool(processorCount);"); //$NON-NLS-1$
		ASTNode poolNode= fRewriter.createStringPlaceholder(pool, ASTNode.EXPRESSION_STATEMENT);
		newStatements.add(poolNode);
		
		VariableDeclarationFragment newTaskDeclFragment= ast.newVariableDeclarationFragment();
		String taskInstanceName= "a" + nameForFJTaskSubtype; //$NON-NLS-1$
		newTaskDeclFragment.setName(ast.newSimpleName(taskInstanceName));
		ClassInstanceCreation createTaskInstance= ast.newClassInstanceCreation();
		newTaskDeclFragment.setInitializer(createTaskInstance);
		createTaskInstance.setType(ast.newSimpleType(ast.newSimpleName(nameForFJTaskSubtype)));
		List<SimpleName> argumentsForTaskCreation= createTaskInstance.arguments();
		
		List<ASTNode> recursiveMethodParameters= fMethodDeclaration.parameters();
		for (Object par : recursiveMethodParameters) {
			SingleVariableDeclaration parameter= (SingleVariableDeclaration) par;
			argumentsForTaskCreation.add(ast.newSimpleName(parameter.getName().getIdentifier()));
		}
		
		VariableDeclarationStatement declTask= ast.newVariableDeclarationStatement(newTaskDeclFragment);
		declTask.setType(ast.newSimpleType(ast.newSimpleName(nameForFJTaskSubtype)));
		newStatements.add(declTask);
		
		String poolInvoke= "pool.invoke(" + taskInstanceName +");"; //$NON-NLS-1$
		ASTNode poolInvokeNode= fRewriter.createStringPlaceholder(poolInvoke, ASTNode.EXPRESSION_STATEMENT);
		newStatements.add(poolInvokeNode);
		
		if (! recursiveMethodReturnsVoid()) {
			String returnSt= "return " + taskInstanceName + ".result;"; //$NON-NLS-1$
			ASTNode returnNode= fRewriter.createStringPlaceholder(returnSt, ASTNode.EXPRESSION_STATEMENT);
			newStatements.add(returnNode);
		}
		
		fRewriter.replace(originalBody, newMethodBody, gd);
		
		ArrayList<TextEditGroup> group= new ArrayList<TextEditGroup>();
		group.add(gd);
		return group;
	}

	private void addImports(ImportRewrite importRewrite) {
		importRewrite.addImport("java.util.concurrent.ForkJoinPool"); //$NON-NLS-1$
		importRewrite.addImport("java.util.concurrent.RecursiveAction"); //$NON-NLS-1$
	}

	private Collection<TextEditGroup> addCreateTaskClass(CompilationUnit root, RefactoringStatus result) {
		
		
		TextEditGroup gd= new TextEditGroup("Create RecursiveAction Subtype"); //$NON-NLS-1$
		
		AST ast= root.getAST();
		TypeDeclaration recursiveActionSubtype= ast.newTypeDeclaration();
		recursiveActionSubtype.setName(ast.newSimpleName(nameForFJTaskSubtype));
		recursiveActionSubtype.setSuperclassType(ast.newSimpleType(ast.newSimpleName("RecursiveAction")));	//$NON-NLS-1$	
		ModifierRewrite.create(fRewriter, recursiveActionSubtype).copyAllModifiers(fMethodDeclaration, gd);
		
		createFields(recursiveActionSubtype, ast);

		createContructor(recursiveActionSubtype, ast);
		
		createComputeMethod(recursiveActionSubtype,ast, result);
		
		copyRecursiveMethod(recursiveActionSubtype, ast);
		
		ChildListPropertyDescriptor descriptor= getBodyDeclarationsProperty(fMethodDeclaration.getParent());
		fRewriter.getListRewrite(fMethodDeclaration.getParent(), descriptor).insertAfter(recursiveActionSubtype, fMethodDeclaration, gd);
		
		
		ArrayList<TextEditGroup> group= new ArrayList<TextEditGroup>();
		group.add(gd);
		return group;
	}

	private void copyRecursiveMethod(TypeDeclaration recursiveActionSubtype, AST ast) {
		ASTNode copyRecursiveMethod= ASTNode.copySubtree(ast, fMethodDeclaration);
		recursiveActionSubtype.bodyDeclarations().add(copyRecursiveMethod);
	}

	private void createComputeMethod(TypeDeclaration recursiveActionSubtype, AST ast, RefactoringStatus result) {
		MethodDeclaration computeMethod= ast.newMethodDeclaration();
		computeMethod.setName(ast.newSimpleName("compute")); //$NON-NLS-1$
		computeMethod.modifiers().add(ast.newModifier(ModifierKeyword.PROTECTED_KEYWORD));
		
		final TextEditGroup editGroup= new TextEditGroup("generate compute() method"); //$NON-NLS-1$
		
		Statement recursionBaseCaseBranch= identifyRecursionBaseCaseBranch(fMethodDeclaration.getBody());
		if (recursionBaseCaseBranch== null) {
			RefactoringStatus fatalError= new RefactoringStatus();
			fatalError.addFatalError("Cannot identify the base " + //$NON-NLS-1$
					"case for recursion. Maybe " + fMethod.getElementName() + " is not a recursive divide-and-conquer"); //$NON-NLS-1$
			result.merge(fatalError);
			return;
		}

		final ASTRewrite scratchRewriter= ASTRewrite.create(fRoot.getAST());
		ASTNode sequentialThresholdCheck= scratchRewriter.createStringPlaceholder("(" + sequentialThreshold + ")", ASTNode.PARENTHESIZED_EXPRESSION); //$NON-NLS-1$
		
		IfStatement enclosingIf= (IfStatement) recursionBaseCaseBranch.getParent();
		scratchRewriter.replace(enclosingIf.getExpression(), (Expression) sequentialThresholdCheck, editGroup);
		
		if (recursionBaseCaseBranch instanceof Block) {
			Block baseCaseBlock= (Block) recursionBaseCaseBranch;
			List<ASTNode> statementsInBaseCase= baseCaseBlock.statements();
			ASTNode lastStatementInBaseCase= statementsInBaseCase.get(statementsInBaseCase.size() - 1 );
			if (recursiveMethodReturnsVoid()) {
				ExpressionStatement sequentialMethodInvocation= ast.newExpressionStatement(createSequentialMethodInvocation(ast));
				ListRewrite listRewriteForBaseBlock= scratchRewriter.getListRewrite(baseCaseBlock, Block.STATEMENTS_PROPERTY);
				listRewriteForBaseBlock.insertBefore(sequentialMethodInvocation, lastStatementInBaseCase, editGroup);
			} else {
				Assignment assignmentToResult= ast.newAssignment();
				assignmentToResult.setLeftHandSide(ast.newSimpleName("result")); //$NON-NLS-1$
				assignmentToResult.setRightHandSide(createSequentialMethodInvocation(ast));
				ExpressionStatement newExpressionStatement= ast.newExpressionStatement(assignmentToResult);
				
				ListRewrite listRewriteForBaseBlock= scratchRewriter.getListRewrite(baseCaseBlock, Block.STATEMENTS_PROPERTY);
				listRewriteForBaseBlock.insertBefore(newExpressionStatement, lastStatementInBaseCase, editGroup);
				ReturnStatement newReturnResult= ast.newReturnStatement();
				scratchRewriter.replace(lastStatementInBaseCase, newReturnResult, editGroup);
			}
		} else if (recursionBaseCaseBranch instanceof ReturnStatement) {
			Block basecaseBlock= ast.newBlock();
			List<ASTNode> basecaseStatements= basecaseBlock.statements();
			if (recursiveMethodReturnsVoid()) {
				ExpressionStatement sequentialMethodInvocation= ast.newExpressionStatement(createSequentialMethodInvocation(ast));
				basecaseStatements.add(sequentialMethodInvocation);
			} else {
				Assignment assignmentToResult= ast.newAssignment();
				assignmentToResult.setLeftHandSide(ast.newSimpleName("result")); //$NON-NLS-1$
				assignmentToResult.setRightHandSide(createSequentialMethodInvocation(ast));
				ExpressionStatement newExpressionStatement= ast.newExpressionStatement(assignmentToResult);
				basecaseStatements.add(newExpressionStatement);
			}
			basecaseStatements.add(ast.newReturnStatement());
			scratchRewriter.replace(recursionBaseCaseBranch, basecaseBlock, editGroup);
		}
		
		
		final List<Statement> lastStatementWithRecursiveMethodInvocation= new ArrayList<Statement>();
		final int[] taskNumber= new int[] {0};
		final List<String> partialComputationsNames= new ArrayList<String>();
		final List<String> typesOfComputations= new ArrayList<String>();
		fMethodDeclaration.accept(new ASTVisitor() {
			public boolean visit(MethodInvocation methodCall) {
				IMethodBinding bindingForMethodCall= methodCall.resolveMethodBinding();
				IMethodBinding bindingForMethodDeclaration= fMethodDeclaration.resolveBinding();
				if (Bindings.equals(bindingForMethodDeclaration, bindingForMethodCall)) {
					String codeForTaskDecl= nameForFJTaskSubtype + " task" + ++taskNumber[0] +  //$NON-NLS-1$
				    " = new " + nameForFJTaskSubtype + "("; //$NON-NLS-1$
					String methodArguments= "";
					List<Expression> arguments= methodCall.arguments();
					for (Iterator<Expression> iterator= arguments.iterator(); iterator
							.hasNext();) {
						ASTNode argument= iterator.next();
						methodArguments += argument.toString();
						if (iterator.hasNext()) {
							methodArguments += ", "; //$NON-NLS-1$
						}
					}
					codeForTaskDecl += methodArguments + ");"; //$NON-NLS-1$
					VariableDeclarationStatement taskDeclStatement= (VariableDeclarationStatement) scratchRewriter.createStringPlaceholder(codeForTaskDecl , ASTNode.VARIABLE_DECLARATION_STATEMENT);
					Statement parentOfMethodCall= findParentStatement(methodCall);
					if (recursiveMethodReturnsVoid()) {
						scratchRewriter.replace(parentOfMethodCall, taskDeclStatement, editGroup);
					}
					else {
						
						if (parentOfMethodCall instanceof VariableDeclarationStatement){
							VariableDeclarationStatement varDeclaration= (VariableDeclarationStatement) parentOfMethodCall;
							VariableDeclarationFragment varFragment= (VariableDeclarationFragment) varDeclaration.fragments().get(0);
							partialComputationsNames.add(varFragment.getName().getIdentifier());
							typesOfComputations.add(varDeclaration.getType().toString());
							scratchRewriter.replace(parentOfMethodCall, taskDeclStatement, editGroup);
						}
						else if (parentOfMethodCall instanceof ExpressionStatement) {
							ExpressionStatement exprStatement= (ExpressionStatement) parentOfMethodCall;
							Expression expressionContainer= exprStatement.getExpression();
							if (expressionContainer instanceof Assignment) {
								Assignment assignment= (Assignment) expressionContainer;
								Expression leftHandSide= assignment.getLeftHandSide();
								partialComputationsNames.add(leftHandSide.toString());
								typesOfComputations.add(leftHandSide.resolveTypeBinding().getName());
								scratchRewriter.replace(parentOfMethodCall, taskDeclStatement, editGroup);
							}  
							else
								System.err.println("Scenario not handled yet: recursive method call is within " + parentOfMethodCall.toString() ); //$NON-NLS-1$
						}
						else if (parentOfMethodCall instanceof ReturnStatement) {
							ASTNode tempNode= parentOfMethodCall.getParent();
							if(tempNode instanceof Block) {
								Block blockWithReturn= (Block) tempNode;
								ListRewrite listRewriteForBlock= scratchRewriter.getListRewrite(blockWithReturn, Block.STATEMENTS_PROPERTY);
								List<ASTNode> statementsInBlockWithReturn= blockWithReturn.statements();
								Statement lastStatementInBlock= (Statement) statementsInBlockWithReturn.get(statementsInBlockWithReturn.size() - 1);
								if (lastStatementInBlock instanceof ReturnStatement) {  //TODO Do I need this check?
									listRewriteForBlock.insertBefore(taskDeclStatement, lastStatementInBlock, editGroup);
								}
							}
							Expression exprTemp= ((ReturnStatement) parentOfMethodCall).getExpression();
							if(exprTemp instanceof InfixExpression) {
								fInfixExpressionFlag= true;
							}
							else if(exprTemp instanceof MethodInvocation) {
								fMethodInvocationFlag= true;
							}
							else
								System.err.println("Scenario not handled yet: recursive method call is within " + parentOfMethodCall.toString() ); //$NON-NLS-1$
						}
						else 
							System.err.println("Scenario not handled yet: recursive method call is within " + parentOfMethodCall.toString() ); //$NON-NLS-1$
					}
					lastStatementWithRecursiveMethodInvocation.add(parentOfMethodCall);
					
				}
				return true;
			}
		});
		try {
			Block blockContainingTaskDecl= (Block) ASTNodes.getParent(lastStatementWithRecursiveMethodInvocation.get(0), Block.class);
			ListRewrite listRewriteForBlock= scratchRewriter.getListRewrite(blockContainingTaskDecl, Block.STATEMENTS_PROPERTY);
			
			MethodInvocation forkJoinInvocation= ast.newMethodInvocation();
			//TODO the code below assumes that there are just two tasks passed to the
			// invokeAll. Need to implement the scenario with more than two tasks - COMPLETED (I think, seemed like was too easy...)
			forkJoinInvocation.setName(ast.newSimpleName("invokeAll")); //$NON-NLS-1$
			List<Expression> argumentsForkJoin= forkJoinInvocation.arguments();
			for (int i= 1; i <= taskNumber[0]; i++) {
				argumentsForkJoin.add(ast.newSimpleName("task" + i)); //$NON-NLS-1$
			}
			Statement lastStatementWithRecursiveCall= lastStatementWithRecursiveMethodInvocation.get(lastStatementWithRecursiveMethodInvocation.size() - 1);
			
			if (!recursiveMethodReturnsVoid()) {
				if (partialComputationsNames.size() >= 1)
					if (lastStatementWithRecursiveCall instanceof VariableDeclarationStatement) {
						for (int i= partialComputationsNames.size() - 1; i >= 0 ; ) {
							String varStatement= typesOfComputations.get(i) + " " + partialComputationsNames.get(i) + " = task" + (i + 1) + ".result;"; //$NON-NLS-1$
							ASTNode variableStatement= scratchRewriter.createStringPlaceholder(varStatement, ASTNode.VARIABLE_DECLARATION_STATEMENT);
							listRewriteForBlock.insertAfter(variableStatement, lastStatementWithRecursiveCall, editGroup);
							i--;
						}
					} else if (lastStatementWithRecursiveCall instanceof ExpressionStatement) {
						for (int i= partialComputationsNames.size() - 1; i >= 0 ; ) {
							String varStatement= partialComputationsNames.get(i) + " = task" + (i + 1) + ".result;"; //$NON-NLS-1$
							ASTNode exprStatement= scratchRewriter.createStringPlaceholder(varStatement, ASTNode.EXPRESSION_STATEMENT);
							listRewriteForBlock.insertAfter(exprStatement, lastStatementWithRecursiveCall, editGroup);
							i--;
						}
					}
				
				List<ASTNode> statementsInBlockWithTaskDecl= blockContainingTaskDecl.statements();
				Statement lastStatementInBlock= (Statement) statementsInBlockWithTaskDecl.get(statementsInBlockWithTaskDecl.size() - 1);
				if (lastStatementInBlock instanceof ReturnStatement) {
					if(fInfixExpressionFlag) {
						Assignment assignToResult= ast.newAssignment();
						assignToResult.setLeftHandSide(ast.newSimpleName("result")); //$NON-NLS-1$
						InfixExpression infixExpression= ((InfixExpression)(ASTNode.copySubtree(ast, ((ReturnStatement)lastStatementInBlock).getExpression())));
						int taskNum= 1;
						infixExpression.setLeftOperand((Expression) ast.newQualifiedName(ast.newSimpleName("task" + taskNum++), ast.newSimpleName("result"))); //$NON-NLS-1$
						infixExpression.setRightOperand((Expression) ast.newQualifiedName(ast.newSimpleName("task" + taskNum++), ast.newSimpleName("result"))); //$NON-NLS-1$
						List<Expression> extendedOperands = infixExpression.extendedOperands();
						for (int i= 0; i < extendedOperands.size(); ) {
							extendedOperands.set(i, (Expression) ast.newQualifiedName(ast.newSimpleName("task" + taskNum++), ast.newSimpleName("result"))); //$NON-NLS-1$
							i++;
						}
						assignToResult.setRightHandSide(infixExpression);
						scratchRewriter.replace(lastStatementInBlock, ast.newExpressionStatement(assignToResult), editGroup);
					}
					else if(fMethodInvocationFlag) {
						Assignment assignToResult= ast.newAssignment();
						assignToResult.setLeftHandSide(ast.newSimpleName("result")); //$NON-NLS-1$
						MethodInvocation methodInvocation= ((MethodInvocation)(ASTNode.copySubtree(ast, ((ReturnStatement)lastStatementInBlock).getExpression())));
						int taskNum= 1;
						List<Expression> methodArguments= methodInvocation.arguments();
						for (int index= 0; index < methodArguments.size(); ) {
							methodArguments.set(index++, (Expression) ast.newQualifiedName(ast.newSimpleName("task" + taskNum++), ast.newSimpleName("result"))); //$NON-NLS-1$
						}
						assignToResult.setRightHandSide(methodInvocation);
						scratchRewriter.replace(lastStatementInBlock, ast.newExpressionStatement(assignToResult), editGroup);
					}
					else {
						Assignment assignToResult= ast.newAssignment();
						assignToResult.setLeftHandSide(ast.newSimpleName("result")); //$NON-NLS-1$
						assignToResult.setRightHandSide((Expression) ASTNode.copySubtree(ast, ((ReturnStatement) lastStatementInBlock).getExpression()));
						scratchRewriter.replace(lastStatementInBlock, ast.newExpressionStatement(assignToResult), editGroup);
					}
				}
			}
			if(fInfixExpressionFlag || fMethodInvocationFlag ) {
				listRewriteForBlock.insertBefore(ast.newExpressionStatement(forkJoinInvocation), lastStatementWithRecursiveCall, editGroup);
			} else {
				listRewriteForBlock.insertAfter(ast.newExpressionStatement(forkJoinInvocation), lastStatementWithRecursiveCall, editGroup);
			}
			TextEdit edits= scratchRewriter.rewriteAST();
			IDocument scratchDocument= new Document(((ICompilationUnit)fRoot.getJavaElement()).getSource());
			try {
				//System.out.println(scratchDocument.get());
				edits.apply(scratchDocument);
				//System.out.println(scratchDocument.get());
				
				ASTParser parser= ASTParser.newParser(AST.JLS3);
				parser.setSource(scratchDocument.get().toCharArray());
				CompilationUnit scratchCU= (CompilationUnit)parser.createAST(null);
				final TypeDeclaration[] declaringClass= new TypeDeclaration[1];
				scratchCU.accept(new ASTVisitor() {
					public boolean visit(TypeDeclaration typedecl){
						if (typedecl.getName().getIdentifier().equals(fMethod.getDeclaringType().getElementName())) {
							declaringClass[0]= typedecl;
						}
						return true;
					}
				});
				MethodDeclaration[] methodsInRefactoredClass= declaringClass[0].getMethods();
				for (MethodDeclaration methodDeclaration : methodsInRefactoredClass) {
					if (methodDeclaration.getName().getIdentifier().equals(fMethodDeclaration.getName().getIdentifier())
							&& methodsHaveSameSignature(methodDeclaration,fMethodDeclaration)) {
						Block block= methodDeclaration.getBody();
						Block copySubtree= (Block) ASTNode.copySubtree(ast, block);
						computeMethod.setBody(copySubtree);
						break;
					}
				}
			} catch (MalformedTreeException e) {
				e.printStackTrace();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		
		
		
		recursiveActionSubtype.bodyDeclarations().add(computeMethod);
	}

	Statement findParentStatement(MethodInvocation methodCall) {
		Statement  parentOfMethodCall= null;
		if (ASTNodes.getParent(methodCall, ASTNode.VARIABLE_DECLARATION_STATEMENT) != null)
			parentOfMethodCall= (Statement) ASTNodes.getParent(methodCall, ASTNode.VARIABLE_DECLARATION_STATEMENT);
		else if (ASTNodes.getParent(methodCall, ASTNode.EXPRESSION_STATEMENT) != null) {
			parentOfMethodCall= (Statement) ASTNodes.getParent(methodCall, ASTNode.EXPRESSION_STATEMENT);
		} else if (ASTNodes.getParent(methodCall, ASTNode.RETURN_STATEMENT) != null)
			parentOfMethodCall= (Statement) ASTNodes.getParent(methodCall, ASTNode.RETURN_STATEMENT);
		return parentOfMethodCall;	
	}

	private boolean methodsHaveSameSignature(
			MethodDeclaration methodDeclaration,
			MethodDeclaration methodDeclaration2) {
		String methodArguments= ""; //$NON-NLS-1$
		List<ASTNode> arguments= methodDeclaration.parameters();
		for (Iterator<ASTNode> iterator= arguments.iterator(); iterator
				.hasNext();) {
			ASTNode argument= iterator.next();
			methodArguments += argument.toString();
			if (iterator.hasNext()) {
				methodArguments += ", "; //$NON-NLS-1$
			}
		}
		
		String methodArguments2= ""; //$NON-NLS-1$
		arguments= methodDeclaration2.parameters();
		for (Iterator<ASTNode> iterator= arguments.iterator(); iterator
				.hasNext();) {
			ASTNode argument= iterator.next();
			methodArguments2 += argument.toString();
			if (iterator.hasNext()) {
				methodArguments2 += ", "; //$NON-NLS-1$
			}
		}
		return methodArguments.equals(methodArguments2);
	}


	private MethodInvocation createSequentialMethodInvocation(AST ast) {
		MethodInvocation invokeSequentialMethod= ast.newMethodInvocation();
		invokeSequentialMethod.setName(ast.newSimpleName(fMethod.getElementName()));
		List<Expression> argumentsForInvokingSeqMethod= invokeSequentialMethod.arguments();
		List<ASTNode> recursiveMethodParameters= fMethodDeclaration.parameters();
		for (Object par : recursiveMethodParameters) {
			SingleVariableDeclaration parameter= (SingleVariableDeclaration) par;
			argumentsForInvokingSeqMethod.add(ast.newSimpleName(parameter.getName().getIdentifier()));
		}
		return invokeSequentialMethod;
	}

	private Statement identifyRecursionBaseCaseBranch(Block computeBodyBlock) {
		final Statement[] baseCase= new Statement[] {null};
		computeBodyBlock.accept(new ASTVisitor() {
			public boolean visit(IfStatement ifStatement) {
				Statement thenStatement= ifStatement.getThenStatement();
				Statement elseStatement= ifStatement.getElseStatement();
				if (statementIsBaseCase(thenStatement))
					baseCase[0]= thenStatement;
				else if ((elseStatement!= null) && (statementIsBaseCase(elseStatement)))
					baseCase[0]= elseStatement;
				return false;
			}

			private boolean statementIsBaseCase(Statement statement) {
				return statementEndsWithReturn(statement) && !statementContainsRecursiveCall(statement);
			}

			private boolean statementEndsWithReturn(Statement statement) {
				if (statement instanceof Block) {
					Block blockStatement= (Block) statement;
					List<ASTNode> statements= blockStatement.statements();
					ASTNode lastStatement= statements.get(statements.size() - 1);
					if ( (lastStatement instanceof ReturnStatement)) {
						return true;
					}
				} else if (statement instanceof ReturnStatement)
					return true;
				return false;
			}
		});
		return baseCase[0];
	}

	private boolean statementContainsRecursiveCall(Statement statement) {
		final boolean[] result= new boolean[] {false};
		statement.accept(new ASTVisitor() {
			public boolean visit(MethodInvocation methodCall) {
				IMethodBinding bindingForMethodCall= methodCall.resolveMethodBinding();
				IMethodBinding bindingForMethodDeclaration= fMethodDeclaration.resolveBinding();
				if (Bindings.equals(bindingForMethodDeclaration, bindingForMethodCall)) {
					result[0]= true;
				}
				return true;
			}
		});
		return result[0];
	}
	
	private List<MethodInvocation> getRecursiveCalls(Statement statement) {
		final List<MethodInvocation> result= new ArrayList<MethodInvocation>();
		statement.accept(new ASTVisitor() {
			public boolean visit(MethodInvocation methodCall) {
				IMethodBinding bindingForMethodCall= methodCall.resolveMethodBinding();
				IMethodBinding bindingForMethodDeclaration= fMethodDeclaration.resolveBinding();
				if (Bindings.equals(bindingForMethodCall, bindingForMethodDeclaration)) {
					result.add(methodCall);
				}
				return false;
			}
		});
		return result;
	}
	
	private void createContructor(TypeDeclaration recursiveActionSubtype, AST ast) {
		
		MethodDeclaration newConstructor= ast.newMethodDeclaration();
		newConstructor.setConstructor(true);
		newConstructor.setName(ast.newSimpleName(nameForFJTaskSubtype));
		List<ASTNode> constructorParameters= newConstructor.parameters();
		List<ASTNode> recursiveMethodParameters= fMethodDeclaration.parameters();
		for (Object par : recursiveMethodParameters) {
			SingleVariableDeclaration parameter= (SingleVariableDeclaration) par;
			constructorParameters.add(ASTNode.copySubtree(ast, parameter));
		}
		
		newConstructor.modifiers().add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
		
		Block newConstructorBlock= ast.newBlock();
		newConstructor.setBody(newConstructorBlock);
		
		List<ASTNode> newConstructorStatements= newConstructorBlock.statements();
		for (Object par : recursiveMethodParameters) {
			SingleVariableDeclaration parameter= (SingleVariableDeclaration) par;
			Assignment newAssignment= ast.newAssignment();
			FieldAccess newFieldAccess= ast.newFieldAccess();
			newFieldAccess.setExpression(ast.newThisExpression());
			newFieldAccess.setName(ast.newSimpleName(parameter.getName().getIdentifier()));
			newAssignment.setLeftHandSide(newFieldAccess);
			
			newAssignment.setRightHandSide(ast.newSimpleName(parameter.getName().getIdentifier()));
			
			ExpressionStatement newExpressionStatement= ast.newExpressionStatement(newAssignment);
			newConstructorStatements.add(newExpressionStatement);
		}
		
		
		recursiveActionSubtype.bodyDeclarations().add(newConstructor);
		
	}

	private void createFields(TypeDeclaration recursiveActionSubtype, AST ast) {
		List<ASTNode> recursiveMethodParameters= fMethodDeclaration.parameters();
		for (Object par : recursiveMethodParameters) {
			SingleVariableDeclaration parameter= (SingleVariableDeclaration) par;
			
			VariableDeclarationFragment newDeclarationFragment= ast.newVariableDeclarationFragment();
			newDeclarationFragment.setName(ast.newSimpleName(parameter.getName().getIdentifier()));
			
			FieldDeclaration newFieldDeclaration= ast.newFieldDeclaration(newDeclarationFragment);
			newFieldDeclaration.setType((Type) ASTNode.copySubtree(ast, parameter.getType()));
			List<Modifier> modifiers= newFieldDeclaration.modifiers();
			modifiers.add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
			
			recursiveActionSubtype.bodyDeclarations().add(newFieldDeclaration);
		}
		
		if (!recursiveMethodReturnsVoid()) {
			VariableDeclarationFragment newDeclarationFragment= ast.newVariableDeclarationFragment();
			newDeclarationFragment.setName(ast.newSimpleName("result")); //$NON-NLS-1$
			
			FieldDeclaration newFieldDeclaration= ast.newFieldDeclaration(newDeclarationFragment);
			newFieldDeclaration.setType((Type) ASTNode.copySubtree(ast, fMethodDeclaration.getReturnType2()));
			List<Modifier> modifiers= newFieldDeclaration.modifiers();
			modifiers.add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
			
			recursiveActionSubtype.bodyDeclarations().add(newFieldDeclaration);
		}
	}
	
	boolean recursiveMethodReturnsVoid() {
		Type returnType= fMethodDeclaration.getReturnType2();
		return (returnType.isPrimitiveType() && ((PrimitiveType)returnType).getPrimitiveTypeCode().equals(PrimitiveType.VOID));
	}

	private ChildListPropertyDescriptor getBodyDeclarationsProperty(ASTNode declaration) {
		if (declaration instanceof AnonymousClassDeclaration)
			return AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY;
		else if (declaration instanceof AbstractTypeDeclaration)
			return ((AbstractTypeDeclaration) declaration).getBodyDeclarationsProperty();
		Assert.isTrue(false);
		return null;
	}
	
	private void createEdits(ICompilationUnit unit, ASTRewrite rewriter, List<TextEditGroup> groups, ImportRewrite importRewrite) throws CoreException {
		TextChange change= fChangeManager.get(unit);
		MultiTextEdit root= new MultiTextEdit();
		change.setEdit(root);
		
		TextEdit importEdit= importRewrite.rewriteImports(null);
		TextChangeCompatibility.addTextEdit(fChangeManager.get(unit), "Update Imports", importEdit); //$NON-NLS-1$
		
		root.addChild(rewriter.rewriteAST());
		for (Iterator<TextEditGroup> iter= groups.iterator(); iter.hasNext();) {
			change.addTextEditGroup(iter.next());
		}
	}

	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus result=  new RefactoringStatus();
		result.merge(Checks.checkAvailability(fMethod));
		
		if (result.hasFatalError())
			return result;
		
		fRoot= new RefactoringASTParser(AST.JLS3).parse(fMethod.getCompilationUnit(), true, pm);
		ISourceRange sourceRange= fMethod.getNameRange();
		ASTNode node= NodeFinder.perform(fRoot, sourceRange.getOffset(), sourceRange.getLength());
		if (node== null) {
			return mappingErrorFound(result, node);
		}
		fMethodDeclaration= (MethodDeclaration)ASTNodes.getParent(node, MethodDeclaration.class);
		if (fMethodDeclaration== null) {
			return mappingErrorFound(result, node);
		}
		if (fMethodDeclaration.resolveBinding()== null) {
			if (!processCompilerError(result, node))
				result.addFatalError("type not resolveable"); //$NON-NLS-1$
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
			"Convert to FJTask cannot analyze selected method",  //$NON-NLS-1$
			new String[] {fMethod.getElementName()});
	}

	private boolean processCompilerError(RefactoringStatus result, ASTNode node) {
		Message[] messages= ASTNodes.getMessages(node, ASTNodes.INCLUDE_ALL_PARENTS);
		if (messages.length== 0)
			return false;
		result.addFatalError(Messages.format(
			"Convert to FJTask compile errors",  //$NON-NLS-1$
			new String[] { fMethod.getElementName(), messages[0].getMessage()}));
		return true;
	}
	
	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		String project= null;
		IJavaProject javaProject= fMethod.getJavaProject();
		if (javaProject != null)
			project= javaProject.getElementName();
		int flags= JavaRefactoringDescriptor.JAR_MIGRATION | JavaRefactoringDescriptor.JAR_REFACTORING | RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE;
		final IType declaring= fMethod.getDeclaringType();
		try {
			if (declaring.isAnonymous() || declaring.isLocal())
				flags|= JavaRefactoringDescriptor.JAR_SOURCE_ATTACHMENT;
		} catch (JavaModelException exception) {
			JavaPlugin.log(exception);
		}

		//TODO need to properly initialize the arguments so that this refactoring becomes recordable
		final Map<String, String> arguments= new HashMap<String, String>();
		String description= "Convert Recursive Method to FJTask"; //$NON-NLS-1$
		String comment= "Convert Recursive Method to FJTask"; //$NON-NLS-1$
		
		final JavaRefactoringDescriptor descriptor= new JavaRefactoringDescriptor(IJavaRefactorings.ENCAPSULATE_FIELD, project, description, comment, arguments, flags) {};
		
		arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT, JavaRefactoringDescriptorUtil.elementToHandle(project, fMethod));
		arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME, fMethod.getElementName());
		//arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + 1, JavaRefactoringDescriptorUtil.elementToHandle(project, nameForFJTaskSubtype));
		
		
		final DynamicValidationRefactoringChange result= new DynamicValidationRefactoringChange(descriptor, getName());
		TextChange[] changes= fChangeManager.getAllChanges();
		pm.beginTask(NO_NAME, changes.length);
		pm.setTaskName("ConvertToFJTask create changes"); //$NON-NLS-1$
		for (int i= 0; i < changes.length; i++) {
			result.add(changes[i]);
			pm.worked(1);
		}
		pm.done();
		return result;
	}

	@Override
	public String getName() {
		return "Convert to FJTask"; //$NON-NLS-1$
	}

	public void setMethod(IMethod method) {
		this.fMethod= method;
	}

	public RefactoringStatus setFieldName(String text) {
		// TODO Auto-generated method stub
		return null;
	}
	

	public IMethod getMethod() {
		return fMethod;
	}

	public String getMethodName() {
		return fMethod.getElementName();
	}

	public String getNameForFJTaskSubtype() {
		return nameForFJTaskSubtype;
	}

	public RefactoringStatus setNameForFJTaskSubtype(String nameForFJTaskSubtype) {
		this.nameForFJTaskSubtype= nameForFJTaskSubtype;
		return new RefactoringStatus();
	}
	
	private boolean isIgnorableProblem(IProblem problem) {
		if (problem.getID()== IProblem.NotVisibleField)
			return true;
		return false;
	}
	
	private void checkCompileErrors(RefactoringStatus result, CompilationUnit root, ICompilationUnit element) {
		IProblem[] messages= root.getProblems();
		for (int i= 0; i < messages.length; i++) {
			IProblem problem= messages[i];
			if (!isIgnorableProblem(problem)) {
				result.addError(Messages.format(
						"ConvertToAtomicInteger compiler errors update",  //$NON-NLS-1$
						element.getElementName()), JavaStatusContext.create(element));
				return;
			}
		}
	}

	public String suggestTaskName() {
		String methodName= fMethod.getElementName();
		return methodName.substring(0, 1).toUpperCase() + methodName.substring(1, methodName.length()) + "Impl"; //$NON-NLS-1$
	}

	public RefactoringStatus setSequentialThreshold(String text) {
		if (text== null || "".equals(text)) //$NON-NLS-1$
			return new RefactoringStatus().createErrorStatus("Sequential Threshold is Mandatory"); //$NON-NLS-1$
		sequentialThreshold= text;
		return new RefactoringStatus();
	}

	public String getMethodNameAndSignature() {
		String nameAndSignature= fMethodDeclaration.getName().getIdentifier() + "("; //$NON-NLS-1$
		List<ASTNode> recursiveMethodParameters= fMethodDeclaration.parameters();
		for (Iterator<ASTNode> iterator= recursiveMethodParameters.iterator(); iterator
				.hasNext();) {
			SingleVariableDeclaration parameter= (SingleVariableDeclaration) iterator.next();
			nameAndSignature += parameter.getType() + " " + parameter.getName().getIdentifier(); //$NON-NLS-1$
			if (iterator.hasNext())
				nameAndSignature +=", "; //$NON-NLS-1$
		}
		nameAndSignature += ")"; //$NON-NLS-1$
		return nameAndSignature;
	}	
	
	private <T> List<T> castList(Class<? extends T> toCastTo, Collection<?> c) {
		if(c.size() == 0) {
			return null;
		} else {
			List<T> tempList = new ArrayList<T>(c.size());
			for(Object objCast: c) {
				tempList.add(toCastTo.cast(objCast));
			}
			return tempList;
		}
	}
}

//TODO Externalize translatable strings, fix warnings