/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.tests;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class PullUpSelectedFieldByFinishTest extends PullUpTest {

	@Override
	protected void selectElementToRefactor() {
		selectElementToRefactor(11, 11, 6);
	}

	@Override
	protected String[] getRefactoringDialogApplyButtonSequence() {
		return new String[] { FINISH_BUTTON_LABEL };
	}

}