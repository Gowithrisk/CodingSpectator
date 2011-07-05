/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.extractlocalvariable;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * Test performing an extract local variable refactoring.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class T01 extends RefactoringTest {

	private static final String MENU_ITEM= "Extract Local Variable...";

	private static final String TEST_FILE_NAME= "ExtractLocalVariableTestFile";

	private static final String SELECTION= "\"Test\"";

	@Override
	protected String getTestFileName() {
		return TEST_FILE_NAME;
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 8, 27, SELECTION.length());
		bot.invokeRefactoringFromMenu(MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
