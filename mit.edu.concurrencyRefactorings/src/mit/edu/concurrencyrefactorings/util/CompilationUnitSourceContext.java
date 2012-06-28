package mit.edu.concurrencyrefactorings.util;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;

public class CompilationUnitSourceContext extends RefactoringStatusContext {
	private ICompilationUnit fCUnit;
	private ISourceRange fSourceRange;
	public CompilationUnitSourceContext(ICompilationUnit cunit, ISourceRange range) {
		fCUnit= cunit;
		fSourceRange= range;
		if (fSourceRange == null)
			fSourceRange= new SourceRange(0,0);
	}
	public boolean isBinary() {
		return false;
	}
	public ICompilationUnit getCompilationUnit() {
		return fCUnit;
	}
	public IClassFile getClassFile() {
		return null;
	}
	public ISourceRange getSourceRange() {
		return fSourceRange;
	}
	@Override
	public String toString() {
		return getSourceRange() + " in " + super.toString(); //$NON-NLS-1$
	}
	@Override
	public Object getCorrespondingElement() {
		// TODO Auto-generated method stub
		return fCUnit;
	}
}