package de.maxhenkel.modupdateserver.configuration;

import de.maxhenkel.modupdateserver.entities.Error;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    protected ResponseEntity<Error> handleEntityNotFound(ResponseStatusException ex) {
        Error error = new Error();
        error.setStatusCode(ex.getStatusCode().value());
        error.setMessage(ex.getReason());
        return new ResponseEntity<>(error, ex.getStatusCode());
    }

}
