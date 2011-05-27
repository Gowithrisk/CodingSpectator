/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.logstocsv.tests;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

import edu.illinois.codingspectator.efs.EFSFile;
import edu.illinois.codingspectator.logstocsv.ConvertLogsToCSV;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class Test01 {

	@Test
	public void test() throws CoreException, IOException {
		IPath pathToTestFolder= new Path("resources").append("01");
		IPath pathToInputFolder= pathToTestFolder.append("input");
		EFSFile csvActualLogFolder= new EFSFile(pathToTestFolder.append("actual-output"));
		csvActualLogFolder.mkdir();
		EFSFile csvActualLog= new EFSFile(csvActualLogFolder.getPath().append("logs.csv"));
		ConvertLogsToCSV.main(new String[] { null, pathToInputFolder.toOSString(), csvActualLog.getPath().toOSString() });
		assertTrue(csvActualLog.exists());
		//FIXME: Compare the actual and expected log files.
		csvActualLogFolder.delete();
	}
}
