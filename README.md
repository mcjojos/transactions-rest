## Synopsis

A simple RESTful web service that stores some transactions in memory and returns information about those transactions.

The transactions to be stored have a type and an amount. The service should support returning all transactions of a type.
Also, transactions can be linked to each other (using a "parent_id").
We need to know the total amount involved for all transactions linked to a particular transaction.

## Requirements

You'll need Java 8 to compile and run the application. You'll also need maven to build it.

## Build
mvn package

## How do I run it?

mvn exec:java

Alternatively you can run it following these steps

Build the application with 

mvn package

Then get the *-SNAPSHOT-jar-with-dependencies.jar which is under the target directory and copy it to your running directory.
Run the jar file from the console issuing the following command:

java -jar transactions-rest-VERSION-jar-with-dependencies.jar

## In detail the api spec looks like the following:
  
  PUT /transactionservice/transaction/$transaction_id   
  Body:   
  { "amount":double,"type":string,"parent_id":long } 
  
where: 
  transaction_id is a long specifying a new transaction
  amount is a double specifying the amount
  type is a string specifying a type of the transaction.
  parent_id is an optional long that may specify the parent transaction of this transaction. 
  
  GET /transactionservice/transaction/$transaction_id 
Returns: 
  { "amount":double,"type":string,"parent_id":long } 
 
  GET /transactionservice/types/$type 
Returns: 
  [ long, long, .... ] 
  A json list of all transaction ids that share the same type $type.
  
  GET /transactionservice/sum/$transaction_id 
Returns:
  { "sum", double }
  A sum of all transactions that are transitively linked by their parent_id to $transaction_id.
  
# Some simple examples would be: 
  
  PUT /transactionservice/transaction/10 { "amount": 5000, "type": "cars" } 
  => { "status": "ok" } 
  
  PUT /transactionservice/transaction/11 { "amount": 10000, "type": "shopping", "parent_id": 10 } 
  => { "status": "ok" } 
  
  GET /transactionservice/types/cars => [10] 
  
  GET /transactionservice/sum/10 => {"sum":15000} 
  
  GET /transactionservice/sum/11 => {"sum":10000}
  
A simple way to test the application is to use curl (check https://curl.haxx.se/)
You can find some usage examples of the tool specifically for our application under examples/curl_usage_examples.txt

ENJOY!


## Asymptotic Behaviour

Insert a transaction: O(N) just because we allow a transaction with a parent_id referencing to a non-existing transaction.
If we weren't allowing this condition we'd have O(1).

Retrieve a transaction:
O(1)

Get a list of all transaction ids that share the same specific type:
O(1) because we cache the type on insertion

Get the sum of all transactions that are transitively linked by their parent_id:
O(N) worst case. A simple cache stores each sum upon retrieval. The whole cache is invalidated on any insert

## License

The address comprehension tool is made available under the terms of the Berkeley Software Distribution (BSD) license. This allow you complete freedom to use and distribute the code in source and/or binary form as long as you respect the original copyright.
Please see the LICENCE file for exact terms.