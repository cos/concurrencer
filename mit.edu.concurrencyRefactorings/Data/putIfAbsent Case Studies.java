import java.io.File;
import java.io.OutputStream;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import ModJkMX.MBeanProxy;

=============================================================================================
JDT Core 2.1.3:
=============================================================================================


JavaCore.java has 6 matches dealing with HashSets. Not quite what we want, but they're there.

----------------------------------------------------------------------------------------------

CompletionEngine.java

field:
HashtableOfObject knownTypes = new HashtableOfObject(10);

public void acceptClass(char[] packageName, char[] className, int modifiers) {
...
if (this.knownTypes.containsKey(completionName)) return;
this.knownTypes.put(completionName, this);
...
}

public void acceptInterface(char[] packageName, char[] interfaceName, int modifiers) {
...
if (this.knownTypes.containsKey(completionName)) return;
this.knownTypes.put(completionName, this);
...
}

public void acceptPackage(char[] packageName) {

if (this.knownPkgs.containsKey(packageName)) return;
this.knownPkgs.put(packageName, this);
...
}

public void acceptType(char[] packageName, char[] typeName) {
...		
if (this.knownTypes.containsKey(completionName)) return;
this.knownTypes.put(completionName, this);
...

Object type = typeCache.get(compoundName);
			
ISourceType sourceType = null;
if(type != null) {
	if(type instanceof ISourceType) {
		sourceType = (ISourceType) type;
	}
} else {
	NameEnvironmentAnswer answer = nameEnvironment.findType(bindingType.compoundName);
	if(answer != null && answer.isSourceType()) {
		sourceType = answer.getSourceTypes()[0];
		typeCache.put(compoundName, sourceType);
	}
}
...
}

----------------------------------------------------------------------------------------------

ClassPathJar.java

public boolean isPackage(String qualifiedPackageName) {
if (packageCache != null)
	return packageCache.containsKey(qualifiedPackageName);
...

// extract the package name
String packageName = fileName.substring(0, last);
if (packageCache.containsKey(packageName))
	continue nextEntry;
packageCache.put(packageName, packageName);

...

return packageCache.containsKey(qualifiedPackageName);

}


---------------------------------------------------------------------------------------------

ClassScope

if (knownFieldNames.containsKey(field.name)) {
	duplicate = true;
	FieldBinding previousBinding = (FieldBinding) knownFieldNames.get(field.name);
	if (previousBinding != null) {
		for (int f = 0; f < i; f++) {
			FieldDeclaration previousField = fields[f];
			if (previousField.binding == previousBinding) {
				problemReporter().duplicateFieldInType(referenceContext.binding, previousField);
				previousField.binding = null;
				break;
			}
		}
	}
	knownFieldNames.put(field.name, null); // ensure that the duplicate field is found & removed
	problemReporter().duplicateFieldInType(referenceContext.binding, field);
	field.binding = null;
} else {
	knownFieldNames.put(field.name, fieldBinding);
	// remember that we have seen a field with this name
	if (fieldBinding != null)
		fieldBindings[count++] = fieldBinding;
}

---------------------------------------------------------------------------------------------

CompilationUnitScope

6 matches dealing with SimpleNameVector: form is like if(!contains) {add();}


---------------------------------------------------------------------------------------------

IndexBasedHierarchyBuilder

public static void searchAllPossibleSubTypes(...) {
...

if (!foundSuperNames.containsKey(typeName)){
	foundSuperNames.put(typeName, typeName);
	awaitings.add(typeName);
}

...
}

---------------------------------------------------------------------------------------------

Tons of examples of similar usages if(!contains) {add();} with ArrayLists, HierarchyTypeBuilders or whatever, HashSets, etc.

---------------------------------------------------------------------------------------------

TypeHierarchy

protected void addSubtype(IType type, IType subtype) {
	TypeVector subtypes = (TypeVector)this.typeToSubtypes.get(type);
	if (subtypes == null) {
		subtypes = new TypeVector();
		this.typeToSubtypes.put(type, subtypes);
	}
	if (!subtypes.contains(subtype)) {
		subtypes.add(subtype);
	}
...
}


public void store(OutputStream output, IProgressMonitor monitor) throws JavaModelException {
...

Object[] types = classToSuperclass.keySet().toArray();
		for (int i = 0; i < types.length; i++) {
			Object t = types[i];
			if(hashtable.get(t) == null) {
				Integer index = new Integer(count++);
				hashtable.put(t, index);
				hashtable2.put(index, t);
			}
			Object superClass = classToSuperclass.get(t);
			if(superClass != null && hashtable.get(superClass) == null) {
				Integer index = new Integer(count++);
				hashtable.put(superClass, index);
				hashtable2.put(index, superClass);
			}
		}
		types = typeToSuperInterfaces.keySet().toArray();
		for (int i = 0; i < types.length; i++) {
			Object t = types[i];
			if(hashtable.get(t) == null) {
				Integer index = new Integer(count++);
				hashtable.put(t, index);
				hashtable2.put(index, t);
			}
			Object[] sp = (Object[])typeToSuperInterfaces.get(t);
			if(sp != null) {
				for (int j = 0; j < sp.length; j++) {
					Object superInterface = sp[j];
					if(sp[j] != null && hashtable.get(superInterface) == null) {
						Integer index = new Integer(count++);
						hashtable.put(superInterface, index);
						hashtable2.put(index, superInterface);
					}
				}
			}
		}
}


---------------------------------------------------------------------------------------------

Index

public void remove(String documentName) throws IOException {
		IndexedFile file= addsIndex.getIndexedFile(documentName);
		if (file != null) {
			//the file is in the adds Index, we remove it from this one
			Int lastRemoved= (Int) removedInAdds.get(documentName);
			if (lastRemoved != null) {
				int fileNum= file.getFileNumber();
				if (lastRemoved.value < fileNum)
					lastRemoved.value= fileNum;
			} else
				removedInAdds.put(documentName, new Int(file.getFileNumber()));
		} else {
			//we remove the file from the old index
			removedInOld.put(documentName, new Int(1));
		}
		state= CAN_MERGE;
	}
	/**
	 * Removes the given document from the given index (MergeFactory.ADDS_INDEX for the
	 * in memory index, MergeFactory.OLD_INDEX for the index on the disk).
	 */
	protected void remove(IndexedFile file, int index) throws IOException {
		String name= file.getPath();
		if (index == MergeFactory.ADDS_INDEX) {
			Int lastRemoved= (Int) removedInAdds.get(name);
			if (lastRemoved != null) {
				if (lastRemoved.value < file.getFileNumber())
					lastRemoved.value= file.getFileNumber();
			} else
				removedInAdds.put(name, new Int(file.getFileNumber()));
		} else if (index == MergeFactory.OLD_INDEX)
			removedInOld.put(name, new Int(1));
		else
			throw new Error();
		state= CAN_MERGE;
	}

---------------------------------------------------------------------------------------------

IndexManager

String computeIndexName(IPath path) {
	String name = (String) indexNames.get(path);
	if (name == null) {
		String pathString = path.toOSString();
		checksumCalculator.reset();
		checksumCalculator.update(pathString.getBytes());
		String fileName = Long.toString(checksumCalculator.getValue()) + ".index"; //$NON-NLS-1$
		if (VERBOSE)
			JobManager.verbose("-> index name for " + pathString + " is " + fileName); //$NON-NLS-1$ //$NON-NLS-2$
		name = getJavaPluginWorkingLocation().append(fileName).toOSString();
		indexNames.put(path, name);
	}
	return name;
}
/**
 * Returns the index for a given project, according to the following algorithm:
 * - if index is already in memory: answers this one back
 * - if (reuseExistingFile) then read it and return this index and record it in memory
 * - if (createIfMissing) then create a new empty index and record it in memory
 * 
 * Warning: Does not check whether index is consistent (not being used)
 */
public synchronized IIndex getIndex(IPath path, boolean reuseExistingFile, boolean createIfMissing) {
	// Path is already canonical per construction
	IIndex index = (IIndex) indexes.get(path);
	if (index == null) {
		String indexName = computeIndexName(path);
		Object state = getIndexStates().get(indexName);
		Integer currentIndexState = state == null ? UNKNOWN_STATE : (Integer) state;
		if (currentIndexState == UNKNOWN_STATE) {
			// should only be reachable for query jobs
			// IF you put an index in the cache, then AddJarFileToIndex fails because it thinks there is nothing to do
			rebuildIndex(indexName, path);
			return null;
		}

// index isn't cached, consider reusing an existing index file
		if (reuseExistingFile) {
			File indexFile = new File(indexName);
			if (indexFile.exists()) { // check before creating index so as to avoid creating a new empty index if file is missing
				try {
					index = new Index(indexName, "Index for " + path.toOSString(), true /*reuse index file*/); //$NON-NLS-1$
					indexes.put(path, index);
					monitors.put(index, new ReadWriteMonitor());
					return index;
				} catch (IOException e) {
					// failed to read the existing file or its no longer compatible
					if (currentIndexState != REBUILDING_STATE) { // rebuild index if existing file is corrupt, unless the index is already being rebuilt
						if (VERBOSE)
							JobManager.verbose("-> cannot reuse existing index: "+indexName+" path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$
						rebuildIndex(indexName, path);
						return null;
					} else {
						index = null; // will fall thru to createIfMissing & create a empty index for the rebuild all job to populate
					}
				}
			}
			if (currentIndexState == SAVED_STATE) { // rebuild index if existing file is missing
				rebuildIndex(indexName, path);
				return null;
			}
		} 
		// index wasn't found on disk, consider creating an empty new one
		if (createIfMissing) {
			try {
				if (VERBOSE)
					JobManager.verbose("-> create empty index: "+indexName+" path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$
				index = new Index(indexName, "Index for " + path.toOSString(), false /*do not reuse index file*/); //$NON-NLS-1$
				indexes.put(path, index);
				monitors.put(index, new ReadWriteMonitor());
				return index;
			} catch (IOException e) {
				if (VERBOSE)
					JobManager.verbose("-> unable to create empty index: "+indexName+" path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$
				// The file could not be created. Possible reason: the project has been deleted.
				return null;
			}
		}
	}
	//System.out.println(" index name: " + path.toOSString() + " <----> " + index.getIndexFile().getName());	
	return index;
}


<<<<<<<<<<<<<< possibly a replace? >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
public synchronized IIndex recreateIndex(IPath path) {
	// only called to over write an existing cached index...
	try {
		IIndex index = (IIndex) this.indexes.get(path);
		ReadWriteMonitor monitor = (ReadWriteMonitor) this.monitors.remove(index);

		// Path is already canonical
		String indexPath = computeIndexName(path);
		if (VERBOSE)
			JobManager.verbose("-> recreating index: "+indexPath+" for path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$
		index = new Index(indexPath, "Index for " + path.toOSString(), false /*reuse index file*/); //$NON-NLS-1$
		indexes.put(path, index);
		monitors.put(index, monitor);
		return index;
	} catch (IOException e) {
		// The file could not be created. Possible reason: the project has been deleted.
		if (VERBOSE) {
			JobManager.verbose("-> failed to recreate index for path: "+path.toOSString()); //$NON-NLS-1$ //$NON-NLS-2$
			e.printStackTrace();
		}
		return null;
	}
}

----------------------------------------------------------------------------------------------

MatchingSet:

<<<<<<<<<<<<<<<< possibly a replace? >>>>>>>>>>>>>>>>.

public void addPossibleMatch(AstNode node) {

	// remove existing node at same position from set
	// (case of recovery that created the same node several time
	// see http://bugs.eclipse.org/bugs/show_bug.cgi?id=29366)
	long key = (((long) node.sourceStart) << 32) + node.sourceEnd;
	AstNode existing = (AstNode)this.potentialMatchingNodesKeys.get(key);
	if (existing != null && existing.getClass().equals(node.getClass())) {
		this.potentialMatchingNodes.remove(existing);
	}

	// add node to set
	this.potentialMatchingNodes.put(node, new Integer(SearchPattern.POSSIBLE_MATCH));
	this.potentialMatchingNodesKeys.put(key, node);
}


Similarly in addTrustedMatch()

Similarly in file MatchingNodeSet.java

----------------------------------------------------------------------------------------------

SuperTypeReferencePattern

public void findIndexMatches(IndexInput input, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, IJavaSearchScope scope) throws IOException {
	if (this.entryResults == null) {
		// non-optimized case
		super.findIndexMatches(input, requestor, detailLevel, progressMonitor, scope);	
		return;
	}
	
	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();
	
	/* narrow down a set of entries using prefix criteria */
	IEntryResult[] entries = (IEntryResult[])this.entryResults.get(input);
	if (entries == null) {
		entries = input.queryEntriesPrefixedBy(SUPER_REF);
		if (entries == null) {
			entries = NO_ENTRY_RESULT;
		}
		this.entryResults.put(input, entries);
	}

...

}

----------------------------------------------------------------------------------------------

HierarchyScope

private void buildResourceVector() throws JavaModelException {
		HashMap resources = new HashMap();
		HashMap paths = new HashMap();
		this.types = this.hierarchy.getAllTypes();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (int i = 0; i < this.types.length; i++) {
			IType type = this.types[i];
			IResource resource = type.getResource();
			if (resource != null && resources.get(resource) == null) {
				resources.put(resource, resource);
				add(resource);
			}
...
}

----------------------------------------------------------------------------------------------

SubTypeSearchJob

public boolean search(IIndex index, IProgressMonitor progressMonitor) {
...
		if ((input = (IndexInput) inputs.get(index)) == null){
			input = new BlocksIndexInput(index.getIndexFile());
			input.open();
			inputs.put(index, input);
			//System.out.println("Acquiring INPUT for "+index);
		}
...
}

----------------------------------------------------------------------------------------------

copyElementsOperation

private String getSourceFor(IJavaElement element) throws JavaModelException {
	String source = (String) fSources.get(element);
	if (source == null && element instanceof IMember) {
		IMember member = (IMember)element;
		ICompilationUnit cu = member.getCompilationUnit();
		String cuSource = cu.getSource();
		IDOMCompilationUnit domCU = new DOMFactory().createCompilationUnit(cuSource, cu.getElementName());
		IDOMNode node = ((JavaElement)element).findNode(domCU);
		source = new String(node.getCharacters());
		fSources.put(element, source);
	}
	return source;
}

----------------------------------------------------------------------------------------------

DeleteElementsOperation

	protected void groupElements() throws JavaModelException {
		fChildrenToRemove = new HashMap(1);
		int uniqueCUs = 0;
		for (int i = 0, length = fElementsToProcess.length; i < length; i++) {
			IJavaElement e = fElementsToProcess[i];
			ICompilationUnit cu = getCompilationUnitFor(e);
			if (cu == null) {
				throw new JavaModelException(new JavaModelStatus(JavaModelStatus.READ_ONLY, e));
			} else {
				IRegion region = (IRegion) fChildrenToRemove.get(cu);
				if (region == null) {
					region = new Region();
					fChildrenToRemove.put(cu, region);
					uniqueCUs += 1;
				}
				region.add(e);
			}
		}
...
}

----------------------------------------------------------------------------------------------

DeltaProcessor

<<<<<<<<<<<<<<<<<<<<<< looks like a replace >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
public boolean createExternalArchiveDelta(IProgressMonitor monitor) throws JavaModelException {

...

if (this.externalTimeStamps.containsKey(entryPath)){
								this.externalTimeStamps.remove(entryPath);
								externalArchivesStatus.put(entryPath, EXTERNAL_JAR_REMOVED);
								// the jar was physically removed: remove the index
								indexManager.removeIndex(entryPath);
							}

...
}

public void initializeRoots() {
...
if (this.roots.get(path) == null) {
					this.roots.put(path, new RootInfo(project, path, ((ClasspathEntry)entry).fullExclusionPatternChars()));
				} else {
					ArrayList rootList = (ArrayList)this.otherRoots.get(path);
					if (rootList == null) {
						rootList = new ArrayList();
						this.otherRoots.put(path, rootList);
					}
					rootList.add(new RootInfo(project, path, ((ClasspathEntry)entry).fullExclusionPatternChars()));
				}
...
}

----------------------------------------------------------------------------------------------

HandleFactory

public Openable createOpenable(String resourcePath, IJavaSearchScope scope) {

...

IPackageFragment pkgFragment= (IPackageFragment) this.packageHandles.get(packageName);
			if (pkgFragment == null) {
				pkgFragment= this.lastPkgFragmentRoot.getPackageFragment(packageName);
				this.packageHandles.put(packageName, pkgFragment);
			}

...

IPackageFragment pkgFragment= (IPackageFragment) this.packageHandles.get(packageName);
			if (pkgFragment == null) {
				pkgFragment= this.lastPkgFragmentRoot.getPackageFragment(packageName);
				this.packageHandles.put(packageName, pkgFragment);
			}
...
}

----------------------------------------------------------------------------------------------

JavaModelOperation

	protected void addReconcileDelta(IWorkingCopy workingCopy, IJavaElementDelta delta) {
		HashMap reconcileDeltas = JavaModelManager.getJavaModelManager().reconcileDeltas;
		JavaElementDelta previousDelta = (JavaElementDelta)reconcileDeltas.get(workingCopy);
		if (previousDelta != null) {
			IJavaElementDelta[] children = delta.getAffectedChildren();
			for (int i = 0, length = children.length; i < length; i++) {
				JavaElementDelta child = (JavaElementDelta)children[i];
				previousDelta.insertDeltaTree(child.getElement(), child);
			}
		} else {
			reconcileDeltas.put(workingCopy, delta);
		}
	}

----------------------------------------------------------------------------------------------

NameLookup

	private void configureFromProject(IJavaProject project) throws JavaModelException {
		workspace= ResourcesPlugin.getWorkspace();
		fPackageFragmentRoots= ((JavaProject) project).getAllPackageFragmentRoots();
		fPackageFragments= new HashMap();
		IPackageFragment[] frags = this.getPackageFragmentsInRoots(fPackageFragmentRoots, project);
		for (int i= 0; i < frags.length; i++) {
			IPackageFragment fragment= frags[i];
			IPackageFragment[] entry= (IPackageFragment[]) fPackageFragments.get(fragment.getElementName());
			if (entry == null) {
				entry= new IPackageFragment[1];
				entry[0]= fragment;
				fPackageFragments.put(fragment.getElementName(), entry);
			} else {
				IPackageFragment[] copy= new IPackageFragment[entry.length + 1];
				System.arraycopy(entry, 0, copy, 0, entry.length);
				copy[entry.length]= fragment;
				fPackageFragments.put(fragment.getElementName(), copy);
			}
		}
	}

----------------------------------------------------------------------------------------------

CodeSnippetEvaluator

protected void addEvaluationResultForCompilationProblem(Map resultsByIDs, IProblem problem, char[] cuSource) {
...

	EvaluationResult result = (EvaluationResult)resultsByIDs.get(evaluationID);
	if (result == null) {
		resultsByIDs.put(evaluationID, new EvaluationResult(evaluationID, evaluationType, new IProblem[] {problem}));
	} else {
		result.addProblem(problem);
	}
}


----------------------------------------------------------------------------------------------

VariablesEvaluator

protected void addEvaluationResultForCompilationProblem(Map resultsByIDs, IProblem problem, char[] cuSource) {
...

	EvaluationResult result = (EvaluationResult)resultsByIDs.get(evaluationID);
	if (result == null) {
		resultsByIDs.put(evaluationID, new EvaluationResult(evaluationID, evaluationType, new IProblem[] {problem}));
	} else {
		result.addProblem(problem);
	}
}


----------------------------------------------------------------------------------------------

DefaultBindingResolver

<<<<<<<<<<<<< looks like a new replace pattern >>>>>>>>>>>>

	/*
	 * Method declared on BindingResolver.
	 */
	synchronized void updateKey(ASTNode node, ASTNode newNode) {
		Object astNode = this.newAstToOldAst.remove(node);
		if (astNode != null) {
			this.newAstToOldAst.put(newNode, astNode);
		}
	}



synchronized ITypeBinding getTypeBinding(org.eclipse.jdt.internal.compiler.lookup.TypeBinding referenceBinding) {
		if (referenceBinding == null) {
			return null;
		} else if (!referenceBinding.isValidBinding()) {
			switch(referenceBinding.problemId()) {
				case ProblemReasons.NotVisible : 
				case ProblemReasons.NonStaticReferenceInStaticContext :
					if (referenceBinding instanceof ProblemReferenceBinding) {
						ProblemReferenceBinding problemReferenceBinding = (ProblemReferenceBinding) referenceBinding;
						Binding binding2 = problemReferenceBinding.original;
						if (binding2 != null && binding2 instanceof org.eclipse.jdt.internal.compiler.lookup.TypeBinding) {
							TypeBinding binding = (TypeBinding) this.compilerBindingsToASTBindings.get(binding2);
							if (binding != null) {
								return binding;
							}
							binding = new TypeBinding(this, (org.eclipse.jdt.internal.compiler.lookup.TypeBinding) binding2);
							this.compilerBindingsToASTBindings.put(binding2, binding);
							return binding;
						} 
					}
			}
			return null;
		} else {
			TypeBinding binding = (TypeBinding) this.compilerBindingsToASTBindings.get(referenceBinding);
			if (binding != null) {
				return binding;
			}
			binding = new TypeBinding(this, referenceBinding);
			this.compilerBindingsToASTBindings.put(referenceBinding, binding);
			return binding;
		}
	}


	/*
	 * Method declared on BindingResolver.
	 */
	synchronized IPackageBinding getPackageBinding(org.eclipse.jdt.internal.compiler.lookup.PackageBinding packageBinding) {
		if (packageBinding == null || !packageBinding.isValidBinding()) {
			return null;
		}
		IPackageBinding binding = (IPackageBinding) this.compilerBindingsToASTBindings.get(packageBinding);
		if (binding != null) {
			return binding;
		}
		binding = new PackageBinding(packageBinding);
		this.compilerBindingsToASTBindings.put(packageBinding, binding);
		return binding;
	}


	synchronized IVariableBinding getVariableBinding(org.eclipse.jdt.internal.compiler.lookup.VariableBinding variableBinding) {
 		if (variableBinding != null) {
	 		if (variableBinding.isValidBinding()) {
				IVariableBinding binding = (IVariableBinding) this.compilerBindingsToASTBindings.get(variableBinding);
				if (binding != null) {
					return binding;
				}
				binding = new VariableBinding(this, variableBinding);
				this.compilerBindingsToASTBindings.put(variableBinding, binding);
				return binding;
	 		} else {
				/*
				 * http://dev.eclipse.org/bugs/show_bug.cgi?id=24449
				 */
				if (variableBinding instanceof ProblemFieldBinding) {
					ProblemFieldBinding problemFieldBinding = (ProblemFieldBinding) variableBinding;
					switch(problemFieldBinding.problemId()) {
						case ProblemReasons.NotVisible : 
						case ProblemReasons.NonStaticReferenceInStaticContext :
						case ProblemReasons.NonStaticReferenceInConstructorInvocation :
							ReferenceBinding declaringClass = problemFieldBinding.declaringClass;
							FieldBinding exactBinding = declaringClass.getField(problemFieldBinding.name, true /*resolve*/);
							if (exactBinding != null) {
								IVariableBinding variableBinding2 = (IVariableBinding) this.compilerBindingsToASTBindings.get(exactBinding);
								if (variableBinding2 != null) {
									return variableBinding2;
								}
								variableBinding2 = new VariableBinding(this, exactBinding);
								this.compilerBindingsToASTBindings.put(exactBinding, variableBinding2);
								return variableBinding2;
							}
							break;
					}
				}
	 		}
 		}
		return null;
	}
	
	/*
	 * Method declared on BindingResolver.
	 */
	synchronized IMethodBinding getMethodBinding(org.eclipse.jdt.internal.compiler.lookup.MethodBinding methodBinding) {
		if (methodBinding != null) {
			if (methodBinding.isValidBinding()) {
				IMethodBinding binding = (IMethodBinding) this.compilerBindingsToASTBindings.get(methodBinding);
				if (binding != null) {
					return binding;
				}
				binding = new MethodBinding(this, methodBinding);
				this.compilerBindingsToASTBindings.put(methodBinding, binding);
				return binding;
			} else {
				/*
				 * http://dev.eclipse.org/bugs/show_bug.cgi?id=23597
				 */
				switch(methodBinding.problemId()) {
					case ProblemReasons.NotVisible : 
					case ProblemReasons.NonStaticReferenceInStaticContext :
					case ProblemReasons.NonStaticReferenceInConstructorInvocation :
						ReferenceBinding declaringClass = methodBinding.declaringClass;
						if (declaringClass != null) {
							org.eclipse.jdt.internal.compiler.lookup.MethodBinding exactBinding = declaringClass.getExactMethod(methodBinding.selector, methodBinding.parameters);
							if (exactBinding != null) {
								IMethodBinding binding = (IMethodBinding) this.compilerBindingsToASTBindings.get(exactBinding);
								if (binding != null) {
									return binding;
								}
								binding = new MethodBinding(this, exactBinding);
								this.compilerBindingsToASTBindings.put(exactBinding, binding);
								return binding;
							}
						}
						break;
				}
			}
		}
		return null;
	}

----------------------------------------------------------------------------------------------

Main

public CompilationUnit[] getCompilationUnits() throws InvalidInputException {

...

if (knownFileNames.get(charName) != null)
				throw new InvalidInputException(Main.bind("unit.more", this.filenames[i])); //$NON-NLS-1$
			knownFileNames.put(charName, charName);
...

}

----------------------------------------------------------------------------------------------

ASTRewriteAnalyzer

	final TextEdit getCopySourceEdit(CopySourceInfo info) {
		TextEdit edit= (TextEdit) this.sourceCopyInfoToEdit.get(info);
		if (edit == null) {
			int start= getExtendedOffset(info.getStartNode());
			int end= getExtendedEnd(info.getEndNode());
			if (info.isMove) {
				MoveSourceEdit moveSourceEdit= new MoveSourceEdit(start, end - start);
				moveSourceEdit.setTargetEdit(new MoveTargetEdit(0));
				edit= moveSourceEdit;
			} else {
				CopySourceEdit copySourceEdit= new CopySourceEdit(start, end - start);
				copySourceEdit.setTargetEdit(new CopyTargetEdit(0));
				edit= copySourceEdit;
			}
			this.sourceCopyInfoToEdit.put(info, edit);
		}
		return edit;
	}

TextEdit create(CopySourceInfo info) {
	int start= getExtendedOffset(info.getStartNode());
	int end= getExtendedEnd(info.getEndNode());
	if (info.isMove) {
		MoveSourceEdit moveSourceEdit= new MoveSourceEdit(start, end - start);
		moveSourceEdit.setTargetEdit(new MoveTargetEdit(0));
		edit= moveSourceEdit;
	} else {
		CopySourceEdit copySourceEdit= new CopySourceEdit(start, end - start);
		copySourceEdit.setTargetEdit(new CopyTargetEdit(0));
		edit= copySourceEdit;
	}
}


final TextEdit getCopySourceEdit(CopySourceInfo info) {
	
	sourceCopyInfoToEdit.putIfAbsent(info, create(info));
	return edit;
}


----------------------------------------------------------------------------------------------

ChangeCollector

<<< this shows that we can have intermediate statements in the form and still have it be okay... or does it? Maybe we can't touch it, after all. >>>

private void addChange(IImportDeclaration importDecl, IJavaElementDelta newDelta) {
		SimpleDelta existingDelta = (SimpleDelta)this.changes.get(importDecl);
		int newKind = newDelta.getKind();
		if (existingDelta != null) {
			switch (newKind) {
				case IJavaElementDelta.ADDED:
					if (existingDelta.getKind() == IJavaElementDelta.REMOVED) {
						// REMOVED then ADDED
						this.changes.remove(importDecl);
					}
					break;
				case IJavaElementDelta.REMOVED:
					if (existingDelta.getKind() == IJavaElementDelta.ADDED) {
						// ADDED then REMOVED
						this.changes.remove(importDecl);
					}
					break;
				// CHANGED cannot happen for import declaration
			}
		} else {
			SimpleDelta delta = new SimpleDelta();
			switch (newKind) {
				case IJavaElementDelta.ADDED:
					delta.added();
					break;
				case IJavaElementDelta.REMOVED:
					delta.removed();
					break;
			}
			this.changes.put(importDecl, delta);
		}
	}

Similarly in:
	private void addChange(IImportContainer importContainer, IJavaElementDelta newDelta) throws JavaModelException
	private void addTypeAddition(IType type, SimpleDelta existingDelta) throws JavaModelException
	private void addTypeChange(IType type, int newFlags, SimpleDelta existingDelta) throws JavaModelException
	private void addTypeRemoval(IType type, SimpleDelta existingDelta)

----------------------------------------------------------------------------------------------

HierarchyBuilder

protected IType getHandle(IGenericType genericType) {
		if (genericType == null)
			return null;
		if (genericType instanceof HierarchyType) {
			IType handle = (IType)this.infoToHandle.get(genericType);
			if (handle == null) {
				handle = ((HierarchyType)genericType).typeHandle;
				this.infoToHandle.put(genericType, handle);
			}
			return handle;
		} else if (genericType.isBinaryType()) {
			IClassFile classFile = (IClassFile) this.infoToHandle.get(genericType);
			// if it's null, it's from outside the region, so do lookup
			if (classFile == null) {
				IType handle = lookupBinaryHandle((IBinaryType) genericType);
				if (handle == null)
					return null;
				// case of an anonymous type (see 1G2O5WK: ITPJCORE:WINNT - NullPointerException when selecting "Show in Type Hierarchy" for a inner class)
				// optimization: remember the handle for next call (case of java.io.Serializable that a lot of classes implement)
				this.infoToHandle.put(genericType, handle.getParent());
				return handle;
			} else {
				try {
					return classFile.getType();
				} catch (JavaModelException e) {
					return null;
				}
			}
		} else if (genericType instanceof SourceTypeElementInfo) {
			return ((SourceTypeElementInfo) genericType).getHandle();
		} else
			return null;
	}
	protected IType getType() {
		return this.hierarchy.getType();
	}


----------------------------------------------------------------------------------------------

TypeHierarchy

protected void initializeRegions() {

	IType[] allTypes = getAllTypes();
	for (int i = 0; i < allTypes.length; i++) {
		IType type = allTypes[i];
		Openable o = (Openable) ((JavaElement) type).getOpenableParent();
		if (o != null) {
			ArrayList types = (ArrayList)this.files.get(o);
			if (types == null) {
				types = new ArrayList();
				this.files.put(o, types);
			}
			...
		}
		

protected void addSubtype(IType type, IType subtype) {
	TypeVector subtypes = (TypeVector)this.typeToSubtypes.get(type);
	if (subtypes == null) {
		subtypes = new TypeVector();
		this.typeToSubtypes.put(type, subtypes);
	}
	if (!subtypes.contains(subtype)) {
		subtypes.add(subtype);
	}
}

public void store(OutputStream output, IProgressMonitor monitor) throws JavaModelException {
	...
	for (int i = 0; i < types.length; i++) {
		Object t = types[i];
		if(hashtable.get(t) == null) {
			Integer index = new Integer(count++);
			hashtable.put(t, index);
			hashtable2.put(index, t);
		}
		Object superClass = this.classToSuperclass.get(t);
		if(superClass != null && hashtable.get(superClass) == null) {
			Integer index = new Integer(count++);
			hashtable.put(superClass, index);
			hashtable2.put(index, superClass);
		}
	}
	types = this.typeToSuperInterfaces.keySet().toArray();
	for (int i = 0; i < types.length; i++) {
		Object t = types[i];
		if(hashtable.get(t) == null) {
			Integer index = new Integer(count++);
			hashtable.put(t, index);
			hashtable2.put(index, t);
		}
		Object[] sp = (Object[])this.typeToSuperInterfaces.get(t);
		if(sp != null) {
			for (int j = 0; j < sp.length; j++) {
				Object superInterface = sp[j];
				if(sp[j] != null && hashtable.get(superInterface) == null) {
					Integer index = new Integer(count++);
					hashtable.put(superInterface, index);
					hashtable2.put(index, superInterface);
				}
			}
		}
	}
	...
}

----------------------------------------------------------------------------------------------

HandleFactory

public Openable createOpenable(String resourcePath, IJavaSearchScope scope) {
	
	...
	
	IPackageFragment pkgFragment= (IPackageFragment) this.packageHandles.get(packageName);
	if (pkgFragment == null) {
		pkgFragment= this.lastPkgFragmentRoot.getPackageFragment(packageName);
		this.packageHandles.put(packageName, pkgFragment);
	}
	
	...
	
	IPackageFragment pkgFragment= (IPackageFragment) this.packageHandles.get(packageName);
	if (pkgFragment == null) {
		pkgFragment= this.lastPkgFragmentRoot.getPackageFragment(packageName);
		this.packageHandles.put(packageName, pkgFragment);
	}
	
	...
}


<<<<<<<< looks like a replace form >>>>>>>>>>
			
			private IJavaElement createElement(Scope scope, int elementPosition, ICompilationUnit unit, HashSet existingElements, HashMap knownScopes) {
				IJavaElement newElement = (IJavaElement)knownScopes.get(scope);
				if (newElement != null) return newElement;
			
				switch(scope.kind) {
					case Scope.COMPILATION_UNIT_SCOPE :
						newElement = unit;
						break;			
					case Scope.CLASS_SCOPE :
						IJavaElement parentElement = createElement(scope.parent, elementPosition, unit, existingElements, knownScopes);
						switch (parentElement.getElementType()) {
							case IJavaElement.COMPILATION_UNIT :
								newElement = ((ICompilationUnit)parentElement).getType(new String(scope.enclosingSourceType().sourceName));
								break;						
							case IJavaElement.TYPE :
								newElement = ((IType)parentElement).getType(new String(scope.enclosingSourceType().sourceName));
								break;
							case IJavaElement.FIELD :
							case IJavaElement.INITIALIZER :
							case IJavaElement.METHOD :
							    IMember member = (IMember)parentElement;
							    if (member.isBinary()) {
							        return null;
							    } else {
									newElement = member.getType(new String(scope.enclosingSourceType().sourceName), 1);
									// increment occurrence count if collision is detected
									if (newElement != null) {
										while (!existingElements.add(newElement)) ((JavaElement)newElement).occurrenceCount++;
									}
							    }
								break;						
						}
						if (newElement != null) {
							knownScopes.put(scope, newElement);
						}
						break;
						...
				}

----------------------------------------------------------------------------------------------

CopyElementsOperation

private String getSourceFor(IJavaElement element) throws JavaModelException {
	String source = (String) this.sources.get(element);
	if (source == null && element instanceof IMember) {
		IMember member = (IMember)element;
		ICompilationUnit cu = member.getCompilationUnit();
		String cuSource = cu.getSource();
		String cuName = cu.getElementName();
		source = computeSourceForElement(element, cuSource, cuName);
		this.sources.put(element, source);
	}
	return source;
}

----------------------------------------------------------------------------------------------

DeleteElementsOperation

protected void groupElements() throws JavaModelException {
	childrenToRemove = new HashMap(1);
	int uniqueCUs = 0;
	for (int i = 0, length = elementsToProcess.length; i < length; i++) {
		IJavaElement e = elementsToProcess[i];
		ICompilationUnit cu = getCompilationUnitFor(e);
		if (cu == null) {
			throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, e));
		} else {
			IRegion region = (IRegion) childrenToRemove.get(cu);
			if (region == null) {
				region = new Region();
				childrenToRemove.put(cu, region);
				uniqueCUs += 1;
			}
			region.add(e);
		}
	}
	elementsToProcess = new IJavaElement[uniqueCUs];
	Iterator iter = childrenToRemove.keySet().iterator();
	int i = 0;
	while (iter.hasNext()) {
		elementsToProcess[i++] = (IJavaElement) iter.next();
	}
}

----------------------------------------------------------------------------------------------

DeltaProcessingState

public void initializeRoots() {
	
	...
	
	// root path
	IPath path = entry.getPath();
	if (newRoots.get(path) == null) {
		newRoots.put(path, new DeltaProcessor.RootInfo(project, path, ((ClasspathEntry)entry).fullInclusionPatternChars(), ((ClasspathEntry)entry).fullExclusionPatternChars(), entry.getEntryKind()));
	} else {
		ArrayList rootList = (ArrayList)newOtherRoots.get(path);
		if (rootList == null) {
			rootList = new ArrayList();
			newOtherRoots.put(path, rootList);
		}
		rootList.add(new DeltaProcessor.RootInfo(project, path, ((ClasspathEntry)entry).fullInclusionPatternChars(), ((ClasspathEntry)entry).fullExclusionPatternChars(), entry.getEntryKind()));
	}
	
	...
	
	IPath sourceAttachmentPath;
	if (propertyString != null) {
		int index= propertyString.lastIndexOf(PackageFragmentRoot.ATTACHMENT_PROPERTY_DELIMITER);
		sourceAttachmentPath = (index < 0) ?  new Path(propertyString) : new Path(propertyString.substring(0, index));
	} else {
		sourceAttachmentPath = entry.getSourceAttachmentPath();
	}
	if (sourceAttachmentPath != null) {
		newSourceAttachments.put(sourceAttachmentPath, path);
	}
}

public synchronized void recordProjectUpdate(ProjectUpdateInfo newInfo) {
    
    JavaProject project = newInfo.project;
    ProjectUpdateInfo oldInfo = (ProjectUpdateInfo) this.projectUpdates.get(project);
    if (oldInfo != null) { // refresh new classpath information
        oldInfo.newRawPath = newInfo.newRawPath;
        oldInfo.newResolvedPath = newInfo.newResolvedPath;
    } else {
        this.projectUpdates.put(project, newInfo);
    }
}



----------------------------------------------------------------------------------------------

DeltaProcessor

<<< interesting: is a replace AND a putIfAbsent form: and multiple ones for either >>>

private boolean createExternalArchiveDelta(IProgressMonitor monitor) {
	...
	if (entries[j].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
		
		IPath entryPath = entries[j].getPath();
		
		if (!archivePathsToRefresh.contains(entryPath)) continue; // not supposed to be refreshed
		
		String status = (String)externalArchivesStatus.get(entryPath); 
		if (status == null){
			
			// compute shared status
			Object targetLibrary = JavaModel.getTarget(wksRoot, entryPath, true);

			if (targetLibrary == null){ // missing JAR
				if (this.state.externalTimeStamps.remove(entryPath) != null){
					externalArchivesStatus.put(entryPath, EXTERNAL_JAR_REMOVED);
					// the jar was physically removed: remove the index
					this.manager.indexManager.removeIndex(entryPath);
				}

			} else if (targetLibrary instanceof File){ // external JAR

				File externalFile = (File)targetLibrary;
				
				// check timestamp to figure if JAR has changed in some way
				Long oldTimestamp =(Long) this.state.externalTimeStamps.get(entryPath);
				long newTimeStamp = getTimeStamp(externalFile);
				if (oldTimestamp != null){

					if (newTimeStamp == 0){ // file doesn't exist
						externalArchivesStatus.put(entryPath, EXTERNAL_JAR_REMOVED);
						this.state.externalTimeStamps.remove(entryPath);
						// remove the index
						this.manager.indexManager.removeIndex(entryPath);

					} else if (oldTimestamp.longValue() != newTimeStamp){
						externalArchivesStatus.put(entryPath, EXTERNAL_JAR_CHANGED);
						this.state.externalTimeStamps.put(entryPath, new Long(newTimeStamp));
						// first remove the index so that it is forced to be re-indexed
						this.manager.indexManager.removeIndex(entryPath);
						// then index the jar
						this.manager.indexManager.indexLibrary(entryPath, project.getProject());
					} else {
						externalArchivesStatus.put(entryPath, EXTERNAL_JAR_UNCHANGED);
					}
				} else {
					if (newTimeStamp == 0){ // jar still doesn't exist
						externalArchivesStatus.put(entryPath, EXTERNAL_JAR_UNCHANGED);
					} else {
						externalArchivesStatus.put(entryPath, EXTERNAL_JAR_ADDED);
						this.state.externalTimeStamps.put(entryPath, new Long(newTimeStamp));
						// index the new jar
						this.manager.indexManager.indexLibrary(entryPath, project.getProject());
					}
				}
			} else { // internal JAR
				externalArchivesStatus.put(entryPath, INTERNAL_JAR_IGNORE);
			}
		}
		// according to computed status, generate a delta
		status = (String)externalArchivesStatus.get(entryPath); 
		if (status != null){
			if (status == EXTERNAL_JAR_ADDED){
				PackageFragmentRoot root = (PackageFragmentRoot)project.getPackageFragmentRoot(entryPath.toString());
				if (VERBOSE){
					System.out.println("- External JAR ADDED, affecting root: "+root.getElementName()); //$NON-NLS-1$
				} 
				elementAdded(root, null, null);
				hasDelta = true;
			} else if (status == EXTERNAL_JAR_CHANGED) {
				PackageFragmentRoot root = (PackageFragmentRoot)project.getPackageFragmentRoot(entryPath.toString());
				if (VERBOSE){
					System.out.println("- External JAR CHANGED, affecting root: "+root.getElementName()); //$NON-NLS-1$
				}
				// reset the corresponding project built state, since the builder would miss this change
				this.manager.setLastBuiltState(project.getProject(), null /*no state*/);
				contentChanged(root);
				hasDelta = true;
			} else if (status == EXTERNAL_JAR_REMOVED) {
				PackageFragmentRoot root = (PackageFragmentRoot)project.getPackageFragmentRoot(entryPath.toString());
				if (VERBOSE){
					System.out.println("- External JAR REMOVED, affecting root: "+root.getElementName()); //$NON-NLS-1$
				}
				elementRemoved(root, null, null);
				hasDelta = true;
			}
		}
	}
	...
}
}

----------------------------------------------------------------------------------------------

JavaModelOperation

<< replace form >>

protected void addReconcileDelta(ICompilationUnit workingCopy, IJavaElementDelta delta) {
	HashMap reconcileDeltas = JavaModelManager.getJavaModelManager().getDeltaProcessor().reconcileDeltas;
	JavaElementDelta previousDelta = (JavaElementDelta)reconcileDeltas.get(workingCopy);
	if (previousDelta != null) {
		IJavaElementDelta[] children = delta.getAffectedChildren();
		for (int i = 0, length = children.length; i < length; i++) {
			JavaElementDelta child = (JavaElementDelta)children[i];
			previousDelta.insertDeltaTree(child.getElement(), child);
		}
	} else {
		reconcileDeltas.put(workingCopy, delta);
	}
}

<< replace form >>

protected void setAttribute(Object key, Object attribute) {
	JavaModelOperation topLevelOp = (JavaModelOperation)this.getCurrentOperationStack().get(0);
	if (topLevelOp.attributes == null) {
		topLevelOp.attributes = new HashMap();
	}
	topLevelOp.attributes.put(key, attribute);
}

----------------------------------------------------------------------------------------------

JavaProject

protected boolean buildStructure(OpenableElementInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource) throws JavaModelException {
	...
	
	if (externalTimeStamps.get(path) == null) {
		long timestamp = DeltaProcessor.getTimeStamp((java.io.File)target);
		externalTimeStamps.put(path, new Long(timestamp));							
	}
	...
}


<< replace form >>

public Map getOptions(boolean inheritJavaCoreOptions) {
	...
	if (optionNames.contains(propertyName)){
		options.put(propertyName, value);
	}
	...
}

public IClasspathEntry[] getResolvedClasspath (
		...
		if (reverseMap != null && reverseMap.get(resolvedPath = resolvedEntry.getPath()) == null) reverseMap.put(resolvedPath , rawEntry);
		...
	}
	
<< The above happens three times. >>

----------------------------------------------------------------------------------------------

JavaProjectElementInfo

HashMap getAllPackageFragments(JavaProject project) {
	...
	
	IPackageFragment[] entry= (IPackageFragment[]) cache.get(fragment.getElementName());
	if (entry == null) {
		entry= new IPackageFragment[1];
		entry[0]= fragment;
		cache.put(fragment.getElementName(), entry);
	} else {
		IPackageFragment[] copy= new IPackageFragment[entry.length + 1];
		System.arraycopy(entry, 0, copy, 0, entry.length);
		copy[entry.length]= fragment;
		cache.put(fragment.getElementName(), copy);
	}
	...
}

----------------------------------------------------------------------------------------------

----------------------------------------------------------------------------------------------

----------------------------------------------------------------------------------------------

----------------------------------------------------------------------------------------------

==============================================================================================

Tomcat 4.1.37

JspRuntimeContext

<< replace form >>

public void addWrapper(String jspUri, JspServletWrapper jsw) {
    jsps.remove(jspUri);
    jsps.put(jspUri,jsw);
}

----------------------------------------------------------------------------------------------

TreeControl

void addNode(TreeControlNode node) throws IllegalArgumentException {

    synchronized (registry) {
        String name = node.getName();
        if (registry.containsKey(name))
            throw new IllegalArgumentException("Name '" + name +
                                               "' is not unique");
        node.setTree(this);
        registry.put(name, node);
    }

}

----------------------------------------------------------------------------------------------

TestClient

protected void save(String name, String value) {

    String key = name.toLowerCase();
    ArrayList list = (ArrayList) saveHeaders.get(key);
    if (list == null) {
        list = new ArrayList();
        saveHeaders.put(key, list);
    }
    list.add(value);

}

----------------------------------------------------------------------------------------------

FastHttpDateFormat

/**
 * Get the HTTP format of the specified date.
 */
public static String getDate(Date date) {

    String cachedDate = (String) dateCache.get(date);
    if (cachedDate != null)
        return cachedDate;

    String newDate = null;
    synchronized (format) {
        newDate = format.format(date);
        dateCache.put(date, newDate);
    }
    return newDate;

}

----------------------------------------------------------------------------------------------

RequestUtil

<< replace form >>

private static void putMapEntry( Map map, String name, String value) {
    String[] newValues = null;
    String[] oldValues = (String[]) map.get(name);
    if (oldValues == null) {
        newValues = new String[1];
        newValues[0] = value;
    } else {
        newValues = new String[oldValues.length + 1];
        System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
        newValues[oldValues.length] = value;
    }
    map.put(name, newValues);
}

----------------------------------------------------------------------------------------------

HostConfig

<< replace AND putIfAbsent forms >>

protected void checkWebXmlLastModified() {
	...
	 Long lastModified = (Long) webXmlLastModified.get(contextName);
    if (lastModified == null) {
        webXmlLastModified.put
            (contextName, new Long(newLastModified));
    } else {
        if (lastModified.longValue() != newLastModified) {
            webXmlLastModified.remove(contextName);
            ((Lifecycle) context).stop();
            // Note: If the context was already stopped, a 
            // Lifecycle exception will be thrown, and the context
            // won't be restarted
            ((Lifecycle) context).start();
        }
    }
    ...
}

----------------------------------------------------------------------------------------------

StandardSession

<< replace form, not an inferrable putIfAbsent form? >>

public void setAttribute(String name, Object value) {
	...
	// Replace or add this attribute
    Object unbound = null;
    synchronized (attributes) {
        unbound = attributes.get(name);
        attributes.put(name, value);
    }
}

----------------------------------------------------------------------------------------------

StandardClassLoader

protected final PermissionCollection getPermissions(CodeSource codeSource) {
    if (!policy_refresh) {
        // Refresh the security policies
        Policy policy = Policy.getPolicy();
        policy.refresh();
        policy_refresh = true;
    }
    String codeUrl = codeSource.getLocation().toString();
    PermissionCollection pc;
    if ((pc = (PermissionCollection)loaderPC.get(codeUrl)) == null) {
        pc = super.getPermissions(codeSource);
        if (pc != null) {
            Iterator perms = permissionList.iterator();
            while (perms.hasNext()) {
                Permission p = (Permission)perms.next();
                pc.add(p);
            }
            loaderPC.put(codeUrl,pc);
        }
    }
    return (pc);

}

----------------------------------------------------------------------------------------------

WebappClassLoader

protected PermissionCollection getPermissions(CodeSource codeSource) {

    String codeUrl = codeSource.getLocation().toString();
    PermissionCollection pc;
    if ((pc = (PermissionCollection)loaderPC.get(codeUrl)) == null) {
        pc = super.getPermissions(codeSource);
        if (pc != null) {
            Iterator perms = permissionList.iterator();
            while (perms.hasNext()) {
                Permission p = (Permission)perms.next();
                pc.add(p);
            }
            loaderPC.put(codeUrl,pc);
        }
    }
    return (pc);

}

protected ResourceEntry findResourceInternal(String name, String path) {
	...
    // Add the entry in the local resource repository
    synchronized (resourceEntries) {
        // Ensures that all the threads which may be in a race to load
        // a particular class all end up with the same ResourceEntry
        // instance
        ResourceEntry entry2 = (ResourceEntry) resourceEntries.get(name);
        if (entry2 == null) {
            resourceEntries.put(name, entry);
        } else {
            entry = entry2;
        }
    }
    return entry;
}

----------------------------------------------------------------------------------------------

NamingResources

public void addEjb(ContextEjb ejb) {

    if (entries.containsKey(ejb.getName())) {
        return;
    } else {
        entries.put(ejb.getName(), ejb.getType());
    }
    ...
}

public void addEnvironment(ContextEnvironment environment) {

    if (entries.containsKey(environment.getName())) {
        return;
    } else {
        entries.put(environment.getName(), environment.getType());
    }
    ...
}

public void addResourceParams(ResourceParams resourceParameters) {

    synchronized (resourceParams) {
        if (resourceParams.containsKey(resourceParameters.getName())) {
            return;
        }
        resourceParameters.setNamingResources(this);
        resourceParams.put(resourceParameters.getName(),
                           resourceParameters);
    }
    support.firePropertyChange("resourceParams", null, resourceParameters);

}

public void addLocalEjb(ContextLocalEjb ejb) {

    if (entries.containsKey(ejb.getName())) {
        return;
    } else {
        entries.put(ejb.getName(), ejb.getType());
    }

    synchronized (localEjbs) {
        ejb.setNamingResources(this);
        localEjbs.put(ejb.getName(), ejb);
    }
    support.firePropertyChange("localEjb", null, ejb);

}

public void addResource(ContextResource resource) {

    if (entries.containsKey(resource.getName())) {
        return;
    } else {
        entries.put(resource.getName(), resource.getType());
    }

    synchronized (resources) {
        resource.setNamingResources(this);
        resources.put(resource.getName(), resource);
    }
    support.firePropertyChange("resource", null, resource);

}

public void addResourceEnvRef(String name, String type) {

    if (entries.containsKey(name)) {
        return;
    } else {
        entries.put(name, type);
    }

    synchronized (resourceEnvRefs) {
        resourceEnvRefs.put(name, type);
    }
    support.firePropertyChange("resourceEnvRef", null,
                               name + ":" + type);

}

public void addResourceLink(ContextResourceLink resourceLink) {

    if (entries.containsKey(resourceLink.getName())) {
        return;
    } else {
        Object value = resourceLink.getType();
        if (value == null) {
            value = "";
        }
        entries.put(resourceLink.getName(), value);
    }
...
}

----------------------------------------------------------------------------------------------

ClassLoaderLogManager

public synchronized boolean addLogger(final Logger logger) {

    final String loggerName = logger.getName();

    ClassLoader classLoader = 
        Thread.currentThread().getContextClassLoader();
    ClassLoaderLogInfo info = getClassLoaderInfo(classLoader);
    if (info.loggers.containsKey(loggerName)) {
        return false;
    }
    info.loggers.put(loggerName, logger);
    ...
}

protected static final class LogNode {
	...
	LogNode childNode = (LogNode) currentNode.children.get(nextName);
	if (childNode == null) {
		childNode = new LogNode(currentNode);
		currentNode.children.put(nextName, childNode);
	}
	...
}

----------------------------------------------------------------------------------------------

ModjkMX

public void refreshMetadata() {
	...
	if( mbeans.get( name ) ==null ) {
        // New component
        newCnt++;
        MBeanProxy mproxy=new MBeanProxy(this);
        mproxy.init( name, getters, setters, methods);
        mbeans.put( name, mproxy );
    }
	...
}

----------------------------------------------------------------------------------------------

Ajp13Request

protected void parseLocalesHeader(String value) {
	...
	ArrayList values = (ArrayList) locales.get(key);
    if (values == null) {
        values = new ArrayList();
        locales.put(key, values);
    }
    ...
}

----------------------------------------------------------------------------------------------

StringCache

public static String toString(ByteChunk bc) {
	...
	 ArrayList list = (ArrayList) tempMap.get(count);
    if (list == null) {
        // Create list
        list = new ArrayList();
        tempMap.put(count, list);
    }
    ...
    int[] count = (int[]) bcStats.get(entry);
    if (count == null) {
        int end = bc.getEnd();
        int start = bc.getStart();
        // Create byte array and copy bytes
        entry.name = new byte[bc.getLength()];
        System.arraycopy(bc.getBuffer(), start, entry.name, 0, end - start);
        // Set encoding
        entry.enc = bc.getEncoding();
        // Initialize occurrence count to one 
        count = new int[1];
        count[0] = 1;
        // Set in the stats hash map
        bcStats.put(entry, count);
    }
    ...
}

public static String toString(CharChunk cc) {
	...
	ArrayList list = (ArrayList) tempMap.get(count);
    if (list == null) {
        // Create list
        list = new ArrayList();
        tempMap.put(count, list);
    }
    ...
    int[] count = (int[]) ccStats.get(entry);
    if (count == null) {
        int end = cc.getEnd();
        int start = cc.getStart();
        // Create char array and copy chars
        entry.name = new char[cc.getLength()];
        System.arraycopy(cc.getBuffer(), start, entry.name, 0, end - start);
        // Initialize occurrence count to one 
        count = new int[1];
        count[0] = 1;
        // Set in the stats hash map
        ccStats.put(entry, count);
    }
    ...
}

----------------------------------------------------------------------------------------------

Digester

public void startPrefixMapping(String prefix, String namespaceURI) throws SAXException {
	...
	// Register this prefix mapping
    ArrayStack stack = (ArrayStack) namespaces.get(prefix);
    if (stack == null) {
        stack = new ArrayStack();
        namespaces.put(prefix, stack);
    }
    ...
}

public void push(String stackName, Object value) {
    ArrayStack namedStack = (ArrayStack) stacksByName.get(stackName);
    if (namedStack == null) {
        namedStack = new ArrayStack();
        stacksByName.put(stackName, namedStack);
    }
    namedStack.push(value);
}

----------------------------------------------------------------------------------------------

RulesBase

public void add(String pattern, Rule rule) {
	...
	List list = (List) cache.get(pattern);
    if (list == null) {
        list = new ArrayList();
        cache.put(pattern, list);
    }
    ...
}

----------------------------------------------------------------------------------------------

----------------------------------------------------------------------------------------------

----------------------------------------------------------------------------------------------

----------------------------------------------------------------------------------------------

----------------------------------------------------------------------------------------------

----------------------------------------------------------------------------------------------

----------------------------------------------------------------------------------------------

==============================================================================================

==============================================================================================
	
==============================================================================================	

TODO

- Search for putIfAbsent in Tomcat 6 and MINA 1.1

- Ask Danny whether he thinks we can transform the intermediate statement case and be okay, because this happens pretty much everywhere
- There are a lot of "if not in this map, put in this other map" cases. Can we do anything with these?





=============================================================================================

http://code.google.com/p/google-web-toolkit/source/browse/trunk/dev/core/src/com/google/gwt/dev/jdt/CacheManager.java?r=27

=============================================================================================

private void add(String dependerFilename, String dependeeFilename) {
      if (!map.containsKey(dependeeFilename)) {
        map.put(dependeeFilename, new HashSet());
      }
      get(dependeeFilename).add(dependerFilename);
    }




void addDependentsToChangedFiles() {
...

ICompilationUnit findUnitForCup(CompilationUnitProvider cup) {
    if (!unitsByCup.containsKey(cup.getLocation())) {
      unitsByCup.put(cup.getLocation(), new ICompilationUnitAdapter(cup));
    }
    return (ICompilationUnit) unitsByCup.get(cup.getLocation());
  }

...
}











=====================================
	TODO deal with these cases


TextEdit create(CopySourceInfo info) {
	int start= getExtendedOffset(info.getStartNode());
	int end= getExtendedEnd(info.getEndNode());
	if (info.isMove) {
		MoveSourceEdit moveSourceEdit= new MoveSourceEdit(start, end - start);
		moveSourceEdit.setTargetEdit(new MoveTargetEdit(0));
		edit= moveSourceEdit;
	} else {
		CopySourceEdit copySourceEdit= new CopySourceEdit(start, end - start);
		copySourceEdit.setTargetEdit(new CopyTargetEdit(0));
		edit= copySourceEdit;
	}
}


final TextEdit getCopySourceEdit(CopySourceInfo info) {
	
	sourceCopyInfoToEdit.putIfAbsent(info, create(info));
	return edit;
}