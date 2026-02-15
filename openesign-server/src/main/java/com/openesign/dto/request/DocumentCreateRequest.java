package com.openesign.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class DocumentCreateRequest {
    private String title;
    private String description;
    private List<RecipientRequest> recipients;
}

@Data
class RecipientRequest {
    private String email;
    private String name;
    private Integer signingOrder;
}
