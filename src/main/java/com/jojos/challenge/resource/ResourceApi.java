package com.jojos.challenge.resource;

import com.jojos.challenge.json.InsertStatus;
import com.jojos.challenge.json.Sum;
import com.jojos.challenge.json.Transaction;
import com.jojos.challenge.transact.TransactionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

/**
 * The class defining the rest api
 *
 * In detail the api spec looks like the following:
 * GET /transactionservice/transaction/$transaction_id
 * Returns:
 * { "amount":double,"type":string,"parent_id":long }
 *
 * A json list of all transaction ids that share the same type $type.
 * GET /transactionservice/types/$type
 * Returns:
 * [ long, long, .... ]
 *
 * A sum of all transactions that are transitively linked by their parent_id to $transaction_id.
 * GET /transactionservice/sum/$transaction_id
 * Returns
 * { "sum": double }
 *
 * PUT /transactionservice/transaction/$transaction_id
 * Body:
 * { "amount":double,"type":string,"parent_id":long }
 * where:
 * transaction_id is a long specifying a new transaction
 * amount is a double specifying the amount
 * type is a string specifying a type of the transaction.
 * parent_id is an optional long that may specify the parent transaction of this transaction.
 *
 * @author karanikasg@gmail.com.
 */
@Path("/transactionservice")
public class ResourceApi {

	private static final Logger log = LoggerFactory.getLogger(ResourceApi.class);

	private static final TransactionHandler handler = TransactionHandler.INSTANCE;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String get() {
		return "\n This is the REST API via HTTPServer. Use it with wisdom";
	}

	@GET @Path("transaction/{transaction_id:\\d+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Transaction getTransaction(@PathParam("transaction_id") String transaction_id) {
		log.debug("GET transaction/{}", transaction_id);

		Transaction transaction = new Transaction();

		try {
			transaction = handler.getTransaction(Long.parseLong(transaction_id));
			log.debug("GET returning {}", transaction);
		} catch (NumberFormatException e) {
			log.error("Unable to complete GET transaction/{}. {}", transaction_id, e.getMessage());
		}
		return transaction;
	}

	@GET @Path("types/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<Long> getTypes(@PathParam("type") String type) {
		log.debug("GET types/{}", type);

		Set<Long> types = handler.getTypes(type);

		log.debug("GET returning {}", types.toString());
		return types;
	}

	@GET @Path("sum/{transaction_id:\\d+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Sum getSum(@PathParam("transaction_id") String transaction_id) {
		log.debug("GET sum/{}", transaction_id);

		Sum sum = new Sum();

		try {
			sum = new Sum(handler.getSum(Long.parseLong(transaction_id)));
		} catch (NumberFormatException e) {
			log.error("Unable to calculate sum for {}. Reason {}", transaction_id, e.getMessage());
		} catch ( Exception e) {
			e.printStackTrace();
		}

		log.debug("GET returning {}", sum.toString());
		return sum;
	}

	@PUT @Path("transaction/{transaction_id:\\d+}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public InsertStatus insertTransaction(@PathParam("transaction_id") String transaction_id, Transaction transaction) {
		boolean result = false;
		try {
			transaction.setId(Long.parseLong(transaction_id));
			log.debug("PUT transaction/{} {}", transaction_id, transaction);

			result = handler.insert(transaction);

		} catch (NumberFormatException e) {
			log.error("Unable to complete PUT transaction/{}. {}", transaction_id, e.getMessage());
		}

		Response.Status responseStatus = result ? Response.Status.OK : Response.Status.BAD_REQUEST;
		InsertStatus insertStatus = new InsertStatus(responseStatus.toString());

		log.debug("PUT returning {}", insertStatus);
		return insertStatus;
	}

}