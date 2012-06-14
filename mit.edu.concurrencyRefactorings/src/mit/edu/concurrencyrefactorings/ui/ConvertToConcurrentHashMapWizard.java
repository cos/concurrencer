package mit.edu.concurrencyrefactorings.ui;

import mit.edu.concurrencyrefactorings.refactorings.ConvertToConcurrentHashMapRefactoring;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class ConvertToConcurrentHashMapWizard extends RefactoringWizard {

	public ConvertToConcurrentHashMapWizard(Refactoring refactoring, int flags) {
		super(refactoring, flags);
		// TODO Auto-generated constructor stub
	}

	public ConvertToConcurrentHashMapWizard(
			ConvertToConcurrentHashMapRefactoring refactoring, String string) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(string);
	}

	@Override
	protected void addUserInputPages() {
		addPage(new ConvertToConcurrentHashMapInputPage("ConvertToConcurrentHashMap"));

	}

}
