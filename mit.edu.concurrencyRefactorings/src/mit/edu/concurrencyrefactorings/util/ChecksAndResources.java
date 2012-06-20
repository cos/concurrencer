package mit.edu.concurrencyrefactorings.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.CorextMessages;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.ui.IJavaStatusConstants;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.TextProcessor;

public class ChecksAndResources {
	
	public static RefactoringStatus validateModifiesFiles(IFile[] filesToModify, Object context) {
		RefactoringStatus result= new RefactoringStatus();
		IStatus status= checkInSync(filesToModify);
		if (!status.isOK())
			result.merge(RefactoringStatus.create(status));
		status= makeCommittable(filesToModify, context);
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
	
	
	/*
	 * Resources
	 */
	
	/**
	 * Checks if the given resources are in sync with the underlying file
	 * system.
	 *
	 * @param resources the resources to be checked
	 * @return IStatus status describing the check's result. If <code>status.
	 *  isOK() </code> returns <code>true</code> then the resources are in sync
	 */
	public static IStatus checkInSync(IResource[] resources) {
		IStatus result= null;
		for (int i= 0; i < resources.length; i++) {
			IResource resource= resources[i];
			if (!resource.isSynchronized(IResource.DEPTH_INFINITE)) {
				result= addOutOfSync(result, resource);
			}
		}
		if (result != null)
			return result;
		return Status.OK_STATUS;
	}
	
	private static IStatus addOutOfSync(IStatus status, IResource resource) {
		IStatus entry= new Status(
			IStatus.ERROR,
			ResourcesPlugin.PI_RESOURCES,
			IResourceStatus.OUT_OF_SYNC_LOCAL,
			MessageFormat.format(CorextMessages.Resources_outOfSync, markLTR(resource.getFullPath().makeRelative().toString())),
			null);
		if (status == null) {
			return entry;
		} else if (status.isMultiStatus()) {
			((MultiStatus)status).add(entry);
			return status;
		} else {
			MultiStatus result= new MultiStatus(
				ResourcesPlugin.PI_RESOURCES,
				IResourceStatus.OUT_OF_SYNC_LOCAL,
				CorextMessages.Resources_outOfSyncResources, null);
			result.add(status);
			result.add(entry);
			return result;
		}
	}
	
	/**
	 * Makes the given resources committable. Committable means that all
	 * resources are writeable and that the content of the resources hasn't
	 * changed by calling <code>validateEdit</code> for a given file on
	 * <tt>IWorkspace</tt>.
	 *
	 * @param resources the resources to be checked
	 * @param context the context passed to <code>validateEdit</code>
	 * @return IStatus status describing the method's result. If <code>status.
	 * isOK()</code> returns <code>true</code> then the add resources are
	 * committable
	 *
	 * @see org.eclipse.core.resources.IWorkspace#validateEdit(org.eclipse.core.resources.IFile[], java.lang.Object)
	 */
	public static IStatus makeCommittable(IResource[] resources, Object context) {
		List<IResource> readOnlyFiles= new ArrayList<IResource>();
		for (int i= 0; i < resources.length; i++) {
			IResource resource= resources[i];
			if (resource.getType() == IResource.FILE && isReadOnly(resource))
				readOnlyFiles.add(resource);
		}
		if (readOnlyFiles.size() == 0)
			return Status.OK_STATUS;

		Map<IFile, Long> oldTimeStamps= createModificationStampMap(readOnlyFiles);
		IStatus status= ResourcesPlugin.getWorkspace().validateEdit(
			readOnlyFiles.toArray(new IFile[readOnlyFiles.size()]), context);
		if (!status.isOK())
			return status;

		IStatus modified= null;
		Map<IFile, Long> newTimeStamps= createModificationStampMap(readOnlyFiles);
		for (Iterator<IFile> iter= oldTimeStamps.keySet().iterator(); iter.hasNext();) {
			IFile file= iter.next();
			if (!oldTimeStamps.get(file).equals(newTimeStamps.get(file)))
				modified= addModified(modified, file);
		}
		if (modified != null)
			return modified;
		return Status.OK_STATUS;
	}
	
	public static boolean isReadOnly(IResource resource) {
		ResourceAttributes resourceAttributes = resource.getResourceAttributes();
		if (resourceAttributes == null)  // not supported on this platform for this resource
			return false;
		return resourceAttributes.isReadOnly();
	}
	
	private static Map<IFile, Long> createModificationStampMap(List<IResource> files){
		Map<IFile, Long> map= new HashMap<IFile, Long>();
		for (Iterator<IResource> iter= files.iterator(); iter.hasNext(); ) {
			IFile file= (IFile)iter.next();
			map.put(file, new Long(file.getModificationStamp()));
		}
		return map;
	}
	
	private static IStatus addModified(IStatus status, IFile file) {
		IStatus entry= new Status(IStatus.ERROR, JavaPlugin.getPluginId(),
			IJavaStatusConstants.VALIDATE_EDIT_CHANGED_CONTENT,
			MessageFormat.format(CorextMessages.Resources_fileModified, markLTR(file.getFullPath().makeRelative().toString())),
			null);
		if (status == null) {
			return entry;
		} else if (status.isMultiStatus()) {
			((MultiStatus)status).add(entry);
			return status;
		} else {
			MultiStatus result= new MultiStatus(JavaPlugin.getPluginId(),
				IJavaStatusConstants.VALIDATE_EDIT_CHANGED_CONTENT,
				CorextMessages.Resources_modifiedResources, null);
			result.add(status);
			result.add(entry);
			return result;
		}
	}
	
	/*
	 * Strings
	 */
	
	/**
	 * Tells whether we have to use the {@link TextProcessor}
	 * <p>
	 * This is used for performance optimization.
	 * </p>
	 * @since 3.4
	 */
	public static final boolean USE_TEXT_PROCESSOR;
	static {
		String testString= "args : String[]"; //$NON-NLS-1$
		USE_TEXT_PROCESSOR= testString != TextProcessor.process(testString);
	}
	
	/**
	 * Adds special marks so that that the given string is readable in a BiDi environment.
	 * 
	 * @param string the string
	 * @return the processed styled string
	 * @since 3.4
	 */
	public static String markLTR(String string) {
		if (!USE_TEXT_PROCESSOR)
			return string;

		return TextProcessor.process(string);
	}
}
