/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.OperationDeserializer;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.tests.CodingTrackerTest;

/**
 * This is a base class for all CodingTracker postprocessors and analyzers.
 * 
 * This class is implemented as a plugin test to ensure the proper functionality of the text
 * recorder (which requires loading of particular plugins).
 * 
 * @author Stas Negara
 * 
 */
public abstract class CodingTrackerPostprocessor extends CodingTrackerTest {

	protected final static String VERSION_FOLDER_COMMON_PREFIX= "1.0.0.201";

	private final static String COMBINED_FILE_PREFIX= "combined";

	private final boolean shouldOverwriteOutputFiles= true;

	private File mergedOutputFile;

	protected String postprocessedVersion;

	protected String postprocessedWorkspaceID;

	protected String postprocessedUsername;


	//@Ignore
	@Test
	public void execute() {
		checkPostprocessingPreconditions();
		prepareMergedOutputFile();
		visitLocation(new File(Configuration.postprocessorRootFolderName));
	}

	private void prepareMergedOutputFile() {
		if (shouldMergeResults()) {
			mergedOutputFile= new File(Configuration.postprocessorRootFolderName, COMBINED_FILE_PREFIX + getResultFilePostfix());
			checkExistance(mergedOutputFile);
			writeToFile(mergedOutputFile, getMergedFilePrefix(), false);
		}
	}

	private void visitLocation(File file) {
		if (file.isDirectory()) {
			for (File childFile : file.listFiles()) {
				visitLocation(childFile);
			}
		} else if (shouldPostprocessFile(file)) {
			try {
				postprocess(file);
			} catch (Exception e) {
				//Output the exception explicitly since it would not be printed to the console in a JUnit test.
				e.printStackTrace();
				if (shouldStopAfterPostprocessingFailed()) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private boolean shouldPostprocessFile(File file) {
		String versionFolderName= file.getParentFile().getParentFile().getName();
		return shouldPostprocessVersionFolder(versionFolderName) && isRecordFile(file);
	}

	private boolean isRecordFile(File file) {
		return file.getName().equals(getRecordFileName());
	}

	protected String getRecordFileName() {
		return "codechanges.txt";
	}

	private void postprocess(File file) {
		System.out.println("Postprocessing file: " + file.getAbsolutePath());
		initializeFileData(file);
		String inputSequence= ResourceHelper.readFileContent(file);
		List<UserOperation> userOperations= OperationDeserializer.getUserOperations(inputSequence);
		try {
			postprocess(userOperations);
		} finally { //Write out the accumulated result even if the postprocessing did not complete successfully.
			if (shouldOutputIndividualResults()) {
				File outputFile= new File(file.getAbsolutePath() + getResultFilePostfix());
				checkExistance(outputFile);
				writeToFile(outputFile, getResult(), false);
			}
			if (shouldMergeResults()) {
				writeToFile(mergedOutputFile, getResultToMerge(), true);
			}
		}
		System.out.println("DONE");
		before(); //After a file is postprocessed, reset the main record files.
	}

	private void writeToFile(File file, String text, boolean append) {
		try {
			ResourceHelper.writeFileContent(file, text, append);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void checkExistance(File file) {
		if (file.exists() && !shouldOverwriteOutputFiles) {
			throw new RuntimeException("Output file already exists: " + file.getName());
		}
	}

	private void initializeFileData(File file) {
		final String defaulValue= "undefined";
		postprocessedVersion= defaulValue;
		postprocessedWorkspaceID= defaulValue;
		postprocessedUsername= defaulValue;
		try {
			File versionFolder= file.getParentFile().getParentFile();
			postprocessedVersion= versionFolder.getName();
			File workspaceIDFolder= versionFolder.getParentFile();
			postprocessedWorkspaceID= workspaceIDFolder.getName();
			File usernameFolder= workspaceIDFolder.getParentFile();
			postprocessedUsername= usernameFolder.getName();
		} catch (Exception e) {
			//A NullPointerException could be thrown, for example, when there are no sufficient parent folders.
			handleFileDataInitializationException(file, e);
		}
	}

	protected boolean shouldStopAfterPostprocessingFailed() {
		return true;
	}

	protected void handleFileDataInitializationException(File file, Exception e) {
		//ignore by default
	}

	protected boolean shouldOutputIndividualResults() {
		return true;
	}

	protected boolean shouldMergeResults() {
		return false;
	}

	protected String getResultToMerge() {
		return "";
	}

	protected String getMergedFilePrefix() {
		return "";
	}

	protected abstract boolean shouldPostprocessVersionFolder(String folderName);

	protected abstract void checkPostprocessingPreconditions();

	protected abstract void postprocess(List<UserOperation> userOperations);

	protected abstract String getResultFilePostfix();

	protected abstract String getResult();

}
