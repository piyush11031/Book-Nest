package org.network.backend.handler;

import jakarta.mail.MessagingException;
import org.network.backend.exception.OperationNotPermittedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashSet;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ExceptionResponse> handleException(LockedException exp){
        return ResponseEntity.
                status(HttpStatus.UNAUTHORIZED)
                .body(ExceptionResponse.builder()
                        .businessErrorCode(BusinessErrorCodes.ACCOUNT_LOCKED.getCode())
                        .businessErrorDescription(BusinessErrorCodes.ACCOUNT_LOCKED.getDescription())
                        .error(exp.getMessage())
                        .build());
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ExceptionResponse> handleException(DisabledException exp){
        return ResponseEntity.
                status(HttpStatus.UNAUTHORIZED)
                .body(ExceptionResponse.builder()
                        .businessErrorCode(BusinessErrorCodes.ACCOUNT_DISABLED.getCode())
                        .businessErrorDescription(BusinessErrorCodes.ACCOUNT_DISABLED.getDescription())
                        .error(exp.getMessage())
                        .build());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleException(BadCredentialsException exp){
        return ResponseEntity.
                status(HttpStatus.UNAUTHORIZED)
                .body(ExceptionResponse.builder()
                        .businessErrorCode(BusinessErrorCodes.BAD_CREDENTIALS.getCode())
                        .businessErrorDescription(BusinessErrorCodes.BAD_CREDENTIALS.getDescription())
                        .error(BusinessErrorCodes.BAD_CREDENTIALS.getDescription())
                        .build());
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ExceptionResponse> handleException(MessagingException exp){
        return ResponseEntity.
                status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExceptionResponse.builder()
                        .error(exp.getMessage())
                        .build());
    }

    //@Valid will throw this if AuthenticationRequest fields are not valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleException(MethodArgumentNotValidException exp){
        //Contains error messages
        Set<String> errors = new HashSet<>();

        exp.getBindingResult().getAllErrors() //duplicate error messages are not included
                .forEach(error -> {
                    var errorMsg = error.getDefaultMessage();
                    errors.add(errorMsg);
                });
        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponse.builder()
                        .validationErrors(errors)
                        .build());
    }

    @ExceptionHandler(OperationNotPermittedException.class)
    public ResponseEntity<ExceptionResponse> handleException(OperationNotPermittedException exp){
        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponse.builder()
                        .error(exp.getMessage())
                        .build());
    }

    //For any other unhandled exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception exp){
        //log the exception
        exp.printStackTrace();
        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponse.builder()
                        .businessErrorDescription("Internal error, contact the admin")
                        .error(exp.getMessage())
                        .build());
    }
}
