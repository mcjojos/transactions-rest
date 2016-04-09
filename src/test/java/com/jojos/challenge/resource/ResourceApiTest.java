package com.jojos.challenge.resource;

import com.jojos.challenge.json.InsertStatus;
import com.jojos.challenge.json.Sum;
import com.jojos.challenge.json.Transaction;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * Testing our resource api. Also the {@link com.jojos.challenge.transact.TransactionHandler} is tested to some extend
 *
 * @implNote In this unit test order matters! The order in which the tests are executed are important,
 * ie the insertions must be made before checking for sums, types etc.
 * The pattern followed is test1XXX, test2XXX, etc
 *
 * Created by karanikasg@gmail.com.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ResourceApiTest {

    private static final Server server = new Server();

    private static Client client;
    private static WebTarget rootWebTarget;
    private static final List<TargetAndTransaction> targetAndTransactions = new ArrayList<>();

    @BeforeClass
    public static void setUp() {
        server.start();
        client = ClientBuilder.newClient().register(JacksonFeature.class);

        rootWebTarget = client.target(server.getURI()).path("/transactionservice");

        createDummyTransactions();
    }

    @AfterClass
    public static void cleanUp() {
        client.close();
        server.stop();
    }

    @Test
    public void test1InsertTransactions() {
        targetAndTransactions.forEach(targetAndTransaction -> {
            Response response = targetAndTransaction.transactionWT.request(MediaType.APPLICATION_JSON_TYPE).
                    put(Entity.entity(targetAndTransaction.transaction, MediaType.APPLICATION_JSON_TYPE));

            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals("OK", response.readEntity(InsertStatus.class).getStatus());
        });
    }

    @Test
    public void test2GetTransactions() {
        targetAndTransactions.forEach(targetAndTransaction -> {
            Response response = targetAndTransaction.transactionWT.request(MediaType.APPLICATION_JSON_TYPE).get();

            Assert.assertEquals(200, response.getStatus());
            double delta = 0.001d;
            Transaction transaction = targetAndTransaction.transaction;
            Transaction returnedTransaction = response.readEntity(Transaction.class);
            Assert.assertEquals(transaction.getAmount(), returnedTransaction.getAmount(), delta);
            Assert.assertEquals(transaction.getParentId(), returnedTransaction.getParentId());
            Assert.assertEquals(transaction.getType(), returnedTransaction.getType());
        });
    }

    @Test
    public void test3GetTypes() {
        targetAndTransactions.forEach(targetAndTransaction -> {
            Response response = targetAndTransaction.typesWT.request(MediaType.TEXT_PLAIN).get();

            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals(getIdsForType(targetAndTransaction.transaction.getType()).toString(), response.readEntity(String.class));
        });
    }

    @Test
    public void test4GetSums() {
        targetAndTransactions.forEach(targetAndTransaction -> {
            Response response = targetAndTransaction.sumWT.request(MediaType.APPLICATION_JSON_TYPE).get();

            Assert.assertEquals(200, response.getStatus());
            Sum sum = response.readEntity(Sum.class);
            Assert.assertEquals(getSumForId(targetAndTransaction.id), sum.getSum(), 0.01d);
        });
    }

    private List<Long> getIdsForType(String type) {
        List<Long> ids = new ArrayList<>();

        targetAndTransactions.forEach(targetAndTransaction -> {
            if (targetAndTransaction.transaction.getType().equals(type)) {
                ids.add(targetAndTransaction.id);
            }
        });
        return ids;
    }

    private double getSumForId(long id) {
        DoubleAdder result = new DoubleAdder();

        targetAndTransactions.forEach(targetAndTransaction -> {
            if (targetAndTransaction.id == id) {
                result.add(targetAndTransaction.transaction.getAmount());
                result.add(getAmountForParent(targetAndTransaction.id));
            }
        });
        return result.sum();
    }

    private double getAmountForParent(long parentId) {
        for (TargetAndTransaction targetAndTransaction : targetAndTransactions) {
            if (targetAndTransaction.transaction.getParentId() == parentId) {
                return targetAndTransaction.transaction.getAmount() + getAmountForParent(targetAndTransaction.id);
            }
        }
        return 0d;
    }

    private static final class TargetAndTransaction {
        private final WebTarget transactionWT;
        private final WebTarget typesWT;
        private final WebTarget sumWT;
        private final Transaction transaction;
        private final long id;

        public TargetAndTransaction(WebTarget rootWebTarget, long id, Transaction transaction) {
            this.transactionWT = rootWebTarget.path("transaction/" + id);
            this.typesWT = rootWebTarget.path("types/" + transaction.getType());
            this.sumWT = rootWebTarget.path("sum/" + id);
            this.transaction = transaction;
            this.id = id;
        }
    }

    private static void createDummyTransactions() {
        // alternatively we could have mocked our transactions
        targetAndTransactions.add(new TargetAndTransaction(rootWebTarget, 10L, new Transaction(10000d, "cars", 0L)));
        targetAndTransactions.add(new TargetAndTransaction(rootWebTarget, 11L, new Transaction(15000d, "shopping", 10L)));
        targetAndTransactions.add(new TargetAndTransaction(rootWebTarget, 12L, new Transaction(20000d, "keys", 11L)));
        targetAndTransactions.add(new TargetAndTransaction(rootWebTarget, 13L, new Transaction(25000d, "furniture", 12L)));
        targetAndTransactions.add(new TargetAndTransaction(rootWebTarget, 14L, new Transaction(30000d, "keys", 13L)));
        targetAndTransactions.add(new TargetAndTransaction(rootWebTarget, 15L, new Transaction(40000d, "cars", 14L)));
        targetAndTransactions.add(new TargetAndTransaction(rootWebTarget, 16L, new Transaction(50000d, "cars", 15L)));
    }

}