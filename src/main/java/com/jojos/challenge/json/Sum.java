package com.jojos.challenge.json;

/**
 * The json representing the response to the sum request, e.g.
 * {"sum":10000}
 *
 * Created by karanikasg@gmail.com.
 */
public class Sum {
    private double sum;

    public Sum() {
    }

    public Sum(double sum) {
        this.sum = sum;
    }

    public double getSum() {
        return sum;
    }

    @Override
    public String toString() {
        return "Sum{" +
                "sum=" + sum +
                '}';
    }
}
