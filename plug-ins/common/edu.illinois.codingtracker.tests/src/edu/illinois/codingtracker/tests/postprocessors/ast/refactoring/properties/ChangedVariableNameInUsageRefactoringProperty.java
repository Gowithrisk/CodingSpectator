/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This class represents changing a variable's name in its usage.
 * 
 * @author Stas Negara
 * 
 */
public class ChangedVariableNameInUsageRefactoringProperty extends RefactoringProperty {

	private final String oldVariableName;

	private final String newVariableName;


	public ChangedVariableNameInUsageRefactoringProperty(String oldVariableName, String newVariableName) {
		this.oldVariableName= oldVariableName;
		this.newVariableName= newVariableName;
	}

	public String getOldVariableName() {
		return oldVariableName;
	}


	public String getNewVariableName() {
		return newVariableName;
	}

}
