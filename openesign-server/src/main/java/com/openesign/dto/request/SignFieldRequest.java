package com.openesign.dto.request;

import lombok.Data;

@Data
public class SignFieldRequest {
    private Long fieldId;
    private String signatureData; // Base64 encoded signature image or drawn signature
    private String textValue; // For text fields
}
