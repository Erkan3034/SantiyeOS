package com.santiyeos.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class IsEmriNotResponse {

    private Integer notId;
    private Integer firmaId;
    private Integer isEmriId;
    private Integer kullaniciId;
    private String icerik;
    private LocalDateTime createdAt;
    private String yazan;
    private String rol;
}
