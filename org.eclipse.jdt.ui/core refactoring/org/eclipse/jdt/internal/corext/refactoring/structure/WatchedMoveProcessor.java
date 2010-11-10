package org.eclipse.jdt.internal.corext.refactoring.structure;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ltk.core.refactoring.IWatchedProcessor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveProcessor;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;

import org.eclipse.jdt.internal.ui.JavaPlugin;

/**
 * 
 * @author nchen
 * @author Mohsen Vakilian
 * 
 */
abstract public class WatchedMoveProcessor extends MoveProcessor implements IWatchedProcessor {

	public RefactoringDescriptor getSimpleRefactoringDescriptor(RefactoringStatus refactoringStatus) {
		JavaRefactoringDescriptor d= createRefactoringDescriptor();
		final Map augmentedArguments= populateInstrumentationData(refactoringStatus, getArguments(d));

		return createRefactoringDescriptor(d.getProject(), d.getDescription(), d.getComment(), augmentedArguments, d.getFlags());
	}

	abstract protected RefactoringDescriptor createRefactoringDescriptor(String project, String description, String comment, Map arguments, int flags);

	protected Map getArguments(JavaRefactoringDescriptor d) {
		try {
			Class c= JavaRefactoringDescriptor.class;
			Method getArgumentsMethod= c.getDeclaredMethod("getArguments", new Class[] {}); //$NON-NLS-1$
			getArgumentsMethod.setAccessible(true);
			return (Map)getArgumentsMethod.invoke(d, new Object[] {});
		} catch (Exception e) {
			JavaPlugin.log(e);
		}
		return new HashMap();

	}

	protected abstract JavaRefactoringDescriptor createRefactoringDescriptor();

	protected Map populateInstrumentationData(RefactoringStatus refactoringStatus, Map basicArguments) {
		basicArguments.put(RefactoringDescriptor.ATTRIBUTE_CODE_SNIPPET, getCodeSnippet());
		basicArguments.put(RefactoringDescriptor.ATTRIBUTE_SELECTION, getSelection());
		basicArguments.put(RefactoringDescriptor.ATTRIBUTE_STATUS, refactoringStatus.toString());
		return basicArguments;
	}

	protected String getSelection() {
		IJavaElement javaElementIfPossible= getJavaElementIfPossible();
		if (javaElementIfPossible != null)
			return javaElementIfPossible.getElementName();
		return "CODINGSPECTATOR: non-Java element selected"; //$NON-NLS-1$
	}


	protected String getCodeSnippet() {
		IJavaElement javaElementIfPossible= getJavaElementIfPossible();
		if (javaElementIfPossible != null)
			return javaElementIfPossible.toString();
		return "CODINGSPECTATOR: non-Java element selected"; //$NON-NLS-1$
	}

	private IJavaElement getJavaElementIfPossible() {
		if (getElements()[0] instanceof IJavaElement)
			return ((IJavaElement)getElements()[0]);
		return null;
	}

}
