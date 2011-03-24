/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.pullup;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;

import edu.illinois.codingspectator.ui.tests.RefactoringLog.LogType;
import edu.illinois.codingspectator.ui.tests.RefactoringLogChecker;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class T02 extends RefactoringTest {

	private static final String PULL_UP_MENU_ITEM= "Pull Up...";


	@Override
	protected String getTestFileName() {
		return "InvalidPullUpMethodTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 14, 9, "m".length());
		bot.invokeRefactoringFromMenu(PULL_UP_MENU_ITEM);
		bot.clickButtons(IDialogConstants.FINISH_LABEL);
		try {
			bot.clickButtons(IDialogConstants.OK_LABEL);
		} catch (WidgetNotFoundException exception) {
			// FIXME: On my machine i.e. Mac, the second dialog box does not appear so SWTBot can't click on OK.
			bot.clickButtons(IDialogConstants.FINISH_LABEL);
		}
		System.err
				.println("Eclipse (with or without CodingSpectator) fails to perform this refactoring and doesn't log it. This test makes Eclipse throw the following exceptions: java.lang.reflect.InvocationTargetException\nCaused by: java.lang.NullPointerException\nRoot exception:\njava.lang.NullPointerException");
	}

	@Override
	protected Collection<RefactoringLogChecker> getRefactoringLogCheckers() {
		return Arrays.asList(new RefactoringLogChecker(LogType.PERFORMED, getRefactoringKind(), getClass().getSimpleName(), getProjectName()));
	}
}
