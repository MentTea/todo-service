package com.simplesystem.todoservice.exception;

public class PastDueModificationNotAllowedException extends RuntimeException {

    public PastDueModificationNotAllowedException(Long id) {
        super("Cannot modify past due todo item with id " + id);
    }
}
