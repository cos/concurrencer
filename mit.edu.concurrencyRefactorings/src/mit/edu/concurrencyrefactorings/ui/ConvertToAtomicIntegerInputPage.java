package mit.edu.concurrencyrefactorings.ui;

import mit.edu.concurrencyrefactorings.Activator;
import mit.edu.concurrencyrefactorings.refactorings.ConvertToAtomicIntegerRefactoring;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionStatusDialog;

public class ConvertToAtomicIntegerInputPage extends UserInputWizardPage implements
		IWizardPage {

	Text fNameField;
	private Button initializeDeclarationButton;

	//Combo fTypeCombo;

	public ConvertToAtomicIntegerInputPage(String name) {
		super(name);
	}

	public void createControl(Composite parent) {
		Composite result= new Composite(parent, SWT.NONE);

		setControl(result);

		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		result.setLayout(layout);

		Label label= new Label(result, SWT.NONE);
		label.setText("&Field name:");

		fNameField= createNameField(result);
		fNameField.setEditable(false);

		initializeDeclarationButton = new Button(result, SWT.CHECK);
		initializeDeclarationButton.setText("&Initialize field declaration");
		GridData data= new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan= 2;
		data.verticalIndent= 2;
		initializeDeclarationButton.setLayoutData(data);

		final ConvertToAtomicIntegerRefactoring refactoring= getConvertToAtomicIntegerRefactoring();
		fNameField.setText(refactoring.getFieldName());

		fNameField.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent event) {
				handleInputChanged();
			}
		});

		initializeDeclarationButton.setSelection(true);
		
		initializeDeclarationButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				refactoring.setInitializeDeclaration(initializeDeclarationButton.getSelection());
				handleInputChanged();
			}
			
		});

//		fNameField.setFocus();
//		fNameField.selectAll();
		handleInputChanged();
	}

	private Text createNameField(Composite result) {
		Text field= new Text(result, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return field;
	}

	private Combo createTypeCombo(Composite composite) {
		Combo combo= new Combo(composite, SWT.SINGLE | SWT.BORDER);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setVisibleItemCount(4);
		return combo;
	}

	private ConvertToAtomicIntegerRefactoring getConvertToAtomicIntegerRefactoring() {
		return (ConvertToAtomicIntegerRefactoring) getRefactoring();
	}

	void handleInputChanged() {
		RefactoringStatus status= new RefactoringStatus();
		ConvertToAtomicIntegerRefactoring refactoring= getConvertToAtomicIntegerRefactoring();
		status.merge(refactoring.setFieldName(fNameField.getText()));
		status.merge(refactoring.setInitializeDeclaration(initializeDeclarationButton.getSelection()));
		

		setPageComplete(!status.hasError());
		int severity= status.getSeverity();
		String message= status.getMessageMatchingSeverity(severity);
		if (severity >= RefactoringStatus.INFO) {
			setMessage(message, severity);
		} else {
			setMessage("", NONE); //$NON-NLS-1$
		}
	}
}
