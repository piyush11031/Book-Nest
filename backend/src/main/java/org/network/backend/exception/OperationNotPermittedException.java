package org.network.backend.exception;

public class OperationNotPermittedException extends RuntimeException {
    public OperationNotPermittedException(String s) {
        super(s);
    }
}
