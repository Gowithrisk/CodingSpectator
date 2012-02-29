/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;
import edu.illinois.codingtracker.tests.postprocessors.ast.move.NodeDescriptor;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringProperties;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringProperty;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringPropertyAttributes;



/**
 * This class represents an instance of partially or fully inferred manual Extract Variable
 * refactoring.
 * 
 * @author Stas Negara
 * 
 */
public class ExtractVariableRefactoring extends InferredRefactoring {

	private static final Set<String> acceptableProperties= new HashSet<String>();

	static {
		acceptableProperties.add(RefactoringProperties.MOVED_TO_INITIALIZATION);
		acceptableProperties.add(RefactoringProperties.ADDED_VARIABLE_DECLARATION);
		acceptableProperties.add(RefactoringProperties.MOVED_FROM_USAGE);
		acceptableProperties.add(RefactoringProperties.ADDED_VARIABLE_REFERENCE);
	}


	private ExtractVariableRefactoring() {

	}

	public static ExtractVariableRefactoring createRefactoring(RefactoringProperty refactoringProperty) {
		if (!isAcceptableProperty(refactoringProperty)) {
			throw new RuntimeException("Can not create ExtractVariableRefactoring for property: " + refactoringProperty);
		}
		ExtractVariableRefactoring newRefactoring= new ExtractVariableRefactoring();
		addProperty(newRefactoring, refactoringProperty);
		return newRefactoring;
	}

	public static boolean isAcceptableProperty(RefactoringProperty refactoringProperty) {
		return acceptableProperties.contains(refactoringProperty.getClassName());
	}

	@Override
	protected InferredRefactoring createFreshInstance() {
		return new ExtractVariableRefactoring();
	}

	@Override
	protected Set<String> getAcceptableProperties() {
		return acceptableProperties;
	}

	@Override
	public RefactoringKind getKind() {
		return RefactoringKind.EXTRACT_LOCAL_VARIABLE;
	}

	@Override
	public Map<String, String> getArguments() {
		RefactoringProperty refactoringProperty= getProperty(RefactoringProperties.MOVED_TO_INITIALIZATION);
		String variableName= (String)refactoringProperty.getAttribute(RefactoringPropertyAttributes.VARIABLE_NAME);
		NodeDescriptor nodeDescriptor= (NodeDescriptor)refactoringProperty.getAttribute(RefactoringPropertyAttributes.MOVED_NODE);
		Map<String, String> arguments= new HashMap<String, String>();
		arguments.put("VariableName", variableName);
		arguments.put("ExtractedValue", nodeDescriptor.getNodeText());
		return arguments;
	}

}
