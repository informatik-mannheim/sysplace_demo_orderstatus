package de.hs_mannheim.carsalesmansbuddy;

import java.util.concurrent.ThreadLocalRandom;

public class Customer {

    private String mName, mMessage;
    private int mPercentage = 50;

    public Customer(String name) {
        mName = name;
        mPercentage = ThreadLocalRandom.current().nextInt(0, 99 );
    }

    public Customer(String name, int percentage, String message) {
        mName = name;
        mPercentage = percentage;
        mMessage = message;
    }


    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String mMessage) {
        this.mMessage = mMessage;
    }

    public int getPercentage() {
        return mPercentage;
    }

    public void setPercentage(int mPercentage) {
        this.mPercentage = mPercentage;
    }

    @Override
    public String toString() {
        return mName;
    }

}
