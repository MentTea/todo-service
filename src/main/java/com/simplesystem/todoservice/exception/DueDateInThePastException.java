package com.simplesystem.todoservice.exception;

public class DueDateInThePastException extends RuntimeException {

    public DueDateInThePastException() {
        super("Due date must not be in the past");
    }
}
