/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.refactorings;

import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationSymbols;
import edu.illinois.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public class FinishedRefactoringOperation extends UserOperation {

	private boolean success;

	public FinishedRefactoringOperation() {
		super();
	}

	public FinishedRefactoringOperation(boolean success) {
		super();
		this.success= success;
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.REFACTORING_FINISHED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Finished refactoring";
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(success);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		success= operationLexer.readBoolean();
	}

	@Override
	public void replay() {
		isRefactoring= false;
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Success: " + success + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
