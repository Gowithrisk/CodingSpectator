/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.textchanges;

import org.eclipse.jface.text.TextEvent;

import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
public class UndoneTextChangeOperation extends TextChangeOperation {

	public UndoneTextChangeOperation() {
		super();
	}

	public UndoneTextChangeOperation(TextEvent textEvent) {
		super(textEvent);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.TEXT_CHANGE_UNDONE_SYMBOL;
	}

	@Override
	protected String getDebugMessage() {
		return "Undone text change: ";
	}

}
