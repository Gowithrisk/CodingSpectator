/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;


/**
 * This interface contains class names for all classes derived from RefactoringProperty.
 * 
 * @author Stas Negara
 * 
 */
public interface RefactoringProperties {

	public static final String ADDED_VARIABLE_DECLARATION= "AddedVariableDeclarationRefactoringProperty";

	public static final String ADDED_ENTITY_REFERENCE= "AddedEntityReferenceRefactoringProperty";

	public static final String CHANGED_FIELD_NAME_IN_DECLARATION= "ChangedFieldNameInDeclarationRefactoringProperty";

	public static final String CHANGED_VARIABLE_NAME_IN_DECLARATION= "ChangedVariableNameInDeclarationRefactoringProperty";

	public static final String CHANGED_ENTITY_NAME_IN_USAGE= "ChangedEntityNameInUsageRefactoringProperty";

	public static final String DELETED_VARIABLE_DECLARATION= "DeletedVariableDeclarationRefactoringProperty";

	public static final String DELETED_ENTITY_REFERENCE= "DeletedEntityReferenceRefactoringProperty";

	public static final String MOVED_FROM_INITIALIZATION= "MovedFromInitializationRefactoringProperty";

	public static final String MOVED_FROM_USAGE= "MovedFromUsageRefactoringProperty";

	public static final String MOVED_TO_INITIALIZATION= "MovedToInitializationRefactoringProperty";

	public static final String MOVED_TO_USAGE= "MovedToUsageRefactoringProperty";

}
