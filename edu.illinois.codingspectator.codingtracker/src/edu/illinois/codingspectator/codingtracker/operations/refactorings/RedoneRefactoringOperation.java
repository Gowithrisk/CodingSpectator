/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.refactorings;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
public class RedoneRefactoringOperation extends RefactoringOperation {

	public RedoneRefactoringOperation(RefactoringDescriptor refactoringDescriptor) {
		super(refactoringDescriptor);
	}

	@Override
	protected String getOperationSymbol() {
		return OperationSymbols.REFACTORING_REDONE_SYMBOL;
	}

	@Override
	protected String getDebugMessage() {
		return "Redone refactoring: ";
	}

}
