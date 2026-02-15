package com.openesign.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String company;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
}
