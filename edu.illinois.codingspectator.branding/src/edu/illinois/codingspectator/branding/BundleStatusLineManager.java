/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.branding;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.StatusLineContributionItem;
import org.osgi.framework.Bundle;

/**
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class BundleStatusLineManager {

	private final static String STATUS_LINE_CONTRIBUTION_ITEM_ID= "edu.illinois.codingspectator.branding.StatusLine";

	private IStatusLineManager statusLineManager;

	/**
	 * See org.eclipse.equinox.internal.p2.ui.sdk.scheduler.AutomaticUpdater#getStatusLineManager
	 * 
	 * @return
	 */
	private IStatusLineManager getStatusLineManager() {
		if (statusLineManager != null)
			return statusLineManager;
		IWorkbenchWindow activeWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWindow == null)
			return null;
		// YUCK! YUCK! YUCK!
		// IWorkbenchWindow does not define getStatusLineManager(), yet
		// WorkbenchWindow does
		try {
			Method method= activeWindow.getClass().getDeclaredMethod("getStatusLineManager", new Class[0]); //$NON-NLS-1$
			try {
				Object statusLine= method.invoke(activeWindow, new Object[0]);
				if (statusLine instanceof IStatusLineManager) {
					statusLineManager= (IStatusLineManager)statusLine;
					return statusLineManager;
				}
			} catch (InvocationTargetException e) {
				// oh well
			} catch (IllegalAccessException e) {
				// I tried
			}
		} catch (NoSuchMethodException e) {
			// can't blame us for trying.
		}

		IWorkbenchPartSite site= activeWindow.getActivePage().getActivePart().getSite();
		if (site instanceof IViewSite) {
			statusLineManager= ((IViewSite)site).getActionBars().getStatusLineManager();
		} else if (site instanceof IEditorSite) {
			statusLineManager= ((IEditorSite)site).getActionBars().getStatusLineManager();
		}
		return statusLineManager;
	}

	private boolean logoExists() {
		return getStatusLineManager().find(STATUS_LINE_CONTRIBUTION_ITEM_ID) != null;
	}

	private void addLogoToStatusLine() {
		Image codingspectatorLogo= createImageDescriptor(Activator.getDefault().getBundle(), new Path("icons/codingspectator-logo.gif"), true).createImage(); //$NON-NLS-1$
		StatusLineContributionItem contributionItem= new StatusLineContributionItem(STATUS_LINE_CONTRIBUTION_ITEM_ID);
		contributionItem.setImage(codingspectatorLogo);
		contributionItem.setToolTipText(Messages.StatusLineBranding_status_bar_tool_tip);
		getStatusLineManager().add(contributionItem);
		getStatusLineManager().update(false);

		try {
			Assert.isTrue(logoExists());
		} catch (AssertionFailedException e) {
			Activator.getDefault().logErrorStatus("Failed to add the logo to the status line.", e);
		}
	}

	private static ImageDescriptor createImageDescriptor(Bundle bundle, IPath path, boolean useMissingImageDescriptor) {
		URL url= FileLocator.find(bundle, path, null);
		if (url != null) {
			return ImageDescriptor.createFromURL(url);
		}
		return null;
	}

	public void removeLogoFromStatusLine() {
		getStatusLineManager().remove(STATUS_LINE_CONTRIBUTION_ITEM_ID);
		getStatusLineManager().markDirty();
		getStatusLineManager().update(false);

		try {
			Assert.isTrue(!logoExists());
		} catch (AssertionFailedException e) {
			Activator.getDefault().logErrorStatus("Failed to remove the logo from the status line.", e);
		}
	}

	public void addLogoToStatusLineIfNecessary() {
		if (!logoExists()) {
			addLogoToStatusLine();
		}
	}

}
