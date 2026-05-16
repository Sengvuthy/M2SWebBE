package SuperiorPro.SuperiorPOS.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends API_Exception{

	public ResourceNotFoundException(String resourceName, String name) {
		super(HttpStatus.NOT_FOUND, String.format("%s with name = %s is not found", resourceName, name));
	}
}
