/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.tests.old;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.utils.FileUtils;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;


/**
 * Superclass to encapsulate common functionalities for testing refactorings.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public abstract class CodingSpectatorTest {

	private static final String GENERIC_VERSION_NUMBER= "1.0.0.qualifier";

	static final String PLUGIN_NAME= "edu.illinois.codingspectator.ui.tests";

	static final String REFACTORING_HISTORY_LOCATION= Platform.getStateLocation(Platform.getBundle("org.eclipse.ltk.core.refactoring")).toOSString();

	static final String CANCELED_REFACTORINGS= "refactorings/canceled";

	static final String PERFORMED_REFACTORINGS= "refactorings/performed";

	static final String PACKAGE_NAME= "edu.illinois.codingspectator";

	static SWTWorkbenchBot bot;

	protected IFileStore performedRefactorings;

	protected IFileStore canceledRefactorings;

	protected static final int SLEEPTIME= 1500;

	protected static final String YES_BUTTON_LABEL= IDialogConstants.YES_LABEL;

	protected static final String CANCEL_BUTTON_LABEL= IDialogConstants.CANCEL_LABEL;

	protected static final String OK_BUTTON_LABEL= IDialogConstants.OK_LABEL;

	protected static final String FINISH_BUTTON_LABEL= IDialogConstants.FINISH_LABEL;

	protected static final String NEXT_BUTTON_LABEL= IDialogConstants.NEXT_LABEL;

	protected static final String CONTINUE_BUTTON_LABEL= "Continue";

	protected static final String REFACTOR_MENU_NAME= "Refactor";

	private static Version getFeatureVersion() {
		Bundle bundle= Platform.getBundle("edu.illinois.codingspectator.monitor");
		if (bundle != null)
			return bundle.getVersion();
		else
			return new Version(GENERIC_VERSION_NUMBER);
	}

	{
		performedRefactorings= EFS.getLocalFileSystem().getStore(new Path(getRefactoringStorageLocation(PERFORMED_REFACTORINGS)));
		canceledRefactorings= EFS.getLocalFileSystem().getStore(new Path(getRefactoringStorageLocation(CANCELED_REFACTORINGS)));
	}

	private String getRefactoringStorageLocation(String directory) {
		StringBuilder fullDirectory= new StringBuilder();
		fullDirectory.append(REFACTORING_HISTORY_LOCATION);
		fullDirectory.append(getSystemFileSeparator());
		fullDirectory.append(getFeatureVersion());

		String directorySeparator= "/";
		String[] directories= directory.split(directorySeparator);
		for (int i= 0; i < directories.length; i++) {
			fullDirectory.append(getSystemFileSeparator());
			fullDirectory.append(directories[i]);
		}

		return fullDirectory.toString();
	}

	private String getSystemFileSeparator() {
		return System.getProperty("file.separator");
	}

	private static void dismissWelcomeScreenIfPresent() {
		try {
			bot.viewByTitle("Welcome").close();
		} catch (WidgetNotFoundException exception) {
			// The welcome screen might not be shown so just ignore
		}
	}

	public void canCreateANewJavaProject() throws Exception {
		bot.menu("File").menu("New").menu("Project...").click();

		bot.shell("New Project").activate();
		bot.tree().expandNode("Java").select("Java Project");
		bot.button(NEXT_BUTTON_LABEL).click();

		bot.textWithLabel("Project name:").setText(getProjectName());

		bot.button(FINISH_BUTTON_LABEL).click();

		dismissJavaPerspectiveIfPresent();
	}

	private void dismissJavaPerspectiveIfPresent() {
		try {
			bot.button(YES_BUTTON_LABEL).click();
		} catch (WidgetNotFoundException exception) {
			// The second and subsequent time this is invoked the Java perspective change dialog will not be shown.
		}
	}


	public void canCreateANewJavaClass() throws Exception {
		selectCurrentJavaProject();

		bot.menu("File").menu("New").menu("Class").click();

		bot.shell("New Java Class").activate();

		bot.textWithLabel("Package:").setText(PACKAGE_NAME);
		bot.textWithLabel("Name:").setText(getTestFileName());

		bot.button(FINISH_BUTTON_LABEL).click();
	}

	protected SWTBotTree selectCurrentJavaProject() {
		SWTBotView packageExplorerView= bot.viewByTitle("Package Explorer");
		packageExplorerView.show();

		Composite packageExplorerComposite= (Composite)packageExplorerView.getWidget();

		Tree swtTree= (Tree)bot.widget(WidgetMatcherFactory.widgetOfType(Tree.class), packageExplorerComposite);
		SWTBotTree tree= new SWTBotTree(swtTree);

		return tree.select(getProjectName());
	}

	public void prepareJavaTextInEditor() throws Exception {

		Bundle bundle= Platform.getBundle(PLUGIN_NAME);
		String contents= FileUtils.read(bundle.getEntry("test-files/" + getTestFileFullName()));

		SWTBotEclipseEditor editor= bot.editorByTitle(getTestFileFullName()).toTextEditor();
		editor.setText(contents);
		editor.save();
	}

	public String getProjectName() {
		return "TestProject_" + getProjectNameSuffix();
	}

	abstract protected String refactoringMenuItemName();

	/**
	 * Selects part of the given line from the given column number for the specified length.
	 * 
	 * @param line The line number to be selected. This number is zero-based.
	 * @param column The column number of the beginning of the selection. This number is zero-based.
	 *            If you use Eclipse editor to find the column number, you should be aware that the
	 *            Eclipse editor displays offsets of the caret by expanding tabs into spaces. So, to
	 *            get the column number from Eclipse editor safely, convert the tabs to spaces
	 *            first.
	 * @param length The length of the selection.
	 */
	protected void selectElementToRefactor(int line, int column, int length) {
		SWTBotEclipseEditor editor= bot.editorByTitle(getTestFileFullName()).toTextEditor();

		editor.setFocus();
		editor.selectRange(line, column, length);
	}

	protected void invokeRefactoring() {
		SWTBotMenu refactorMenu= bot.menu(REFACTOR_MENU_NAME);
		assertTrue(refactorMenu.isEnabled());

		SWTBotMenu refactoringMenuItem= refactorMenu.menu(refactoringMenuItemName());
		assertTrue(refactoringMenuItem.isEnabled());

		refactoringMenuItem.click();
	}

	protected abstract void selectElementToRefactor();

	private void prepareRefactoring() {
		selectElementToRefactor();
		invokeRefactoring();
	}

	protected String getProjectNameSuffix() {
		return getClass().toString();
	}

	abstract String getTestFileName();

	abstract protected String getRefactoringDialogName();

	protected void activateRefactoringDialog() {
		bot.shell(getRefactoringDialogName()).activate();
	}

	protected void clickButtons(String[] buttonNames) {
		for (String buttonName : buttonNames) {
			bot.button(buttonName).click();
		}
	}

	protected void cancelRefactoring() {
		activateRefactoringDialog();
		clickButtons(getRefactoringDialogCancelButtonSequence());
	}

	protected void configureRefactoringToPerform() {
		activateRefactoringDialog();
	}

	protected void configureRefactoringToCancel() {
		activateRefactoringDialog();
	}

	protected void performRefactoring() {
		activateRefactoringDialog();
		clickButtons(getRefactoringDialogPerformButtonSequence());
	}

	protected String[] getRefactoringDialogPerformButtonSequence() {
		return new String[] { OK_BUTTON_LABEL };
	}

	protected String[] getRefactoringDialogCancelButtonSequence() {
		return new String[] { CANCEL_BUTTON_LABEL };
	}

	protected String getTestFileFullName() {
		return getTestFileName() + ".java";
	}

	protected void reportProblemsWithTest(String message) {
		System.out.println(message);
	}

	///////////////////////////////////////////////////////////////////////////
	//
	// SWTBot tests run in order which is why we can take advantage of this
	// and capture the canceled refactoring first and then do the actual 
	// refactoring. Currently we are just testing that the appropriates folders 
	// are created.
	//
	///////////////////////////////////////////////////////////////////////////

	@BeforeClass
	public static void beforeClass() throws Exception {
		bot= new SWTWorkbenchBot();
		dismissWelcomeScreenIfPresent();
	}

	@Test
	public void canSetupProject() throws Exception {
		canCreateANewJavaProject();
		canCreateANewJavaClass();
		prepareJavaTextInEditor();
	}

	@Test
	public void currentRefactoringsCapturedShouldBeEmpty() {
		bot.sleep(SLEEPTIME);
		assertFalse(performedRefactorings.fetchInfo().exists());
		assertFalse(canceledRefactorings.fetchInfo().exists());
	}

	@Test
	public void shouldCaptureCancelledRefactoring() {
		prepareRefactoring();
		configureRefactoringToCancel();
		cancelRefactoring();
	}

	// This needs to be interleaved here after the refactoring has been canceled.
	@Test
	public void currentRefactoringsCanceledShouldBePopulated() {
		verifyCanceledRefactoringBehavior();
	}

	public void verifyCanceledRefactoringBehavior() {
		bot.sleep(SLEEPTIME);
		assertFalse(performedRefactorings.fetchInfo().exists());
		assertTrue(canceledRefactorings.fetchInfo().exists());
	}

	@Test
	public void shouldCapturePerformedRefactoring() throws Exception {
		prepareRefactoring();
		configureRefactoringToPerform();
		performRefactoring();
	}

	// This needs to be interleaved here after the refactoring has been performed.
	@Test
	public void currentRefactoringsPerformedShouldBePopulated() {
		verifyPerformedRefactoringBehavior();
	}

	public void verifyPerformedRefactoringBehavior() {
		bot.sleep(SLEEPTIME);
		assertTrue(performedRefactorings.fetchInfo().exists());
		assertTrue(canceledRefactorings.fetchInfo().exists());
	}

	// This is a hack to ensure that refactorings are cleared at the end of each test
	@Test
	public void cleanRefactoringHistory() throws CoreException {
		canceledRefactorings.delete(EFS.NONE, null);
		performedRefactorings.delete(EFS.NONE, null);
	}

	@Test
	public void closeCurrentProject() {
		selectCurrentJavaProject().contextMenu("Close Project").click();
	}

}
