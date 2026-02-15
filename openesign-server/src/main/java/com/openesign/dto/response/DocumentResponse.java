package com.openesign.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DocumentResponse {
    private Long id;
    private String title;
    private String description;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String documentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RecipientResponse> recipients;
    private List<SignatureFieldResponse> fields;
}
