/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast.inferencing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;

import edu.illinois.codingtracker.recording.ast.helpers.ASTHelper;
import edu.illinois.codingtracker.recording.ast.identification.ASTNodesIdentifier;


/**
 * 
 * @author Stas Negara
 * 
 */
public class ASTOperationInferencer {

	CoveringNodesFinder affectedNodesFinder;

	private ASTNode newCommonCoveringNode;

	private ASTNode oldCommonCoveringNode;

	private Map<ASTNode, ASTNode> matchedNodes= new HashMap<ASTNode, ASTNode>();

	private Map<ASTNode, ASTNode> changedNodes= new HashMap<ASTNode, ASTNode>(); //Is a subset of matchedNodes.

	private Set<ASTNode> deletedNodes= new HashSet<ASTNode>();

	private Set<ASTNode> addedNodes= new HashSet<ASTNode>();


	public ASTOperationInferencer(CoherentTextChange coherentTextChange) {
		List<CoherentTextChange> coherentTextChanges= new LinkedList<CoherentTextChange>();
		coherentTextChanges.add(coherentTextChange);
		initializeInferencer(coherentTextChanges);
	}

	public ASTOperationInferencer(List<CoherentTextChange> coherentTextChanges) {
		initializeInferencer(coherentTextChanges);
	}

	public ASTNode getNewCommonCoveringNode() {
		return newCommonCoveringNode;
	}

	public Map<ASTNode, ASTNode> getMatchedNodes() {
		return matchedNodes;
	}

	public Map<ASTNode, ASTNode> getChangedNodes() {
		return changedNodes;
	}

	public Set<ASTNode> getDeletedNodes() {
		return deletedNodes;
	}

	public Set<ASTNode> getAddedNodes() {
		return addedNodes;
	}

	private void initializeInferencer(List<CoherentTextChange> coherentTextChanges) {
		affectedNodesFinder= new CoveringNodesFinder(coherentTextChanges);

		ASTNode oldRootNode= affectedNodesFinder.getOldRootNode();
		ASTNode oldCoveringNode= affectedNodesFinder.getOldCoveringNode();

		ASTNode newRootNode= affectedNodesFinder.getNewRootNode();
		ASTNode newCoveringNode= affectedNodesFinder.getNewCoveringNode();

		String initialCommonCoveringNodeID= ASTNodesIdentifier.getCommonPositonalNodeID(oldCoveringNode, newCoveringNode);
		oldCommonCoveringNode= ASTNodesIdentifier.getASTNodeFromPositonalID(oldRootNode, initialCommonCoveringNodeID);
		newCommonCoveringNode= ASTNodesIdentifier.getASTNodeFromPositonalID(newRootNode, initialCommonCoveringNodeID);
		while (areUnmatchingCoveringNodes(oldCommonCoveringNode, newCommonCoveringNode)) {
			oldCommonCoveringNode= oldCommonCoveringNode.getParent();
			newCommonCoveringNode= newCommonCoveringNode.getParent();
		}
	}

	private boolean areUnmatchingCoveringNodes(ASTNode oldCoveringNode, ASTNode newCoveringNode) {
		return oldCoveringNode.getStartPosition() != newCoveringNode.getStartPosition() ||
				oldCoveringNode.getLength() + affectedNodesFinder.getTotalDelta() != newCoveringNode.getLength() ||
				oldCoveringNode.getNodeType() != newCoveringNode.getNodeType();
	}

	//TODO: Consider that the old AST could be problematic as well.
	public boolean isProblematicInference() {
		//Note that covering nodes sometimes could be outliers (i.e. not affected), e.g. when text is added at the last offset
		//in a file. Therefore, covering nodes correctness should be checked separately.
		ASTNode newCoveringNode= affectedNodesFinder.getNewCoveringNode();
		if (ASTHelper.isRecoveredOrMalformed(newCoveringNode) || ASTHelper.isRecoveredOrMalformed(newCommonCoveringNode)) {
			return true;
		}
		return hasProblematicAffectedNodes();
	}

	private boolean hasProblematicAffectedNodes() {
		Set<ASTNode> oldChildren= ASTHelper.getAllChildren(oldCommonCoveringNode);
		for (ASTNode newChildNode : ASTHelper.getAllChildren(newCommonCoveringNode)) {
			if (ASTHelper.isRecoveredOrMalformed(newChildNode)) {
				Integer outlierDelta= affectedNodesFinder.getOutlierDelta(newChildNode, false);
				if (outlierDelta == null) {
					//If the node is not outlier (i.e. it is affected), then it should be well formed.
					return true;
				} else {
					//If an outlier is not well formed, there should exist the matching old node that is also not well formed.
					ASTNode oldNode= findMatchingNode(newChildNode, oldChildren, outlierDelta);
					if (oldNode == null || !ASTHelper.isRecoveredOrMalformed(oldNode)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void inferASTOperations() {
		matchedNodes.put(oldCommonCoveringNode, newCommonCoveringNode);
		Set<ASTNode> oldChildren= ASTHelper.getAllChildren(oldCommonCoveringNode);
		Set<ASTNode> newChildren= ASTHelper.getAllChildren(newCommonCoveringNode);

		//First, match nodes that are completely before or completely after the changed range.
		matchNodesOutsideOfChangedRange(oldChildren, newChildren);

		//Next, match yet unmatched nodes with the same structural positions and types.
		matchNodesStructurally(oldChildren);

		//Finally, collect the remaining unmatched nodes as deleted and added nodes. Also, collect the changed matched nodes.
		collectDeletedNodes(oldChildren);
		collectAddedNodes(newChildren);
		collectChangedNodes();
	}

	private void matchNodesOutsideOfChangedRange(Set<ASTNode> oldNodes, Set<ASTNode> newNodes) {
		for (ASTNode oldNode : oldNodes) {
			Integer outlierDelta= affectedNodesFinder.getOutlierDelta(oldNode, true);
			if (outlierDelta != null) {
				ASTNode newNode= findMatchingNode(oldNode, newNodes, outlierDelta);
				if (newNode != null) {
					matchedNodes.put(oldNode, newNode);
				}
			}
		}
	}

	private ASTNode findMatchingNode(ASTNode nodeToMatch, Set<ASTNode> candidateNodes, int deltaTextLength) {
		for (ASTNode node : candidateNodes) {
			if (nodeToMatch.getStartPosition() + deltaTextLength == node.getStartPosition() &&
					nodeToMatch.getLength() == node.getLength() && nodeToMatch.getNodeType() == node.getNodeType()) {
				return node;
			}
		}
		return null;
	}

	private void matchNodesStructurally(Set<ASTNode> oldNodes) {
		ASTNode newRootNode= affectedNodesFinder.getNewRootNode();
		for (ASTNode oldNode : oldNodes) {
			if (!matchedNodes.containsKey(oldNode)) {
				String oldNodePositionalID= ASTNodesIdentifier.getPositionalNodeID(oldNode);
				ASTNode tentativeNewMatchingNode= ASTNodesIdentifier.getASTNodeFromPositonalID(newRootNode, oldNodePositionalID);
				if (tentativeNewMatchingNode != null && !matchedNodes.containsValue(tentativeNewMatchingNode) &&
						oldNode.getNodeType() == tentativeNewMatchingNode.getNodeType()) {
					matchedNodes.put(oldNode, tentativeNewMatchingNode);
				}
			}
		}
	}

	private void collectDeletedNodes(Set<ASTNode> oldNodes) {
		for (ASTNode oldNode : oldNodes) {
			if (!matchedNodes.containsKey(oldNode)) {
				deletedNodes.add(oldNode);
			}
		}
	}

	private void collectAddedNodes(Set<ASTNode> newNodes) {
		for (ASTNode newNode : newNodes) {
			if (!matchedNodes.containsValue(newNode)) {
				addedNodes.add(newNode);
			}
		}
	}

	private void collectChangedNodes() {
		for (Entry<ASTNode, ASTNode> mapEntry : matchedNodes.entrySet()) {
			ASTNode oldNode= mapEntry.getKey();
			ASTNode newNode= mapEntry.getValue();
			for (SimplePropertyDescriptor simplePropertyDescriptor : ASTHelper.getSimplePropertyDescriptors(oldNode)) {
				Object oldProperty= oldNode.getStructuralProperty(simplePropertyDescriptor);
				Object newProperty= newNode.getStructuralProperty(simplePropertyDescriptor);
				if (oldProperty == null && newProperty != null || oldProperty != null && !oldProperty.equals(newProperty)) {
					//Matched node is changed.
					changedNodes.put(oldNode, newNode);
					break;
				}
			}
		}
	}

}
