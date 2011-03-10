/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import java.util.Map;

import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.codingspectator.Logger;

/**
 * 
 * This class provides provides easy access to the contents of a refactoring descriptor.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
@SuppressWarnings("restriction")
public class CapturedRefactoringDescriptor {

	private JavaRefactoringDescriptor descriptor;

	public CapturedRefactoringDescriptor(JavaRefactoringDescriptor descriptor) {
		this.descriptor= descriptor;
	}

	@SuppressWarnings("rawtypes")
	private Map getArguments() {
		return descriptor.getArguments();
	}

	private String getAttribute(String attributeKey) {
		return (String)getArguments().get(attributeKey);
	}

	public String getComment() {
		return descriptor.getComment();
	}

	public String getDescription() {
		return descriptor.getDescription();
	}

	public int getFlags() {
		return descriptor.getFlags();
	}

	public String getID() {
		return descriptor.getID();
	}

	public String getProject() {
		return descriptor.getProject();
	}

	public long getTimestamp() {
		return descriptor.getTimeStamp();
	}

	public String getElement() {
		return getAttribute(JavaRefactoringDescriptor.ATTRIBUTE_ELEMENT);
	}

	public String getInput() {
		return getAttribute(JavaRefactoringDescriptor.ATTRIBUTE_INPUT);
	}

	public String getName() {
		return getAttribute(JavaRefactoringDescriptor.ATTRIBUTE_NAME);
	}

	public boolean doesReference() {
		return Boolean.valueOf(getAttribute(JavaRefactoringDescriptor.ATTRIBUTE_REFERENCES));
	}

	public String getSelection() {
		return getAttribute(JavaRefactoringDescriptor.ATTRIBUTE_SELECTION);
	}

	// Attributes added by CodingSpectator

	public String getSelectionText() {
		return getAttribute(RefactoringDescriptor.ATTRIBUTE_SELECTION_TEXT);
	}

	public String getSelectionInCodeSnippet() {
		return getAttribute(RefactoringDescriptor.ATTRIBUTE_SELECTION_IN_CODE_SNIPPET);
	}

	public String getStatus() {
		return getAttribute(RefactoringDescriptor.ATTRIBUTE_STATUS);
	}

	public String getCodeSnippet() {
		return getAttribute(RefactoringDescriptor.ATTRIBUTE_CODE_SNIPPET);
	}

	public boolean isInvokedByQuickAssist() {
		return Boolean.valueOf(getAttribute(RefactoringDescriptor.ATTRIBUTE_INVOKED_BY_QUICKASSIST));
	}

	public String getNavigationHistory() {
		return getAttribute(Logger.NAVIGATION_HISTORY_ATTRIBUTE);
	}

	// Attributes declared in ExtractMethodRefactoring.

	public int getVisibility() {
		return Integer.valueOf(getAttribute(ExtractMethodRefactoring.ATTRIBUTE_VISIBILITY));
	}

	public int getDestination() {
		return Integer.valueOf(getAttribute(ExtractMethodRefactoring.ATTRIBUTE_DESTINATION));
	}

	public boolean getComments() {
		return Boolean.valueOf(getAttribute(ExtractMethodRefactoring.ATTRIBUTE_COMMENTS));
	}

	public boolean getReplace() {
		return Boolean.valueOf(getAttribute(ExtractMethodRefactoring.ATTRIBUTE_REPLACE));
	}

	public boolean getExceptions() {
		return Boolean.valueOf(getAttribute(ExtractMethodRefactoring.ATTRIBUTE_EXCEPTIONS));
	}

}
