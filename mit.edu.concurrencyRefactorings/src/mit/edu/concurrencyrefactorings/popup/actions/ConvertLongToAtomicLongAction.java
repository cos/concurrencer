package mit.edu.concurrencyrefactorings.popup.actions;

import mit.edu.concurrencyrefactorings.refactorings.ConvertToAtomicLongRefactoring;
import mit.edu.concurrencyrefactorings.ui.ConvertToAtomicLongWizard;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class ConvertLongToAtomicLongAction implements IObjectActionDelegate {

	private Shell shell;
	private IField fField;

	/**
	 * Constructor for Action1.
	 */
	public ConvertLongToAtomicLongAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		try {
			if (fField != null && shell != null && isConvertToAtomicLongAvailable()) {
				ConvertToAtomicLongRefactoring refactoring= new ConvertToAtomicLongRefactoring(fField);
				run(new ConvertToAtomicLongWizard(refactoring, "Convert to Atomic Long"), shell, "Convert to Atomic Long");
			} else
				MessageDialog.openError(shell, "Error ConvertToAtomicLong", "ConvertToAtomicLong not applicable for current selection"); 
			
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}
	
	public void run(RefactoringWizard wizard, Shell parent, String dialogTitle) {
		try {
			RefactoringWizardOpenOperation operation= new RefactoringWizardOpenOperation(wizard);
			operation.run(parent, dialogTitle);
		} catch (InterruptedException exception) {
			// Do nothing
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fField= null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection extended= (IStructuredSelection) selection;
			Object[] elements= extended.toArray();
			if (elements.length == 1 && elements[0] instanceof IField) {
				fField= (IField) elements[0];
			}
		}
//		try {
//			action.setEnabled(isConvertToAtomicLongAvailable());
//		} catch (JavaModelException exception) {
//			action.setEnabled(false);
//		}
	}

	private boolean isConvertToAtomicLongAvailable()
			throws JavaModelException {
		return fField != null && fField.exists() && fField.isStructureKnown() && !fField.getDeclaringType().isAnnotation() && "J".equals(fField.getTypeSignature());
	}

}
