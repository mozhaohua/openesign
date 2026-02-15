package com.openesign.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("documents")
public class Document {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    private String title;
    private String description;
    
    @TableField("file_name")
    private String fileName;
    
    @TableField("file_path")
    private String filePath;
    
    @TableField("file_size")
    private Long fileSize;
    
    @TableField("file_type")
    private String fileType;
    
    @TableField("document_status")
    private String documentStatus; // DRAFT, PENDING, COMPLETED, DECLINED
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
