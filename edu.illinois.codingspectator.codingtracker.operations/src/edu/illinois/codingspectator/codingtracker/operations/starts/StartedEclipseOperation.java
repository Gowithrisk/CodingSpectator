/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.starts;

import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingspectator.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public class StartedEclipseOperation extends UserOperation {

	public StartedEclipseOperation() {
		super();
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.ECLIPSE_STARTED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Started Eclipse";
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		//Nothing to populate here
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		//Nothing to initialize		
	}

}
