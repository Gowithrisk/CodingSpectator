/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.transformation;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.tests.analyzers.ast.transformation.RemainingItemsComparator.ItemPairStatus;
import edu.illinois.codingtracker.tests.analyzers.ast.transformation.helpers.SetMapHelper;
import edu.illinois.codingtracker.tests.postprocessors.CodingTrackerPostprocessor;


/**
 * 
 * @author Stas Negara
 * 
 */
public class UnknownTransformationMiner {

	public enum SubsumptionStatus {
		NOT_SUBSUMED, SUBSUMED_FROM_RESULTS, FULLY_SUBSUMED
	};

	private static final Map<Integer, Transaction> transactions= new HashMap<Integer, Transaction>();

	//This is used to cache itemset frequencies to avoid recomputation as well as for ordering the result. 
	private static final Map<TreeSet<Item>, Frequency> itemSetFrequencies= new HashMap<TreeSet<Item>, Frequency>();

	//It is TreeMap to be able to call tailMap on it.
	private static final TreeMap<Item, Set<Integer>> inputItemTransactions= new TreeMap<Item, Set<Integer>>();

	private static final Map<TreeSet<Item>, Set<Integer>> resultItemSetTransactions= new TreeMap<TreeSet<Item>, Set<Integer>>(new ResultItemSetsComparator(itemSetFrequencies));

	private static final Map<Long, Set<TreeSet<Item>>> hashedResultItemSets= new HashMap<Long, Set<TreeSet<Item>>>();

	private static int blockNumber= 1;


	public static Set<Integer> getResultItemSetTransactions(TreeSet<Item> itemSet) {
		return resultItemSetTransactions.get(itemSet);
	}

	public static Set<Integer> getInputItemTransactions(Item item) {
		return inputItemTransactions.get(item);
	}

	public static int getTransactionsMemorySize() {
		int memorySize= 0;
		for (Entry<Integer, Transaction> entry : transactions.entrySet()) {
			memorySize+= 4 + entry.getValue().getMemorySize();
		}
		return memorySize;
	}

	public static int getItemSetFrequenciesMemorySize() {
		int memorySize= 0;
		for (Entry<TreeSet<Item>, Frequency> entry : itemSetFrequencies.entrySet()) {
			memorySize+= entry.getKey().size() * 8 + entry.getValue().getMemorySize();
		}
		return memorySize;
	}

	public static int getResultItemSetTransactionsMemorySize() {
		int memorySize= 0;
		for (Entry<TreeSet<Item>, Set<Integer>> entry : resultItemSetTransactions.entrySet()) {
			memorySize+= entry.getKey().size() * 8 + entry.getValue().size() * 4;
		}
		return memorySize;
	}

	public static void computeMemoryFootPrint() {
		System.out.println("Transactions: " + getTransactionsMemorySize());
		System.out.println("Cached frequencies: " + getItemSetFrequenciesMemorySize());
		System.out.println("Results: " + getResultItemSetTransactionsMemorySize());
	}

	public static void mine() {
		long startTime= System.currentTimeMillis();

		solve(new TreeSet<Item>(), new TreeSet<Integer>(), inputItemTransactions);

		System.out.println("Mining time: " + (System.currentTimeMillis() - startTime));
	}

	private static void solve(TreeSet<Item> itemSet, Set<Integer> transactionIDs, NavigableMap<Item, Set<Integer>> remainingItems) {
		RemainingItemsComparator remainingItemsComparator= new RemainingItemsComparator(itemSet, transactionIDs);
		TreeMap<Item, Set<Integer>> localRemainingItems= new TreeMap<Item, Set<Integer>>(remainingItemsComparator);
		localRemainingItems.putAll(remainingItems);
		while (!localRemainingItems.isEmpty()) {
			Item currentItem= localRemainingItems.pollFirstEntry().getKey();
			if (remainingItemsComparator.isFrequent(currentItem)) {
				Frequency currentItemFrequency= remainingItemsComparator.getItemFrequency(currentItem);
				TreeMap<Item, Set<Integer>> newRemainingItems= SetMapHelper.createCopy(localRemainingItems);
				TreeSet<Item> newItemSet= fuseWithSiblings(currentItem, remainingItemsComparator, localRemainingItems, newRemainingItems);
				Set<Integer> commonTransactionIDs= remainingItemsComparator.getCommonTransactionIDs(currentItem);
				SubsumptionStatus subsumptionStatus= getSubsumptionStatus(newItemSet, commonTransactionIDs);
				if (subsumptionStatus != SubsumptionStatus.FULLY_SUBSUMED) {
					Frequency frequency= getFrequency(newItemSet, commonTransactionIDs); //To ensure itemset's frequency is computed before adding to results.
					if (!frequency.isEqualTo(currentItemFrequency)) {
						throw new RuntimeException("Frequency got skewed!");
					}
					if (subsumptionStatus != SubsumptionStatus.SUBSUMED_FROM_RESULTS) {
						resultItemSetTransactions.put(newItemSet, commonTransactionIDs);
						addToHashedResultItemSets(newItemSet, commonTransactionIDs);
					}
					solve(newItemSet, commonTransactionIDs, newRemainingItems);
				}
			}
		}
	}

	private static TreeSet<Item> fuseWithSiblings(Item currentItem, RemainingItemsComparator remainingItemsComparator, TreeMap<Item, Set<Integer>> localRemainingItems,
														TreeMap<Item, Set<Integer>> newRemainingItems) {
		TreeSet<Item> newItemSet= new TreeSet<Item>(remainingItemsComparator.getBaseItemSet());
		newItemSet.add(currentItem);
		Iterator<Entry<Item, Set<Integer>>> siblingsIterator= localRemainingItems.entrySet().iterator();
		while (siblingsIterator.hasNext()) {
			Item siblingItem= siblingsIterator.next().getKey();
			ItemPairStatus itemPairStatus= remainingItemsComparator.compareItems(currentItem, siblingItem);
			if (itemPairStatus == ItemPairStatus.SECOND_PREVAILS || itemPairStatus == ItemPairStatus.EQUIVALENT) {
				newItemSet.add(siblingItem);
				newRemainingItems.remove(siblingItem);
				if (itemPairStatus == ItemPairStatus.EQUIVALENT) {
					siblingsIterator.remove();
				}
			}
		}
		return newItemSet;
	}

	/**
	 * Also, removes the previously added elements if they are subsumed by the new one.
	 * 
	 * @param checkedItemSet
	 * @param transactionIDs
	 * @return
	 */
	private static SubsumptionStatus getSubsumptionStatus(TreeSet<Item> checkedItemSet, Set<Integer> transactionIDs) {
		SubsumptionStatus subsumptionStatus= SubsumptionStatus.NOT_SUBSUMED;
		Set<TreeSet<Item>> itemSets= hashedResultItemSets.get(getHashForTransactions(transactionIDs));
		if (itemSets == null) {
			return subsumptionStatus;
		}
		Frequency chekedItemSetFrequency= getFrequency(checkedItemSet, transactionIDs);
		Iterator<TreeSet<Item>> itemSetsIterator= itemSets.iterator();
		while (itemSetsIterator.hasNext()) {
			TreeSet<Item> itemSet= itemSetsIterator.next();
			if (itemSet.containsAll(checkedItemSet)) {
				Set<Integer> itemSetTransactionIDs= resultItemSetTransactions.get(itemSet);
				if (itemSetTransactionIDs.equals(transactionIDs)) {
					Frequency itemSetFrequency= getFrequency(itemSet, itemSetTransactionIDs);
					if (itemSetFrequency.isNotLessPowerfulThan(chekedItemSetFrequency)) {
						return SubsumptionStatus.FULLY_SUBSUMED;
					}
					if (itemSetFrequency.getOverallFrequency() == chekedItemSetFrequency.getOverallFrequency()) {
						subsumptionStatus= SubsumptionStatus.SUBSUMED_FROM_RESULTS;
					}
				}
			} else if (checkedItemSet.containsAll(itemSet)) {
				Set<Integer> itemSetTransactionIDs= resultItemSetTransactions.get(itemSet);
				if (itemSetTransactionIDs.equals(transactionIDs) &&
						getFrequency(itemSet, itemSetTransactionIDs).getOverallFrequency() == chekedItemSetFrequency.getOverallFrequency()) {
					itemSetsIterator.remove();
					resultItemSetTransactions.remove(itemSet);
				}
			}
		}
		return subsumptionStatus;
	}

	private static void addToHashedResultItemSets(TreeSet<Item> itemSet, Set<Integer> transactionIDs) {
		Long hash= getHashForTransactions(transactionIDs);
		Set<TreeSet<Item>> itemSets= hashedResultItemSets.get(hash);
		if (itemSets == null) {
			itemSets= new HashSet<TreeSet<Item>>();
			hashedResultItemSets.put(hash, itemSets);
		}
		itemSets.add(itemSet);
	}

	private static Long getHashForTransactions(Set<Integer> transactionIDs) {
		long hash= 0;
		for (Integer transactionID : transactionIDs) {
			hash+= transactionID;
		}
		return hash;
	}

	public static void addItemToTransactions(TreeMap<Long, Item> items, boolean isFirstBlock, boolean isLastBlock) {
		if (items.isEmpty()) {
			throw new RuntimeException("A block should contain at least one item.");
		}
		for (Entry<Long, Item> entry : items.entrySet()) {
			long itemID= entry.getKey();
			Item item= entry.getValue();
			Set<Integer> itemTransactions= inputItemTransactions.get(item);
			if (itemTransactions == null) {
				itemTransactions= new TreeSet<Integer>();
				inputItemTransactions.put(item, itemTransactions);
			}
			if (isFirstBlock || !isLastBlock) {
				itemTransactions.add(blockNumber);
				addItemInstanceToTransation(blockNumber, itemID, item);
			}
			if (!isFirstBlock) { //If not the first block, add to the preceding transaction as well.
				itemTransactions.add(blockNumber - 1);
				addItemInstanceToTransation(blockNumber - 1, itemID, item);
			}
		}
		if (isFirstBlock || !isLastBlock) {
			blockNumber++;
		}
	}

	private static void addItemInstanceToTransation(int transactionID, long itemID, Item item) {
		Transaction transaction= transactions.get(transactionID);
		if (transaction == null) {
			transaction= new Transaction(transactionID);
			transactions.put(transactionID, transaction);
		}
		transaction.addItemInstance(item, itemID);
	}

	/**
	 * Should be called only after all results are computed.
	 * 
	 * @param itemSet
	 * @return
	 */
	public static int getFrequency(TreeSet<Item> itemSet) {
		Frequency frequency= itemSetFrequencies.get(itemSet);
		if (frequency == null) {
			return 0;
		}
		if (resultItemSetTransactions.get(itemSet) == null) {
			return 0;
		}
		return frequency.getOverallFrequency();
	}

	static Frequency getSubsetFrequency(TreeSet<Item> itemSet, Set<Integer> subsetTransactionIDs) {
		return computeFrequency(itemSet, subsetTransactionIDs);
	}

	static Frequency getFrequency(TreeSet<Item> itemSet, Set<Integer> transactionIDs) {
		if (transactionIDs.isEmpty()) {
			return new Frequency(new LinkedList<Integer>(), new LinkedList<Integer>());
		}
		Frequency frequency= itemSetFrequencies.get(itemSet);
		if (frequency == null) {
			frequency= computeFrequency(itemSet, transactionIDs);
			//Create a copy since the itemset might get modified externally.
			itemSetFrequencies.put(SetMapHelper.createCopy(itemSet), frequency);
		}
		return frequency;
	}

	private static Frequency computeFrequency(TreeSet<Item> itemSet, Set<Integer> transactionIDs) {
		removeDuplicatedInstances(itemSet, transactionIDs);
		Frequency frequency= addFrequencyForTransactions(itemSet, transactionIDs);
		clearRemovedDuplicatedInstances(transactionIDs);
		return frequency;
	}

	private static void removeDuplicatedInstances(TreeSet<Item> itemSet, Set<Integer> transactionIDs) {
		Iterator<Integer> transactionIDIterator= transactionIDs.iterator();
		Transaction precedingTransaction= transactions.get(transactionIDIterator.next());
		while (transactionIDIterator.hasNext()) {
			Transaction subsequentTransaction= transactions.get(transactionIDIterator.next());
			precedingTransaction.removeDuplicatedInstances(subsequentTransaction, itemSet);
			precedingTransaction= subsequentTransaction;
		}
	}

	private static Frequency addFrequencyForTransactions(TreeSet<Item> itemSet, Set<Integer> transactionIDs) {
		List<Integer> minimalFrequencies= new LinkedList<Integer>();
		List<Integer> maximalFrequencies= new LinkedList<Integer>();
		for (int transactionID : transactionIDs) {
			Transaction transaction= transactions.get(transactionID);
			minimalFrequencies.add(transaction.getMinimalFrequency(itemSet));
			maximalFrequencies.add(transaction.getMaximalFrequency(itemSet));
		}
		return new Frequency(minimalFrequencies, maximalFrequencies);
	}

	private static void clearRemovedDuplicatedInstances(Set<Integer> transactionIDs) {
		for (int transactionID : transactionIDs) {
			transactions.get(transactionID).clearRemovedDuplicatedItemInstances();
		}
	}

	public static void resetState() {
		transactions.clear();
		inputItemTransactions.clear();
		resultItemSetTransactions.clear();
		hashedResultItemSets.clear();
		itemSetFrequencies.clear();
		blockNumber= 1;
	}

	public static void printState() {
		for (Entry<TreeSet<Item>, Set<Integer>> entry : resultItemSetTransactions.entrySet()) {
			System.out.print(getItemSetResultAsText(entry.getKey(), entry.getValue()));
		}
	}

	private static StringBuffer getItemSetResultAsText(TreeSet<Item> itemSet, Set<Integer> transactionIDs) {
		StringBuffer result= new StringBuffer();
		result.append("Frequency of item set ").append(itemSet).append(" is ").append(getFrequency(itemSet)).append(":\n");
		removeDuplicatedInstances(itemSet, transactionIDs);
		for (int transactionID : transactionIDs) {
			result.append("Instances for transaction ").append(transactionID).append(": ");
			result.append(transactions.get(transactionID).getItemSetInstancesAsText(itemSet));
		}
		clearRemovedDuplicatedInstances(transactionIDs);
		return result;
	}

	public static void writeResultsToFolder(File miningResultsFolder) {
		miningResultsFolder.mkdir();
		for (File file : miningResultsFolder.listFiles()) {
			if (!file.delete()) {
				throw new RuntimeException("Could not delete the old file with the mining result!");
			}
		}
		int counter= 1;
		for (Entry<TreeSet<Item>, Set<Integer>> entry : resultItemSetTransactions.entrySet()) {
			if (counter > Configuration.miningMaxOutputItemSetsCount) {
				return;
			}
			File itemSetFile= new File(miningResultsFolder, "itemSet" + counter++);
			CodingTrackerPostprocessor.writeToFile(itemSetFile, getItemSetResultAsText(entry.getKey(), entry.getValue()), false);
		}
	}

}
