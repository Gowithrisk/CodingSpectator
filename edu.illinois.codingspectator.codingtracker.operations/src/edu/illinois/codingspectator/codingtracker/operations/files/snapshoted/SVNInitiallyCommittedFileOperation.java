/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.files.snapshoted;

import org.eclipse.core.resources.IFile;

import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
public class SVNInitiallyCommittedFileOperation extends CommittedFileOperation {

	public SVNInitiallyCommittedFileOperation() {
		super();
	}

	public SVNInitiallyCommittedFileOperation(IFile initiallyCommittedFile) {
		super(initiallyCommittedFile);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.FILE_SVN_INITIALLY_COMMITTED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "SVN initially committed file";
	}

}
