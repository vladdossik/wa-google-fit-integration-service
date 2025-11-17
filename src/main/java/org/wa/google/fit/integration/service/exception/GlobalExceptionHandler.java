package org.wa.google.fit.integration.service.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.wa.google.fit.integration.service.mapper.ErrorMapper;
import org.wa.google.fit.integration.service.model.ErrorResponse;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ErrorMapper errorMapper;

    @ExceptionHandler(ParseTokenException.class)
    public ResponseEntity<ErrorResponse> handleParseTokenException(ParseTokenException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTokenNotFoundException(TokenNotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(GoogleFitException.class)
    public ResponseEntity<ErrorResponse> handleGoogleFitException(GoogleFitException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildResponse(String message, HttpStatus status) {
        return ResponseEntity
                .status(status)
                .body(errorMapper.toErrorResponse(message, status));
    }
}
