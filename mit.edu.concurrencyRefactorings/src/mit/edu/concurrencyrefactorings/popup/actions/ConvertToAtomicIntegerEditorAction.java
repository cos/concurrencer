package mit.edu.concurrencyrefactorings.popup.actions;

import java.lang.reflect.InvocationTargetException;

import mit.edu.concurrencyrefactorings.refactorings.ConvertToAtomicIntegerRefactoring;
import mit.edu.concurrencyrefactorings.ui.ConvertToAtomicIntegerWizard;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

public class ConvertToAtomicIntegerEditorAction implements IEditorActionDelegate{

	private JavaEditor fEditor;
	private ITextSelection fTextSelection;

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor instanceof JavaEditor) {
			fEditor = (JavaEditor) targetEditor;
		}
	}

	@Override
	public void run(IAction action) {
		IJavaElement[] elements;
		try {
			elements = SelectionConverter.codeResolveForked(fEditor, true);
			if (elements.length == 1 && (elements[0] instanceof IField)) {
				IField field= (IField) elements[0];
				
				if (isRefactoringAvailableFor(field)) {
					ConvertToAtomicIntegerRefactoring refactoring= new ConvertToAtomicIntegerRefactoring(field);
					run(new ConvertToAtomicIntegerWizard(refactoring, "Convert to Atomic Integer"), getShell(), "Convert to Atomic Integer");
					return;
				}
			}
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MessageDialog.openError(getShell(), "Error ConvertToAtomicInteger", "ConvertToAtomicInteger not applicable for current selection"); 
	}
	
	private boolean isRefactoringAvailableFor(IField field) throws JavaModelException {
		return field != null && field.exists() && field.isStructureKnown() && !field.getDeclaringType().isAnnotation() && "I".equals(field.getTypeSignature());
	}

	public void run(RefactoringWizard wizard, Shell parent, String dialogTitle) {
		try {
			RefactoringWizardOpenOperation operation= new RefactoringWizardOpenOperation(wizard);
			operation.run(parent, dialogTitle);
		} catch (InterruptedException exception) {
			// Do nothing
		}
	}
	
	
	private Shell getShell() {
		return fEditor.getSite().getShell();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof ITextSelection) {
			fTextSelection = (ITextSelection) selection;
		}
	}
}
