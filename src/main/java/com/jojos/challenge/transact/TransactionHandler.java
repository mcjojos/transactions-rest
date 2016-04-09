package com.jojos.challenge.transact;

import com.jojos.challenge.json.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * The class performing operations on transactions, and store them in-memory
 * Transactions can also reference parent transactions. The parent-child relationship is also handled in this class.
 *
 * The following operations are currently supported
 * Insert a transaction
 * Retrieve a transaction
 * Get the sum of all transactions that are transitively linked by their parent_id
 * Get a list of all transaction ids that share the same specific type.
 *
 * @implNote All transactions are stored in a sorted (and concurrent) map because the order does matter for the client
 *
 * @author g.karanikas@iontrading.com.
 */
public class TransactionHandler {

	private static final Logger log = LoggerFactory.getLogger(TransactionHandler.class);

	private final ConcurrentMap<Long, Transaction> transactions;

	public final static TransactionHandler INSTANCE = new TransactionHandler();

	private TransactionHandler() {
		transactions = new ConcurrentSkipListMap<>();
	}

	/**
	 * Store a transaction in-memory.
	 * Also handles the parent-child relationship.
	 * Cyclic references between two transactions that reference each other with the parent_id are not allowed
	 * Storing a transaction with a parent_id referencing a non-existent transaction is allowed.
	 * @param transaction to be storeed in memory
	 * @return true if the transaction succeeded, false otherwise
	 */
	public boolean insert(Transaction transaction) {
		// don't allow cyclic references between transaction, ie the parent of the transaction is not allowed to have itself as a parent
		Transaction parentTransaction = transactions.get(transaction.getParentId());
		if (parentTransaction != null) {
			if (transaction.getId() == parentTransaction.getParentId()) {
				log.error("We are not allowed to have cyclic reference between parent-child transactions." +
						          "Parent transaction {} already contains transaction {} as parent. Not added.", parentTransaction.getId(), transaction);
				return false;
			}
		}

		Transaction previousTransaction = transactions.put(transaction.getId(), transaction);

		// resolve all parent-children relationship. Our life gets easier since we can never remove a transaction
		if (previousTransaction != null) {
			transaction.setChildren(previousTransaction.getChildren());
			if (previousTransaction.getParentId() != transaction.getParentId()) {
				// remove the reference of the previousTransaction's parent to that transaction
				Transaction previousParentTransaction = transactions.get(previousTransaction.getParentId());
				if (previousParentTransaction != null) {
					if (!previousParentTransaction.removeChild(previousTransaction.getId())) {
						log.error("Parent {} didn't contain child {}", previousParentTransaction, previousTransaction.getId());
					}
				}
			}
		}

		// if transaction has a parent add transaction to the parent's kids
		if (parentTransaction != null) {
			parentTransaction.addChild(transaction.getId());
		}

		// since we allow a transaction to reference another transaction as its parent without the parent existing
		// we'll need to check if the transaction we just added is a parent of any previous transactions
		transactions.forEach((l, d) -> {
			if (d.getParentId() == transaction.getId()) {
				transaction.addChild(d.getId());
			}
		});

		return true;
	}

	/**
	 * Retrieves the transaction with the specific transaction id
	 * @param transactionId the transaction id we are looking for
	 * @return the transaction if found, null otherwise
	 */
	public Transaction getTransaction(long transactionId) {
		return transactions.get(transactionId);
	}

	/**
	 * Get the sum of all transactions that are transitively linked by their parent_id
	 * @param transaction_id the transaction id that acts as the parent_id for everything else.
	 * @return the sum of the value of this transaction with all of it's children
	 */
	public double getSum(long transaction_id) {
		double result = 0d;
		Transaction transaction = transactions.get(transaction_id);
		if (transaction != null) {
			// apparently jersey 1.17 lacks support for java8 see https://java.net/jira/browse/JERSEY-2429 so we can't use the following line
			// DoubleAdder doubleAdder = new DoubleAdder();
			// transaction.getChildren().forEach(aLong -> doubleAdder.add(getSum(aLong)));
			// doubleAdder.add(transaction.getAmount());
			// result = doubleAdder.doubleValue();

			result = transaction.getAmount();
			for (Long aLong : transaction.getChildren()) {
				result += getSum(aLong);
			}

		}

		return result;
	}

	/**
	 * Get a list of all transaction ids that share the same specific type.
	 * @param type the transaction type in question
	 * @return a list of all transaction ids for that type
	 */
	public List<Long> getTypes(String type) {
		List<Long> types = new ArrayList<>();

		for (Transaction transaction : transactions.values()) {
			String tmpType = transaction.getType();
			if (tmpType != null && tmpType.equals(type)) {
				types.add(transaction.getId());
			}
		}

		return types;
	}

}
