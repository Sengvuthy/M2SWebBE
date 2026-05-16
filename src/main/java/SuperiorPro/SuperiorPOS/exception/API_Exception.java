package SuperiorPro.SuperiorPOS.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class API_Exception extends RuntimeException{

	private final HttpStatus status;
	private final String message;
}
