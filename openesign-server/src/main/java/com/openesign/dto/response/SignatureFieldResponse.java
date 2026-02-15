package com.openesign.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SignatureFieldResponse {
    private Long id;
    private String type;
    private String page;
    private BigDecimal positionX;
    private BigDecimal positionY;
    private BigDecimal width;
    private BigDecimal height;
    private String fieldStatus;
    private LocalDateTime signedAt;
}
