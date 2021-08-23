package com.fikarnot.ui.main;

public class AccountsModel {
    private String title;
    private String username;
    private String password;

    public AccountsModel(String title, String username, String password) {
        this.title = title;
        this.username = username;
        this.password = password;
    }

    public AccountsModel(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
