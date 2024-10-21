package com.example;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Shoe {
    private final SimpleStringProperty shoeId;
    private final SimpleStringProperty shoeName;
    private final SimpleIntegerProperty shoePrice;
    private final SimpleStringProperty shoeDescription;

    // INITIALIZATOR SHOE CLASS
    public Shoe(String shoeId, String shoeName, int shoePrice, String shoeDescription) {
        this.shoeId = new SimpleStringProperty(shoeId);
        this.shoeName = new SimpleStringProperty(shoeName);
        this.shoePrice = new SimpleIntegerProperty(shoePrice);
        this.shoeDescription = new SimpleStringProperty(shoeDescription);
    }

    // GETTER
    public String getShoeId() {
        return shoeId.get();
    }

    public String getShoeName() {
        return shoeName.get();
    }

    public int getShoePrice() {
        return shoePrice.get();
    }

    public String getShoeDescription() {
        return shoeDescription.get();
    }
}

