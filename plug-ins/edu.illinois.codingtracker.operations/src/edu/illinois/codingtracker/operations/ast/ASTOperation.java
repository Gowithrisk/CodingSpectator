/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.ast;

import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationSymbols;
import edu.illinois.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperationDescriptor.OperationKind;

/**
 * 
 * @author Stas Negara
 * 
 */
public class ASTOperation extends UserOperation {

	private ASTOperationDescriptor operationDescriptor;

	private CompositeNodeDescriptor affectedNodeDescriptor;


	public ASTOperation() {
		super();
	}

	public ASTOperation(ASTOperationDescriptor operationDescriptor, CompositeNodeDescriptor affectedNodeDescriptor, long timestamp) {
		super(timestamp);
		this.operationDescriptor= operationDescriptor;
		this.affectedNodeDescriptor= affectedNodeDescriptor;
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.AST_OPERATION_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "AST operation";
	}

	public long getNodeID() {
		return affectedNodeDescriptor.getNodeID();
	}

	public long getMethodID() {
		return affectedNodeDescriptor.getMethodID();
	}

	public String getMethodName() {
		return affectedNodeDescriptor.getMethodFullName();
	}

	public int getMethodLinesCount() {
		return affectedNodeDescriptor.getMethodLinesCount();
	}

	public int getMethodCyclomaticComplexity() {
		return affectedNodeDescriptor.getMethodCyclomaticComplexity();
	}

	public boolean isCommentingOrUncommenting() {
		return operationDescriptor.isCommentingOrUncommenting();
	}

	public boolean isUndoing() {
		return operationDescriptor.isUndoing();
	}

	public boolean isAdd() {
		return operationDescriptor.isAdd();
	}

	public boolean isChange() {
		return operationDescriptor.isChange();
	}

	public boolean isDelete() {
		return operationDescriptor.isDelete();
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(operationDescriptor.getOperationKind().ordinal());
		textChunk.append(operationDescriptor.isCommentingOrUncommenting());
		textChunk.append(operationDescriptor.isUndoing());
		textChunk.append(affectedNodeDescriptor.getNodeID());
		textChunk.append(affectedNodeDescriptor.getNodeType());
		textChunk.append(affectedNodeDescriptor.getNodeText());
		textChunk.append(affectedNodeDescriptor.getNodeNewText());
		textChunk.append(affectedNodeDescriptor.getNodeOffset());
		textChunk.append(affectedNodeDescriptor.getNodeLength());
		textChunk.append(affectedNodeDescriptor.getMethodID());
		textChunk.append(affectedNodeDescriptor.getMethodFullName());
		textChunk.append(affectedNodeDescriptor.getMethodLinesCount());
		textChunk.append(affectedNodeDescriptor.getMethodCyclomaticComplexity());
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		operationDescriptor= new ASTOperationDescriptor(OperationKind.values()[operationLexer.readInt()],
				operationLexer.readBoolean(), operationLexer.readBoolean());
		ASTNodeDescriptor astNodeDescriptor= new ASTNodeDescriptor(operationLexer.readLong(), operationLexer.readString(),
				operationLexer.readString(), operationLexer.readString(), operationLexer.readInt(), operationLexer.readInt());
		ASTMethodDescriptor astMethodDescriptor= new ASTMethodDescriptor(operationLexer.readLong(),
				operationLexer.readString(), operationLexer.readInt(), operationLexer.readInt());
		affectedNodeDescriptor= new CompositeNodeDescriptor(astNodeDescriptor, astMethodDescriptor);
	}

	@Override
	public void replay() {
		//do nothing
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Operation kind: " + operationDescriptor.getOperationKind() + "\n");
		sb.append("Is commenting or uncommenting: " + operationDescriptor.isCommentingOrUncommenting() + "\n");
		sb.append("Is undoing: " + operationDescriptor.isUndoing() + "\n");
		sb.append("Node ID: " + affectedNodeDescriptor.getNodeID() + "\n");
		sb.append("Node type: " + affectedNodeDescriptor.getNodeType() + "\n");
		sb.append("Node text: " + affectedNodeDescriptor.getNodeText() + "\n");
		if (isChange()) { //New node text is not empty only for CHANGE operations.
			sb.append("New node text: " + affectedNodeDescriptor.getNodeNewText() + "\n");
		}
		sb.append("Node offset: " + affectedNodeDescriptor.getNodeOffset() + "\n");
		sb.append("Node length: " + affectedNodeDescriptor.getNodeLength() + "\n");
		sb.append("Method ID: " + affectedNodeDescriptor.getMethodID() + "\n");
		sb.append("Fully qualified method name: " + affectedNodeDescriptor.getMethodFullName() + "\n");
		sb.append("Method lines count: " + affectedNodeDescriptor.getMethodLinesCount() + "\n");
		sb.append("Method cyclomatic complexity: " + affectedNodeDescriptor.getMethodCyclomaticComplexity() + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
