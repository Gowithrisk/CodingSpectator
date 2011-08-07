/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.logstocsv;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.files.UpdatedFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.CVSCommittedFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.CVSInitiallyCommittedFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.SVNCommittedFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.SVNInitiallyCommittedFileOperation;
import edu.illinois.codingtracker.operations.junit.TestSessionStartedOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation.RefactoringMode;
import edu.illinois.codingtracker.operations.refactorings.RedoneRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.UndoneRefactoringOperation;
import edu.illinois.codingtracker.operations.starts.LaunchedApplicationOperation;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class UserOperationEvent extends Event {

	@SuppressWarnings("unchecked")
	private static List<Class<? extends UserOperation>> shouldBeIncludedInCSV= Arrays.asList(TestSessionStartedOperation.class, LaunchedApplicationOperation.class, RedoneRefactoringOperation.class,
			UndoneRefactoringOperation.class, CVSCommittedFileOperation.class, CVSInitiallyCommittedFileOperation.class, SVNCommittedFileOperation.class, SVNInitiallyCommittedFileOperation.class,
			UpdatedFileOperation.class);

	private UserOperation userOperation;

	public UserOperationEvent(UserOperation userOperation, String username, String workspaceID, String codingSpectatorVersion) {
		super(username, workspaceID, codingSpectatorVersion);
		this.userOperation= userOperation;
	}

	@Override
	public Map<String, String> toMap() {
		Map<String, String> map= super.toMap();
		map.put("codingtracker description", String.valueOf(getDescription()));
		map.put("timestamp", String.valueOf(getTimestamp()));
		Date timestampDate= userOperation.getDate();
		map.put("human-readable timestamp", timestampDate.toString());
		SimpleDateFormat tableauDateFormat= new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		map.put("Tableau timestamp", tableauDateFormat.format(timestampDate));
		map.put("recorder", "CODINGTRACKER");
		if (isStartedRefactoringOperation()) {
			addRefactoringInformation(map);
		}
		return map;
	}

	public boolean isStartedPerformedRefactoringOperation() {
		if (isStartedRefactoringOperation()) {
			NewStartedRefactoringOperation startedRefactoring= (NewStartedRefactoringOperation)userOperation;
			return startedRefactoring.getRefactoringMode() == RefactoringMode.PERFORM;
		} else {
			return false;
		}
	}

	private boolean isStartedRefactoringOperation() {
		return userOperation instanceof NewStartedRefactoringOperation;
	}

	private void addRefactoringInformation(Map<String, String> map) {
		NewStartedRefactoringOperation startedRefactoringOperation= (NewStartedRefactoringOperation)userOperation;
		map.put("refactoring kind", toString(startedRefactoringOperation.getRefactoringMode()));
		map.put("flags", String.valueOf(startedRefactoringOperation.getFlags()));
		map.put("id", startedRefactoringOperation.getID());
		map.put("project", startedRefactoringOperation.getProject());
	}

	private String toString(RefactoringMode refactoringMode) {
		switch (refactoringMode) {
			case PERFORM:
				return "PERFORMED";
			case UNDO:
				return "UNDONE";
			case REDO:
				return "REDONE";
			default:
				throw new IllegalArgumentException();
		}
	}

	public String getDescription() {
		return userOperation.getDescription();
	}

	@Override
	public long getTimestamp() {
		return userOperation.getTime();
	}

	public boolean shouldBeIncludedInCSV() {
		return shouldBeIncludedInCSV.contains(userOperation.getClass()) || isStartedPerformedRefactoringOperation();
	}

}
