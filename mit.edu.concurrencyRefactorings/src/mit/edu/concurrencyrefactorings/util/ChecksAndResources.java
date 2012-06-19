package mit.edu.concurrencyrefactorings.util;

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.util.Resources;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class ChecksAndResources {
	
	public static RefactoringStatus validateModifiesFiles(IFile[] filesToModify, Object context) {
		RefactoringStatus result= new RefactoringStatus();
		IStatus status= Resources.checkInSync(filesToModify);
		if (!status.isOK())
			result.merge(RefactoringStatus.create(status));
		status= Resources.makeCommittable(filesToModify, context);
		if (!status.isOK()) {
			result.merge(RefactoringStatus.create(status));
			if (!result.hasFatalError()) {
				result.addFatalError(RefactoringCoreMessages.Checks_validateEdit);
			}
		}
		return result;
	}
	
	/**
	 * Checks whether it is possible to modify the given <code>IJavaElement</code>.
	 * The <code>IJavaElement</code> must exist and be non read-only to be modifiable.
	 * Moreover, if it is a <code>IMember</code> it must not be binary.
	 * The returned <code>RefactoringStatus</code> has <code>ERROR</code> severity if
	 * it is not possible to modify the element.
	 * @param javaElement
	 * @return the status
	 * @throws JavaModelException
	 *
	 * @see IJavaElement#exists
	 * @see IJavaElement#isReadOnly
	 * @see IMember#isBinary
	 * @see RefactoringStatus
	 */
	public static RefactoringStatus checkAvailability(IJavaElement javaElement) throws JavaModelException{
		RefactoringStatus result= new RefactoringStatus();
		if (! javaElement.exists())
			result.addFatalError(MessageFormat.format(RefactoringCoreMessages.Refactoring_not_in_model, getJavaElementName(javaElement)));
		if (javaElement.isReadOnly())
			result.addFatalError(MessageFormat.format(RefactoringCoreMessages.Refactoring_read_only, getJavaElementName(javaElement)));
		if (javaElement.exists() && !javaElement.isStructureKnown())
			result.addFatalError(MessageFormat.format(RefactoringCoreMessages.Refactoring_unknown_structure, getJavaElementName(javaElement)));
		if (javaElement instanceof IMember && ((IMember)javaElement).isBinary())
			result.addFatalError(MessageFormat.format(RefactoringCoreMessages.Refactoring_binary, getJavaElementName(javaElement)));
		return result;
	}
	
	private static String getJavaElementName(IJavaElement element) {
		return JavaElementLabels.getElementLabel(element, JavaElementLabels.ALL_DEFAULT);
	}
}
