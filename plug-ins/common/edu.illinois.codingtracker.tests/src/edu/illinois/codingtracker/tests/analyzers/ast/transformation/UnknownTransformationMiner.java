/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.transformation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * 
 * @author Stas Negara
 * 
 */
public class UnknownTransformationMiner {

	private static Map<Integer, Transaction> transactions= new HashMap<Integer, Transaction>();

	//It is TreeMap to be able to call tailMap on it.
	private static TreeMap<Item, Set<Integer>> inputItemTransactions= new TreeMap<Item, Set<Integer>>();

	private static Map<TreeSet<Item>, Set<Integer>> resultItemsetTransactions= new HashMap<TreeSet<Item>, Set<Integer>>();

	private static int blockNumber= 1;


	public static void mine() {
		addInputTransactionsToResult();
		for (Item item : inputItemTransactions.keySet()) {
			solve(SetHelper.createItemSetForItem(item), inputItemTransactions.tailMap(item, false));
		}
	}

	private static void addInputTransactionsToResult() {
		for (Entry<Item, Set<Integer>> inputEntry : inputItemTransactions.entrySet()) {
			resultItemsetTransactions.put(SetHelper.createItemSetForItem(inputEntry.getKey()), inputEntry.getValue());
		}
	}

	private static void solve(TreeSet<Item> itemSet, NavigableMap<Item, Set<Integer>> tailMap) {
		Set<Integer> itemSetTransactions= resultItemsetTransactions.get(itemSet);
		for (Entry<Item, Set<Integer>> entry : tailMap.entrySet()) {
			Set<Integer> commonTransactions= SetHelper.intersectTreeSets(itemSetTransactions, entry.getValue());
			if (commonTransactions.size() > 0) {
				Item item= entry.getKey();
				TreeSet<Item> newItemSet= new TreeSet<Item>(itemSet);
				newItemSet.add(item);
				resultItemsetTransactions.put(newItemSet, commonTransactions);
				solve(newItemSet, inputItemTransactions.tailMap(item, false));
			}
		}
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

	public static int getFrequency(TreeSet<Item> itemSet) {
		int frequency= 0;
		Set<Integer> transactionIDs= resultItemsetTransactions.get(itemSet);
		if (transactionIDs != null && transactionIDs.size() > 0) {
			Iterator<Integer> transactionIDIterator= transactionIDs.iterator();
			Transaction precedingTransaction= transactions.get(transactionIDIterator.next());
			while (transactionIDIterator.hasNext()) {
				Transaction subsequentTransaction= transactions.get(transactionIDIterator.next());
				precedingTransaction.removeDuplicatedInstances(subsequentTransaction, itemSet);
				frequency+= precedingTransaction.getFrequency(itemSet);
				precedingTransaction= subsequentTransaction;
			}
			frequency+= precedingTransaction.getFrequency(itemSet);
		}
		return frequency;
	}

	public static void resetState() {
		transactions.clear();
		inputItemTransactions.clear();
		resultItemsetTransactions.clear();
		blockNumber= 1;
	}

	public static void printState() {
		for (Entry<TreeSet<Item>, Set<Integer>> entry : resultItemsetTransactions.entrySet()) {
			TreeSet<Item> itemSet= entry.getKey();
			System.out.println("Frequency of item set " + itemSet + " is " + getFrequency(itemSet) + ":");
			for (int transactionNumber : entry.getValue()) {
				System.out.print("Instances for transaction " + transactionNumber + ": ");
				transactions.get(transactionNumber).printItemSetInstances(itemSet);
			}
		}
	}

}
