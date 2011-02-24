/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.conflicteditors;

import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingspectator.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class ConflictEditorOperation extends UserOperation {

	private String editorID;

	public ConflictEditorOperation() {
		super();
	}

	public ConflictEditorOperation(String editorID) {
		super();
		this.editorID= editorID;
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(editorID);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		editorID= operationLexer.getNextLexeme();
	}

	@Override
	public void replay() {
		throw new RuntimeException("Unsupported operation");
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Editor ID: " + editorID + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
