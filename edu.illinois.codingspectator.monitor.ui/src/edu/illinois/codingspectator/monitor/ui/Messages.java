package edu.illinois.codingspectator.monitor.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME= "edu.illinois.codingspectator.monitor.ui.messages"; //$NON-NLS-1$

	public static String AuthenticationPrompter_DialogDescription;

	public static String AuthenticationPrompter_DialogDescriptionForReenteringAuthenticationInfo;

	public static String AuthenticationPrompter_DialogTitle;

	public static String AuthenticationPrompter_FailureMessage;

	public static String PrefsFacade_ForcedAutomaticUpdateHasBeenSetKey;

	public static String PrefsFacade_LastUploadTimeKey;

	public static String SecureStorage_PasswordKey;

	public static String SecureStorage_AuthenticationNodeName;

	public static String SecureStorage_UsernameKey;

	public static String UserValidationDialog_Password;

	public static String UserValidationDialog_SavePassword;

	public static String UserValidationDialog_Username;

	public static String WorkbenchPreferencePage_FailedToUploadMessage;

	public static String WorkbenchPreferencePage_LastUploadTextField;

	public static String WorkbenchPreferencePage_PluginName;

	public static String WorkbenchPreferencePage_Title;

	public static String WorkbenchPreferencePage_UploadingMessage;

	public static String WorkbenchPreferencePage_UploadNowButtonText;

	public static String WorkbenchPreferencePage_UUIDFieldPreferenceKey;

	public static String WorkbenchPreferencePage_UUIDTextField;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
