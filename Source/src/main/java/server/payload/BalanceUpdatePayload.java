package server.payload;

import java.io.Serializable;

public class BalanceUpdatePayload implements Serializable {
    private static final long serialVersionUID = 1L;
    private double newBalance;

    public BalanceUpdatePayload(double newBalance) {
        this.newBalance = newBalance;
    }

    public double getNewBalance() {
        return newBalance;
    }
}
