package de.maxhenkel.modupdateserver.exceptionhandlers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class ResponseStatusExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Error> generateNotFoundException(ResponseStatusException e) {
        return new ResponseEntity<>(new Error(e.getReason()), e.getStatus());
    }

}
