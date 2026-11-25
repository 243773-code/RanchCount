package com.ranchcount.api.exception;

import com.ranchcount.api.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 400 - Errores de validacion Jakarta (@Valid en DTOs de entrada)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ApiErrorResponse response = ApiErrorResponse.ofValidation(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Error de validacion en los datos de entrada",
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 400 - Violaciones de restricciones en parametros (@RequestParam / @PathVariable)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v ->
                errors.put(v.getPropertyPath().toString(), v.getMessage())
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Parametros de consulta invalidos",
                request.getRequestURI(),
                errors
        );
    }

    // 400 - Reglas de negocio, JSON malformado o tipos incompatibles
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRuleException(
            BusinessRuleException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "El cuerpo JSON de la peticion tiene formato invalido o contiene valores enum/fecha no reconocidos",
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequestParameters(
            Exception ex,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Parametro de entrada invalido o faltante: " + ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    // 401 - No autenticado o credenciales invalidas
    @ExceptionHandler({BadCredentialsException.class, DisabledException.class, AuthenticationException.class})
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        String message;
        if (ex instanceof BadCredentialsException) {
            message = "Usuario o contrasena incorrectos";
        } else if (ex instanceof DisabledException) {
            message = "La cuenta de usuario se encuentra desactivada";
        } else {
            message = "No autenticado: " + ex.getMessage();
        }
        return buildResponse(HttpStatus.UNAUTHORIZED, message, request.getRequestURI(), null);
    }

    // 403 - Permisos insuficientes (RBAC ROLE_OWNER vs ROLE_OPERATOR)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "Acceso denegado: su rol actual no cuenta con permisos para realizar esta operacion",
                request.getRequestURI(),
                null
        );
    }

    // 404 - Recurso o ruta no encontrada
    @ExceptionHandler({ResourceNotFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleNotFoundException(
            Exception ex,
            HttpServletRequest request) {
        String msg = ex instanceof ResourceNotFoundException
                ? ex.getMessage()
                : "El endpoint solicitado no existe: " + request.getRequestURI();
        return buildResponse(HttpStatus.NOT_FOUND, msg, request.getRequestURI(), null);
    }

    // 409 - Duplicidad de registros unicos (arete, username, codigo de lote)
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI(), null);
    }

    // 502 - Fallo al consumir APIs externas (Open-Meteo / CallMeBot)
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ApiErrorResponse> handleExternalApiException(
            ExternalApiException ex,
            HttpServletRequest request) {
        log.error("Error en integracion con API externa: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_GATEWAY, ex.getMessage(), request.getRequestURI(), null);
    }

    // 500 - Error interno inesperado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request) {
        log.error("Error interno no controlado en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrio un error interno inesperado en el servidor",
                request.getRequestURI(),
                null
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> validationErrors) {

        ApiErrorResponse body = validationErrors == null
                ? ApiErrorResponse.of(status.value(), status.getReasonPhrase(), message, path)
                : ApiErrorResponse.ofValidation(status.value(), status.getReasonPhrase(), message, path, validationErrors);

        return ResponseEntity.status(status).body(body);
    }
}
