package org.example;

import java.util.ArrayList;
import java.util.List;


public class Settings {
    private double dailyAllowanceRate = 15.0;
    private double mileageRate = 0.3;
    private double totalAmount = 0.0;
    private List<String> receipts = new ArrayList<>();

     public void setDailyAllowanceRate(double rate) {
        this.dailyAllowanceRate = rate;
    }

    public void setMileageRate(double rate) {
        this.mileageRate = rate;
    }

    public double calculateTotalReimbursement(int days, double distance) {
        return totalAmount + (days * dailyAllowanceRate) + (distance * mileageRate);
    }
    public void addReceipt(String receiptType, double amount) {
        receipts.add(receiptType);
        totalAmount += amount;
    }
}