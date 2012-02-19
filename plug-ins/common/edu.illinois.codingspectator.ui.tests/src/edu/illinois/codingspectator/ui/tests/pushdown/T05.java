/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.pushdown;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.CodingSpectatorBot;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * @author Balaji Ambresh Rajkumar
 * @author Mohsen Vakilian
 * 
 */
public class T05 extends RefactoringTest {

	private static final String PUSH_DOWN_MENU_ITEM= "Push Down...";

	@Override
	protected String getTestFileName() {
		return "PushDownSingleFieldTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 6, 16, "fieldToBePushedDown".length());
		bot.invokeRefactoringFromMenu(PUSH_DOWN_MENU_ITEM);

		bot.clickButtons(CodingSpectatorBot.PREVIEW_LABEL, IDialogConstants.CANCEL_LABEL);

		bot.invokeRefactoringFromMenu(PUSH_DOWN_MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
