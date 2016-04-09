package com.jojos.challenge.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;

/**
 * Class/json object that represents the transaction data.
 *
 * @author g.karanikas@iontrading.com.
 */
public class Transaction {

	private long id;
	private double amount;
	private String type;

	@JsonProperty("parent_id")
	private long parentId;

	@JsonIgnore
	private Set<Long> children = new HashSet<>();

	public Transaction() {
	}

	public Transaction(double amount, String type, long parentId) {
		this.amount = amount;
		this.type = type;
		this.parentId = parentId;
	}

	public long getId() {
		return id;
	}

	public double getAmount() {
		return amount;
	}

	public String getType() {
		return type;
	}

	public long getParentId() {
		return parentId;
	}

	public Set<Long> getChildren() {
		return children;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setChildren(Set<Long> children) {
		this.children = children;
	}

	public void addChild(long childId) {
		children.add(childId);
	}

	public boolean removeChild(long childId) {
		return children.remove(childId);
	}

	@Override
	public String toString() {
		return "Transaction{" +
				"id=" + id +
				", amount=" + amount +
				", type='" + type + '\'' +
				", parent_id=" + parentId +
				'}';
	}

}
