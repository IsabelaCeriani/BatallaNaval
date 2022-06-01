package dpoi.BatallaNaval.exceptionHandler;

import dpoi.BatallaNaval.exception.NotValidTokenException;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class DefaultExceptionHandler {

    Logger logger = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    @ExceptionHandler(NotValidTokenException.class)
    public ResponseEntity<?> handlePostNotFoundException(NotValidTokenException exception) {
        logger.info(exception.getMessage());
        return new ResponseEntity(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleValidationExceptions(ConstraintViolationException exception) {
        logger.info(exception.getMessage());
        return ResponseEntity.badRequest().build();
    }
}