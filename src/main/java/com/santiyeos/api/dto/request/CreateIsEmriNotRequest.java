package com.santiyeos.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateIsEmriNotRequest {

    @NotBlank(message = "Not icerigi zorunludur")
    @Size(max = 5000, message = "Not icerigi en fazla 5000 karakter olabilir")
    private String icerik;
}
