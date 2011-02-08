/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.tests.old;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.runner.RunWith;

/**
 * @author Balaji Ambresh Rajkumar
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class RenameInterfaceTypeTest extends CodingSpectatorTest {

	private static final String RENAME_TYPE_DIALOG_NAME= "Rename Local Variable";

	private static final String RENAME_TYPE_MENU_ITEM= "Rename...";

	private static final String TEST_FILE_NAME= "RenameInterfaceTypeTestFile";

	@Override
	protected String getRefactoringDialogName() {
		return RENAME_TYPE_DIALOG_NAME;
	}

	@Override
	public void selectElementToRefactor() {
		selectElementToRefactor(28, 64, 72 - 64);
	}

	@Override
	String getTestFileName() {
		return TEST_FILE_NAME;
	}

	@Override
	protected void configureRefactoringToPerform() {
		super.configureRefactoringToPerform();
		configureRefactoring();
	};

	@Override
	protected void configureRefactoringToCancel() {
		super.configureRefactoringToCancel();
		configureRefactoring();
	}

	protected void configureRefactoring() {
		final String originalInterfaceName= bot.textWithLabel("New name:").getText();
		// the lower case is on purpose so that a warning dialog would come up for renaming an interface
		// starting with a lower case. 
		bot.textWithLabel("New name:").setText("renamed_" + originalInterfaceName);
	}


	/**
	 * Invoking the Rename menu option twice from the Refactor menu brings up the Rename dialog.
	 */
	@Override
	protected void invokeRefactoring() {
		super.invokeRefactoring();
		super.invokeRefactoring();
	}

	@Override
	protected String refactoringMenuItemName() {
		return RENAME_TYPE_MENU_ITEM;
	}

}
