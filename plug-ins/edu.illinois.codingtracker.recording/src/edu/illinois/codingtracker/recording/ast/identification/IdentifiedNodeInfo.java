/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast.identification;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.illinois.codingtracker.operations.ast.ASTMethodDescriptor;
import edu.illinois.codingtracker.operations.ast.ASTNodeDescriptor;
import edu.illinois.codingtracker.recording.ast.helpers.ASTHelper;

/**
 * 
 * @author Stas Negara
 * 
 */
public class IdentifiedNodeInfo {

	private final String positionalNodeID;

	private long containingMethodID= -1;

	//Note that nodeDescriptor.nodeOffset could have an old value if there are changes in methods that precede in code 
	//the containing method of this AST node. This is OK since nodeOffset is used only for debugging purposes.
	private final ASTNodeDescriptor nodeDescriptor;

	private final ASTMethodDescriptor methodDescriptor; //It is null if this identified node is not a MethodDeclaration.


	public IdentifiedNodeInfo(String filePath, ASTNode identifiedNode, long persistentNodeID) {
		positionalNodeID= ASTNodesIdentifier.getPositionalNodeID(identifiedNode);
		nodeDescriptor= ASTHelper.createASTNodeDescriptor(persistentNodeID, identifiedNode, "");
		if (identifiedNode instanceof MethodDeclaration) {
			containingMethodID= persistentNodeID; //A method contains itself.
			//Note that instances of IdentifiedNodeInfo are created only during recording of AST operations, and the cache of 
			//the cyclomatic complexity calculator is always reset before this recording, so no need to reset it here again.
			methodDescriptor= ASTHelper.createASTMethodDescriptor(persistentNodeID, (MethodDeclaration)identifiedNode);
		} else {
			methodDescriptor= null;
			MethodDeclaration containingMethod= ASTHelper.getContainingMethod(identifiedNode);
			if (containingMethod != null) {
				containingMethodID= ASTNodesIdentifier.getPersistentNodeID(filePath, containingMethod);
			}
		}
	}

	public long getNodeID() {
		return nodeDescriptor.getNodeID();
	}

	public String getPositionalNodeID() {
		return positionalNodeID;
	}

	public ASTNodeDescriptor getASTNodeDescriptor() {
		return nodeDescriptor;
	}

	public ASTMethodDescriptor getMethodDescriptor() {
		return methodDescriptor;
	}

	public ASTMethodDescriptor getContainingMethodDescriptor() {
		if (containingMethodID != -1) {
			IdentifiedNodeInfo containingMethodNodeInfo= ASTNodesIdentifier.getIdentifiedNodeInfo(containingMethodID);
			ASTMethodDescriptor containingMethodDecriptor= containingMethodNodeInfo.getMethodDescriptor();
			if (containingMethodDecriptor == null) {
				throw new RuntimeException("Containing method's node info does not represent a method declaration!");
			}
			return containingMethodDecriptor;
		}
		return ASTHelper.createEmptyASTMethodDescriptor();
	}

}
