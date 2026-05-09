package com.santiyeos.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectHakedisRequest {

    @NotBlank(message = "Red gerekçesi zorunludur")
    @Size(max = 2000, message = "Red gerekçesi en fazla 2000 karakter olabilir")
    private String redGerekce;
}
