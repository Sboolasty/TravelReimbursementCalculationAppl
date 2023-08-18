package org.example;

import java.util.ArrayList;
import java.util.List;

public class Admin {
    private List<String> receiptTypes = new ArrayList<>();

    public List<String> getReceiptTypes() {
        return receiptTypes;
    }
    public void addReceiptType(String type) {
        receiptTypes.add(type);
    }

    public void removeReceiptType(String type) {
        receiptTypes.remove(type);
    }


}