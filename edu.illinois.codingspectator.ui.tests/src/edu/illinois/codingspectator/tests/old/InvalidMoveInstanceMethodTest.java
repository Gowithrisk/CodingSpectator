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
public class InvalidMoveInstanceMethodTest extends MoveInstanceMethodTest {

	@Override
	public void selectElementToRefactor() {
		selectElementToRefactor(11, 9, 2);
	}

}
