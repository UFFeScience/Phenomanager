
package com.uff.phenomanager.controller.core.advice;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.uff.phenomanager.Constants.MSG_ERROR;
import com.uff.phenomanager.exception.BadRequestApiException;
import com.uff.phenomanager.exception.InternalErrorApiException;
import com.uff.phenomanager.exception.NotFoundApiException;
import com.uff.phenomanager.exception.UnauthorizedApiException;

@RestControllerAdvice
public class ExceptionHandlerControllerAdvice {
	
	private static Logger log = LoggerFactory.getLogger(ExceptionHandlerControllerAdvice.class);

    @ExceptionHandler(NotFoundApiException.class)
    public ResponseEntity<Map<String, String>> notFound(Exception exception) {
    	log.error(exception.getMessage(), exception);
        return new ResponseEntity<>(Collections.singletonMap(MSG_ERROR.KEY, exception.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ BadRequestApiException.class, HttpMediaTypeNotSupportedException.class, 
    	DataIntegrityViolationException.class, HttpRequestMethodNotSupportedException.class, 
    	InvalidDataAccessApiUsageException.class, NumberFormatException.class,
    	MissingServletRequestParameterException.class, MissingPathVariableException.class,
    	HttpMessageNotReadableException.class })
    public ResponseEntity<Map<String, String>> badRequest(HttpServletRequest req, Exception exception) {
        log.error(exception.getMessage(), exception);
        return new ResponseEntity<>(Collections.singletonMap(MSG_ERROR.KEY, exception.getMessage()), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler({ AccessDeniedException.class })
    public ResponseEntity<Map<String, String>> forbidden(HttpServletRequest req, Exception exception) {
    	log.error(exception.getMessage(), exception);
        return new ResponseEntity<>(Collections.singletonMap(MSG_ERROR.KEY, exception.getMessage()), HttpStatus.FORBIDDEN);
    }
    
    @ExceptionHandler({ AuthenticationException.class, AuthenticationCredentialsNotFoundException.class, 
    	UnauthorizedApiException.class })
    public ResponseEntity<Map<String, String>> unauthorized(HttpServletRequest req, Exception exception) {
    	log.error(exception.getMessage(), exception);
        return new ResponseEntity<>(Collections.singletonMap(MSG_ERROR.KEY, exception.getMessage()), HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler({ Exception.class, InternalErrorApiException.class })
    public ResponseEntity<Map<String, String>>exception(HttpServletRequest req, Exception exception) {
    	log.error(exception.getMessage(), exception);
        return new ResponseEntity<>(Collections.singletonMap(MSG_ERROR.KEY, MSG_ERROR.INTERNAL_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
}