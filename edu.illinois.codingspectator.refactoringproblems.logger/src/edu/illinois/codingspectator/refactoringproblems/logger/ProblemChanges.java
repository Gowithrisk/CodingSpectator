/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.refactoringproblems.logger;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.core.internal.resources.XMLWriter;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import edu.illinois.codingspectator.saferecorder.SafeRecorder;

/**
 * This class contains the two way differences between the set of compilation problems before and
 * after a refactoring.
 * 
 * @author Mohsen Vakilian
 * @author Balaji Ambresh Rajkumar
 * 
 */
@SuppressWarnings("restriction")
public class ProblemChanges {

	private static final String PROBLEM_CHANGES_TAG_NAME= "problem-changes";

	private static final String PROBLEM_REFACTORING_TIMESTAMP_ATTRIBUTE_NAME= "refactoring-timestamp";

	private static final String BEFORE_MINUS_AFTER_TAG_NAME= "before-minus-after";

	private static final String AFTER_MINUS_BEFORE_TAG_NAME= "after-minus-before";

	private static final String TIMESTAMP_ATTRIBUTE_NAME= "timestamp";

	long refactoringTimestamp, beforeTimestamp, afterTimestamp;

	Set<DefaultProblemWrapper> afterMinusBefore;

	Set<DefaultProblemWrapper> beforeMinusAfter;

	public ProblemChanges(long refactoringTimestamp, long afterTimestamp, Set<DefaultProblemWrapper> afterMinusBefore, long beforeTimestamp, Set<DefaultProblemWrapper> beforeMinusAfter) {
		this.refactoringTimestamp= refactoringTimestamp;
		this.afterTimestamp= afterTimestamp;
		this.afterMinusBefore= afterMinusBefore;
		this.beforeTimestamp= beforeTimestamp;
		this.beforeMinusAfter= beforeMinusAfter;
	}

	private void startProblemChangesTag(XMLWriter xmlWriter) {
		HashMap<String, Object> attributes= new HashMap<String, Object>();
		attributes.put(PROBLEM_REFACTORING_TIMESTAMP_ATTRIBUTE_NAME, refactoringTimestamp);
		xmlWriter.startTag(PROBLEM_CHANGES_TAG_NAME, attributes);
	}

	private void addTo(Set<DefaultProblemWrapper> problems, String tagName, long timestamp, XMLWriter xmlWriter) throws UnsupportedEncodingException {
		HashMap<String, Object> attributes= new HashMap<String, Object>();
		attributes.put(TIMESTAMP_ATTRIBUTE_NAME, timestamp);
		xmlWriter.startTag(tagName, attributes);
		for (DefaultProblemWrapper problem : problems) {
			problem.addTo(xmlWriter);
		}
		xmlWriter.endTag(tagName);
	}

	@Override
	public String toString() {
		StringBuilder builder= new StringBuilder();
		builder.append("ProblemChanges [refactoringTimestamp=");
		builder.append(refactoringTimestamp);
		builder.append(", beforeTimestamp=");
		builder.append(beforeTimestamp);
		builder.append(", afterTimestamp=");
		builder.append(afterTimestamp);
		builder.append(", afterMinusBefore=");
		builder.append(afterMinusBefore);
		builder.append(", beforeMinusAfter=");
		builder.append(beforeMinusAfter);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * 
	 * This method prints out the changes of the problems in the following format:
	 * 
	 * <problem-changes refactoring-timestamp="...">
	 * 
	 * <after-minus-before timestamp="...">...</after-minus-before>
	 * 
	 * <before-minus-after timestamp="...">...</before- minus-after>
	 * 
	 * </problem-changes>
	 */
	public void log() {
		SafeRecorder safeRecorder= new SafeRecorder("refactorings/refactoring-problems.log");
		ByteArrayOutputStream outputStream= new ByteArrayOutputStream();
		try {
			XMLWriter xmlWriter= new XMLWriter(outputStream);
			startProblemChangesTag(xmlWriter);
			addTo(afterMinusBefore, AFTER_MINUS_BEFORE_TAG_NAME, afterTimestamp, xmlWriter);
			addTo(beforeMinusAfter, BEFORE_MINUS_AFTER_TAG_NAME, beforeTimestamp, xmlWriter);
			xmlWriter.endTag(PROBLEM_CHANGES_TAG_NAME);
			xmlWriter.close();
		} catch (UnsupportedEncodingException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "CODINGSPECTATOR: Failed to serialize the compilation problems.", e));
		}
		final String XMLWriter_XML_VERSION= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		safeRecorder.record(outputStream.toString().substring(XMLWriter_XML_VERSION.length()));
	}

}
