/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations;

import java.util.LinkedList;
import java.util.List;

import edu.illinois.codingspectator.codingtracker.operations.conflicteditors.ClosedConflictEditorOperation;
import edu.illinois.codingspectator.codingtracker.operations.conflicteditors.OpenedConflictEditorOperation;
import edu.illinois.codingspectator.codingtracker.operations.conflicteditors.SavedConflictEditorOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.ClosedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.CommittedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.EditedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.ExternallyModifiedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.InitiallyCommittedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.NewFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.RefactoredSavedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.SavedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.files.UpdatedFileOperation;
import edu.illinois.codingspectator.codingtracker.operations.refactorings.PerformedRefactoringOperation;
import edu.illinois.codingspectator.codingtracker.operations.refactorings.RedoneRefactoringOperation;
import edu.illinois.codingspectator.codingtracker.operations.refactorings.UndoneRefactoringOperation;
import edu.illinois.codingspectator.codingtracker.operations.starts.StartedEclipseOperation;
import edu.illinois.codingspectator.codingtracker.operations.starts.StartedRefactoringOperation;
import edu.illinois.codingspectator.codingtracker.operations.textchanges.PerformedConflictEditorTextChangeOperation;
import edu.illinois.codingspectator.codingtracker.operations.textchanges.PerformedTextChangeOperation;
import edu.illinois.codingspectator.codingtracker.operations.textchanges.RedoneConflictEditorTextChangeOperation;
import edu.illinois.codingspectator.codingtracker.operations.textchanges.RedoneTextChangeOperation;
import edu.illinois.codingspectator.codingtracker.operations.textchanges.UndoneConflictEditorTextChangeOperation;
import edu.illinois.codingspectator.codingtracker.operations.textchanges.UndoneTextChangeOperation;

//TODO: Decide on where this class should be and how it should be used
/**
 * 
 * @author Stas Negara
 * 
 */
public class OperationDeserializer {

	public static List<UserOperation> getUserOperations(String operationsRecord) {
		List<UserOperation> userOperations= new LinkedList<UserOperation>();
		OperationLexer operationLexer= new OperationLexer(operationsRecord);
		while (operationLexer.hasNextOperation()) {
			operationLexer.startNewOperation();
			UserOperation userOperation= createEmptyUserOperation(operationLexer.getCurrentOperationSymbol());
			userOperation.deserialize(operationLexer);
			userOperations.add(userOperation);
		}
		return userOperations;
	}

	private static UserOperation createEmptyUserOperation(char operationSymbol) {
		UserOperation userOperation;
		switch (operationSymbol) {
			case OperationSymbols.ECLIPSE_STARTED_SYMBOL:
				userOperation= new StartedEclipseOperation();
				break;
			case OperationSymbols.REFACTORING_STARTED_SYMBOL:
				userOperation= new StartedRefactoringOperation();
				break;
			case OperationSymbols.REFACTORING_PERFORMED_SYMBOL:
				userOperation= new PerformedRefactoringOperation();
				break;
			case OperationSymbols.REFACTORING_UNDONE_SYMBOL:
				userOperation= new UndoneRefactoringOperation();
				break;
			case OperationSymbols.REFACTORING_REDONE_SYMBOL:
				userOperation= new RedoneRefactoringOperation();
				break;
			case OperationSymbols.CONFLICT_EDITOR_OPENED_SYMBOL:
				userOperation= new OpenedConflictEditorOperation();
				break;
			case OperationSymbols.CONFLICT_EDITOR_CLOSED_SYMBOL:
				userOperation= new ClosedConflictEditorOperation();
				break;
			case OperationSymbols.CONFLICT_EDITOR_SAVED_SYMBOL:
				userOperation= new SavedConflictEditorOperation();
				break;
			case OperationSymbols.FILE_CLOSED_SYMBOL:
				userOperation= new ClosedFileOperation();
				break;
			case OperationSymbols.FILE_SAVED_SYMBOL:
				userOperation= new SavedFileOperation();
				break;
			case OperationSymbols.FILE_EXTERNALLY_MODIFIED_SYMBOL:
				userOperation= new ExternallyModifiedFileOperation();
				break;
			case OperationSymbols.FILE_UPDATED_SYMBOL:
				userOperation= new UpdatedFileOperation();
				break;
			case OperationSymbols.FILE_INITIALLY_COMMITTED_SYMBOL:
				userOperation= new InitiallyCommittedFileOperation();
				break;
			case OperationSymbols.FILE_COMMITTED_SYMBOL:
				userOperation= new CommittedFileOperation();
				break;
			case OperationSymbols.FILE_REFACTORED_SAVED_SYMBOL:
				userOperation= new RefactoredSavedFileOperation();
				break;
			case OperationSymbols.FILE_NEW_SYMBOL:
				userOperation= new NewFileOperation();
				break;
			case OperationSymbols.FILE_EDITED_SYMBOL:
				userOperation= new EditedFileOperation();
				break;
			case OperationSymbols.TEXT_CHANGE_PERFORMED_SYMBOL:
				userOperation= new PerformedTextChangeOperation();
				break;
			case OperationSymbols.TEXT_CHANGE_UNDONE_SYMBOL:
				userOperation= new UndoneTextChangeOperation();
				break;
			case OperationSymbols.TEXT_CHANGE_REDONE_SYMBOL:
				userOperation= new RedoneTextChangeOperation();
				break;
			case OperationSymbols.CONFLICT_EDITOR_TEXT_CHANGE_PERFORMED_SYMBOL:
				userOperation= new PerformedConflictEditorTextChangeOperation();
				break;
			case OperationSymbols.CONFLICT_EDITOR_TEXT_CHANGE_UNDONE_SYMBOL:
				userOperation= new UndoneConflictEditorTextChangeOperation();
				break;
			case OperationSymbols.CONFLICT_EDITOR_TEXT_CHANGE_REDONE_SYMBOL:
				userOperation= new RedoneConflictEditorTextChangeOperation();
				break;
			default:
				throw new RuntimeException("Unsupported operation symbol: " + operationSymbol);
		}
		return userOperation;
	}

}
