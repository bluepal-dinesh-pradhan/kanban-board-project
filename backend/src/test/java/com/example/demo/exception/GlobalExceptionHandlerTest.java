package com.example.demo.exception;

import com.example.demo.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<ApiResponse<Void>> response = handler.handleNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody().getMessage());
    }

    @Test
    void testHandleBadRequest() {
        BadRequestException ex = new BadRequestException("Bad request");
        ResponseEntity<ApiResponse<Void>> response = handler.handleBadRequest(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad request", response.getBody().getMessage());
    }

    @Test
    void testHandleRuntime() {
        RuntimeException ex = new RuntimeException("Runtime");
        ResponseEntity<ApiResponse<Void>> response = handler.handleRuntime(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Runtime", response.getBody().getMessage());
    }

    @Test
    void testHandleBadCredentials() {
        BadCredentialsException ex = new BadCredentialsException("Bad creds");
        ResponseEntity<ApiResponse<Void>> response = handler.handleBadCredentials(ex);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password", response.getBody().getMessage());
    }

    @Test
    void testHandleValidation() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult result = mock(BindingResult.class);
        FieldError error = new FieldError("obj", "field", "must not be null");
        
        when(ex.getBindingResult()).thenReturn(result);
        when(result.getFieldErrors()).thenReturn(List.of(error));

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("field: must not be null"));
    }
    
    @Test
    void testHandleValidationEmpty() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult result = mock(BindingResult.class);
        
        when(ex.getBindingResult()).thenReturn(result);
        when(result.getFieldErrors()).thenReturn(List.of());

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().getMessage());
    }
}
