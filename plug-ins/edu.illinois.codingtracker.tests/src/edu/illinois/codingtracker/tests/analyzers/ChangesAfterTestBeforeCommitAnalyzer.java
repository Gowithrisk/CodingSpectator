/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.helpers.StringHelper;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTFileOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.CommittedFileOperation;
import edu.illinois.codingtracker.operations.junit.TestSessionStartedOperation;
import edu.illinois.codingtracker.operations.resources.MovedResourceOperation;


/**
 * This analyzer calculates per commit: how many changes are performed and how many of them are
 * performed after running tests but before committing. Thus, commits without preceding tests are
 * ignored.
 * 
 * TODO: This class has some similarity with ChangesReachingCommitAnalyzer and
 * RefactoringsAndChangesMixReachingCommitAnalyzer. Consider factoring out common parts.
 * 
 * @author Stas Negara
 * 
 */
public class ChangesAfterTestBeforeCommitAnalyzer extends CSVProducingAnalyzer {

	private final Map<String, Integer> commitChangesCounter= new HashMap<String, Integer>();

	private final Map<String, Integer> commitAfterTestChangesCounter= new HashMap<String, Integer>();

	private final Set<String> filesCommittedAfterTest= new HashSet<String>();

	private boolean isNeverTested= true;

	private String currentASTFilePath;

	private int totalCommitChangesCount, totalCommitAfterTestChangesCount;


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,commit timestamp,changes count,after test changes count\n";
	}

	@Override
	protected void checkPostprocessingPreconditions() {
		//no preconditions
	}

	@Override
	protected boolean shouldPostprocessVersionFolder(String folderName) {
		return true;
	}

	@Override
	protected String getRecordFileName() {
		return "codechanges.txt.inferred_ast_operations";
	}

	@Override
	protected void postprocess(List<UserOperation> userOperations) {
		initialize();
		for (UserOperation userOperation : userOperations) {
			if (userOperation instanceof ASTFileOperation) {
				currentASTFilePath= ((ASTFileOperation)userOperation).getResourcePath();
			} else if (userOperation instanceof TestSessionStartedOperation) {
				handleTestSessionStartedOperation();
			} else if (userOperation instanceof ASTOperation) {
				handleASTOperation((ASTOperation)userOperation);
			} else if (userOperation instanceof CommittedFileOperation) {
				handleCommittedFileOperation((CommittedFileOperation)userOperation);
			} else if (userOperation instanceof MovedResourceOperation) {
				handleMovedResourceOperation((MovedResourceOperation)userOperation);
			}
		}
		System.out.println("Total commit changes count: " + totalCommitChangesCount);
		System.out.println("Total commit after test changes count: " + totalCommitAfterTestChangesCount);
	}

	private void handleTestSessionStartedOperation() {
		isNeverTested= false;
		filesCommittedAfterTest.clear();
		commitAfterTestChangesCounter.clear();
	}

	private void handleASTOperation(ASTOperation astOperation) {
		incrementCounter(commitChangesCounter);
		incrementCounter(commitAfterTestChangesCounter);
	}

	private void handleCommittedFileOperation(CommittedFileOperation committedFileOperation) {
		String committedFilePath= committedFileOperation.getResourcePath();
		if (isNeverTested || filesCommittedAfterTest.contains(committedFilePath)) {
			//Skip this commit as there is no preceding test run. But first, reset statistics for the committed file.
			commitChangesCounter.remove(committedFilePath);
			return;
		}
		int commitChangesCount= getCount(commitChangesCounter, committedFilePath);
		int commitAfterTestChangesCount= getCount(commitAfterTestChangesCounter, committedFilePath);
		totalCommitChangesCount+= commitChangesCount;
		totalCommitAfterTestChangesCount+= commitAfterTestChangesCount;

		appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, committedFileOperation.getTime(),
						commitChangesCount, commitAfterTestChangesCount);

		filesCommittedAfterTest.add(committedFilePath);
		//Reset statistics for the committed file.
		commitChangesCounter.remove(committedFilePath);
	}

	private void handleMovedResourceOperation(MovedResourceOperation movedResourceOperation) {
		String oldPrefix= movedResourceOperation.getResourcePath();
		String newPrefix= movedResourceOperation.getDestinationPath();
		for (String filePath : ResourceHelper.getFilePathsPrefixedBy(oldPrefix, commitChangesCounter.keySet())) {
			String newFilePath= StringHelper.replacePrefix(filePath, oldPrefix, newPrefix);
			Integer commitChangesCount= commitChangesCounter.remove(filePath);
			commitChangesCounter.put(newFilePath, commitChangesCount);
			Integer commitAfterTestChangesCount= commitAfterTestChangesCounter.remove(filePath);
			commitAfterTestChangesCounter.put(newFilePath, commitAfterTestChangesCount);
			if (filesCommittedAfterTest.remove(filePath)) {
				filesCommittedAfterTest.add(newFilePath);
			}
		}
	}

	private void initialize() {
		result= new StringBuffer();
		commitChangesCounter.clear();
		commitAfterTestChangesCounter.clear();
		filesCommittedAfterTest.clear();
		isNeverTested= true;
		currentASTFilePath= null;
		totalCommitChangesCount= 0;
		totalCommitAfterTestChangesCount= 0;
	}

	private void incrementCounter(Map<String, Integer> counter) {
		int count= getCount(counter, currentASTFilePath);
		count++;
		counter.put(currentASTFilePath, count);
	}

	private int getCount(Map<String, Integer> counter, String entry) {
		Integer count= counter.get(entry);
		if (count == null) {
			count= 0;
		}
		return count;
	}

	@Override
	protected String getResultFilePostfix() {
		return ".changes_after_test_before_commit";
	}

	@Override
	protected boolean shouldMergeResults() {
		return true;
	}

}
