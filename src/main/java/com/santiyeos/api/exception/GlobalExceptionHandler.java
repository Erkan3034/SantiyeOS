package com.santiyeos.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(
                LocalDateTime.now(),
                exception.getStatus().value(),
                exception.getStatus().getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                List.of()
        );

        return ResponseEntity.status(exception.getStatus()).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<ValidationError> validationErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationError(error.getField(), error.getDefaultMessage()))
                .toList();

        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validasyon hatası.",
                request.getRequestURI(),
                validationErrors
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiError> handleMissingHeaderException(
            MissingRequestHeaderException exception,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Zorunlu header eksik: " + exception.getHeaderName(),
                request.getRequestURI(),
                List.of()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error("Beklenmeyen hata. Path: {}", request.getRequestURI(), exception);

        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Beklenmeyen bir hata oluştu.",
                request.getRequestURI(),
                List.of()
        );

        return ResponseEntity.internalServerError().body(error);
    }


    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDataAccessException(
            DataAccessException exception,
            HttpServletRequest request
    ) {
        log.error("Veritabani hatasi. Path: {}", request.getRequestURI(), exception);

        HttpStatus status = resolveDatabaseStatus(exception);
        String message = resolveDatabaseMessage(exception);

        ApiError error = new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                List.of()
        );

        return ResponseEntity.status(status).body(error);
    }

    private HttpStatus resolveDatabaseStatus(DataAccessException exception) {
        if (exception instanceof DataAccessResourceFailureException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }

        if (exception instanceof DuplicateKeyException) {
            return HttpStatus.CONFLICT;
        }

        if (exception instanceof DataIntegrityViolationException) {
            return HttpStatus.CONFLICT;
        }

        return HttpStatus.BAD_REQUEST;
    }

    private String resolveDatabaseMessage(DataAccessException exception) {
        Throwable rootCause = exception.getMostSpecificCause();

        if (rootCause != null && rootCause.getMessage() != null) {
            return cleanDatabaseMessage(rootCause.getMessage());
        }

        return "Veritabani islemi tamamlanamadi.";
    }

    private String cleanDatabaseMessage(String message) {
        if (message.contains("Firma bulunamadi")) {
            return "Firma bulunamadı veya pasif.";
        }

        if (message.contains("Taseron adi zorunludur")) {
            return "Taşeron adı zorunludur.";
        }


        if (message.contains("Bu vergi numarasi")) {
            return "Bu vergi numarası ile kayıtlı aktif taşeron zaten var.";
        }

        if (message.contains("Yetkisiz islem")) {
            return "Bu işlem için yetkiniz yok.";
        }

        if (message.contains("Taseron bulunamadi")) {
            return "Taşeron bulunamadı.";
        }

        if (message.contains("Aktif is emirleri olan taseron silinemez")) {
            return "Aktif iş emirleri olan taşeron silinemez.";
        }

        if (message.contains("Aktif is emirleri olan proje silinemez")) {
            return "Aktif iş emirleri olan proje silinemez.";
        }

        return "Veritabani islemi tamamlanamadi.";
    }


}
