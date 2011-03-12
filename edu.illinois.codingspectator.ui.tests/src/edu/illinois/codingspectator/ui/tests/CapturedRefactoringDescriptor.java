/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import java.util.Map;

import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractConstantRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.code.InlineConstantRefactoring;
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

	// Declared in ExtractMethodRefactoring and ExtractConstantRefactoring.
	public int getVisibility() {
		if (!"visibility".equals(ExtractMethodRefactoring.ATTRIBUTE_VISIBILITY) || !"visibility".equals(ExtractConstantRefactoring.ATTRIBUTE_VISIBILITY)) {
			throw new RuntimeException("Inconsistent attribute names.");
		}

		return Integer.valueOf(getAttribute(ExtractMethodRefactoring.ATTRIBUTE_VISIBILITY));
	}

	// Declared in ExtractMethodRefactoring.
	public int getDestination() {
		return Integer.valueOf(getAttribute(ExtractMethodRefactoring.ATTRIBUTE_DESTINATION));
	}

	// Declared in ExtractMethodRefactoring.
	public boolean getComments() {
		return Boolean.valueOf(getAttribute(ExtractMethodRefactoring.ATTRIBUTE_COMMENTS));
	}

	// Declared in ExtractMethodRefactoring, InlineConstantRefactoring and ExtractConstantRefactoring.
	public boolean getReplace() {
		if (!"replace".equals(InlineConstantRefactoring.ATTRIBUTE_REPLACE) || !"replace".equals(ExtractMethodRefactoring.ATTRIBUTE_REPLACE)
				|| !"replace".equals(ExtractMethodRefactoring.ATTRIBUTE_REPLACE)) {
			throw new RuntimeException("Inconsistent attribute names.");
		}

		return Boolean.valueOf(getAttribute("replace"));
	}

	// Declared in ExtractMethodRefactoring.
	public boolean getExceptions() {
		return Boolean.valueOf(getAttribute(ExtractMethodRefactoring.ATTRIBUTE_EXCEPTIONS));
	}

	// Declared in InlineConstantRefactoring.
	public boolean getRemove() {
		return Boolean.valueOf(getAttribute("remove"));
	}

	// Declared in ExtractConstantRefactoring.
	public boolean getQualify() {
		return Boolean.valueOf(getAttribute(ExtractConstantRefactoring.ATTRIBUTE_QUALIFY));
	}

}
