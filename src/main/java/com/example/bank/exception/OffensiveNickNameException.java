package com.example.bank.exception;

public class OffensiveNickNameException extends RuntimeException {

    public OffensiveNickNameException() {
        super("The account nick name is not allowed");
    }
}
