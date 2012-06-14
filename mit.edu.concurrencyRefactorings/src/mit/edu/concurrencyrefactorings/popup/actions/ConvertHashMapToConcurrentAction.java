package mit.edu.concurrencyrefactorings.popup.actions;

import mit.edu.concurrencyrefactorings.refactorings.ConvertToConcurrentHashMapRefactoring;
import mit.edu.concurrencyrefactorings.ui.ConvertToConcurrentHashMapWizard;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class ConvertHashMapToConcurrentAction implements IObjectActionDelegate {

	private Shell shell;
	private IField fField;

	/**
	 * Constructor for Action1.
	 */
	public ConvertHashMapToConcurrentAction() {
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
			if (fField != null && shell != null && isConvertToConcurrentHashMapAvailable()) {
				ConvertToConcurrentHashMapRefactoring refactoring= new ConvertToConcurrentHashMapRefactoring(fField);
				run(new ConvertToConcurrentHashMapWizard(refactoring, "Convert to ConcurrentHashMap"), shell, "Convert to ConcurrentHashMap");
			} else
				MessageDialog.openError(shell, "Error ConvertToConcurrentHashMap", "ConvertToConcurrentHashMap not applicable for current selection"); 
			
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
//			action.setEnabled(isConvertToConcurrentHashMapAvailable());
//		} catch (JavaModelException exception) {
//			action.setEnabled(false);
//		}
	}

	private boolean isConvertToConcurrentHashMapAvailable()
			throws JavaModelException {
		return fField != null && fField.exists() && fField.isStructureKnown() && !fField.getDeclaringType().isAnnotation() &&
		("QHashMap;".equals(fField.getTypeSignature())  || "QMap;".equals(fField.getTypeSignature()) || isTypeSignatureForParametrized(fField.getTypeSignature()));
	}

	/**
	 * Returns true if typeSignature is of the form QHashMap<*>, where * can be anything. 
	 */
	private boolean isTypeSignatureForParametrized(String typeSignature) {
		int sigLength = typeSignature.length();
		//return typeSignature.substring(0, 9).equals("QHashMap<") && typeSignature.substring(sigLength-2, sigLength).equals(">;");
		return ((typeSignature.indexOf("QHashMap<") != -1) || (typeSignature.indexOf("QMap<") != -1));
	}
	
	

}
