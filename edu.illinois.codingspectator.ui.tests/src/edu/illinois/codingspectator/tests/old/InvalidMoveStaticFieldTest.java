/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.tests.old;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class InvalidMoveStaticFieldTest extends MoveStaticMemberTest {

	@Override
	protected String getDestinationType() {
		return "edu.illinois.codingspectator.C3";
	}

	@Override
	protected String getSelectedMember() {
		return "field";
	}

	@Override
	public void selectElementToRefactor() {
		selectElementToRefactor(7, 18, 24 - 19);
	}

	@Override
	protected String[] getRefactoringDialogPerformButtonSequence() {
		return new String[] { OK_BUTTON_LABEL, CONTINUE_BUTTON_LABEL };
	}

}
