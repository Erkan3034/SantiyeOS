package com.santiyeos.api.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
}
