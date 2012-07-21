/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * @author Stas Negara
 * 
 */
public class ChangedMethodNameInInvocationRefactoringProperty extends ChangedEntityNameInUsageRefactoringProperty {

	public ChangedMethodNameInInvocationRefactoringProperty(String oldEntityName, String newEntityName, long entityNameNodeID, String sourceMethodName, long activationTimestamp) {
		super(oldEntityName, newEntityName, entityNameNodeID, sourceMethodName, activationTimestamp);
	}

}
