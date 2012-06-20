/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package mit.edu.concurrencyrefactorings.util;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.text.edits.TextEditGroup;

/**
 *
 */
public class ModifierRewrite {

	public static final int VISIBILITY_MODIFIERS= Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED;

	private ListRewrite fModifierRewrite;

	
	public static ModifierRewrite create(ASTRewrite rewrite, ASTNode declNode) {
		return new ModifierRewrite(rewrite, declNode);
	}

	private ModifierRewrite(ASTRewrite rewrite, ASTNode declNode) {
		fModifierRewrite= evaluateListRewrite(rewrite, declNode);
	}

	private ListRewrite evaluateListRewrite(ASTRewrite rewrite, ASTNode declNode) {
		switch (declNode.getNodeType()) {
			case ASTNode.METHOD_DECLARATION:
				return rewrite.getListRewrite(declNode, MethodDeclaration.MODIFIERS2_PROPERTY);
			case ASTNode.FIELD_DECLARATION:
				return rewrite.getListRewrite(declNode, FieldDeclaration.MODIFIERS2_PROPERTY);
			case ASTNode.VARIABLE_DECLARATION_EXPRESSION:
				return rewrite.getListRewrite(declNode, VariableDeclarationExpression.MODIFIERS2_PROPERTY);
			case ASTNode.VARIABLE_DECLARATION_STATEMENT:
				return rewrite.getListRewrite(declNode, VariableDeclarationStatement.MODIFIERS2_PROPERTY);
			case ASTNode.SINGLE_VARIABLE_DECLARATION:
				return rewrite.getListRewrite(declNode, SingleVariableDeclaration.MODIFIERS2_PROPERTY);
			case ASTNode.TYPE_DECLARATION:
				return rewrite.getListRewrite(declNode, TypeDeclaration.MODIFIERS2_PROPERTY);
			case ASTNode.ENUM_DECLARATION:
				return rewrite.getListRewrite(declNode, EnumDeclaration.MODIFIERS2_PROPERTY);
			case ASTNode.ANNOTATION_TYPE_DECLARATION:
				return rewrite.getListRewrite(declNode, AnnotationTypeDeclaration.MODIFIERS2_PROPERTY);
			case ASTNode.ENUM_CONSTANT_DECLARATION:
				return rewrite.getListRewrite(declNode, EnumConstantDeclaration.MODIFIERS2_PROPERTY);
			case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION:
				return rewrite.getListRewrite(declNode, AnnotationTypeMemberDeclaration.MODIFIERS2_PROPERTY);
			default:
				throw new IllegalArgumentException("node has no modifiers: " + declNode.getClass().getName()); //$NON-NLS-1$
		}
	}

	public ListRewrite getModifierRewrite() {
		return fModifierRewrite;
	}

	public void copyAllModifiers(ASTNode otherDecl, TextEditGroup editGroup) {
		copyAllModifiers(otherDecl, editGroup, false);
	}
	
	public void copyAllModifiers(ASTNode otherDecl, TextEditGroup editGroup, boolean copyIndividually) {
		ListRewrite modifierList= evaluateListRewrite(fModifierRewrite.getASTRewrite(), otherDecl);
		List<IExtendedModifier> originalList= modifierList.getOriginalList();
		if (originalList.isEmpty()) {
			return;
		}

		if (copyIndividually) {
			for (Iterator<IExtendedModifier> iterator= originalList.iterator(); iterator.hasNext();) {
				ASTNode modifier= (ASTNode) iterator.next();
				ASTNode copy= fModifierRewrite.getASTRewrite().createCopyTarget(modifier);
				if (copy != null) { //paranoia check (only left here because we're in RC1)
					fModifierRewrite.insertLast(copy, editGroup);
				}
			}
		} else {
			ASTNode copy= modifierList.createCopyTarget((ASTNode) originalList.get(0), (ASTNode) originalList.get(originalList.size() - 1));
			if (copy != null) { //paranoia check (only left here because we're in RC1)
				fModifierRewrite.insertLast(copy, editGroup);
			}
		}
	}
}