package mit.edu.concurrencyrefactorings.refactorings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.TextEditGroup;

public class AccessAnalyzerForFJTask extends ASTVisitor {

	private final ConvertToFJTaskRefactoring convertToFJTaskRefactoring;
	private final ICompilationUnit unit;
	private final MethodDeclaration methodDeclaration;
	private final ITypeBinding declaringClass;
	private final ASTRewrite rewriter;
	private final ImportRewrite importRewrite;
	private RefactoringStatus status;
	private List<TextEditGroup> fGroupDescriptions;

	public AccessAnalyzerForFJTask(
			ConvertToFJTaskRefactoring convertToFJTaskRefactoring,
			ICompilationUnit unit, MethodDeclaration methodDeclaration,
			ITypeBinding declaringClass, ASTRewrite rewriter,
			ImportRewrite importRewrite) {
				this.convertToFJTaskRefactoring = convertToFJTaskRefactoring;
				this.unit = unit;
				this.methodDeclaration = methodDeclaration;
				this.declaringClass = declaringClass;
				this.rewriter = rewriter;
				this.importRewrite = importRewrite;
				status = new RefactoringStatus();
				fGroupDescriptions= new ArrayList<TextEditGroup>();
	}

	public RefactoringStatus getStatus() {
		return status;
	}

	public Collection<TextEditGroup> getGroupDescriptions() {
		return fGroupDescriptions;
	}

}
