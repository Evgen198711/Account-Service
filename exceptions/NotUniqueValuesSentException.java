package account.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "You should send payrolls with unique time periods")
public class NotUniqueValuesSentException extends RuntimeException{
}
