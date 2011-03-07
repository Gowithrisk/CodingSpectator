package edu.illinois.codingspectator.codingtracker.helpers;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME= "edu.illinois.codingspectator.codingtracker.helpers.messages";

	public static String Recorder_CreateRecordFileException;

	public static String Recorder_CreateTempRecordFileException;

	public static String Recorder_UnrecognizedRefactoringType;

	public static String Recorder_AppendRecordFileException;

	public static String CodeChangeTracker_FailedToGetActiveWorkbenchWindow;

	public static String Recorder_OpenKnowfilesFileException;

	public static String Recorder_WriteKnownfilesFileException;

	public static String Recorder_ReadUnknownFileException;

	public static String Recorder_LaunchConfigurationException;

	public static String Recorder_CompleteReadUnknownFileException;

	public static String Recorder_BadDocumentLocation;

	public static String Recorder_UnsynchronizedDocumentNotifications;

	public static String Recorder_CVSEntriesCopyFailure;

	public static String Recorder_CVSFolderMembersFailure;

	public static String CodeChangeTracker_FailedToVisitResourceDelta;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
