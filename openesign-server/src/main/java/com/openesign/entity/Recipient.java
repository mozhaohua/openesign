package com.openesign.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("recipients")
public class Recipient {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("document_id")
    private Long documentId;
    
    private String email;
    private String name;
    
    @TableField("recipient_type")
    private String recipientType; // CC, SIGNER, APPROVER
    
    @TableField("signing_order")
    private Integer signingOrder;
    
    @TableField("recipient_status")
    private String recipientStatus; // PENDING, SENT, SIGNED, DECLINED
    
    @TableField("signed_at")
    private LocalDateTime signedAt;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
