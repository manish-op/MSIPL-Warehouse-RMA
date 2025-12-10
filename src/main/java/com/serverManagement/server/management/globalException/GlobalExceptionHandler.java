package com.serverManagement.server.management.globalException;

import java.io.FileNotFoundException;
import java.sql.SQLException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;



@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> exception(Exception ex) {

//		GlobalExceptionResponse response = new GlobalExceptionResponse(
//				String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), ex.getMessage());

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
	}

	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<?> nullPointerException(NullPointerException ex) {

//		GlobalExceptionResponse response = new GlobalExceptionResponse(
//				String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), ex.getMessage());

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<?> runtimeException(RuntimeException ex) {

//		GlobalExceptionResponse response = new GlobalExceptionResponse(
//				String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), ex.getMessage());

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
	}

//	@ExceptionHandler(NumberFormatException.class)
//	public ResponseEntity<GlobalExceptionResponse> numberFormatException(NumberFormatException ex) {
//
//		GlobalExceptionResponse response = new GlobalExceptionResponse(
//				String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), ex.getMessage());
//
//		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//	}

//	@ExceptionHandler(IllegalArgumentException.class)
//	public ResponseEntity<GlobalExceptionResponse> illegalArgumentException(IllegalArgumentException ex) {
//
//		GlobalExceptionResponse response = new GlobalExceptionResponse(
//				String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), ex.getMessage());
//
//		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//	}

//	@ExceptionHandler(IOException.class)
//	public ResponseEntity<GlobalExceptionResponse> iOException(IOException ex) {
//
//		GlobalExceptionResponse response = new GlobalExceptionResponse(
//				String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), ex.getMessage());
//
//		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//	}
//
//	@ExceptionHandler(NoSuchFieldException.class)
//	public ResponseEntity<GlobalExceptionResponse> noSuchFieldException(NoSuchFieldException ex) {
//
//		GlobalExceptionResponse response = new GlobalExceptionResponse(
//				String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), ex.getMessage());
//
//		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//	}

//	@ExceptionHandler(ArithmeticException.class)
//	public ResponseEntity<GlobalExceptionResponse> arithmeticException(ArithmeticException ex) {
//
//		GlobalExceptionResponse response = new GlobalExceptionResponse(
//				String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), ex.getMessage());
//
//		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//	}
//
//	@ExceptionHandler(ArrayIndexOutOfBoundsException.class)
//	public ResponseEntity<GlobalExceptionResponse> arrayIndexOutOfBoundsException(ArrayIndexOutOfBoundsException ex) {
//
//		GlobalExceptionResponse response = new GlobalExceptionResponse(
//				String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), ex.getMessage());
//
//		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//	}

	@ExceptionHandler(ClassNotFoundException.class)
	public ResponseEntity<?> classNotFoundException(ClassNotFoundException ex) {

//		GlobalExceptionResponse response = new GlobalExceptionResponse(String.valueOf(HttpStatus.NOT_FOUND.value()),
//				ex.getMessage());

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	}

	@ExceptionHandler(FileNotFoundException.class)
	public ResponseEntity<?> fileNotFoundException(FileNotFoundException ex) {

//		GlobalExceptionResponse response = new GlobalExceptionResponse(String.valueOf(HttpStatus.NOT_FOUND.value()),
//				ex.getMessage());

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	}

	@ExceptionHandler(InterruptedException.class)
	public ResponseEntity<?> interruptedException(InterruptedException ex) {

//		GlobalExceptionResponse response = new GlobalExceptionResponse(
//				String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), ex.getMessage());

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
	}

	@ExceptionHandler(StringIndexOutOfBoundsException.class)
	public ResponseEntity<?> stringIndexOutOfBoundsException(StringIndexOutOfBoundsException ex) {

//		GlobalExceptionResponse response = new GlobalExceptionResponse(
//				String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), ex.getMessage());

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<?> illegalStateException(IllegalStateException ex) {

//		GlobalExceptionResponse response = new GlobalExceptionResponse(
//				String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), ex.getMessage());

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
	}

	@ExceptionHandler(NoSuchMethodException.class)
	public ResponseEntity<?> noSuchMethodException(NoSuchMethodException ex) {

//		GlobalExceptionResponse response = new GlobalExceptionResponse(
//				String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), ex.getMessage());

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
	}

	@ExceptionHandler(SQLException.class)
	public ResponseEntity<?> sqlException(SQLException ex) {

//		GlobalExceptionResponse response = new GlobalExceptionResponse(
//				String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), ex.getMessage());

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
	}

	
	@ExceptionHandler(NullValueException.class)
	public ResponseEntity<?> NullValueException(NullValueException ex) {

//		GlobalExceptionResponse response = new GlobalExceptionResponse(
//				String.valueOf(HttpStatus.BAD_REQUEST.value()), ex.getMessage());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
	}
	
	@ExceptionHandler(UnathorizedException.class)
	public ResponseEntity<?> UnathorizedException(UnathorizedException ex) {

//		GlobalExceptionResponse response = new GlobalExceptionResponse(
//				String.valueOf(HttpStatus.UNAUTHORIZED.value()), ex.getMessage());

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
	}
	
	@ExceptionHandler(DuplicateValueException.class)
	public ResponseEntity<?> DuplicateValueException(DuplicateValueException ex) {

//		GlobalExceptionResponse response = new GlobalExceptionResponse(
//				String.valueOf(HttpStatus.BAD_REQUEST.value()), ex.getMessage());
//		String message=ex.getMessage();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
	}

}
