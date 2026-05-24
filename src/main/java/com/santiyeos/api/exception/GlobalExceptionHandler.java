package com.santiyeos.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
        ApiError error = buildError(
                exception.getStatus(),
                exception.getMessage(),
                request,
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

        ApiError error = buildError(
                HttpStatus.BAD_REQUEST,
                "Validasyon hatasi.",
                request,
                validationErrors
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> handleBindException(
            BindException exception,
            HttpServletRequest request
    ) {
        List<ValidationError> validationErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationError(error.getField(), error.getDefaultMessage()))
                .toList();

        ApiError error = buildError(
                HttpStatus.BAD_REQUEST,
                "Validasyon hatasi.",
                request,
                validationErrors
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolationException(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<ValidationError> validationErrors = exception.getConstraintViolations()
                .stream()
                .map(error -> new ValidationError(error.getPropertyPath().toString(), error.getMessage()))
                .toList();

        ApiError error = buildError(
                HttpStatus.BAD_REQUEST,
                "Validasyon hatasi.",
                request,
                validationErrors
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiError> handleMissingHeaderException(
            MissingRequestHeaderException exception,
            HttpServletRequest request
    ) {
        ApiError error = buildError(
                HttpStatus.BAD_REQUEST,
                "Zorunlu header eksik: " + exception.getHeaderName(),
                request,
                List.of()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParameterException(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        ApiError error = buildError(
                HttpStatus.BAD_REQUEST,
                "Zorunlu parametre eksik: " + exception.getParameterName(),
                request,
                List.of()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatchException(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        ApiError error = buildError(
                HttpStatus.BAD_REQUEST,
                "Parametre tipi hatali: " + exception.getName(),
                request,
                List.of()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMessageNotReadableException(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        ApiError error = buildError(
                HttpStatus.BAD_REQUEST,
                "Istek govdesi okunamadi. JSON formatini kontrol edin.",
                request,
                List.of()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler({
            NoHandlerFoundException.class,
            NoResourceFoundException.class
    })
    public ResponseEntity<ApiError> handleEndpointNotFoundException(
            Exception exception,
            HttpServletRequest request
    ) {
        ApiError error = buildError(
                HttpStatus.NOT_FOUND,
                "Endpoint bulunamadi.",
                request,
                List.of()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        ApiError error = buildError(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Bu endpoint icin HTTP metodu desteklenmiyor.",
                request,
                List.of()
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request
    ) {
        ApiError error = buildError(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Desteklenmeyen icerik tipi.",
                request,
                List.of()
        );

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        String message = exception.getMessage() == null || exception.getMessage().isBlank()
                ? "Gecersiz istek."
                : exception.getMessage();

        ApiError error = buildError(
                HttpStatus.BAD_REQUEST,
                message,
                request,
                List.of()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        ApiError error = buildError(
                HttpStatus.FORBIDDEN,
                "Bu islem icin yetkiniz yok.",
                request,
                List.of()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDataAccessException(
            DataAccessException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = resolveDatabaseStatus(exception);
        String message = resolveDatabaseMessage(exception);
        logDatabaseException(exception, request, status, message);

        ApiError error = buildError(
                status,
                message,
                request,
                List.of()
        );

        return ResponseEntity.status(status).body(error);
    }

    private void logDatabaseException(
            DataAccessException exception,
            HttpServletRequest request,
            HttpStatus status,
            String message
    ) {
        if (status.is5xxServerError()) {
            log.error("Veritabani hatasi. Path: {}", request.getRequestURI(), exception);
            return;
        }

        log.warn(
                "Veritabani is kurali hatasi. Path: {}, Status: {}, Message: {}",
                request.getRequestURI(),
                status.value(),
                message
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error("Beklenmeyen hata. Path: {}", request.getRequestURI(), exception);

        ApiError error = buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Beklenmeyen bir hata olustu.",
                request,
                List.of()
        );

        return ResponseEntity.internalServerError().body(error);
    }

    private ApiError buildError(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<ValidationError> validationErrors
    ) {
        return new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                validationErrors
        );
    }

    private HttpStatus resolveDatabaseStatus(DataAccessException exception) {
        String databaseMessage = resolveRawDatabaseMessage(exception);

        if (exception instanceof DataAccessResourceFailureException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }

        if (databaseMessage.contains("Yetkisiz islem")) {
            return HttpStatus.FORBIDDEN;
        }

        if (databaseMessage.contains("bulunamadi")) {
            return HttpStatus.NOT_FOUND;
        }

        if (isConflictDatabaseMessage(databaseMessage)) {
            return HttpStatus.CONFLICT;
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
        String rawMessage = resolveRawDatabaseMessage(exception);

        if (!rawMessage.isBlank()) {
            return cleanDatabaseMessage(rawMessage);
        }

        return "Veritabani islemi tamamlanamadi.";
    }

    private String resolveRawDatabaseMessage(DataAccessException exception) {
        Throwable rootCause = exception.getMostSpecificCause();

        if (rootCause != null && rootCause.getMessage() != null) {
            return rootCause.getMessage();
        }

        return "";
    }

    private boolean isConflictDatabaseMessage(String message) {
        return message.contains("Duplicate entry")
                || message.contains("zaten var")
                || message.contains("limiti doldu")
                || message.contains("aktif hakedis zaten var")
                || message.contains("tamamlanamadi");
    }

    private String cleanDatabaseMessage(String message) {
        if (message.contains("Firma bulunamadi")) {
            return "Firma bulunamadi veya pasif.";
        }

        if (message.contains("Taseron adi zorunludur")) {
            return "Taseron adi zorunludur.";
        }

        if (message.contains("Bu vergi numarasi")) {
            return "Bu vergi numarasi ile kayitli aktif taseron zaten var.";
        }

        if (message.contains("Bu proje icin yetkiniz yok.")) {
            return "Bu proje icin yetkiniz yok.";
        }

        if (message.contains("Yetkisiz islem")) {
            return "Bu islem icin yetkiniz yok.";
        }

        if (message.contains("Taseron bulunamadi")) {
            return "Taseron bulunamadi.";
        }

        if (message.contains("Aktif is emirleri olan taseron silinemez")) {
            return "Aktif is emirleri olan taseron silinemez.";
        }

        if (message.contains("Aktif is emirleri olan proje silinemez")) {
            return "Aktif is emirleri olan proje silinemez.";
        }

        if (message.contains("Is emri bulunamadi")) {
            return "Is emri bulunamadi.";
        }

        if (message.contains("Is emri basligi zorunludur")) {
            return "Is emri basligi zorunludur.";
        }

        if (message.contains("Gecersiz is emri onceligi")) {
            return "Gecersiz is emri onceligi.";
        }

        if (message.contains("Gecersiz is emri durumu")) {
            return "Gecersiz is emri durumu.";
        }

        if (message.contains("Proje bulunamadi veya aktif degil")) {
            return "Proje bulunamadi veya aktif degil.";
        }

        if (message.contains("Taseron bulunamadi veya pasif")) {
            return "Taseron bulunamadi veya pasif.";
        }

        if (message.contains("Atanan kullanici bulunamadi veya yetkisiz")) {
            return "Atanan kullanici bulunamadi veya yetkisiz.";
        }

        if (message.contains("Atanan taseron temsilcisi secilen taserona ait degil")) {
            return "Atanan taseron temsilcisi secilen taserona ait degil.";
        }

        if (message.contains("Bu is emri bu taseron kullanicisina ait degil")) {
            return "Bu is emri bu taseron kullanicisina ait degil.";
        }

        if (message.contains("Bitis tarihi baslangic tarihinden once olamaz")) {
            return "Bitis tarihi baslangic tarihinden once olamaz.";
        }

        if (message.contains("Hakedis bulunamadi")) {
            return "Hakedis bulunamadi.";
        }

        if (message.contains("Hakedis tutari sifirdan buyuk olmalidir")) {
            return "Hakedis tutari sifirdan buyuk olmalidir.";
        }

        if (message.contains("Hakedis sadece tamamlanmis is emirleri icin olusturulabilir")) {
            return "Hakedis sadece tamamlanmis is emirleri icin olusturulabilir.";
        }

        if (message.contains("Bu is emri icin aktif hakedis zaten var")) {
            return "Bu is emri icin aktif hakedis zaten var.";
        }

        if (message.contains("Sadece bekleyen veya itirazdaki hakedis onaylanabilir")) {
            return "Sadece bekleyen veya itirazdaki hakedis onaylanabilir.";
        }

        if (message.contains("Sadece bekleyen veya itirazdaki hakedis reddedilebilir")) {
            return "Sadece bekleyen veya itirazdaki hakedis reddedilebilir.";
        }

        if (message.contains("Red gerekcesi zorunludur")) {
            return "Red gerekcesi zorunludur.";
        }

        if (message.contains("Onaylanmis hakedis silinemez")) {
            return "Onaylanmis hakedis silinemez.";
        }

        if (message.contains("Gecersiz hakedis onay durumu")) {
            return "Gecersiz hakedis onay durumu.";
        }

        if (message.contains("Odeme bulunamadi")) {
            return "Odeme bulunamadi.";
        }

        if (message.contains("Odeme tutari sifirdan buyuk olmalidir")) {
            return "Odeme tutari sifirdan buyuk olmalidir.";
        }

        if (message.contains("Gecersiz odeme yontemi")) {
            return "Gecersiz odeme yontemi.";
        }

        if (message.contains("Sadece onaylanmis hakedislere odeme yapilabilir")) {
            return "Sadece onaylanmis hakedislere odeme yapilabilir.";
        }

        if (message.contains("Odeme tutari hakedis tutarini asamaz")) {
            return "Odeme tutari hakedis tutarini asamaz.";
        }

        if (message.contains("Gecersiz kullanici rolu")) {
            return "Gecersiz kullanici rolu.";
        }

        if (message.contains("Firma bilgisi zorunludur")) {
            return "Firma bilgisi zorunludur.";
        }

        if (message.contains("Taseron temsilcisi icin taseron zorunludur")) {
            return "Taseron temsilcisi icin taseron zorunludur.";
        }

        if (message.contains("Sadece taseron temsilcisi taserona baglanabilir")) {
            return "Sadece taseron temsilcisi taserona baglanabilir.";
        }

        if (message.contains("Aktif abonelik bulunamadi")) {
            return "Aktif abonelik bulunamadi.";
        }

        if (message.contains("Proje limiti doldu")) {
            return "Proje limiti doldu. Lutfen abonelik planinizi yukseltin.";
        }

        if (message.contains("Kullanici limiti doldu")) {
            return "Kullanici limiti doldu. Lutfen abonelik planinizi yukseltin.";
        }

        if (message.contains("Taseron limiti doldu")) {
            return "Taseron limiti doldu. Lutfen abonelik planinizi yukseltin.";
        }

        if (message.contains("Duplicate entry") && message.contains("uq_kullanici_email")) {
            return "Bu e-posta adresiyle kayitli kullanici zaten var.";
        }

        if (message.contains("vergi_no") || message.contains("uq_firma_vergi_no")) {
            return "Bu vergi numarasi ile kayitli bir firma zaten var.";
        }

        if (message.contains("email") || message.contains("uq_firma_email")) {
            return "Bu e-posta adresi ile kayitli bir firma zaten var.";
        }

        return "Veritabani islemi tamamlanamadi.";
    }
}
