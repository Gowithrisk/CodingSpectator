/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations;

import java.util.Date;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class UserOperation {

	protected static IDocument currentDocument;

	protected static ISourceViewer currentViewer;

	private long timestamp;

	public UserOperation() {
		timestamp= System.currentTimeMillis();
	}

	public UserOperation(long timestamp) {
		this.timestamp= timestamp;
	}

	public OperationTextChunk generateSerializationText() {
		OperationTextChunk textChunk= new OperationTextChunk(getOperationSymbol());
		populateTextChunk(textChunk);
		textChunk.append(timestamp);
		Debugger.debugTextChunk(getDescription() + ": ", textChunk);
		return textChunk;
	}

	public void deserialize(OperationLexer operationLexer) {
		assert operationLexer.getCurrentOperationSymbol() == getOperationSymbol();
		initializeFrom(operationLexer);
		timestamp= Long.valueOf(operationLexer.getNextLexeme());
	}

	@Override
	public String toString() {
		return "Timestamp: " + timestamp;
	}

	public long getTime() {
		return timestamp;
	}

	public Date getDate() {
		return new Date(timestamp);
	}

	public boolean isTestReplayRecorded() {
		return true;
	}

	protected abstract char getOperationSymbol();

	public abstract String getDescription();

	protected abstract void populateTextChunk(OperationTextChunk textChunk);

	protected abstract void initializeFrom(OperationLexer operationLexer);

	public abstract void replay() throws Exception;

}
