package com.example.demo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое при ошибках в работе с AI сервисом
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class AIServiceException extends RuntimeException {
    
    public AIServiceException(String message) {
        super(message);
    }
    
    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
