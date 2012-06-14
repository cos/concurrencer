package mit.edu.concurrencyrefactorings.ui;

import mit.edu.concurrencyrefactorings.refactorings.ConvertToAtomicIntegerRefactoring;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class ConvertToAtomicIntegerWizard extends RefactoringWizard {

	public ConvertToAtomicIntegerWizard(Refactoring refactoring, int flags) {
		super(refactoring, flags);
		// TODO Auto-generated constructor stub
	}

	public ConvertToAtomicIntegerWizard(
			ConvertToAtomicIntegerRefactoring refactoring, String string) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(string);
	}

	@Override
	protected void addUserInputPages() {
		addPage(new ConvertToAtomicIntegerInputPage("ConvertToAtomicInteger"));

	}

}
