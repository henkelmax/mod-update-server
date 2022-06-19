package de.maxhenkel.modupdateserver.exceptionhandlers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class ResponseStatusExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<JsonResponse> generateNotFoundException(ResponseStatusException e) {
        return new ResponseEntity<>(new JsonResponse(e.getReason()), e.getStatus());
    }

    private static class JsonResponse {
        private String message;

        public JsonResponse() {
        }

        public JsonResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}
