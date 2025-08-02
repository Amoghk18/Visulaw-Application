package com.visulaw.legal_service.exceptions;

public class NoDocumentFoundException extends RuntimeException {
    public NoDocumentFoundException() {
        super();
    }

    public NoDocumentFoundException(String message) {
        super(message);
    }
}
