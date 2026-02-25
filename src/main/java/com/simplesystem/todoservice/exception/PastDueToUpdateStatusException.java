package com.simplesystem.todoservice.exception;

public class PastDueToUpdateStatusException extends RuntimeException {

    public PastDueToUpdateStatusException(Long id) {
        super("Status for todo item with id '" + id + "' can not be updated to 'past_due' via the respective endpoints");
    }
}
