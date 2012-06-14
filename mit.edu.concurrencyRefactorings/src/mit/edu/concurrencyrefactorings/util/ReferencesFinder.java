package mit.edu.concurrencyrefactorings.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.PrefixExpression.Operator;
import org.eclipse.jdt.internal.corext.dom.Bindings;

public class ReferencesFinder extends ASTVisitor {

	List<ASTNode> readReferences;
	List<ASTNode> writeReferences;
	private Name nodetoSearchFor;
	private IBinding targetBinding;

	public List<ASTNode> getReadReferences() {
		return readReferences;
	}
	
	public List<ASTNode> getWriteReferences() {
		return writeReferences;
	}
	
	/**
	 * 
	 * @param searchScope
	 * @param nodeToSearchFor
	 * @param startPosition inclusive
	 * @param endPosition Inclusive
	 */
	public void findReferences(ASTNode searchScope, Name nodeToSearchFor, int startPosition, int endPosition){
		readReferences = new ArrayList<ASTNode>();
		writeReferences = new ArrayList<ASTNode>();
		this.nodetoSearchFor = nodeToSearchFor;
		targetBinding = nodeToSearchFor.resolveBinding();
		searchScope.accept(this);
		writeReferences = filter(writeReferences, startPosition, endPosition);
		readReferences = filter(readReferences, startPosition, endPosition);
	}
	
	
	public boolean visit(QualifiedName node) {
		final IBinding binding= node.resolveBinding();
		if (binding instanceof IVariableBinding) {
			SimpleName name= node.getName();
			return !addUsage(name, name.resolveBinding());
		}
		
		return !addUsage(node, binding);
	}
	
	public boolean visit(SimpleName node) {
		addUsage(node, node.resolveBinding());
		return true;
	}

	/*
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ConstructorInvocation)
	 */
	public boolean visit(ClassInstanceCreation node) {
		// match with the constructor and the type.
		
		Type type= node.getType();
		if (type instanceof ParameterizedType) {
			type= ((ParameterizedType) type).getType();
		}
		if (type instanceof SimpleType) {
			Name name= ((SimpleType) type).getName();
			if (name instanceof QualifiedName)
				name= ((QualifiedName)name).getName();
			addUsage(name, node.resolveConstructorBinding());
		}
		return super.visit(node);
	}
	
	public boolean visit(Assignment node) {
		SimpleName name= getSimpleName(node.getLeftHandSide());
		if (name != null) 
			addWrite(name, name.resolveBinding());
		return true;
	}
	
	public boolean visit(SingleVariableDeclaration node) {
		addWrite(node.getName(), node.resolveBinding());
		return true;
	}
	
	public boolean visit(VariableDeclarationFragment node) {
		if (node.getInitializer() != null)
			addWrite(node.getName(), node.resolveBinding());
		return true;
	}

	public boolean visit(PrefixExpression node) {
		PrefixExpression.Operator operator= node.getOperator();	
		if (operator == Operator.INCREMENT || operator == Operator.DECREMENT) {
			SimpleName name= getSimpleName(node.getOperand());
			if (name != null) 
				if (addWrite(name, name.resolveBinding())) {
					readReferences.add(name);
				}
		}
		return true;
	}

	public boolean visit(PostfixExpression node) {
		SimpleName name= getSimpleName(node.getOperand());
		if (name != null) 
			if (addWrite(name, name.resolveBinding())) {
				readReferences.add(name);
			}
		return true;
	}

	private boolean addWrite(Name node, IBinding binding) {
		if (binding != null && Bindings.equals(getBindingDeclaration(binding), targetBinding)) {
			writeReferences.add(node);
			return true;
		}
		return false;
	}
	
	private boolean addUsage(Name node, IBinding binding) {
		if (binding != null && Bindings.equals(getBindingDeclaration(binding), targetBinding)) {
			if (! writeReferences.contains(node)) {
				//only add the current node if it does not overlap with write access
				readReferences.add(node);
			}
			return true;
		}
		return false;
	}

	private SimpleName getSimpleName(Expression expression) {
		if (expression instanceof SimpleName)
			return ((SimpleName)expression);
		else if (expression instanceof QualifiedName)
			return (((QualifiedName) expression).getName());
		else if (expression instanceof FieldAccess)
			return ((FieldAccess)expression).getName();
		return null;
	}

	private IBinding getBindingDeclaration(IBinding binding) {
		switch (binding.getKind()) {
			case IBinding.TYPE :
				return ((ITypeBinding)binding).getTypeDeclaration();
			case IBinding.METHOD :
				return ((IMethodBinding)binding).getMethodDeclaration();
			case IBinding.VARIABLE :
				return ((IVariableBinding)binding).getVariableDeclaration();
			default:
				return binding;
		}
	}

	
	private List<ASTNode> filter(List<ASTNode> references, int startPosition, int endPosition) {
		List<ASTNode> filteredReferences = new ArrayList<ASTNode>();
		for (ASTNode reference : references) {
			if ((reference.getStartPosition() >= startPosition) &&
					(reference.getStartPosition() <= endPosition))
				filteredReferences.add(reference);
		}
		return filteredReferences;
	}

	public boolean hasReadReferences() {
		return getReadReferences().size() > 0;
	}
	
	public boolean hasWriteReferences() {
		return getWriteReferences().size() > 0;
	}
}
