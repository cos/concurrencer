package mit.edu.concurrencyrefactorings.refactorings;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;


public class SideEffectsFinder extends ASTVisitor {

	private final ICompilationUnit unit;
	private final int indexBeginningStatement;
	private final int indexEndStatement;
	private final IVariableBinding notIncludingField;
	private final RefactoringStatus status;
	private final Block enclosingBlock;

	@SuppressWarnings("restriction")
	public SideEffectsFinder(ICompilationUnit unit,
			Block enclosingBlock, int indexBeginningStatement, int indexEndStatement,
			IVariableBinding notIncludingField, RefactoringStatus status) {
				this.unit = unit;
				this.enclosingBlock = enclosingBlock;
				this.indexBeginningStatement = indexBeginningStatement;
				this.indexEndStatement = indexEndStatement;
				this.notIncludingField = notIncludingField;
				this.status = status;
	}
	
	public void findEffects(){
		final Collection<IVariableBinding> accessedFields = new ArrayList<IVariableBinding>();
		enclosingBlock.accept(new ASTVisitor() {
			public boolean visit(SimpleName identifier){
				IBinding identifierBinding = identifier.resolveBinding();
				if (identifierBinding instanceof IVariableBinding) {
					IVariableBinding idBinding = (IVariableBinding) identifierBinding;
					if (!Bindings.equals(notIncludingField, idBinding) && 
							(idBinding.isField())) {
						accessedFields.add(idBinding);
					}
				}
				return false;
			}
		});
		
		enclosingBlock.accept(new ASTVisitor() {
			public boolean visit(Assignment assignment){
				
				return true;
			}
		});
	}

}
