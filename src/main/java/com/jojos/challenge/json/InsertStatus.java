package com.jojos.challenge.json;

/**
 * The json object representing the response to the insert request
 *
 * It should return the status as sting eg:
 * { "status": "ok" }
 *
 * @author karanikasg@gmail.com.
 */
public class InsertStatus {

	private String status;

	public InsertStatus() {
	}

	public InsertStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return "InsertStatus{" +
				"status='" + status + '\'' +
				'}';
	}
}
