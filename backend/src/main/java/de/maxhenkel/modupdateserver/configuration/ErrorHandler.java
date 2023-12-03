package de.maxhenkel.modupdateserver.configuration;

import de.maxhenkel.modupdateserver.entities.Error;
import jakarta.servlet.ServletException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(ResponseStatusException.class)
    protected ResponseEntity<Error> handleResponseStatusException(ResponseStatusException e) {
        Error error = new Error();
        error.setStatusCode(e.getStatusCode().value());
        error.setMessage(e.getReason());
        return new ResponseEntity<>(error, e.getStatusCode());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Error> handleNoHandlerFound(NoHandlerFoundException e) {
        Error error = new Error();
        error.setStatusCode(e.getStatusCode().value());
        error.setMessage(e.getMessage());
        return new ResponseEntity<>(error, e.getStatusCode());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Error> handleNoResourceFound(NoResourceFoundException e) {
        Error error = new Error();
        error.setStatusCode(e.getStatusCode().value());
        error.setMessage(e.getMessage());
        return new ResponseEntity<>(error, e.getStatusCode());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Error> handleRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        Error error = new Error();
        error.setStatusCode(e.getStatusCode().value());
        error.setMessage(e.getMessage());
        return new ResponseEntity<>(error, e.getStatusCode());
    }

    @ExceptionHandler(ServletException.class)
    public ResponseEntity<Error> handleGeneralError(ServletException e) {
        Error error = new Error();
        error.setStatusCode(500);
        error.setMessage(e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
