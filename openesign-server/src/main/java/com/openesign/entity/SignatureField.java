package com.openesign.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("signature_fields")
public class SignatureField {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("document_id")
    private Long documentId;
    
    @TableField("recipient_id")
    private Long recipientId;
    
    private String type; // SIGNATURE, INITIAL, TEXT, DATE
    private String page;
    private BigDecimal positionX;
    private BigDecimal positionY;
    private BigDecimal width;
    private BigDecimal height;
    
    @TableField("field_value")
    private String fieldValue;
    
    @TableField("field_status")
    private String fieldStatus; // PENDING, SIGNED
    
    @TableField("signed_at")
    private LocalDateTime signedAt;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
