package account.exceptions;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

import java.io.IOException;
import java.time.format.DateTimeParseException;

@RestControllerAdvice
public class ExceptionController {
    @ExceptionHandler(ConstraintViolationException.class)
    public void handleConstraintViolationException(ConstraintViolationException exception,
                                                   ServletWebRequest webRequest) throws IOException {
        assert webRequest.getResponse() != null;
        webRequest.getResponse().sendError(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public void handleConstraintViolationException(DataIntegrityViolationException exception,
                                                   ServletWebRequest webRequest) throws IOException {
        assert webRequest.getResponse() != null;
        webRequest.getResponse().sendError(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
    }

    @ExceptionHandler(DateTimeParseException.class)
    public void handleConstraintViolationException(DateTimeParseException exception,
                                                   ServletWebRequest webRequest) throws IOException {
        assert webRequest.getResponse() != null;
        webRequest.getResponse().sendError(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public void handleConstraintViolationException(UsernameNotFoundException exception,
                                                   ServletWebRequest webRequest) throws IOException {
        assert webRequest.getResponse() != null;
        webRequest.getResponse().sendError(HttpStatus.NOT_FOUND.value(), exception.getMessage());
    }

    @ExceptionHandler(LockedException.class)
    public void handleConstraintViolationException(LockedException exception,
                                                   ServletWebRequest webRequest) throws IOException {
        assert webRequest.getResponse() != null;
        webRequest.getResponse().sendError(HttpStatus.LOCKED.value(), exception.getMessage());
    }
}


