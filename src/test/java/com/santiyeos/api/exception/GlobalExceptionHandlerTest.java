package com.santiyeos.api.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void businessExceptionUsesGivenStatusAndMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");

        var response = handler.handleBusinessException(
                BusinessException.badRequest("Hatali istek."),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Hatali istek.");
        assertThat(response.getBody().path()).isEqualTo("/api/test");
    }

    @Test
    void noResourceFoundReturnsNotFoundInsteadOfInternalServerError() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/yok");

        var response = handler.handleEndpointNotFoundException(
                new NoResourceFoundException(HttpMethod.GET, "/api/yok"),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Endpoint bulunamadi.");
    }

    @Test
    void unreadableJsonReturnsBadRequestInsteadOfInternalServerError() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/test");

        var response = handler.handleMessageNotReadableException(
                new HttpMessageNotReadableException("bad json"),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Istek govdesi okunamadi. JSON formatini kontrol edin.");
    }

    @Test
    void unexpectedExceptionReturnsGenericInternalServerError() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");

        var response = handler.handleUnexpectedException(
                new RuntimeException("internal detail"),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Beklenmeyen bir hata olustu.");
    }

    @Test
    void databaseConnectionFailureReturnsServiceUnavailable() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");

        var response = handler.handleDataAccessException(
                new DataAccessResourceFailureException("connection failed"),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Veri işlemi bilinmeyen bir nedenle tamamlanamadı.");
    }

    @Test
    void databaseDuplicateKeyReturnsConflict() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/kullanicilar");

        var response = handler.handleDataAccessException(
                new DuplicateKeyException("Duplicate entry 'x' for key 'uq_kullanici_email'"),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Bu e-posta adresiyle kayitli kullanici zaten var.");
    }

    @Test
    void databaseNotFoundSignalReturnsNotFound() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/hakedisler/999");

        var response = handler.handleDataAccessException(
                databaseSignal("Hakedis bulunamadi."),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Hakedis bulunamadi.");
    }

    @Test
    void databaseForbiddenSignalReturnsForbidden() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/projeler");

        var response = handler.handleDataAccessException(
                databaseSignal("Yetkisiz islem."),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Bu islem icin yetkiniz yok.");
    }

    @Test
    void databaseLimitSignalReturnsConflict() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/projeler");

        var response = handler.handleDataAccessException(
                databaseSignal("Proje limiti doldu."),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Proje limiti doldu. Lutfen abonelik planinizi yukseltin.");
    }

    private UncategorizedSQLException databaseSignal(String message) {
        return new UncategorizedSQLException("call procedure", "CALL test()", new SQLException(message));
    }
}
