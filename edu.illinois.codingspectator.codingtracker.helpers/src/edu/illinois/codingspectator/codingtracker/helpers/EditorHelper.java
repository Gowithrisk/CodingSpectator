/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.helpers;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.team.internal.ui.mapping.ModelCompareEditorInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian - Extracted this class from CodeChangeTracker
 * 
 */
@SuppressWarnings("restriction")
public class EditorHelper {

	public static boolean isConflictEditor(IWorkbenchPart part) {
		if (!(part instanceof CompareEditor)) {
			return false;
		}
		//TODO: Maybe some other inputs (not of a conflict editor) are good for tracking and are not ModelCompareEditorInput
		if (((CompareEditor)part).getEditorInput() instanceof ModelCompareEditorInput) {
			return false;
		}
		return true;
	}

	public static String getConflictEditorInitialContent(CompareEditor compareEditor) {
		CompareEditorInput compareEditorInput= (CompareEditorInput)compareEditor.getEditorInput();
		ICompareInput compareInput= (ICompareInput)compareEditorInput.getCompareResult();
		ResourceNode resourceNode= (ResourceNode)compareInput.getLeft();
		return new String(resourceNode.getContent());
	}

	public static String getConflictEditorID(CompareEditor compareEditor) {
		String compareEditorString= compareEditor.toString();
		return compareEditorString.substring(compareEditorString.lastIndexOf('@') + 1);
	}

	public static IFile getEditedJavaFile(CompareEditor compareEditor) {
		IFile javaFile= null;
		IEditorInput editorInput= compareEditor.getEditorInput();
		if (editorInput instanceof CompareEditorInput) {
			CompareEditorInput compareEditorInput= (CompareEditorInput)editorInput;
			Object compareResult= compareEditorInput.getCompareResult();
			if (compareResult instanceof ICompareInput) {
				ICompareInput compareInput= (ICompareInput)compareResult;
				ITypedElement leftTypedElement= compareInput.getLeft();
				if (leftTypedElement instanceof ResourceNode) {
					ResourceNode resourceNode= (ResourceNode)leftTypedElement;
					IResource resource= resourceNode.getResource();
					if (resource instanceof IFile) {
						IFile file= (IFile)resource;
						if (ResourceHelper.isJavaFile(file)) {
							javaFile= file;
						}
					}
				}
			}
		}
		return javaFile;
	}

	public static IFile getEditedJavaFile(AbstractDecoratedTextEditor editor) {
		IFile javaFile= null;
		IEditorInput editorInput= editor.getEditorInput();
		if (editorInput instanceof FileEditorInput) {
			IFile file= ((FileEditorInput)editorInput).getFile();
			if (ResourceHelper.isJavaFile(file)) {
				javaFile= file;
			}
		}
		return javaFile;
	}

	public static ISourceViewer getEditingSourceViewer(CompareEditor compareEditor) {
		ISourceViewer sourceViewer= null;
		IEditorInput editorInput= compareEditor.getEditorInput();
		if (editorInput instanceof CompareEditorInput) {
			CompareEditorInput compareEditorInput= (CompareEditorInput)editorInput;
			Viewer contentViewer= compareEditorInput.getContentViewer();
			if (contentViewer instanceof TextMergeViewer) {
				sourceViewer= ((TextMergeViewer)contentViewer).getLeftViewer();
			}
		}
		return sourceViewer;
	}

	public static ISourceViewer getEditingSourceViewer(AbstractDecoratedTextEditor editor) {
		return editor.getHackedViewer();
	}

	public static ITextEditor openEditor(String filePath) throws CoreException {
		ITextEditor fileEditor= getExistingEditor(filePath);
		if (fileEditor != null) {
			activateEditor(fileEditor);
		} else {
			fileEditor= createEditor(filePath);
		}
		return fileEditor;
	}

	public static void activateEditor(IEditorPart editor) {
		JavaPlugin.getActivePage().activate(editor);
	}

	public static IEditorPart getActiveEditor() {
		return JavaPlugin.getActivePage().getActiveEditor();
	}

	public static void closeAllEditors() {
		JavaPlugin.getActivePage().closeAllEditors(false);
	}

	/**
	 * Has a side effect of bringing to top the newly created editor.
	 * 
	 * @return
	 * @throws JavaModelException
	 * @throws PartInitException
	 */
	public static ITextEditor createEditor(String filePath) throws JavaModelException, PartInitException {
		IFile file= (IFile)ResourceHelper.findWorkspaceMember(filePath);
		return (ITextEditor)JavaUI.openInEditor(JavaCore.createCompilationUnitFrom(file));
	}

	public static ITextEditor getExistingEditor(String filePath) throws PartInitException {
		for (IEditorReference editorReference : JavaPlugin.getActivePage().getEditorReferences()) {
			IEditorInput editorInput= editorReference.getEditorInput();
			if (editorInput instanceof FileEditorInput && ((FileEditorInput)editorInput).getPath().toPortableString().endsWith(filePath)) {
				return (ITextEditor)editorReference.getEditor(true);
			}
		}
		return null;
	}

	public static IDocument getEditedDocument(ITextEditor editor) {
		return editor.getDocumentProvider().getDocument(editor.getEditorInput());
	}

}
