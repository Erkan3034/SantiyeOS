package com.santiyeos.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

        return "Veritabanı işlemi tamamlanamadı.";
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

        if (message.contains("Is emri bulunamadi")) {
            return "İş emri bulunamadı.";
        }

        if (message.contains("Is emri basligi zorunludur")) {
            return "İş emri başlığı zorunludur.";
        }

        if (message.contains("Gecersiz is emri onceligi")) {
            return "Geçersiz iş emri önceliği.";
        }

        if (message.contains("Gecersiz is emri durumu")) {
            return "Geçersiz iş emri durumu.";
        }

        if (message.contains("Proje bulunamadi veya aktif degil")) {
            return "Proje bulunamadı veya aktif değil.";
        }

        if (message.contains("Taseron bulunamadi veya pasif")) {
            return "Taşeron bulunamadı veya pasif.";
        }

        if (message.contains("Atanan kullanici bulunamadi veya yetkisiz")) {
            return "Atanan kullanıcı bulunamadı veya yetkisiz.";
        }

        if (message.contains("Atanan taseron temsilcisi secilen taserona ait degil")) {
            return "Atanan taşeron temsilcisi seçilen taşerona ait değil.";
        }

        if (message.contains("Bu is emri bu taseron kullanicisina ait degil")) {
            return "Bu iş emri bu taşeron kullanıcısına ait değil.";
        }

        if (message.contains("Bitis tarihi baslangic tarihinden once olamaz")) {
            return "Bitiş tarihi başlangıç tarihinden önce olamaz.";
        }

        if (message.contains("Hakedis bulunamadi")) {
            return "Hakediş bulunamadı.";
        }

        if (message.contains("Hakedis tutari sifirdan buyuk olmalidir")) {
            return "Hakediş tutarı sıfırdan büyük olmalıdır.";
        }

        if (message.contains("Hakedis sadece tamamlanmis is emirleri icin olusturulabilir")) {
            return "Hakediş sadece tamamlanmış iş emirleri için oluşturulabilir.";
        }

        if (message.contains("Bu is emri icin aktif hakedis zaten var")) {
            return "Bu iş emri için aktif hakediş zaten var.";
        }

        if (message.contains("Sadece bekleyen veya itirazdaki hakedis onaylanabilir")) {
            return "Sadece bekleyen veya itirazdaki hakediş onaylanabilir.";
        }

        if (message.contains("Sadece bekleyen veya itirazdaki hakedis reddedilebilir")) {
            return "Sadece bekleyen veya itirazdaki hakediş reddedilebilir.";
        }

        if (message.contains("Red gerekcesi zorunludur")) {
            return "Red gerekçesi zorunludur.";
        }

        if (message.contains("Onaylanmis hakedis silinemez")) {
            return "Onaylanmış hakediş silinemez.";
        }

        if (message.contains("Gecersiz hakedis onay durumu")) {
            return "Geçersiz hakediş onay durumu.";
        }

        if (message.contains("Odeme bulunamadi")) {
            return "Ödeme bulunamadı.";
        }

        if (message.contains("Odeme tutari sifirdan buyuk olmalidir")) {
            return "Ödeme tutarı sıfırdan büyük olmalıdır.";
        }

        if (message.contains("Gecersiz odeme yontemi")) {
            return "Geçersiz ödeme yöntemi.";
        }

        if (message.contains("Sadece onaylanmis hakedislere odeme yapilabilir")) {
            return "Sadece onaylanmış hakedişlere ödeme yapılabilir.";
        }

        if (message.contains("Odeme tutari hakedis tutarini asamaz")) {
            return "Ödeme tutarı hakediş tutarını aşamaz.";
        }

        return "Veritabanı işlemi tamamlanamadı.";
    }
}
