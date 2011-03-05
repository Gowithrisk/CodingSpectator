/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.helpers;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 * @author Stas Negara
 * 
 */
public class Debugger {

	private static final String PLUGIN_ID= "edu.illinois.codingspectator.codingtracker.helpers";

	private static final boolean isInDebugMode= System.getenv("DEBUG_MODE") != null;


	public static void debug(String message) {
		if (isInDebugMode) {
			System.out.println(message);
		}
	}

	public static void debugFilePath(String message, IFile file) {
		if (isInDebugMode) {
			System.out.println(message + FileHelper.getPortableFilePath(file));
		}
	}

	public static void debugWorkbenchPart(String message, IWorkbenchPart part) {
		if (isInDebugMode) {
			System.out.println(message + part.getClass().getName());
		}
	}

	public static void debugTextChunk(String message, CharSequence textChunk) {
		if (isInDebugMode) {
			System.out.println(message + textChunk);
		}
	}

	public static void debugFileSize(String message, File file) {
		if (isInDebugMode) {
			System.out.println(message + file.length());
		}
	}

	public static void debugRefactoringDescriptor(RefactoringDescriptor refactoringDescriptor) {
		if (isInDebugMode) {
//			System.out.println("Refactoring descriptor id: " + refactoringDescriptor.getID());
//			System.out.println("Project: " + refactoringDescriptor.getProject());
//			System.out.println("Flags: " + refactoringDescriptor.getFlags());
//			System.out.println("Timestamp: " + refactoringDescriptor.getTimeStamp());
//			Map arguments= RecorderHelper.getRefactoringArguments(refactoringDescriptor);
//			if (arguments != null) {
//				Set keys= arguments.keySet();
//				for (Object key : keys) {
//					Object value= arguments.get(key);
//					System.out.println("Argument \"" + key + "\" = \"" + value + "\"");
//				}
//			}
		}
	}

	public static void debugDocumentEvent(DocumentEvent documentEvent, String replacedText) {
		if (isInDebugMode) {
//			System.out.println("Replaced text:\"" + replacedText + "\", new text:\"" + documentEvent.getText() +
//								"\", offset=" + documentEvent.getOffset() + ", length=" + documentEvent.getLength());
		}
	}

	public static void logExceptionToErrorLog(Exception e, String message) {
		IStatus status= new Status(IStatus.ERROR, PLUGIN_ID, 0, message, e);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}

}
