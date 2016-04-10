package com.jojos.challenge.transact;

import com.jojos.challenge.json.Transaction;
import com.jojos.challenge.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The class performing operations on transactions, and store them in-memory
 * Transactions can also reference parent transactions. The parent-child relationship is also handled in this class.
 *
 * The following operations are currently supported
 * Insert a transaction:
 * O(N) just because we allow a transaction with a parent_id referencing to a non-existing transaction
 * If we weren't allowing this condition we'd have O(1).
 *
 * Retrieve a transaction:
 * O(1)
 *
 * Get a list of all transaction ids that share the same specific type:
 * O(1) because we cache the type on insertion
 *
 *
 * Get the sum of all transactions that are transitively linked by their parent_id:
 * O(N) worst case
 *
 * Updating an existing transaction is also not supported. As stated in the spec:
 * "transaction_id is a long specifying a new transaction"
 *
 * @implNote All transactions are stored in a sorted map because the order does matter for the client.
 * Also, the types are cached each time to achieve constant retrieval time.
 *
 * @author karanikasg@gmail.com.
 */
public class TransactionHandler {

	private static final Logger log = LoggerFactory.getLogger(TransactionHandler.class);

	private final ConcurrentMap<Long, Transaction> transactions;
	private final ConcurrentMap<String, Set<Long>> transactionTypes;

	public final static TransactionHandler INSTANCE = new TransactionHandler();

	// the read-write lock are only used while updating the parent-child relationships
	// since it may be the case where we have inconsistencies while calculating the sum (read ) AND updating the
	// relationships on inserting a transaction.
	private final Lock readLock;
	private final Lock writeLock;

	private TransactionHandler() {
		transactions = new ConcurrentSkipListMap<>();
		transactionTypes = new ConcurrentHashMap<>();
		ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
		readLock = readWriteLock.readLock();
		writeLock = readWriteLock.writeLock();
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

		// sanity check - don't allow any update "transaction_id is a long specifying a new transaction
		if (transactions.containsKey(transaction.getId())) {
			log.error("transaction_id {} already exist. We currently don't support update operations.", transaction.getId());
			return false;
		}

		// don't allow cyclic references between transactions, ie the parent of the transaction is not allowed to have the transaction itself as a parent
		Transaction parentTransaction = transactions.get(transaction.getParentId());
		if (parentTransaction != null) {
			if (transaction.getId() == parentTransaction.getParentId()) {
				log.error("We are not allowed to have cyclic reference between parent-child transactions." +
						          "Parent transaction {} already contains transaction {} as parent. Not added.", parentTransaction.getId(), transaction);
				return false;
			}
		}

		// sanity check - also don't allow transactions having a parent_id equal with the id
		if (transaction.getParentId() == transaction.getId()) {
			return false;
		}

		transactions.put(transaction.getId(), transaction);

		// the following try-finally block needs to be write lock protected for the shake of correctness of edge cases
		// when we are asked to calculate the sum of all child transactions while still updating the parent-child relations.
		// this is the smallest possible block of code that need to be write-locked.
		try {
			writeLock.lock();
			// if transaction has a parent then add transaction to the parent's kids
			if (parentTransaction != null) {
				parentTransaction.addChild(transaction.getId());
			}

			// since we allow a transaction to reference another transaction as its parent without
			// the parent existing in the first place we'll need to check here if the transaction
			// we just added is a parent of any previous transactions.
			transactions.forEach((l, d) -> {
				if (d.getParentId() == transaction.getId()) {
					transaction.addChild(d.getId());
				}
			});
		} finally {
			writeLock.unlock();
		}

		// after calculating everything and BEFORE reporting success we'll need to cache the type
		Util.addToContainedSet(transactionTypes, transaction.getType(), transaction.getId());

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
			DoubleAdder doubleAdder = new DoubleAdder();
			try {
				readLock.lock();
				transaction.getChildren().forEach(aLong -> doubleAdder.add(getSum(aLong)));
			} finally {
				readLock.unlock();
			}
			doubleAdder.add(transaction.getAmount());
			result = doubleAdder.doubleValue();
		}

		return result;
	}

	/**
	 * Get a list of all transaction ids that share the same specific type.
	 * Provides O(1) constant time since we have already cached all types inserted.
	 * @param type the transaction type in question
	 * @return a set of all transaction ids for that type
	 */
	public Set<Long> getTypes(String type) {
		return transactionTypes.get(type);
	}

}
