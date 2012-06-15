package mit.edu.concurrencyrefactorings.refactorings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mit.edu.concurrencyrefactorings.util.ReferencesFinder;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.core.search.matching.SecondaryTypeDeclarationPattern;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.dom.JdtASTMatcher;
import org.eclipse.jdt.internal.corext.dom.ModifierRewrite;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextEditBasedChangeGroup;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

public class AccessAnalyzerForConcurrentHashMap extends ASTVisitor {

	private static final String METHOD_INVOCATION = "Method Invocation";
	private static final String INITIALIZATION = "Initialization";
	private static final String REPLACE_WITH_PUT_IF_ABSENT = "Replace with putIfAbsent()";
	private static final String REMOVE_STATEMENT = "Remove Statement";
	private static final String REMOVE_SYNCHRONIZED_BLOCK = "Remove Synchronized Block";
	private static final String REMOVE_SYNCHRONIZED_MODIFIER = "Remove Synchronized Modifier";
	
	private ICompilationUnit fCUnit;
	private IVariableBinding fFieldBinding;
	private AbstractTypeDeclaration fDeclaringClass;
	private ASTRewrite fRewriter;
	private ImportRewrite fImportRewriter;
	private List<TextEditGroup> fGroupDescriptions;
	private boolean fIsFieldFinal;
	private RefactoringStatus fStatus;
	public boolean usingCHMOnlyMethods = false;

	private boolean fSetterMustReturnValue;
	private final CompilationUnit cuRoot;
	private final ConvertToConcurrentHashMapRefactoring refactoring;
	private String fMethodName;

	public AccessAnalyzerForConcurrentHashMap(
			ConvertToConcurrentHashMapRefactoring refactoring,
			ICompilationUnit unit, IVariableBinding field,
			AbstractTypeDeclaration declaringClass, ASTRewrite rewriter,
			ImportRewrite importRewrite, CompilationUnit root) {
		this.refactoring = refactoring;
		fCUnit= unit;
		this.cuRoot = root;
		fFieldBinding= field.getVariableDeclaration();
		fDeclaringClass= declaringClass;
		fRewriter= rewriter;
		fImportRewriter= importRewrite;
		fGroupDescriptions= new ArrayList<TextEditGroup>();
		fMethodName = null;
		try {
			fIsFieldFinal= Flags.isFinal(refactoring.getField().getFlags());
		} catch (JavaModelException e) {
			// assume non final field
		}
		fStatus= new RefactoringStatus();
	}

	public RefactoringStatus getStatus() {
		return fStatus;
	}

	public Collection<TextEditGroup> getGroupDescriptions() {
		return fGroupDescriptions;
	}
	
	public boolean visit(ClassInstanceCreation node) {
		// is HashMap
		// is part of assignment
		// if left-hand binds to our variable
		// then change type
		
		AST ast = node.getAST();
		Type instanceCreationType = node.getType();
		
		if(instanceCreationType instanceof ParameterizedType) {
			instanceCreationType = (Type) ASTNode.copySubtree(ast, ((ParameterizedType)instanceCreationType).getType());
		}
		
		if(instanceCreationType instanceof SimpleType) {
			String fqName = ((SimpleType)instanceCreationType).getName().getFullyQualifiedName();
			if(fqName.equals("HashMap") || fqName.equals("java.util.HashMap")) {
				
				ASTNode assignment = ASTNodes.getParent(node, Assignment.class);
				if(assignment != null) {
					Expression leftHandSide = ((Assignment)assignment).getLeftHandSide();
					if (considerBinding(resolveBinding(leftHandSide))) {
						Expression rightHandSide = ((Assignment)assignment).getRightHandSide();
												
						if (rightHandSide != null) {
							
							Type newTypeRS = ast.newSimpleType(ASTNodeFactory.newName(ast, "ConcurrentHashMap"));
							
							if(rightHandSide instanceof ClassInstanceCreation) {
								ClassInstanceCreation concurrentInstanceCreation = ast.newClassInstanceCreation();
								Type rhsType = ((ClassInstanceCreation)rightHandSide).getType();
								
								if(rhsType instanceof ParameterizedType) {
									newTypeRS = ast.newParameterizedType(newTypeRS);
									Type oldTypeRS = ((ParameterizedType) rhsType).getType();
									List<Type> oldTypeArgumentsRS = ((ParameterizedType) rhsType).typeArguments();
									List<Type> newTypeArgumentsRS = ((ParameterizedType)newTypeRS).typeArguments();
									
									newTypeArgumentsRS.addAll(ASTNode.copySubtrees(ast, oldTypeArgumentsRS));
								}
								
								concurrentInstanceCreation.setType(newTypeRS);
								fRewriter.replace(rightHandSide, concurrentInstanceCreation, createGroupDescription(INITIALIZATION));
							}
						}
					}
				}
			}
		}
		
		
		return true;
	}
	
	@Override
	public boolean visit(IfStatement ifStatement) {
			
			Expression ifExpression = ifStatement.getExpression();
			
			// Remove the parentheses.
			while (ifExpression instanceof ParenthesizedExpression) {
				ifExpression = ((ParenthesizedExpression)ifExpression).getExpression();
			}
			
			if(ifExpression instanceof InfixExpression) {
				// Operator conditional: if(something operator something)
				handleOperatorConditional(ifStatement, ifExpression);
			} else {
				// Direct conditional: if(something)
				handleDirectConditional(ifStatement, ifExpression);
			}
			
			return false;
	}

	private void handleOperatorConditional(IfStatement ifStatement, Expression ifExpression) {
		
		Operator operator = ((InfixExpression)ifExpression).getOperator();
		InfixExpression testExpression = (InfixExpression) ifExpression;
		Expression leftOperand = testExpression.getLeftOperand();
		Expression rightOperand = testExpression.getRightOperand();
		
		// TODO - (cleanup) can factor this out some, but ensure we don't accept other operators
		if(rightOperand instanceof NullLiteral) {
			// if(? operator null)
			if(operator.equals(InfixExpression.Operator.EQUALS)) {
				// if(? == null)
				if(leftOperand instanceof MethodInvocation) {
					// if(method() == null)
					handleMethodAndNullConditional(ifStatement, leftOperand, false);
				} else if(leftOperand instanceof SimpleName) {
					// if(variable == null)
					handleVariableAndNullConditional(ifStatement, leftOperand, false);
				}
			} else if(operator.equals(InfixExpression.Operator.NOT_EQUALS)) {
				// if(? != null)
				if(leftOperand instanceof MethodInvocation) {
					// if(method() != null)
					handleMethodAndNullConditional(ifStatement, leftOperand, true);
				} else if(leftOperand instanceof SimpleName) {
					// if(variable != null)
					handleVariableAndNullConditional(ifStatement, leftOperand, true);
				}
			}
		}
	}

	private void handleDirectConditional(IfStatement ifStatement, Expression ifExpression) {
		
		boolean expressionIsNegation = false;

		// Remove the prefix but record its presence.
		if(ifExpression instanceof PrefixExpression) {
			if (((PrefixExpression)ifExpression).getOperator().equals(PrefixExpression.Operator.NOT)) {
				PrefixExpression negationExpression = (PrefixExpression) ifExpression;
				ifExpression = negationExpression.getOperand();
				expressionIsNegation = true;
			}
		}
		
		// Remove the parentheses.
		while (ifExpression instanceof ParenthesizedExpression) {
			ifExpression = ((ParenthesizedExpression)ifExpression).getExpression();
		}
		
		// if(something) or if(!something)
		if (ifExpression instanceof MethodInvocation) {
			// if(method()) or if(!method())
			handleMethodDirectConditional(ifStatement, ifExpression, expressionIsNegation); 
		} else if (ifExpression instanceof SimpleName) {
			// if(variable) or if(!variable)
			handleVariableDirectConditional(ifStatement, ifExpression, expressionIsNegation);
		}
		
	}

	private void handleVariableDirectConditional(IfStatement ifStatement, Expression operand, boolean hasNegationPrefix) {
		
		// TODO (possible feature, low priority)
		// for the generalization, I think this needs to allow for "get" - might need to restructure,
		// factor out "instanceof MethodInvocation".
		
		// if(variable) or if(!variable)
		
		SimpleName booleanVariableCheck = (SimpleName) operand;
		Block enclosingBlock = (Block) ASTNodes.getParent(ifStatement, Block.class);
		
		if(enclosingBlock != null) {
			List statementsInEnclosingBlock = enclosingBlock.statements();
			int indexOfIfStatement = statementsInEnclosingBlock.indexOf(ifStatement);
			if (indexOfIfStatement > 0) {
				ASTNode statementBeforeIf = (ASTNode) statementsInEnclosingBlock.get(indexOfIfStatement-1);

				if(statementBeforeIf instanceof VariableDeclarationStatement) {
					VariableDeclarationStatement declarationStatementBeforeIf = (VariableDeclarationStatement)statementBeforeIf;
					
					if (declarationStatementBeforeIf.getType().isPrimitiveType()) {
						PrimitiveType declPrimitive = (PrimitiveType)declarationStatementBeforeIf.getType();
						if(declPrimitive.getPrimitiveTypeCode().equals(PrimitiveType.BOOLEAN)) {
							// if(boolean) or if(!boolean)
							VariableDeclarationFragment declarationFragmentBeforeIf = (VariableDeclarationFragment) declarationStatementBeforeIf.fragments().get(0);
							if(Bindings.equals(declarationFragmentBeforeIf.getName().resolveBinding(), booleanVariableCheck.resolveBinding())) {
								ASTNode booleanVariableInitializer = declarationFragmentBeforeIf.getInitializer();
								if(booleanVariableInitializer instanceof MethodInvocation) {
									MethodInvocation booleanVariableInitializerMethod = (MethodInvocation)booleanVariableInitializer;
									Expression booleanVariableInitializerExpression = booleanVariableInitializerMethod.getExpression();
									
									if(booleanVariableInitializerExpression != null && booleanVariableInitializerExpression instanceof MethodInvocation) {
										if(checkMethodNameAndBinding((MethodInvocation)booleanVariableInitializerExpression, "get") && 
												booleanVariableInitializerMethod.getName().getIdentifier().equals("equals")) {
											// boolean = get().equals(); if(boolean) or boolean = get().equals(); if(!boolean)

											ASTNode hashMapKey = (ASTNode) ((MethodInvocation) booleanVariableInitializerExpression).arguments().get(0);
											ASTNode equalsValue = (ASTNode) booleanVariableInitializerMethod.arguments().get(0);
											Statement thenStatement = ifStatement.getThenStatement();
											
											if(thenStatement instanceof Block) {
												List blockStatements = ((Block)thenStatement).statements();
												
												if(blockStatements.size() == 1) {
													Statement statement = (Statement) blockStatements.get(0);
													
													// boolean = get().equals(); if(boolean) { method(); } or !boolean form
													handleSingleInvocationInThenClauseForDirectConditional(ifStatement, hasNegationPrefix, hashMapKey, equalsValue, statement, true);
												} else {
													// boolean = get().equals(); if(boolean) { ... method(); } or !boolean form
													handleMultipleStatementsInThenClause(ifStatement, hashMapKey, equalsValue, blockStatements, true);
												}
											} else if(thenStatement instanceof ExpressionStatement) {
												
												// boolean = get().equals(); if(boolean) method(); or !boolean form
												handleSingleInvocationInThenClauseForDirectConditional(ifStatement, hasNegationPrefix, hashMapKey, equalsValue, thenStatement, true);
											}
										}
									} else if(checkMethodNameAndBinding(booleanVariableInitializerMethod, "containsKey")) {
										// boolean = containsKey(); if(boolean) or boolean = containsKey(); if(!boolean)
										
										ASTNode hashMapKey = (ASTNode) booleanVariableInitializerMethod.arguments().get(0);
										ASTNode equalsValue = null;
										Statement thenStatement = ifStatement.getThenStatement();
										
										if(thenStatement instanceof Block) {
											List blockStatements = ((Block)thenStatement).statements();
											
											if(blockStatements.size() == 1) {
												Statement statement = (Statement) blockStatements.get(0);
												
												// boolean = containsKey(); if(boolean) { method(); } or !boolean form
												handleSingleInvocationInThenClauseForDirectConditional(ifStatement, hasNegationPrefix, hashMapKey, equalsValue, statement, true);
											} else {
												
												// boolean = containsKey(); if(boolean) { ... method(); } or !boolean form
												handleMultipleStatementsInThenClause(ifStatement, hashMapKey, equalsValue, blockStatements, true);
											}
										} else if(thenStatement instanceof ExpressionStatement) {
											
											// boolean = containsKey(); if(boolean) method(); or !boolean form
											handleSingleInvocationInThenClauseForDirectConditional(ifStatement, hasNegationPrefix, hashMapKey, equalsValue, thenStatement, true);
										}
									}
								}
							}
						}
					} else if(declarationStatementBeforeIf.getType().isSimpleType()) {
						// TODO This can be anything other than simple. Don't even really need to check it.
						
						// if(Object) or if(!Object)
						// TODO (possible feature, low priority)
					}
				}
			}
		}
	}

	private void handleMethodDirectConditional(IfStatement ifStatement, Expression operand, boolean hasNegationPrefix) {
		// if(method()) or if(!method())
		
		MethodInvocation mapInvocation;
		mapInvocation = (MethodInvocation) operand;
		Expression mapInvocationExpression = mapInvocation.getExpression();
		
		// TODO (cleanup) can factor the common operations between these two clauses
		if(mapInvocationExpression != null && mapInvocationExpression instanceof MethodInvocation) {
			if(checkMethodNameAndBinding((MethodInvocation)mapInvocationExpression, "get") && 
					mapInvocation.getName().getIdentifier().equals("equals")) {
				
				ASTNode hashMapKey = (ASTNode) ((MethodInvocation) mapInvocationExpression).arguments().get(0);
				ASTNode equalsValue = (ASTNode) mapInvocation.arguments().get(0);
				Statement thenStatement = ifStatement.getThenStatement();
				
				if(thenStatement instanceof Block) {
					List blockStatements = ((Block)thenStatement).statements();
					
					if(blockStatements.size() == 1) {
						Statement statement = (Statement) blockStatements.get(0);
						
						// if(method()) { method2(); } or !method() form
						handleSingleInvocationInThenClauseForDirectConditional(ifStatement, hasNegationPrefix, hashMapKey, equalsValue, statement, false);
					} else {
						
						// if(method()) { ... method2(); } or !method() form
						handleMultipleStatementsInThenClause(ifStatement, hashMapKey, equalsValue, blockStatements, false);
					}
				} else if(thenStatement instanceof ExpressionStatement) {
					
					// if(method()) method2(); or !method() form
					handleSingleInvocationInThenClauseForDirectConditional(ifStatement, hasNegationPrefix, hashMapKey, equalsValue, thenStatement, false);
				}
			}
		} else if (checkMethodNameAndBinding(mapInvocation, "containsKey")) {
			
			ASTNode hashMapKey = (ASTNode) mapInvocation.arguments().get(0);
			ASTNode equalsValue = null;
			Statement thenStatement = ifStatement.getThenStatement();
			
			if(thenStatement instanceof Block) {
				List blockStatements = ((Block)thenStatement).statements();
				
				if(blockStatements.size() == 1) {
					Statement statement = (Statement) blockStatements.get(0);
					
					// if(method()) { method2(); } or !method() form
					handleSingleInvocationInThenClauseForDirectConditional(ifStatement, hasNegationPrefix, hashMapKey, equalsValue, statement, false);
				} else {
					
					// if(method()) { ... method2(); } or !method() form
					handleMultipleStatementsInThenClause(ifStatement, hashMapKey, equalsValue, blockStatements, false);
				}
			} else if(thenStatement instanceof ExpressionStatement) {
				
				// if(method()) method2(); or !method() form
				handleSingleInvocationInThenClauseForDirectConditional(ifStatement, hasNegationPrefix, hashMapKey, equalsValue, thenStatement, false);
			}
		}
	}

	private void handleSingleInvocationInThenClauseForDirectConditional(IfStatement ifStatement,
			boolean hasNegationPrefix, ASTNode hashMapKey, ASTNode equalsValue, Statement statement, boolean removeStatementBeforeIf) {

		if(statement instanceof ExpressionStatement) {
			Expression expression = ((ExpressionStatement)statement).getExpression();
			
			if(expression instanceof MethodInvocation) {
				if(checkMethodNameAndBinding((MethodInvocation)expression, "put")) {
					if(hasNegationPrefix) {
						// boolean = containsKey(); if(!boolean) { put(); } -> putIfAbsent();
						replaceIfStatement(ifStatement, hashMapKey, equalsValue, statement, "putIfAbsent", null, null, removeStatementBeforeIf, false, false, null);
					} else {
						// boolean = containsKey(); if(boolean) { put(); } -> replace();
						replaceIfStatement(ifStatement, hashMapKey, equalsValue,  statement, "replace", null, null, removeStatementBeforeIf, false, false, null);
					}
				} else if(checkMethodNameAndBinding((MethodInvocation)expression, "remove")) {
					if(!hasNegationPrefix) {
						// boolean = containsKey(); if(boolean) { remove(); } -> remove();
						replaceIfStatement(ifStatement, hashMapKey, equalsValue, statement, "remove", null, null, removeStatementBeforeIf, false, false, null);
					}
				}
			}
		}
	}
	
	private void handleSingleInvocationInThenClauseForOperatorConditional(IfStatement ifStatement,
			boolean testNotNull, ASTNode hashMapKey, ASTNode equalsValue, Statement statement, boolean removeStatementBeforeIf) {
		
		if(statement instanceof ExpressionStatement) {
			Expression expression = ((ExpressionStatement)statement).getExpression();
			
			if(expression instanceof MethodInvocation) {
				if(checkMethodNameAndBinding((MethodInvocation)expression, "put")) {
					if(testNotNull) {
						// if(method() != null) { put(); } -> replace();
						replaceIfStatement(ifStatement, hashMapKey, equalsValue, statement, "replace", null, null, removeStatementBeforeIf, false, false, null);
					} else {
						// if(method() == null) { put(); } -> putIfAbsent();
						replaceIfStatement(ifStatement, hashMapKey, equalsValue, statement, "putIfAbsent", null, null, removeStatementBeforeIf, false, false, null);
					}
				} else if(checkMethodNameAndBinding((MethodInvocation)expression, "remove")) {
					if(testNotNull) {
						// if(method() != null) { remove(); } -> remove();
						replaceIfStatement(ifStatement, hashMapKey, equalsValue, statement, "remove", null, null, removeStatementBeforeIf, false, false, null);
					}
				}
			}
		}
	}

	private void handleVariableAndNullConditional(
			IfStatement ifStatement, Expression leftOperand, boolean testNotNull) {
		
		SimpleName nullVariableCheck = (SimpleName) leftOperand;
		Block enclosingBlock = (Block) ASTNodes.getParent(ifStatement, Block.class);
		
		if(enclosingBlock != null) {
			List statementsInEnclosingBlock = enclosingBlock.statements();
			int indexOfIfStatement = statementsInEnclosingBlock.indexOf(ifStatement);
			if (indexOfIfStatement > 0) {
				// TODO TODO - should be able to acquire this statement no matter where it is relative to the if? (Is there another such statement elsewhere in this code?)
				ASTNode statementBeforeIf = (ASTNode) statementsInEnclosingBlock.get(indexOfIfStatement-1);

				if(statementBeforeIf instanceof VariableDeclarationStatement) {
					VariableDeclarationStatement declarationStatementBeforeIf = (VariableDeclarationStatement)statementBeforeIf;
					
					// TODO (mandatory add?) This can be anything, excluding primitive and wildcard. We don't handle any other cases besides simple.
					if (declarationStatementBeforeIf.getType().isSimpleType()) {
						SimpleType declSimple = (SimpleType)declarationStatementBeforeIf.getType();
						VariableDeclarationFragment declarationFragmentBeforeIf = (VariableDeclarationFragment) declarationStatementBeforeIf.fragments().get(0);
						if(Bindings.equals(declarationFragmentBeforeIf.getName().resolveBinding(), nullVariableCheck.resolveBinding())) {
							ASTNode nullVariableInitializer = declarationFragmentBeforeIf.getInitializer();
							if(nullVariableInitializer instanceof CastExpression) {
								nullVariableInitializer = ((CastExpression)nullVariableInitializer).getExpression();
							}
							
							if(nullVariableInitializer instanceof MethodInvocation) {
								MethodInvocation nullVariableInitializerMethod = (MethodInvocation)nullVariableInitializer;
								if(checkMethodNameAndBinding(nullVariableInitializerMethod, "get")) {
									// variable = get(); if(variable == null) or variable = get()); if(variable != null)
									ASTNode hashMapKey = (ASTNode) nullVariableInitializerMethod.arguments().get(0);
									ASTNode equalsValue = null;
									Statement thenStatement = ifStatement.getThenStatement();
									
									if(thenStatement instanceof Block) {
										List blockStatements = ((Block)thenStatement).statements();
										
										if(blockStatements.size() == 1) {
											Statement statement = (Statement) blockStatements.get(0);
											
											if(statement instanceof ExpressionStatement) {
												// variable = get(); if(variable == null) { method(); } or != form
												handleSingleInvocationInThenClauseForOperatorConditional(ifStatement, testNotNull, hashMapKey, equalsValue, statement, true);
											}
											
										} else {
											// variable = get(); if(variable == null) { ... method(); } or != form
											handleMultipleStatementsInThenClause(ifStatement, hashMapKey, equalsValue, blockStatements, true);
										}
									} else if(thenStatement instanceof ExpressionStatement) {
										// variable = get(); if(variable == null) method(); or != form
										handleSingleInvocationInThenClauseForOperatorConditional(ifStatement, testNotNull, hashMapKey, equalsValue, thenStatement, true);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void handleMethodAndNullConditional(IfStatement ifStatement,
			Expression leftOperand, boolean testNotNull) {
		
		MethodInvocation mapInvocation = (MethodInvocation)leftOperand;
		if(checkMethodNameAndBinding(mapInvocation, "get")) {
			// if(get() == null) or != form
			ASTNode hashMapKey = (ASTNode) mapInvocation.arguments().get(0);
			ASTNode equalsValue = null;
			Statement thenStatement = ifStatement.getThenStatement();
			
			if(thenStatement instanceof Block) {
				List blockStatements = ((Block)thenStatement).statements();
				
				if(blockStatements.size() == 1) {
					Statement statement = (Statement) blockStatements.get(0);
					
					// if(method() == null) { method2(); } or != form
					handleSingleInvocationInThenClauseForOperatorConditional(ifStatement, testNotNull, hashMapKey, equalsValue, statement, false);
				} else {
					
					// if(method() == null) { ... method2(); } or != form
					handleMultipleStatementsInThenClause(ifStatement, hashMapKey, equalsValue, blockStatements, false);
				}
			} else if(thenStatement instanceof ExpressionStatement) {
				// if(method() == null) method2(); or != form
				handleSingleInvocationInThenClauseForOperatorConditional(ifStatement, testNotNull, hashMapKey, equalsValue, thenStatement, false);
			}
		}
	}

	@SuppressWarnings("restriction")
	private boolean handleMultipleStatementsInThenClause(
			IfStatement ifStatement, ASTNode hashMapKey, ASTNode equalsValue, List blockStatements, boolean removeStatementBeforeIf) {

		String methodInvocationText = null;
		String codeForCreateValueMethod = null;
		ASTNode replacementForValue = null;
		int numberOfStatementsBeforePut = 0;
		boolean otherStatementsBeforePut = false;
		int beginningOfThenBlockIndex = ((Statement)blockStatements.get(0)).getStartPosition();
		Expression invocationOfPut = null;
		Statement statement = null;
		
		for(Object object : blockStatements) {
			statement = (Statement)object;
			numberOfStatementsBeforePut++;
			if(statement instanceof ExpressionStatement) {
				invocationOfPut = ((ExpressionStatement) statement).getExpression();
				if(invocationOfPut instanceof MethodInvocation) {
					if(checkMethodNameAndBinding((MethodInvocation)invocationOfPut, "put")) {
						numberOfStatementsBeforePut--;
						break;
					}
				}
			}
		}
		
		Statement invocationOfPutStatement = (Statement) ASTNodes.getParent(invocationOfPut, Statement.class);
		final ASTNode secondArgumentToPut = (ASTNode) ((MethodInvocation)invocationOfPut).arguments().get(((MethodInvocation)invocationOfPut).arguments().size()-1);
		
		// Handle statements before put (createValue)
		if(beginningOfThenBlockIndex < invocationOfPut.getStartPosition()) {
			int beforePutStatementIndex = invocationOfPut.getStartPosition()-1;
			
			// If size of statements in BEFORE_PUT == 1, don't create a new method, but use that statement directly
			ReferencesFinder rf = new ReferencesFinder();
			if(secondArgumentToPut instanceof SimpleName) {
				rf.findReferences(ifStatement, (SimpleName) secondArgumentToPut, ifStatement.getStartPosition(), invocationOfPut.getStartPosition());
			}
			
			if(rf.getWriteReferences() != null && rf.hasWriteReferences()) {
				if(numberOfStatementsBeforePut > 1) {
					
					if(rf.getWriteReferences().size() == 1) {
						ASTNode theWrite = rf.getWriteReferences().get(0);
						Statement theWriteStatement = (Statement) ASTNodes.getParent(theWrite, Statement.class);
						fRewriter.remove(theWriteStatement, createGroupDescription(REMOVE_STATEMENT));
					}
					
					fMethodName = "create" + secondArgumentToPut;
					Block enclosingBlock = (Block) ASTNodes.getParent(invocationOfPut, Block.class);
					checkSideEffects(fCUnit, enclosingBlock, beginningOfThenBlockIndex, beforePutStatementIndex - beginningOfThenBlockIndex);
					
					ExtractMethodRefactoring extractMethodRefactoring = 
						new ExtractMethodRefactoring(fCUnit, beginningOfThenBlockIndex, beforePutStatementIndex - beginningOfThenBlockIndex);
					
					extractMethodRefactoring.setMethodName(fMethodName);
					extractMethodRefactoring.setVisibility(Modifier.PRIVATE);
					
					try {
						extractMethodRefactoring.checkAllConditions(new NullProgressMonitor());
						Change extractMethodChanges = extractMethodRefactoring.createChange(new NullProgressMonitor());
						TextEditGroup textEditGroupForNewCreateValueMethod= extractCreateValueMethod((CompilationUnitChange)extractMethodChanges);
						TextEditGroup methodInvocationTextEditGroup= getMethodInvocationTextEdit((CompilationUnitChange)extractMethodChanges);
						refactoring.setExtractMethodTextEdit(textEditGroupForNewCreateValueMethod);
						refactoring.setRemoveSetMethodInvocationEdit(methodInvocationTextEditGroup);
						
						TextEdit[] textEditsForMethodInvocation = methodInvocationTextEditGroup.getTextEdits();
						for (TextEdit editForMethodInvocation : textEditsForMethodInvocation) {
							if (editForMethodInvocation instanceof InsertEdit) {
								InsertEdit insertMethodInvocation = (InsertEdit) editForMethodInvocation;
								String textToBeInserted = insertMethodInvocation.getText();
								int startMethodInvocation = textToBeInserted.indexOf(fMethodName);
								int endMethodInvocation = textToBeInserted.indexOf(";");
								methodInvocationText = textToBeInserted.substring(startMethodInvocation, endMethodInvocation);
							}
						}
						
						MultiTextEdit muxEdit = (MultiTextEdit) textEditsForMethodInvocation[0].getParent().getParent();
						IDocument scratchDocument = new Document(fCUnit.getSource());
						muxEdit.apply(scratchDocument);
						
						ASTParser parser = ASTParser.newParser(AST.JLS3);
						parser.setSource(scratchDocument.get().toCharArray());
						CompilationUnit scratchCU = (CompilationUnit)parser.createAST(null);
						final TypeDeclaration[] declaringClass = new TypeDeclaration[1];
						scratchCU.accept(new ASTVisitor() {
							public boolean visit(TypeDeclaration typedecl){
								if (typedecl.getName().getIdentifier().equals(fFieldBinding.getDeclaringClass().getName())) {
									declaringClass[0] = typedecl;
								}
								return true;
							}
						});
						
						MethodDeclaration[] methodsInRefactoredClass = declaringClass[0].getMethods();
						for (MethodDeclaration methodDeclaration : methodsInRefactoredClass) {
							if (methodDeclaration.getName().getIdentifier().equals(fMethodName)) {
								int startPositionForMethodCode = methodDeclaration.getStartPosition();
								int lengthOfMethod = methodDeclaration.getLength();
								codeForCreateValueMethod = scratchDocument.get(startPositionForMethodCode, lengthOfMethod);
								
								break;
							}
						}
						
					} catch (Exception e) {
						fStatus.addFatalError("Error occurred while handling then-clause with multiple statements: " + e.getMessage());
						return false;
					}
				} else if(numberOfStatementsBeforePut == 1) {
					
					if(rf.getWriteReferences().size() == 1) {
						ASTNode writeStatement = rf.getWriteReferences().get(0);
						fRewriter.remove(writeStatement, createGroupDescription(REMOVE_STATEMENT));
					}
					
					Block thenStatement = ((Block)ifStatement.getThenStatement());
					List oldThenStatements = thenStatement.statements();
					
					rf = new ReferencesFinder();
					if(secondArgumentToPut instanceof SimpleName) {
						rf.findReferences(ifStatement, (SimpleName) secondArgumentToPut, invocationOfPut.getStartPosition() + invocationOfPut.getLength(), thenStatement.getStartPosition() + thenStatement.getLength());

						// If this value is read in AFTER_PUT or AFTER_IF, can't do this
						if(!rf.hasReadReferences()) {
							// TODO can generalize this to a search - try to find only one write of this variable
							ASTNode blockStat = (ASTNode) blockStatements.get(blockStatements.indexOf(invocationOfPutStatement) - 1);
							if(blockStat instanceof VariableDeclarationStatement) {
								VariableDeclarationFragment vdf = (VariableDeclarationFragment) ((VariableDeclarationStatement)blockStat).fragments().get(0);
								// If the left side matches the value argument, set replacementForValue to the right side
								if(secondArgumentToPut.subtreeMatch(new JdtASTMatcher(), vdf.getName())) {
									replacementForValue = vdf.getInitializer();
								}
								
							} else if(blockStat instanceof ExpressionStatement) {
								ExpressionStatement createdValueInit = (ExpressionStatement)blockStat;
								if(createdValueInit.getExpression() instanceof Assignment) {
									Assignment assignment = (Assignment) createdValueInit.getExpression();
									
									// If the left side matches the value argument, set replacementForValue to the right side
									if(secondArgumentToPut.subtreeMatch(new JdtASTMatcher(), assignment.getLeftHandSide())) {
										replacementForValue = assignment.getRightHandSide();
									}
								}
							}
						}
					}
				}
			}
		}
						
		if(numberOfStatementsBeforePut > 0) {
			otherStatementsBeforePut = true;
		}

		// Handle statements after put
		Statement thenStatement = ifStatement.getThenStatement();
		if(thenStatement instanceof Block) {
			List thenStatements = ((Block)thenStatement).statements();
			
			int indexOfPutInvocation = thenStatements.indexOf(invocationOfPutStatement);
			if(indexOfPutInvocation != thenStatements.size()-1) {
				replaceIfStatement(ifStatement, hashMapKey, equalsValue, statement, "putIfAbsent", methodInvocationText,
						codeForCreateValueMethod, removeStatementBeforeIf, otherStatementsBeforePut, true, replacementForValue);
			} else {
				replaceIfStatement(ifStatement, hashMapKey, equalsValue, statement, "putIfAbsent", methodInvocationText,
						codeForCreateValueMethod, removeStatementBeforeIf, otherStatementsBeforePut, false, replacementForValue);
			}
		} else {
			replaceIfStatement(ifStatement, hashMapKey, equalsValue, statement, "putIfAbsent", methodInvocationText,
					codeForCreateValueMethod, removeStatementBeforeIf, otherStatementsBeforePut, false, replacementForValue);	
		}
			
		MethodDeclaration methodDeclaringTheIfStatement = (MethodDeclaration) ASTNodes.getParent(ifStatement, MethodDeclaration.class);
		insertCreateValueMethod(codeForCreateValueMethod, methodDeclaringTheIfStatement);

		return true;
	}
	
	private void checkSideEffects(ICompilationUnit unit,
			Block enclosingBlock, int indexBeginningStatement, int indexEndStatement) {
		SideEffectsFinder sideEffectsFinder = new SideEffectsFinder (unit, enclosingBlock, indexBeginningStatement, indexEndStatement, fFieldBinding, fStatus);
		sideEffectsFinder.findEffects();
		
	}

	private void insertCreateValueMethod(String codeForCreateValueMethod, MethodDeclaration methodContainingPutIfAbsent) {
		if(codeForCreateValueMethod == null) return;
		
		// TODO use CodeFormatter instead of the replaceAll below
		//CodeFormatter cf = new DefaultCodeFormatter();
		// Gives TextEdits, then apply the TextEdits
		
		// Removes excess indentation.
		String codeForCreateValueMethodUnindented = codeForCreateValueMethod.replaceAll("\n\t", "\n");
		ChildListPropertyDescriptor descriptor= getBodyDeclarationsProperty(fDeclaringClass);
		ASTNode nodeForCreateValueMethod = fRewriter.createStringPlaceholder(codeForCreateValueMethodUnindented, ASTNode.METHOD_DECLARATION);
		
		// TODO Use ReferencesFinder to find possible side-effects
		// ReferencesFinder rf = new ReferencesFinder();
		//Assert.isTrue(fDeclaringClass != null, "null declaring class");
		fRewriter.getListRewrite(fDeclaringClass, descriptor).insertAfter(nodeForCreateValueMethod, methodContainingPutIfAbsent, createGroupDescription("CreateValue() Method"));
	}
	
	private ChildListPropertyDescriptor getBodyDeclarationsProperty(ASTNode declaration) {
		if (declaration instanceof AnonymousClassDeclaration)
			return AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY;
		else if (declaration instanceof AbstractTypeDeclaration)
			return ((AbstractTypeDeclaration) declaration).getBodyDeclarationsProperty();
		else if(declaration instanceof MethodDeclaration) {
			return ((MethodDeclaration) declaration).getBody().STATEMENTS_PROPERTY;
		}
		Assert.isTrue(false);
		return null;
	}

	private void replaceIfStatement(IfStatement ifStatement, ASTNode hashMapKey, ASTNode equalsValue,
			Statement statement, String methodToReplaceWithName, String textForInvokingCreateValue, String codeForCreateValueMethod,
			boolean removeStatementBeforeIf, boolean statementsBeforePut, boolean statementsAfterPut, ASTNode replacementForValue ) {
		
		// TODO - Factor out all the common code. Right now, a lot of the branches are very similar. 
		
		AST ast = ifStatement.getAST();
		
		Expression thenStatementMethodInvocation = ((ExpressionStatement)statement).getExpression();
		if(thenStatementMethodInvocation instanceof MethodInvocation) {
			MethodInvocation methodInvoc = (MethodInvocation)thenStatementMethodInvocation;
			Statement methodInvocStatement = (Statement) ASTNodes.getParent(methodInvoc, Statement.class);
			MethodInvocation newMethodInvoc = ast.newMethodInvocation();
			
			ASTNode firstArgument = (ASTNode) methodInvoc.arguments().get(0);

			if(textForInvokingCreateValue != null) {
				List newListOfArguments = new ArrayList();
				ASTNode copyOfFirstArgument = ASTNode.copySubtree(ast, firstArgument);
				MethodInvocation newCreateValueMethodInvocation = ast.newMethodInvocation();
				String createValueMethodName = textForInvokingCreateValue.substring(0, textForInvokingCreateValue.indexOf("("));
				String createValueArgumentList = textForInvokingCreateValue.substring(textForInvokingCreateValue.indexOf("(")+1, textForInvokingCreateValue.indexOf(")"));
				if(!createValueArgumentList.isEmpty()) {
					String[] listOfArguments = createValueArgumentList.split(",");
					for (String argumentName : listOfArguments) {
						newCreateValueMethodInvocation.arguments().add(ast.newSimpleName(argumentName.trim()));
					}
				}
				newCreateValueMethodInvocation.setName(ast.newSimpleName(createValueMethodName));
				newMethodInvoc.arguments().add(copyOfFirstArgument);
				newMethodInvoc.arguments().add(newCreateValueMethodInvocation);
				replacementForValue = newCreateValueMethodInvocation;
			} else {
				newMethodInvoc.arguments().addAll(ASTNode.copySubtrees(ast, methodInvoc.arguments()));
				if(equalsValue != null) {
					if(methodToReplaceWithName.equals("replace"))
						newMethodInvoc.arguments().add(newMethodInvoc.arguments().size()-1, ASTNode.copySubtree(ast, equalsValue));
					else
						newMethodInvoc.arguments().add(ASTNode.copySubtree(ast, equalsValue));
				}
			}

			// Replace directly with the replacement for value if there are no reads in AFTER_PUT or AFTER_IF				
			if(replacementForValue != null) {
				newMethodInvoc.arguments().remove(newMethodInvoc.arguments().size()-1);
				newMethodInvoc.arguments().add(ASTNode.copySubtree(ast, replacementForValue));
			}
			
			if(firstArgument.subtreeMatch(new JdtASTMatcher(), hashMapKey)) {
				
				newMethodInvoc.setName(ast.newSimpleName(methodToReplaceWithName));
				newMethodInvoc.setExpression(ast.newSimpleName(fFieldBinding.getName()));
				Block enclosingBlock = (Block) ASTNodes.getParent(ifStatement, Block.class);
				int indexOfIfStatement = enclosingBlock.statements().indexOf(ifStatement);
				int indexOfMethodInvoc = -1;
				Statement thenStatement = ifStatement.getThenStatement();
				if(thenStatement instanceof Block) {
					indexOfMethodInvoc = ((Block)thenStatement).statements().indexOf(methodInvocStatement);
				} else {
					indexOfMethodInvoc = 0;
				}
				
				ReferencesFinder rf = new ReferencesFinder();
				ReferencesFinder wf = new ReferencesFinder();
				ASTNode secondPutArgument = (ASTNode) methodInvoc.arguments().get(methodInvoc.arguments().size()-1);
				if(secondPutArgument instanceof Name) {
					wf.findReferences(ifStatement, (Name)secondPutArgument, ifStatement.getThenStatement().getStartPosition(), methodInvocStatement.getStartPosition());
					rf.findReferences(enclosingBlock, (Name)secondPutArgument, methodInvocStatement.getStartPosition() + methodInvocStatement.getLength(), enclosingBlock.getStartPosition() + enclosingBlock.getLength());
				}
				
				if(!statementsAfterPut &&
						(!statementsBeforePut ||
								(statementsBeforePut && indexOfMethodInvoc == 1 &&
										wf.getWriteReferences() != null && wf.getWriteReferences().size() == 1 &&
										rf.getReadReferences() != null && !rf.hasReadReferences()))) {
					// if() {
					//   put();		->		putIfAbsent();
					// }

					ExpressionStatement newMethodInvocStatement = ast.newExpressionStatement(newMethodInvoc);
					
					// Removes the statement before if
					if(removeStatementBeforeIf) {
						// TODO TODO - rewrite to find get statement
						ASTNode statementBeforeIf = (ASTNode) enclosingBlock.statements().get(indexOfIfStatement-1);
						fRewriter.remove(statementBeforeIf, createGroupDescription(REMOVE_STATEMENT));
					}				
					
					fRewriter.replace(ifStatement, newMethodInvocStatement, createGroupDescription(METHOD_INVOCATION));
					checkSynchronizedBlock(ifStatement, newMethodInvocStatement, METHOD_INVOCATION);
					checkSynchronizedMethod(ifStatement, newMethodInvocStatement, METHOD_INVOCATION);
				} else {
					IfStatement newIfStatement = (IfStatement) ASTNode.copySubtree(ast, ifStatement);
					Block thenBlock = ((Block)ifStatement.getThenStatement());
					List oldThenStatements = thenBlock.statements();
					List newThenStatements = ((Block)newIfStatement.getThenStatement()).statements();
					int indexOfMethodInvocation = oldThenStatements.indexOf(methodInvocStatement);

					if(indexOfMethodInvocation == -1)
						throw new RuntimeException("Cannot find method invocation.");
					
					if(codeForCreateValueMethod == null) {
						// No extract method, possibly creation with one statement or no creation
						
						if(indexOfMethodInvocation == 1) {
							// If: BEFORE_PUT creates value with one statement and either AFTER_PUT uses it or does not
							//
							// value = get()
							// createdValue = [SINGLE_STATEMENT_FROM_BEFORE_PUT];
							// if(putIfAbsent(createdValue) == null) {
							//   +[value = createdValue]
							//   AFTER_PUT
							// }
							
							ReferencesFinder readFinder = new ReferencesFinder();
							MethodDeclaration methDecl = (MethodDeclaration) ASTNodes.getParent(ifStatement, MethodDeclaration.class);
							if(secondPutArgument instanceof Name) {
								readFinder.findReferences(methDecl, (Name)secondPutArgument, methodInvocStatement.getStartPosition() + methodInvocStatement.getLength(),
										methDecl.getStartPosition() + methDecl.getLength());
							}
							
							// Creates the if(putIfAbsent(...) == null) { ... }
							InfixExpression isNullExpression = ast.newInfixExpression();
							isNullExpression.setLeftOperand(newMethodInvoc);
							isNullExpression.setOperator(Operator.EQUALS);
							isNullExpression.setRightOperand(ast.newNullLiteral());
							newIfStatement.setExpression(isNullExpression);
							
							rf = new ReferencesFinder();
							if(secondPutArgument instanceof Name) {
								rf.findReferences(ifStatement, (Name)secondPutArgument, thenBlock.getStartPosition(), methodInvocStatement.getStartPosition());
								
								if(rf.getWriteReferences().size() == 1) {
									ASTNode theCreateValueWrite = rf.getWriteReferences().get(0);
									ASTNode theCreateValueWriteStatement = ASTNodes.getParent(theCreateValueWrite, Statement.class);
									fRewriter.remove(theCreateValueWriteStatement, createGroupDescription(REMOVE_STATEMENT));
								}
							}
							
							
							// Remove all BEFORE_PUT statements and the method invocation
							for(int i = 0; i <= indexOfMethodInvocation; i++) {
								newThenStatements.remove(0);
							}

							// AFTER_PUT uses the single-statement creation
							if(readFinder.getReadReferences() != null && readFinder.hasReadReferences()) {
								
								Block newEnclosingBlock = (Block) ASTNode.copySubtree(ast, enclosingBlock);
								List newEnclosingBlockStatements = newEnclosingBlock.statements();
								Statement theCreateValueStat = (Statement) ASTNodes.getParent(rf.getWriteReferences().get(0), Statement.class);
								Expression theCreateValueExpStat = null;
								if(theCreateValueStat instanceof ExpressionStatement) {
									theCreateValueExpStat = ((ExpressionStatement)theCreateValueStat).getExpression();
								} else if(theCreateValueStat instanceof VariableDeclarationStatement) {
									VariableDeclarationFragment vdf = (VariableDeclarationFragment) ((VariableDeclarationStatement)theCreateValueStat).fragments().get(0);
									theCreateValueExpStat = vdf.getInitializer();
								}
								
								if(theCreateValueExpStat instanceof Assignment) {
									theCreateValueExpStat = ((Assignment)theCreateValueExpStat).getRightHandSide();
								}
										
								// Create items relevant to <Type> value = createValue(...);
								ASTNode valueArgument = (ASTNode) methodInvoc.arguments().get(1);
								VariableDeclarationFragment newVDF = ast.newVariableDeclarationFragment();
								SimpleName nameOfCreatedValue = (SimpleName) ASTNode.copySubtree(ast, getName(valueArgument));
								SimpleName newNameOfCreatedValue = (SimpleName) ASTNode.copySubtree(ast, getName(valueArgument));
								newNameOfCreatedValue.setIdentifier("created" + newNameOfCreatedValue.getIdentifier());
								newVDF.setName(newNameOfCreatedValue);
								newVDF.setInitializer((Expression) ASTNode.copySubtree(ast,theCreateValueExpStat));
								VariableDeclarationStatement newVariableFromCreateValueDeclStatement = ast.newVariableDeclarationStatement(newVDF);
								if(valueArgument instanceof MethodInvocation) {
									String newType = ((MethodInvocation)valueArgument).resolveTypeBinding().getName();
									SimpleName newSimpleName = ast.newSimpleName(newType);
									newVariableFromCreateValueDeclStatement.setType(ast.newSimpleType(newSimpleName));
								} else if(valueArgument instanceof SimpleName) {
									String newType = ((SimpleName)valueArgument).resolveTypeBinding().getName();
									SimpleName newSimpleName = ast.newSimpleName(newType);
									newVariableFromCreateValueDeclStatement.setType(ast.newSimpleType(newSimpleName));
								} else {
									newVariableFromCreateValueDeclStatement.setType(ast.newSimpleType(ast.newName("Object")));
								}
								
								Assignment setOldValueToCreatedValueExp = ast.newAssignment();
								setOldValueToCreatedValueExp.setLeftHandSide(nameOfCreatedValue);
								setOldValueToCreatedValueExp.setRightHandSide((Expression) ASTNode.copySubtree(ast, newNameOfCreatedValue));
								Statement setOldValueToCreatedValueStatement = ast.newExpressionStatement(setOldValueToCreatedValueExp);
								newThenStatements.add(0, setOldValueToCreatedValueStatement);
								
								newEnclosingBlockStatements.add(indexOfIfStatement, newVariableFromCreateValueDeclStatement);
								newMethodInvoc.arguments().remove(1);
								newMethodInvoc.arguments().add(ASTNode.copySubtree(ast, newNameOfCreatedValue));
								
								newEnclosingBlockStatements.remove(indexOfIfStatement+1);
								newEnclosingBlockStatements.add(indexOfIfStatement+1, newIfStatement);
								fRewriter.replace(enclosingBlock, newEnclosingBlock, createGroupDescription(METHOD_INVOCATION));
							} else {
								if(!statementsBeforePut) {
									if(indexOfIfStatement > 0) {
										ASTNode statementBeforeIf = (ASTNode) enclosingBlock.statements().get(indexOfIfStatement-1);  
										fRewriter.remove(statementBeforeIf, createGroupDescription(REMOVE_STATEMENT));
									}
									ExpressionStatement newMethodInvocStatement = ast.newExpressionStatement((Expression) ASTNode.copySubtree(ast, newMethodInvoc));
									fRewriter.replace(ifStatement, newMethodInvocStatement, createGroupDescription(METHOD_INVOCATION));
									checkSynchronizedBlock(ifStatement, newMethodInvocStatement, METHOD_INVOCATION);
									checkSynchronizedMethod(ifStatement, newMethodInvocStatement, METHOD_INVOCATION);
								} else {
									if(replacementForValue == null) {
										newThenStatements.add(ASTNode.copySubtree(ast, (ASTNode) oldThenStatements.get(0)));
									}
									
									if(indexOfIfStatement > 0) {
										ASTNode statementBeforeIf = (ASTNode) enclosingBlock.statements().get(indexOfIfStatement-1);  
										fRewriter.remove(statementBeforeIf, createGroupDescription(REMOVE_STATEMENT));
									}
									ExpressionStatement newMethodInvocStatement = ast.newExpressionStatement((Expression) ASTNode.copySubtree(ast, newMethodInvoc));
									fRewriter.replace(ifStatement, newIfStatement, createGroupDescription(METHOD_INVOCATION));
								}
							}
							
						} else {
							// If: BEFORE_PUT does not create value and either AFTER_PUT uses it or does not 
							// if(putIfAbsent() == null) {
							//   BEFORE_PUT
							//   AFTER_PUT
							// }
							
							// Creates the if(putIfAbsent(...) == null) { ... }
							InfixExpression isNullExpression = ast.newInfixExpression();
							isNullExpression.setLeftOperand(newMethodInvoc);
							isNullExpression.setOperator(Operator.EQUALS);
							isNullExpression.setRightOperand(ast.newNullLiteral());
							newIfStatement.setExpression(isNullExpression);
							
							// Remove the statement before if
							if(removeStatementBeforeIf) {
								// TODO TODO - rewrite to find get statement
								ASTNode statementBeforeIf = (ASTNode) enclosingBlock.statements().get(indexOfIfStatement-1);
								fRewriter.remove(statementBeforeIf, createGroupDescription(REMOVE_STATEMENT));
							}
							
							newThenStatements.remove(indexOfMethodInvocation);
							
							if(replacementForValue != null) {
								if(rf.getWriteReferences() != null && rf.getWriteReferences().size() == 1) {
									Statement theWriteStatement = (Statement) ASTNodes.getParent(rf.getWriteReferences().get(0), Statement.class);
									int indexOfWriteStatement = ((Block)ifStatement.getThenStatement()).statements().indexOf(theWriteStatement);
									((Block)newIfStatement.getThenStatement()).statements().remove(indexOfWriteStatement);
								}
							}
							
							fRewriter.replace(ifStatement, newIfStatement, createGroupDescription(METHOD_INVOCATION));
						}
					} else {
						// If: BEFORE_PUT extracts method, creates value, and either AFTER_PUT uses it or does not
						Expression ifExpression = ifStatement.getExpression();
						if(ifExpression instanceof PrefixExpression) {
							ifExpression = ((PrefixExpression)ifExpression).getOperand();
						}
						
						if(ifExpression instanceof MethodInvocation && ((MethodInvocation)ifExpression).getName().getIdentifier().equals("containsKey")) {
							// if(containsKey()) {
							//   put();		->		putIfAbsent();
							// }
							
							ReferencesFinder readFinder = new ReferencesFinder();
							MethodDeclaration methDecl = (MethodDeclaration) ASTNodes.getParent(ifStatement, MethodDeclaration.class);
							if(secondPutArgument instanceof Name) {
								readFinder.findReferences(methDecl, (Name)secondPutArgument, methodInvocStatement.getStartPosition() + methodInvocStatement.getLength(),
										methDecl.getStartPosition() + methDecl.getLength());
							}
							
							if(statementsAfterPut || (readFinder.getReadReferences() != null && readFinder.hasReadReferences())) {
								InfixExpression isNullExpression = ast.newInfixExpression();
								isNullExpression.setLeftOperand(newMethodInvoc);
								isNullExpression.setOperator(Operator.EQUALS);
								isNullExpression.setRightOperand(ast.newNullLiteral());
								newIfStatement.setExpression(isNullExpression);
								
								rf = new ReferencesFinder();
								if(secondPutArgument instanceof Name) {
									rf.findReferences(ifStatement, (Name)secondPutArgument, thenBlock.getStartPosition(), methodInvocStatement.getStartPosition());
									
									if(rf.getWriteReferences().size() == 1) {
										ASTNode theCreateValueWrite = rf.getWriteReferences().get(0);
										ASTNode theCreateValueWriteStatement = ASTNodes.getParent(theCreateValueWrite, Statement.class);
										fRewriter.remove(theCreateValueWriteStatement, createGroupDescription(REMOVE_STATEMENT));
									}
								}
								
								Block newEnclosingBlock = (Block) ASTNode.copySubtree(ast, enclosingBlock);
								List newEnclosingBlockStatements = newEnclosingBlock.statements();
								
								// Remove all BEFORE_PUT statements and the method invocation
								for(int i = 0; i <= indexOfMethodInvocation; i++) {
									newThenStatements.remove(0);
								}
								
								if(codeForCreateValueMethod != null) {
									if(!getCreateValueReturnType(codeForCreateValueMethod).equals("void")) {
										
										// Create items relevant to <Type> value = createValue(...);
										ASTNode valueArgument = (ASTNode) methodInvoc.arguments().get(1);
										MethodInvocation newCreateValueMethodInvocation = (MethodInvocation) fRewriter.createStringPlaceholder(textForInvokingCreateValue, ASTNode.METHOD_INVOCATION);
										VariableDeclarationFragment newVDF = ast.newVariableDeclarationFragment();
										SimpleName nameOfCreatedValue = (SimpleName) ASTNode.copySubtree(ast, getName(valueArgument));
										SimpleName newNameOfCreatedValue = (SimpleName) ASTNode.copySubtree(ast, getName(valueArgument));
										newNameOfCreatedValue.setIdentifier("created" + newNameOfCreatedValue.getIdentifier());
										newVDF.setName(newNameOfCreatedValue);
										newVDF.setInitializer(newCreateValueMethodInvocation);
										VariableDeclarationStatement newVariableFromCreateValueDeclStatement = ast.newVariableDeclarationStatement(newVDF);
										String returnType = getCreateValueReturnType(codeForCreateValueMethod);
										newVariableFromCreateValueDeclStatement.setType(ast.newSimpleType(ast.newName(returnType)));
										
										ReferencesFinder readF = new ReferencesFinder();
										readF.findReferences(ifStatement, (Name)secondPutArgument, methodInvocStatement.getStartPosition() + methodInvocStatement.getLength(),
												ifStatement.getStartPosition() + ifStatement.getLength());
										if(!readF.hasReadReferences()) {
											newEnclosingBlockStatements.remove(indexOfIfStatement);
											newEnclosingBlockStatements.add(indexOfIfStatement, newIfStatement);
											fRewriter.replace(enclosingBlock, newEnclosingBlock, createGroupDescription(METHOD_INVOCATION));
										} else {
											Assignment setOldValueToCreatedValueExp = ast.newAssignment();
											setOldValueToCreatedValueExp.setLeftHandSide(nameOfCreatedValue);
											setOldValueToCreatedValueExp.setRightHandSide((Expression) ASTNode.copySubtree(ast, newNameOfCreatedValue));
											Statement setOldValueToCreatedValueStatement = ast.newExpressionStatement(setOldValueToCreatedValueExp);
											newThenStatements.add(0, setOldValueToCreatedValueStatement);
											
											newEnclosingBlockStatements.add(indexOfIfStatement, newVariableFromCreateValueDeclStatement);
											newMethodInvoc.arguments().remove(1);
											newMethodInvoc.arguments().add(ASTNode.copySubtree(ast, newNameOfCreatedValue));
											
											newEnclosingBlockStatements.remove(indexOfIfStatement+1);
											newEnclosingBlockStatements.add(indexOfIfStatement+1, newIfStatement);
											fRewriter.replace(enclosingBlock, newEnclosingBlock, createGroupDescription(METHOD_INVOCATION));
										}
									} else {
										fRewriter.replace(ifStatement, newIfStatement, createGroupDescription(METHOD_INVOCATION));
									}
								} else {
									if(codeForCreateValueMethod == null) {
										// putIfAbsent(replacementForValue) {
										// 	 AFTER_PUT
										// }
										
										newMethodInvoc.arguments().remove(newMethodInvoc.arguments().size()-1);
										newMethodInvoc.arguments().add(ASTNode.copySubtree(ast, replacementForValue));
									} else {
										// TODO anything here?
									}
									
									fRewriter.replace(ifStatement, newIfStatement, createGroupDescription(METHOD_INVOCATION));
								}
							} else {
								ExpressionStatement newMethodInvocStatement = ast.newExpressionStatement(newMethodInvoc);
								fRewriter.replace(ifStatement, newMethodInvocStatement, createGroupDescription(METHOD_INVOCATION));
								checkSynchronizedBlock(ifStatement, newMethodInvocStatement, METHOD_INVOCATION);
								checkSynchronizedMethod(ifStatement, newMethodInvocStatement, METHOD_INVOCATION);
							}
							
						} else{
							// value = get()
							// createdValue = createValue();
							// if(putIfAbsent(createdValue) == null) {
							//   BEFORE_PUT -> extract method
							//   +[value = createdValue]
							//   AFTER_PUT
							// }
							
							ReferencesFinder readFinder = new ReferencesFinder();
							MethodDeclaration methDecl = (MethodDeclaration) ASTNodes.getParent(ifStatement, MethodDeclaration.class);
							readFinder.findReferences(methDecl, (Name)secondPutArgument, methodInvocStatement.getStartPosition() + methodInvocStatement.getLength(),
									methDecl.getStartPosition() + methDecl.getLength());
							
							if(statementsAfterPut || (readFinder.getReadReferences() != null && readFinder.hasReadReferences())) {
								// Creates the if(putIfAbsent(...) == null) { ... }
								InfixExpression isNullExpression = ast.newInfixExpression();
								isNullExpression.setLeftOperand(newMethodInvoc);
								isNullExpression.setOperator(Operator.EQUALS);
								isNullExpression.setRightOperand(ast.newNullLiteral());
								newIfStatement.setExpression(isNullExpression);
								
								rf = new ReferencesFinder();
								if(secondPutArgument instanceof Name) {
									rf.findReferences(ifStatement, (Name)secondPutArgument, thenBlock.getStartPosition(), methodInvocStatement.getStartPosition());
									
									if(rf.getWriteReferences().size() == 1) {
										ASTNode theCreateValueWrite = rf.getWriteReferences().get(0);
										ASTNode theCreateValueWriteStatement = ASTNodes.getParent(theCreateValueWrite, Statement.class);
										fRewriter.remove(theCreateValueWriteStatement, createGroupDescription(REMOVE_STATEMENT));
									}
								}
								
								Block newEnclosingBlock = (Block) ASTNode.copySubtree(ast, enclosingBlock);
								List newEnclosingBlockStatements = newEnclosingBlock.statements();
								
								// Remove all BEFORE_PUT statements and the method invocation
								for(int i = 0; i <= indexOfMethodInvocation; i++) {
									newThenStatements.remove(0);
								}
								
								if(codeForCreateValueMethod != null) {
									if(!getCreateValueReturnType(codeForCreateValueMethod).equals("void")) {
										
										// Create items relevant to <Type> value = createValue(...);
										ASTNode valueArgument = (ASTNode) methodInvoc.arguments().get(1);
										MethodInvocation newCreateValueMethodInvocation = (MethodInvocation) fRewriter.createStringPlaceholder(textForInvokingCreateValue, ASTNode.METHOD_INVOCATION);
										VariableDeclarationFragment newVDF = ast.newVariableDeclarationFragment();
										SimpleName nameOfCreatedValue = (SimpleName) ASTNode.copySubtree(ast, getName(valueArgument));
										SimpleName newNameOfCreatedValue = (SimpleName) ASTNode.copySubtree(ast, getName(valueArgument));
										newNameOfCreatedValue.setIdentifier("created" + newNameOfCreatedValue.getIdentifier());
										newVDF.setName(newNameOfCreatedValue);
										newVDF.setInitializer(newCreateValueMethodInvocation);
										VariableDeclarationStatement newVariableFromCreateValueDeclStatement = ast.newVariableDeclarationStatement(newVDF);
										String returnType = getCreateValueReturnType(codeForCreateValueMethod);
										newVariableFromCreateValueDeclStatement.setType(ast.newSimpleType(ast.newName(returnType)));
										
										Assignment setOldValueToCreatedValueExp = ast.newAssignment();
										setOldValueToCreatedValueExp.setLeftHandSide(nameOfCreatedValue);
										setOldValueToCreatedValueExp.setRightHandSide((Expression) ASTNode.copySubtree(ast, newNameOfCreatedValue));
										Statement setOldValueToCreatedValueStatement = ast.newExpressionStatement(setOldValueToCreatedValueExp);
										newThenStatements.add(0, setOldValueToCreatedValueStatement);
										
										newEnclosingBlockStatements.add(indexOfIfStatement, newVariableFromCreateValueDeclStatement);
										newMethodInvoc.arguments().remove(1);
										newMethodInvoc.arguments().add(ASTNode.copySubtree(ast, newNameOfCreatedValue));
										
										newEnclosingBlockStatements.remove(indexOfIfStatement+1);
										newEnclosingBlockStatements.add(indexOfIfStatement+1, newIfStatement);
										fRewriter.replace(enclosingBlock, newEnclosingBlock, createGroupDescription(METHOD_INVOCATION));
									} else {
										fRewriter.replace(ifStatement, newIfStatement, createGroupDescription(METHOD_INVOCATION));
									}
								} else {
									if(codeForCreateValueMethod == null) {
										// putIfAbsent(replacementForValue) {
										// 	 AFTER_PUT
										// }
										
										newMethodInvoc.arguments().remove(newMethodInvoc.arguments().size()-1);
										newMethodInvoc.arguments().add(ASTNode.copySubtree(ast, replacementForValue));
									} else {
										throw new RuntimeException("Unexpected code path");
									}
									
									fRewriter.replace(ifStatement, newIfStatement, createGroupDescription(METHOD_INVOCATION));
								}
							} else {
								// value = get()
								// createdValue = createValue();
								// if(putIfAbsent(createdValue) == null) {
								//   +[value = createdValue]
								// }
								
								// Creates the if(putIfAbsent(...) == null) { ... }
								InfixExpression isNullExpression = ast.newInfixExpression();
								isNullExpression.setLeftOperand(newMethodInvoc);
								isNullExpression.setOperator(Operator.EQUALS);
								isNullExpression.setRightOperand(ast.newNullLiteral());
								newIfStatement.setExpression(isNullExpression);
								
								Block newEnclosingBlock = (Block) ASTNode.copySubtree(ast, enclosingBlock);
								List newEnclosingBlockStatements = newEnclosingBlock.statements();
								
								// Remove all BEFORE_PUT statements and the method invocation
								for(int i = 0; i <= indexOfMethodInvocation; i++) {
									newThenStatements.remove(0);
								}

								if(codeForCreateValueMethod != null) {
									if(!getCreateValueReturnType(codeForCreateValueMethod).equals("void")) {
										
										// Create items relevant to <Type> value = createValue(...);
										ASTNode valueArgument = (ASTNode) methodInvoc.arguments().get(1);
										MethodInvocation newCreateValueMethodInvocation = (MethodInvocation) fRewriter.createStringPlaceholder(textForInvokingCreateValue, ASTNode.METHOD_INVOCATION);
										VariableDeclarationFragment newVDF = ast.newVariableDeclarationFragment();
										SimpleName nameOfCreatedValue = (SimpleName) ASTNode.copySubtree(ast, getName(valueArgument));
										SimpleName newNameOfCreatedValue = (SimpleName) ASTNode.copySubtree(ast, getName(valueArgument));
										newNameOfCreatedValue.setIdentifier("created" + newNameOfCreatedValue.getIdentifier());
										newVDF.setName(newNameOfCreatedValue);
										newVDF.setInitializer(newCreateValueMethodInvocation);
										VariableDeclarationStatement newVariableFromCreateValueDeclStatement = ast.newVariableDeclarationStatement(newVDF);
										String returnType = getCreateValueReturnType(codeForCreateValueMethod);
										newVariableFromCreateValueDeclStatement.setType(ast.newSimpleType(ast.newName(returnType)));
										
										Assignment setOldValueToCreatedValueExp = ast.newAssignment();
										setOldValueToCreatedValueExp.setLeftHandSide(nameOfCreatedValue);
										setOldValueToCreatedValueExp.setRightHandSide((Expression) ASTNode.copySubtree(ast, newNameOfCreatedValue));
										Statement setOldValueToCreatedValueStatement = ast.newExpressionStatement(setOldValueToCreatedValueExp);
										newThenStatements.add(0, setOldValueToCreatedValueStatement);
										
										newEnclosingBlockStatements.add(indexOfIfStatement, newVariableFromCreateValueDeclStatement);
										newMethodInvoc.arguments().remove(1);
										newMethodInvoc.arguments().add(ASTNode.copySubtree(ast, newNameOfCreatedValue));
										
										newEnclosingBlockStatements.remove(indexOfIfStatement+1);
										newEnclosingBlockStatements.add(indexOfIfStatement+1, newIfStatement);
										fRewriter.replace(enclosingBlock, newEnclosingBlock, createGroupDescription(METHOD_INVOCATION));
									} else {
										fRewriter.replace(ifStatement, newIfStatement, createGroupDescription(METHOD_INVOCATION));
									}
								} else {
									if(codeForCreateValueMethod == null) {
										// putIfAbsent(replacementForValue) {
										// 	 AFTER_PUT
										// }
										
										newMethodInvoc.arguments().remove(newMethodInvoc.arguments().size()-1);
										newMethodInvoc.arguments().add(ASTNode.copySubtree(ast, replacementForValue));
									} else {
										throw new RuntimeException("Unexpected code path");
									}
									
									fRewriter.replace(ifStatement, newIfStatement, createGroupDescription(METHOD_INVOCATION));
								}
							}
						}
					}
				}
				usingCHMOnlyMethods = true;
			}
		}
	}
	
	private String getCreateValueReturnType(String codeForCreateValueMethod) {
		for(String s : codeForCreateValueMethod.split(" ")) {
			if(s.equals("public") || s.equals("private") || s.equals("protected") || s.equals("package") ||
			   s.equals("static") || s.equals("final") || s.equals("abstract") || s.equals("transient") ||
			   s.equals("volatile")) {
				continue;
			} else {
				return s;
			}
		}
		
		return null;
	}

	private TextEditGroup getMethodInvocationTextEdit(
			CompilationUnitChange extractMethodChanges) {
		
		TextEditBasedChangeGroup[] changeGroups = extractMethodChanges.getChangeGroups();
		for (TextEditBasedChangeGroup textEditBasedChangeGroup : changeGroups) {
			if(textEditBasedChangeGroup.getName().indexOf(Messages.format(RefactoringCoreMessages.ExtractMethodRefactoring_substitute_with_call, fMethodName)) != -1) {
				return textEditBasedChangeGroup.getTextEditGroup();
			}
		}
		
		return null;
	}

	private TextEditGroup extractCreateValueMethod(CompilationUnitChange extractMethodChanges) {
		TextEditBasedChangeGroup[] changeGroups = extractMethodChanges.getChangeGroups();
		for (TextEditBasedChangeGroup textEditBasedChangeGroup : changeGroups) {
			if(textEditBasedChangeGroup.getName().indexOf(Messages.format(RefactoringCoreMessages.ExtractMethodRefactoring_add_method, fMethodName)) != -1) {
				return textEditBasedChangeGroup.getTextEditGroup();
			}
		}
		return null;
	}

	private boolean checkMethodNameAndBinding(
			MethodInvocation methodInvocation, String methodName) {
		return considerBinding(resolveBinding(methodInvocation.getExpression())) && 
			methodInvocation.getName().getIdentifier().equals(methodName);
	}
	
	public boolean visit(MethodInvocation methodInvocNode) {
		
		Expression expression = methodInvocNode.getExpression();
		if(!(expression instanceof SimpleName)) {
			return false;
		}
		
		SimpleName expressionName = (SimpleName) expression;
				
		if(!considerBinding(expressionName.resolveBinding())) {
			return false;
		}

		String methodIdentifier = methodInvocNode.getName().getIdentifier();
		
		// clone() is the only method HashMap has that ConcurrentHashMap does not
		if(methodIdentifier.equals("clone")) {
			fStatus.addFatalError("Cannot refactor a method invocation for clone(): " +
					"ConcurrentHashMap has no such method.");
			
			return false;
		}
		
		boolean checkSynchronizedBlock = checkSynchronizedBlock(methodInvocNode, methodInvocNode, METHOD_INVOCATION);
		boolean checkSynchronizedMethod = checkSynchronizedMethod(methodInvocNode, methodInvocNode, METHOD_INVOCATION);
		// TODO check return type (medium priority) throw Exception if false?
		
		return true;
	}
	
	private boolean checkSynchronizedBlock(ASTNode node, ASTNode invocation, String accessType) {
		
		AST ast = node.getAST();
		ASTNode syncStatement = ASTNodes.getParent(node, SynchronizedStatement.class);
		final MethodDeclaration parentMethod = (MethodDeclaration) ASTNodes.getParent(node, MethodDeclaration.class);
		
		if(syncStatement == null)
			return false;
		
		Block parentBlockForSyncStatement = (Block) syncStatement.getParent();
		List parentBlockStatements = parentBlockForSyncStatement.statements();

		final int lineOfOldSyncStatement = parentBlockStatements.indexOf(syncStatement);
		final SynchronizedStatement[] newSyncStatement = new SynchronizedStatement[1];
	
		try {
			TextEdit rewrites = fRewriter.rewriteAST();
			IDocument scratchDocument = new Document(fCUnit.getSource());
			rewrites.apply(scratchDocument);
						
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(scratchDocument.get().toCharArray());
			CompilationUnit scratchCU = (CompilationUnit)parser.createAST(null);
			
			scratchCU.accept(new ASTVisitor() {
				public boolean visit(SynchronizedStatement syncStat){
					MethodDeclaration parentMeth = (MethodDeclaration) ASTNodes.getParent(syncStat, MethodDeclaration.class);
					if(parentMeth.getName().getIdentifier().equals(parentMethod.getName().getIdentifier())) {
						Block parentBlock = (Block) syncStat.getParent();
						List parentBlockStatements = parentBlock.statements();
						if(parentBlockStatements.indexOf(syncStat) == lineOfOldSyncStatement)
						{
							newSyncStatement[0] = syncStat;
							return false;
						}
					}
						
					return true;
				}
			});
		} catch (Exception e) {
			fStatus.addFatalError("Error occurred while checking for synchronized block: " + e.getMessage());
			return false;
		}
		
		if(syncStatement != null) {
			Block syncBody = newSyncStatement[0].getBody();
			List syncBodyStatements = syncBody.statements();
			
			if(syncBodyStatements.size() > 1) {
				fRewriter.replace(node, invocation, createGroupDescription(accessType));
			} else {
				ASTNode nodeParent = node.getParent();
				if(nodeParent instanceof ExpressionStatement) {
					fRewriter.replace(syncStatement, nodeParent, createGroupDescription(REMOVE_SYNCHRONIZED_BLOCK));
				} else {
					if(invocation instanceof Block) {
						fRewriter.replace(parentBlockForSyncStatement, invocation, createGroupDescription(REMOVE_SYNCHRONIZED_BLOCK));
					} else {
						fRewriter.replace(syncStatement, invocation, createGroupDescription(REMOVE_SYNCHRONIZED_BLOCK));
					}
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	private boolean checkSynchronizedMethod(ASTNode node,
			ASTNode invocation, String accessType) {
		
		fRewriter.replace(node, invocation, createGroupDescription(accessType));
			
		final MethodDeclaration methodDecl = (MethodDeclaration) ASTNodes.getParent(node, MethodDeclaration.class);
		final MethodDeclaration[] newMethodWithSync = new MethodDeclaration[1];
		
		try {
			TextEdit rewrites = fRewriter.rewriteAST();
			IDocument scratchDocument = new Document(fCUnit.getSource());
			rewrites.apply(scratchDocument);
						
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(scratchDocument.get().toCharArray());
			CompilationUnit scratchCU = (CompilationUnit)parser.createAST(null);
			
			scratchCU.accept(new ASTVisitor() {
				public boolean visit(MethodDeclaration methDecl){
					if (methDecl.getName().getIdentifier().equals(methodDecl.getName().getIdentifier())) {
						newMethodWithSync[0] = methDecl;
					}
					return true;
				}
			});
		} catch (Exception e) {
			fStatus.addFatalError("Error occurred while checking for synchronized method: " + e.getMessage());
			return false;
		}
		
		int modifiers = methodDecl.getModifiers();

		if(Modifier.isSynchronized(modifiers)) {
			List methodBodyStatements = newMethodWithSync[0].getBody().statements();
			
			if(methodBodyStatements.size() == 1) {
				ModifierRewrite methodRewriter = ModifierRewrite.create(fRewriter, methodDecl);
				int synchronized1 = Modifier.SYNCHRONIZED;
				synchronized1 = ~ synchronized1;
				int newModifiersWithoutSync = modifiers & synchronized1;
				methodRewriter.setModifiers(newModifiersWithoutSync, createGroupDescription(REMOVE_SYNCHRONIZED_MODIFIER));
			}
			return true;
		}
		
		return false;
	}
	
	private boolean considerBinding(IBinding binding) {
		if (!(binding instanceof IVariableBinding))
			return false;
		boolean result = Bindings.equals(fFieldBinding, ((IVariableBinding)binding).getVariableDeclaration());
		return result;
	}
	
	private IBinding resolveBinding(Expression expression) {
		if (expression instanceof SimpleName)
			return ((SimpleName)expression).resolveBinding();
		else if (expression instanceof QualifiedName)
			return ((QualifiedName)expression).resolveBinding();
		else if (expression instanceof FieldAccess)
			return ((FieldAccess)expression).getName().resolveBinding();
		else if (expression instanceof SuperFieldAccess)
			return ((SuperFieldAccess)expression).getName().resolveBinding();
		return null;
	}
	
	public void endVisit(CompilationUnit node) {
		fImportRewriter.addImport("java.util.concurrent.ConcurrentHashMap");
	}
	
	private TextEditGroup createGroupDescription(String name) {
		TextEditGroup result= new TextEditGroup(name);
		fGroupDescriptions.add(result);
		return result;
	}
	
	private SimpleName getName(ASTNode node) {
		int type= node.getNodeType();
		switch(type) {
			case ASTNode.SIMPLE_NAME:
				return ((SimpleName)node);
			case ASTNode.QUALIFIED_NAME:
				return ((QualifiedName)node).getName();
			case ASTNode.FIELD_ACCESS:
				return ((FieldAccess)node).getName();
			case ASTNode.SUPER_FIELD_ACCESS:
				return ((SuperFieldAccess)node).getName();
			case ASTNode.THIS_EXPRESSION:
				return (SimpleName) ((ThisExpression)node).getQualifier();
		}
		return null;
	}
}