/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.listeners;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.internal.resources.Folder;
import org.eclipse.core.internal.resources.IResourceListener;
import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.wc.admin.SVNAdminArea;
import org.tmatesoft.svn.core.internal.wc.admin.SVNAdminAreaFactory;
import org.tmatesoft.svn.core.internal.wc.admin.SVNEntry;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.helpers.Messages;
import edu.illinois.codingspectator.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingspectator.codingtracker.recording.FileRevision;


/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class ResourceListener extends BasicListener implements IResourceListener {

	private enum Manipulation {
		ADDED, REMOVED, CHANGED
	};

	//Populated sets:

	private final Set<IFile> externallyAddedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> externallyChangedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> externallyRemovedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> svnAddedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> svnChangedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> cvsEntriesAddedSet= new HashSet<IFile>();

	private final Set<IFile> cvsEntriesChangedOrRemovedSet= new HashSet<IFile>();

	//Calculated sets:

	private final Set<IFile> externallyModifiedJavaFiles= new HashSet<IFile>();

	private final Set<FileRevision> updatedJavaFileRevisions= new HashSet<FileRevision>();

	private final Set<FileRevision> svnInitiallyCommittedJavaFileRevisions= new HashSet<FileRevision>();

	private final Set<FileRevision> cvsInitiallyCommittedJavaFileRevisions= new HashSet<FileRevision>();

	private final Set<FileRevision> svnCommittedJavaFileRevisions= new HashSet<FileRevision>();

	private final Set<FileRevision> cvsCommittedJavaFileRevisions= new HashSet<FileRevision>();

	//SVN entries caching

	private final Map<String, SVNAdminArea> svnAdminAreaCache= new HashMap<String, SVNAdminArea>();


	public static void register() {
		Resource.resourceListener= new ResourceListener();
	}

	@Override
	public void createdResource(IResource resource, int updateFlags, boolean success) {
		if (isRecordedResource(resource) && isRefactoring) {//Record only during refactorings to avoid recording huge checked out projects
			operationRecorder.recordCreatedResource(resource, updateFlags, success);
		}
	}

	@Override
	public void movedResource(IResource resource, IPath destination, int updateFlags, boolean success) {
		if (isRecordedResource(resource)) {
			operationRecorder.recordMovedResource(resource, destination, updateFlags, success);
		}
	}

	@Override
	public void copiedResource(IResource resource, IPath destination, int updateFlags, boolean success) {
		if (isRecordedResource(resource)) {
			operationRecorder.recordCopiedResource(resource, destination, updateFlags, success);
		}
	}

	@Override
	public void deletedResource(IResource resource, int updateFlags, boolean success) {
		if (isRecordedResource(resource)) {
			operationRecorder.recordDeletedResource(resource, updateFlags, success);
		}
	}

	@Override
	public void externallyModifiedResource(IResource resource, boolean isDeleted) {
		if (resource instanceof IFile) {
			Manipulation manipulation= isDeleted ? Manipulation.REMOVED : Manipulation.CHANGED;
			handleExternalFileManipulation((IFile)resource, manipulation);
		}
	}

	@Override
	public void externallyCreatedResource(IResource resource) {
		if (resource instanceof IFile) {
			handleExternalFileManipulation((IFile)resource, Manipulation.ADDED);
		}
	}

	@Override
	public void refreshedResource(IResource resource) {
		if (!isRefactoring) {
			svnAdminAreaCache.clear(); //Clear SVN entries cache before processing
			calculateSets();
			recordSets();
			updateKnownFiles();
		}
		//Always clear sets for the following refresh operation.
		clearExternallyManipulatedFileSets();
	}

	@Override
	public void savedFile(IPath filePath, boolean success) {
		IResource resource= ResourceHelper.findWorkspaceMember(filePath);
		if (resource instanceof IFile) {
			savedFile((IFile)resource, success);
		}
	}

	@Override
	public void savedFile(IFile file, boolean success) {
		if (ResourceHelper.isJavaFile(file)) {
			operationRecorder.recordSavedFile(file, success);
		}
	}

	@Override
	public void savedCompareEditor(Object compareEditor, boolean success) {
		operationRecorder.recordSavedCompareEditor((CompareEditor)compareEditor, success);
	}

	private boolean isRecordedResource(IResource resource) {
		if (resource instanceof IFile) {
			return ResourceHelper.isJavaFile((IFile)resource);
		}
		return true;
	}

	private void handleExternalFileManipulation(IFile file, Manipulation manipulation) {
		if (file.getName().equals("Entries") && file.getParent().getName().equals("CVS")) {
			if (manipulation == Manipulation.ADDED) {
				cvsEntriesAddedSet.add(file);
			} else {
				cvsEntriesChangedOrRemovedSet.add(file);
			}
		} else if (ResourceHelper.isJavaFile(file)) {
			switch (manipulation) {
				case ADDED:
					externallyAddedJavaFiles.add(file);
					break;
				case REMOVED:
					externallyRemovedJavaFiles.add(file);
					break;
				case CHANGED:
					externallyChangedJavaFiles.add(file);
					break;
			}
		} else if ("svn-base".equals(file.getFileExtension())) {
			IFile javaSourceFile= getJavaSourceFileForSVNFile(file);
			if (javaSourceFile != null) {
				//TODO: Consider REMOVED to track files that a removed as a part of an update operation
				switch (manipulation) {
					case ADDED:
						svnAddedJavaFiles.add(javaSourceFile);
						break;
					case CHANGED:
						svnChangedJavaFiles.add(javaSourceFile);
						break;
				}
			}
		}
	}

	/**
	 * Returns null if there is no corresponding Java source file (e.g. when the SVN file is not
	 * from text-base folder).
	 * 
	 * @param svnFile
	 * @return
	 */
	private IFile getJavaSourceFileForSVNFile(IFile svnFile) {
		IFile javaSourceFile= null;
		String fileName= svnFile.getName();
		if (fileName.endsWith(".java.svn-base")) {
			IPath fileFullPath= svnFile.getFullPath();
			String parentDir= fileFullPath.segment(fileFullPath.segmentCount() - 2);
			if (parentDir.equals("text-base")) {
				String javaSourceFileName= fileName.substring(0, fileName.lastIndexOf("."));
				IPath javaSourceFilePath= fileFullPath.removeLastSegments(3).append(javaSourceFileName);
				javaSourceFile= ResourceHelper.getWorkspaceRoot().getFile(javaSourceFilePath);
			}
		}
		return javaSourceFile;
	}

	private void calculateSets() {
		//should be done only in this order
		calculateCVSSets();
		calculateSVNSets();
		calculateExternallyModifiedJavaFiles();
	}

	private void calculateCVSSets() {
		processAddedCVSEntriesFiles();
		processChangedOrRemovedCVSEntriesFiles();
	}

	private void processAddedCVSEntriesFiles() {
		boolean hasChangedKnownFiles= false;
		for (IFile cvsEntriesFile : cvsEntriesAddedSet) {
			IPath relativePath= cvsEntriesFile.getFullPath().removeLastSegments(2);
			Map<IFile, String> newRevisions= ResourceHelper.getEntriesRevisions(cvsEntriesFile, relativePath);
			boolean isInitialCommit= false;
			for (Entry<IFile, String> newEntry : newRevisions.entrySet()) {
				IFile entryFile= newEntry.getKey();
				FileRevision fileRevision= getCVSFileRevision(entryFile, newEntry.getValue());
				if (externallyChangedJavaFiles.contains(entryFile)) {
					updatedJavaFileRevisions.add(fileRevision);
				} else if (!externallyAddedJavaFiles.contains(entryFile)) {
					cvsInitiallyCommittedJavaFileRevisions.add(fileRevision);
					isInitialCommit= true;
				}
			}
			if (isInitialCommit || doesContainKnownFiles(relativePath)) {
				knownFilesRecorder.addCVSEntriesFile(cvsEntriesFile);
				hasChangedKnownFiles= true;
			}
		}
		if (hasChangedKnownFiles) {
			knownFilesRecorder.recordKnownFiles();
		}
	}

	private boolean doesContainKnownFiles(IPath path) {
		IResource resource= ResourceHelper.findWorkspaceMember(path);
		if (resource instanceof Folder) {
			Folder containerFolder= (Folder)resource;
			try {
				IResource[] members= containerFolder.members();
				for (IResource member : members) {
					if (member instanceof IFile && knownFilesRecorder.isFileKnown((IFile)member, false)) {
						return true;
					}
				}
			} catch (CoreException e) {
				Debugger.logExceptionToErrorLog(e, Messages.Recorder_CVSFolderMembersFailure);
			}
		}
		return false;
	}

	private void processChangedOrRemovedCVSEntriesFiles() {
		boolean hasChangedKnownFiles= false;
		for (IFile cvsEntriesFile : cvsEntriesChangedOrRemovedSet) {
			if (cvsEntriesFile.exists()) {
				IPath relativePath= cvsEntriesFile.getFullPath().removeLastSegments(2);
				Map<IFile, String> newRevisions= ResourceHelper.getEntriesRevisions(cvsEntriesFile, relativePath);
				File trackedCVSEntriesFile= knownFilesRecorder.getTrackedCVSEntriesFile(cvsEntriesFile);
				if (trackedCVSEntriesFile.exists()) {
					Map<IFile, String> previousRevisions= ResourceHelper.getEntriesRevisions(trackedCVSEntriesFile, relativePath);
					processCVSRevisionsDifference(newRevisions, previousRevisions);
					knownFilesRecorder.addCVSEntriesFile(cvsEntriesFile); //overwrite the existing tracked entries file with the new one
					hasChangedKnownFiles= true;
				} else {
					for (Entry<IFile, String> newEntry : newRevisions.entrySet()) {
						IFile entryFile= newEntry.getKey();
						if (externallyChangedJavaFiles.contains(entryFile)) {
							updatedJavaFileRevisions.add(getCVSFileRevision(entryFile, newEntry.getValue()));
						}
					}
				}
			} else {
				// CVS entries file was deleted, so stop tracking it
				knownFilesRecorder.removeKnownFile(cvsEntriesFile);
				hasChangedKnownFiles= true;
			}
		}
		if (hasChangedKnownFiles) {
			knownFilesRecorder.recordKnownFiles();
		}
	}

	private void processCVSRevisionsDifference(Map<IFile, String> newRevisions, Map<IFile, String> previousRevisions) {
		for (Entry<IFile, String> newEntry : newRevisions.entrySet()) {
			IFile entryFile= newEntry.getKey();
			String newRevision= newEntry.getValue();
			FileRevision newFileRevision= getCVSFileRevision(entryFile, newRevision);
			String previousRevision= previousRevisions.get(entryFile);
			if (previousRevision == null) {
				if (!externallyAddedJavaFiles.contains(entryFile)) {
					cvsInitiallyCommittedJavaFileRevisions.add(newFileRevision);
				}
			} else if (!previousRevision.equals(newRevision)) {
				if (externallyChangedJavaFiles.contains(entryFile)) {
					updatedJavaFileRevisions.add(newFileRevision);
				} else {
					cvsCommittedJavaFileRevisions.add(newFileRevision);
				}
			}
		}
	}

	private void calculateSVNSets() {
		for (IFile file : svnChangedJavaFiles) {
			FileRevision fileRevision= getSVNFileRevision(file);
			if (externallyChangedJavaFiles.contains(file)) {
				updatedJavaFileRevisions.add(fileRevision); //if both the java file and its svn storage have changed, then its an update
			} else {
				svnCommittedJavaFileRevisions.add(fileRevision); //if only svn storage of a java file has changed, its a commit
			}
		}
		for (IFile file : svnAddedJavaFiles) {
			if (!externallyAddedJavaFiles.contains(file)) { //if only svn storage was added for a file, its an initial commit
				svnInitiallyCommittedJavaFileRevisions.add(getSVNFileRevision(file));

			}
		}
	}

	private void calculateExternallyModifiedJavaFiles() {
		for (IFile file : externallyChangedJavaFiles) {
			boolean isUpdated= false;
			for (FileRevision fileRevision : updatedJavaFileRevisions) {
				if (fileRevision.getFile().equals(file)) {
					isUpdated= true;
					break;
				}
			}
			if (!isUpdated) {
				externallyModifiedJavaFiles.add(file);
			}
		}
	}

	private void recordSets() {
		operationRecorder.recordExternallyModifiedFiles(externallyRemovedJavaFiles, true);
		operationRecorder.recordExternallyModifiedFiles(externallyModifiedJavaFiles, false);
		operationRecorder.recordUpdatedFiles(updatedJavaFileRevisions);
		operationRecorder.recordCommittedFiles(svnInitiallyCommittedJavaFileRevisions, true, true);
		operationRecorder.recordCommittedFiles(cvsInitiallyCommittedJavaFileRevisions, true, false);
		operationRecorder.recordCommittedFiles(svnCommittedJavaFileRevisions, false, true);
		operationRecorder.recordCommittedFiles(cvsCommittedJavaFileRevisions, false, false);
	}

	private void updateKnownFiles() {
		externallyRemovedJavaFiles.addAll(getFilesFromRevisions(updatedJavaFileRevisions)); //updated files become unknown (like removed)
		externallyRemovedJavaFiles.addAll(externallyModifiedJavaFiles); //externally modified files become unknown
		knownFilesRecorder.removeKnownFiles(externallyRemovedJavaFiles);
	}

	private void clearExternallyManipulatedFileSets() {
		externallyAddedJavaFiles.clear();
		externallyChangedJavaFiles.clear();
		externallyRemovedJavaFiles.clear();
		svnAddedJavaFiles.clear();
		svnChangedJavaFiles.clear();
		cvsEntriesAddedSet.clear();
		cvsEntriesChangedOrRemovedSet.clear();
		externallyModifiedJavaFiles.clear();
		updatedJavaFileRevisions.clear();
		svnInitiallyCommittedJavaFileRevisions.clear();
		cvsInitiallyCommittedJavaFileRevisions.clear();
		svnCommittedJavaFileRevisions.clear();
		cvsCommittedJavaFileRevisions.clear();
	}

	private FileRevision getCVSFileRevision(IFile file, String revision) {
		//CVS does not have a separate committed revision, so use a placeholder "0" instead
		return new FileRevision(file, revision, "0");
	}

	private FileRevision getSVNFileRevision(IFile file) {
		FileRevision fileRevision= new FileRevision(file, "0", "0"); //default file revision
		try {
			IContainer parent= file.getParent();
			String parentPath= ResourceHelper.getPortableResourcePath(parent);
			SVNAdminArea svnAdminArea= svnAdminAreaCache.get(parentPath);
			if (svnAdminArea == null) {
				svnAdminArea= SVNAdminAreaFactory.open(ResourceHelper.getFileForResource(parent), Level.OFF);
			}
			if (svnAdminArea != null) {
				svnAdminAreaCache.put(parentPath, svnAdminArea);
				SVNEntry svnEntry= svnAdminArea.getEntry(file.getName(), true);
				if (svnEntry != null) {
					fileRevision= new FileRevision(file, String.valueOf(svnEntry.getRevision()), String.valueOf(svnEntry.getCommittedRevision()));
				}
			}
		} catch (SVNException e) {
			//ignore SVN exceptions
		} catch (Exception e) {
			//ignore all other exceptions as well
		}
		return fileRevision;
	}

	private Set<IFile> getFilesFromRevisions(Set<FileRevision> fileRevisions) {
		Set<IFile> files= new HashSet<IFile>();
		for (FileRevision fileRevision : fileRevisions) {
			files.add(fileRevision.getFile());
		}
		return files;
	}

}
