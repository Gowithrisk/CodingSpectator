/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.move;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.illinois.codingtracker.operations.ast.ASTOperation;



/**
 * 
 * @author Stas Negara
 * 
 */
public class NodeOperations {

	private static long nextMoveID= 1;

	private static final long timeThreshold= 5 * 60 * 1000; // 5 minutes

	private final List<ASTOperation> operations= new LinkedList<ASTOperation>();

	private int addOrChangeOperationsCount= 0;

	private int deleteOperationsCount= 0;


	public NodeOperations(ASTOperation astOperation) {
		addOperation(astOperation);
	}

	public void addOperation(ASTOperation astOperation) {
		if (willBecomeNtoN(astOperation)) { //Should not add an operation that will make the relation N-to-N.
			//First heuristic - split by time, i.e., group those operations that are closer in time.
			splitByTime(astOperation);
		}
		if (!shouldAddOperation(astOperation)) {
			markMoveAndResetState();
		}
		handleExistingNodes(astOperation);
		operations.add(astOperation);
		updateCounters(astOperation, true);
	}

	private void handleExistingNodes(ASTOperation astOperation) {
		//Should not add more than one operation on the same node.
		if (isExistingNode(astOperation)) {
			if (isCompletedMove()) {
				markMoveAndResetState();
			} else {
				//Drop all operations preceding the duplicated node as well as the duplicated node itself.
				Iterator<ASTOperation> operationsIterator= operations.iterator();
				while (operationsIterator.hasNext()) {
					ASTOperation existingOperation= operationsIterator.next();
					updateCounters(existingOperation, false);
					operationsIterator.remove();
					if (existingOperation.getNodeID() == astOperation.getNodeID()) {
						//Reached the duplicated node, so stop.
						break;
					}
				}
			}
		}
	}

	private boolean shouldAddOperation(ASTOperation astOperation) {
		if (operations.size() == 0) {
			return true; //A single operation should always be added.
		}
		if (getTimeDelta(astOperation, getLastOperation()) > timeThreshold) {
			return false;
		}
		//Second heuristic - split by the method to which the operations' nodes belong.
		if (shouldSplitByMethod(astOperation)) {
			return false;
		}
		return true;
	}

	private boolean willBecomeNtoN(ASTOperation astOperation) {
		if (astOperation.isAdd() || astOperation.isChange()) {
			if (addOrChangeOperationsCount > 0 && deleteOperationsCount > 1) {
				return true;
			}
		} else if (astOperation.isDelete()) {
			if (addOrChangeOperationsCount > 1 && deleteOperationsCount > 0) {
				return true;
			}
		}
		return false;
	}

	private boolean isExistingNode(ASTOperation astOperation) {
		for (ASTOperation addedOperation : operations) {
			if (addedOperation.getNodeID() == astOperation.getNodeID()) {
				return true;
			}
		}
		return false;
	}

	public boolean shouldSplitByMethod(ASTOperation astOperation) {
		long currentMethodID= operations.get(0).getMethodID();
		for (ASTOperation addedOperation : operations) {
			long methodID= addedOperation.getMethodID();
			//If the added operations belong to different methods or there is an operation that does not belong to any method,
			//then there is no reason to split for a new operation.
			if (methodID == -1 || methodID != currentMethodID) {
				return false;
			}
		}
		long newMethodID= astOperation.getMethodID();
		//Also, check if the already added operations form a completed move in order to avoid erroneously splitting moves that 
		//are part of extract method refactoring, i.e., when statements are moved from one method to another.
		if (isCompletedMove() && newMethodID != -1 && newMethodID != currentMethodID) {
			return true;
		}
		return false;
	}

	public void markMoveAndResetState() {
		if (isCompletedMove()) {
			operations.get(0).setFirstMoved(true);
			getLastOperation().setLastMoved(true);
			for (ASTOperation operation : operations) {
				operation.setMoveID(nextMoveID);
			}
			nextMoveID++;
		}
		resetState();
	}

	private void splitByTime(ASTOperation currentOperation) {
		ASTOperation lastOperation= getLastOperation();
		ASTOperation nextToLastOperation= getNextToLastOperation();
		List<ASTOperation> spillOverOperations= new LinkedList<ASTOperation>();
		while (getTimeDelta(currentOperation, lastOperation) < getTimeDelta(lastOperation, nextToLastOperation)) {
			updateCounters(lastOperation, false);
			if (isCompletedMove()) {
				operations.remove(lastOperation);
				spillOverOperations.add(0, lastOperation);
			} else { //Do not split if the current move will be broken.
				//Restore counters since lastOperation is not removed.
				updateCounters(lastOperation, true);
				break;
			}
			currentOperation= lastOperation;
			lastOperation= nextToLastOperation;
			nextToLastOperation= getNextToLastOperation();
		}
		markMoveAndResetState();
		for (ASTOperation spillOverOperation : spillOverOperations) {
			operations.add(spillOverOperation);
			updateCounters(spillOverOperation, true);
		}
	}

	private boolean isCompletedMove() {
		return addOrChangeOperationsCount > 0 && deleteOperationsCount > 0;
	}

	private void resetState() {
		operations.clear();
		addOrChangeOperationsCount= 0;
		deleteOperationsCount= 0;
	}

	private void updateCounters(ASTOperation astOperation, boolean isIncrement) {
		if (astOperation.isAdd() || astOperation.isChange()) {
			if (isIncrement) {
				addOrChangeOperationsCount++;
			} else {
				addOrChangeOperationsCount--;
			}
		} else if (astOperation.isDelete()) {
			if (isIncrement) {
				deleteOperationsCount++;
			} else {
				deleteOperationsCount--;
			}
		} else {
			throw new RuntimeException("Can handle only 'add', 'change', and 'delete' operations: " + astOperation);
		}
	}

	private ASTOperation getLastOperation() {
		return getOperationFromEnd(1);
	}

	private ASTOperation getNextToLastOperation() {
		return getOperationFromEnd(2);
	}

	private ASTOperation getOperationFromEnd(int indexFromEnd) {
		int operationsCount= operations.size();
		if (operationsCount >= indexFromEnd) {
			return operations.get(operationsCount - indexFromEnd);
		} else {
			return null;
		}
	}

	private static long getTimeDelta(ASTOperation operation1, ASTOperation operation2) {
		return Math.abs(operation1.getTime() - operation2.getTime());
	}

}
