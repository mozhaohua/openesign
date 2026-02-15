package com.openesign.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String email;
    private String password;
    private String name;
    private String company;
    
    @TableField("email_verified")
    private Boolean emailVerified;
    
    @TableField("signature_data")
    private String signatureData;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
