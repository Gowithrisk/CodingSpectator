package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveProcessor;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public abstract class WatchedJavaMoveProcessor extends MoveProcessor implements IWatchedJavaProcessor {

	protected WatchedProcessorDelegate watchedProcessorDelegate;

	public RefactoringDescriptor getSimpleRefactoringDescriptor(RefactoringStatus refactoringStatus) {
		return getWatchedProcessorDelegate().getSimpleRefactoringDescriptor(refactoringStatus);
	}

	/**
	 * @deprecated
	 */
	public String getSelection() {
		return getWatchedProcessorDelegate().getSelection();
	}

	public String getJavaProjectName() {
		return getWatchedProcessorDelegate().getJavaProjectName();
	}

	protected WatchedProcessorDelegate getWatchedProcessorDelegate() {
		if (watchedProcessorDelegate == null)
			watchedProcessorDelegate= instantiateDelegate();
		return watchedProcessorDelegate;
	}

	protected abstract WatchedProcessorDelegate instantiateDelegate();

}
