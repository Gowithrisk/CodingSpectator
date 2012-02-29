/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.recording.ast.ASTOperationRecorder;
import edu.illinois.codingtracker.recording.ast.helpers.ASTHelper;
import edu.illinois.codingtracker.recording.ast.identification.ASTNodesIdentifier;
import edu.illinois.codingtracker.tests.postprocessors.ast.move.NodeDescriptor;



/**
 * This class creates distinct properties corresponding to ASTOperations.
 * 
 * @author Stas Negara
 * 
 */
public class RefactoringPropertiesFactory {

	private static final ASTOperationRecorder astOperationRecorder= ASTOperationRecorder.getInstance();

	private static Set<RefactoringProperty> properties;


	/**
	 * Returns null if there is no refactoring property corresponding to the given operation.
	 * 
	 * @param operation
	 * @return
	 */
	public static Set<RefactoringProperty> retrieveProperties(ASTOperation operation) {
		properties= new HashSet<RefactoringProperty>();
		ASTNode rootNode;
		if (operation.isAdd()) {
			rootNode= astOperationRecorder.getLastNewRootNode();
		} else {
			rootNode= astOperationRecorder.getLastOldRootNode();
		}
		ASTNode affectedNode= ASTNodesIdentifier.getASTNodeFromPositonalID(rootNode, operation.getPositionalID());
		if (operation.isAdd()) {
			handleAddedNode(affectedNode, operation);
		} else if (operation.isDelete()) {
			handleDeletedNode(affectedNode, operation);
		} else if (operation.isChange() && affectedNode instanceof SimpleName) {
			handleChangedNode((SimpleName)affectedNode, operation);
		}
		return properties;
	}

	private static void handleChangedNode(SimpleName changedNode, ASTOperation operation) {
		String oldEntityName= changedNode.getIdentifier();
		String newEntityName= operation.getNodeNewText();
		if (isDeclaredEntity(changedNode)) {
			if (isInLocalVariableDeclaration(changedNode)) {
				properties.add(new ChangedVariableNameInDeclarationRefactoringProperty(oldEntityName, newEntityName));
			} else if (isInFieldDeclaration(changedNode)) {
				properties.add(new ChangedFieldNameInDeclarationRefactoringProperty(oldEntityName, newEntityName));
			} else if (isInMethodDeclaration(changedNode)) {
				properties.add(new ChangedMethodNameInDeclarationRefactoringProperty(oldEntityName, newEntityName));
			}
		} else {
			properties.add(new ChangedEntityNameInUsageRefactoringProperty(oldEntityName, newEntityName));
		}
	}

	private static void handleDeletedNode(ASTNode deletedNode, ASTOperation operation) {
		long moveID= operation.getMoveID();
		if (moveID != -1) {
			handleDeletedMovedNode(deletedNode, new NodeDescriptor(operation), moveID);
		} else {
			handleDeletedNotMovedNode(deletedNode);
		}
	}

	private static void handleDeletedMovedNode(ASTNode deletedNode, NodeDescriptor nodeDescriptor, long moveID) {
		String entityName= getDeclaredEntityNameForInitializer(deletedNode);
		if (entityName != null) {
			properties.add(new MovedFromInitializationRefactoringProperty(nodeDescriptor, entityName, moveID));
		} else {
			long parentID= getParentID(deletedNode, true);
			if (parentID != -1) {
				properties.add(new MovedFromUsageRefactoringProperty(nodeDescriptor, moveID, parentID));
			}
		}
	}

	private static void handleDeletedNotMovedNode(ASTNode deletedNode) {
		if (deletedNode instanceof VariableDeclarationFragment) {
			String entityName= getDeclaredEntityName((VariableDeclarationFragment)deletedNode);
			if (isInLocalVariableDeclaration(deletedNode)) {
				properties.add(new DeletedVariableDeclarationRefactoringProperty(entityName));
			}
		} else if (deletedNode instanceof SimpleName && !isDeclaredEntity(deletedNode)) {
			long parentID= getParentID(deletedNode, true);
			if (parentID != -1) {
				String entityName= ((SimpleName)deletedNode).getIdentifier();
				properties.add(new DeletedEntityReferenceRefactoringProperty(entityName, parentID));
			}
		}
	}

	private static void handleAddedNode(ASTNode addedNode, ASTOperation operation) {
		long moveID= operation.getMoveID();
		if (moveID != -1) {
			handleAddedMovedNode(addedNode, new NodeDescriptor(operation), moveID);
		} else {
			handleAddedNotMovedNode(addedNode);
		}
	}

	private static void handleAddedMovedNode(ASTNode addedNode, NodeDescriptor nodeDescriptor, long moveID) {
		String declarationEntityName= getDeclaredEntityNameForInitializer(addedNode);
		if (declarationEntityName != null) {
			properties.add(new MovedToInitializationRefactoringProperty(nodeDescriptor, declarationEntityName, moveID));
		} else {
			long parentID= getParentID(addedNode, false);
			properties.add(new MovedToUsageRefactoringProperty(nodeDescriptor, moveID, parentID));
			if (addedNode instanceof SimpleName) {
				String referenceEntityName= ((SimpleName)addedNode).getIdentifier();
				properties.add(new AddedEntityReferenceRefactoringProperty(referenceEntityName, parentID));
			}
		}
	}

	private static void handleAddedNotMovedNode(ASTNode addedNode) {
		if (addedNode instanceof VariableDeclarationFragment) {
			String entityName= getDeclaredEntityName((VariableDeclarationFragment)addedNode);
			if (isInLocalVariableDeclaration(addedNode)) {
				properties.add(new AddedVariableDeclarationRefactoringProperty(entityName));
			}
		} else if (addedNode instanceof SimpleName && !isDeclaredEntity(addedNode)) {
			String entityName= ((SimpleName)addedNode).getIdentifier();
			properties.add(new AddedEntityReferenceRefactoringProperty(entityName, getParentID(addedNode, false)));
		}
	}

	/**
	 * Returns null if the given node is not an initializer in a variable declaration fragment.
	 * 
	 * @param node
	 * @return
	 */
	private static String getDeclaredEntityNameForInitializer(ASTNode node) {
		VariableDeclarationFragment variableDeclaration= getEnclosingVariableDeclarationFragment(node);
		if (variableDeclaration != null && node == variableDeclaration.getInitializer()) {
			return getDeclaredEntityName(variableDeclaration);
		}
		return null;
	}

	private static boolean isDeclaredEntity(ASTNode node) {
		VariableDeclarationFragment variableDeclaration= getEnclosingVariableDeclarationFragment(node);
		if (variableDeclaration != null && node == variableDeclaration.getName()) {
			return true;
		}
		ASTNode methodDeclaration= ASTHelper.getParent(node, MethodDeclaration.class);
		if (methodDeclaration != null && node == ((MethodDeclaration)methodDeclaration).getName()) {
			return true;
		}
		return false;
	}

	private static boolean isInLocalVariableDeclaration(ASTNode node) {
		return ASTHelper.getParent(node, VariableDeclarationStatement.class) != null;
	}

	private static boolean isInFieldDeclaration(ASTNode node) {
		return ASTHelper.getParent(node, FieldDeclaration.class) != null;
	}

	/**
	 * Ignores constructors. Constructors are detected as methods without a return type. We can not
	 * use isConstructor, since it would not work if the class is being renamed (which makes
	 * original constructors to be considered as ordinary methods until they are renamed as well).
	 * 
	 * @param node
	 * @return
	 */
	private static boolean isInMethodDeclaration(ASTNode node) {
		ASTNode methodDeclaration= ASTHelper.getParent(node, MethodDeclaration.class);
		return methodDeclaration != null && ((MethodDeclaration)methodDeclaration).getReturnType2() != null;
	}

	private static VariableDeclarationFragment getEnclosingVariableDeclarationFragment(ASTNode node) {
		ASTNode parent= ASTHelper.getParent(node, VariableDeclarationFragment.class);
		if (parent != null) {
			return (VariableDeclarationFragment)parent;
		}
		return null;
	}

	private static String getDeclaredEntityName(VariableDeclarationFragment variableDeclaration) {
		return variableDeclaration.getName().getIdentifier();
	}

	private static long getParentID(ASTNode node, boolean isOld) {
		ASTNode parentNode= node.getParent();
		if (isOld) {
			if (astOperationRecorder.isDeleted(parentNode)) {
				return -1;
			} else {
				ASTNode newParentNode= astOperationRecorder.getNewMatch(parentNode);
				if (newParentNode != null) {
					return getNodeID(newParentNode);
				} else {
					throw new RuntimeException("A parent node of a deleted node is neither deleted nor matched");
				}
			}
		} else {
			return getNodeID(parentNode);
		}
	}

	private static long getNodeID(ASTNode node) {
		return ASTNodesIdentifier.getPersistentNodeID(astOperationRecorder.getCurrentRecordedFilePath(), node);
	}

}
