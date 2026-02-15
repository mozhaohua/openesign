package com.openesign.dto.response;

import lombok.Data;

@Data
public class RecipientResponse {
    private Long id;
    private String email;
    private String name;
    private String recipientType;
    private Integer signingOrder;
    private String recipientStatus;
}
