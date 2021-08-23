package com.fikarnot.ui.profile;

public class DataModel {
    private String name;
    private String pin;
    private String uri;

    public DataModel(String name, String pin, String uri) {
        this.name = name;
        this.pin = pin;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}

