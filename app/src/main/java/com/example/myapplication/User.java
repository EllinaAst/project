package com.example.myapplication;

public class User {
    public String firstName;
    public String lastName;

    // пустой конструктор нужен для Firebase
    public User() {}

    public User(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
