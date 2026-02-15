package com.openesign.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.openesign.dto.request.DocumentCreateRequest;
import com.openesign.dto.request.SignFieldRequest;
import com.openesign.dto.response.DocumentResponse;
import com.openesign.dto.response.RecipientResponse;
import com.openesign.dto.response.SignatureFieldResponse;
import com.openesign.entity.Document;
import com.openesign.entity.Recipient;
import com.openesign.entity.SignatureField;
import com.openesign.entity.User;
import com.openesign.mapper.DocumentMapper;
import com.openesign.mapper.RecipientMapper;
import com.openesign.mapper.SignatureFieldMapper;
import com.openesign.mapper.UserMapper;
import com.openesign.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    
    private final DocumentMapper documentMapper;
    private final RecipientMapper recipientMapper;
    private final SignatureFieldMapper signatureFieldMapper;
    private final UserMapper userMapper;
    
    @Value("${file.upload.path:./uploads}")
    private String uploadPath;
    
    @Override
    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file, Long userId) {
        // Save file
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + extension;
        
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        try {
            file.transferTo(new File(dir, fileName));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        }
        
        // Create document record
        Document document = new Document();
        document.setUserId(userId);
        document.setTitle(originalFilename);
        document.setFileName(originalFilename);
        document.setFilePath(fileName);
        document.setFileSize(file.getSize());
        document.setFileType(extension.substring(1));
        document.setDocumentStatus("DRAFT");
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        
        documentMapper.insert(document);
        
        return toDocumentResponse(document);
    }
    
    @Override
    @Transactional
    public DocumentResponse createDocument(DocumentCreateRequest request, Long userId) {
        Document document = new Document();
        document.setUserId(userId);
        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());
        document.setDocumentStatus("DRAFT");
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        
        documentMapper.insert(document);
        
        // Add recipients
        if (request.getRecipients() != null) {
            for (var req : request.getRecipients()) {
                Recipient recipient = new Recipient();
                recipient.setDocumentId(document.getId());
                recipient.setEmail(req.getEmail());
                recipient.setName(req.getName());
                recipient.setSigningOrder(req.getSigningOrder());
                recipient.setRecipientType("SIGNER");
                recipient.setRecipientStatus("PENDING");
                recipient.setCreatedAt(LocalDateTime.now());
                recipient.setUpdatedAt(LocalDateTime.now());
                recipientMapper.insert(recipient);
            }
        }
        
        return getDocument(document.getId(), userId);
    }
    
    @Override
    public DocumentResponse getDocument(Long documentId, Long userId) {
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new RuntimeException("Document not found");
        }
        
        // Check permission
        if (!document.getUserId().equals(userId)) {
            // Check if user is a recipient
            LambdaQueryWrapper<Recipient> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Recipient::getDocumentId, documentId);
            List<Recipient> recipients = recipientMapper.selectList(wrapper);
            boolean isRecipient = recipients.stream()
                    .anyMatch(r -> r.getEmail() != null);
            if (!isRecipient) {
                throw new RuntimeException("Access denied");
            }
        }
        
        return toDocumentResponse(document);
    }
    
    @Override
    public List<DocumentResponse> listDocuments(Long userId) {
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Document::getUserId, userId);
        wrapper.orderByDesc(Document::getCreatedAt);
        
        List<Document> documents = documentMapper.selectList(wrapper);
        return documents.stream()
                .map(this::toDocumentResponse)
                .toList();
    }
    
    @Override
    @Transactional
    public DocumentResponse sendDocument(Long documentId, Long userId) {
        Document document = documentMapper.selectById(documentId);
        if (document == null || !document.getUserId().equals(userId)) {
            throw new RuntimeException("Document not found or access denied");
        }
        
        document.setDocumentStatus("PENDING");
        document.setUpdatedAt(LocalDateTime.now());
        documentMapper.updateById(document);
        
        // Update recipients status
        LambdaQueryWrapper<Recipient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Recipient::getDocumentId, documentId);
        List<Recipient> recipients = recipientMapper.selectList(wrapper);
        
        for (Recipient recipient : recipients) {
            recipient.setRecipientStatus("SENT");
            recipient.setUpdatedAt(LocalDateTime.now());
            recipientMapper.updateById(recipient);
        }
        
        return getDocument(documentId, userId);
    }
    
    @Override
    @Transactional
    public DocumentResponse signField(SignFieldRequest request, Long userId) {
        SignatureField field = signatureFieldMapper.selectById(request.getFieldId());
        if (field == null) {
            throw new RuntimeException("Signature field not found");
        }
        
        // Verify user is the recipient
        Recipient recipient = recipientMapper.selectById(field.getRecipientId());
        User user = userMapper.selectById(userId);
        
        if (!recipient.getEmail().equals(user.getEmail())) {
            throw new RuntimeException("Access denied");
        }
        
        // Update field
        field.setFieldValue(request.getSignatureData() != null ? 
                request.getSignatureData() : request.getTextValue());
        field.setFieldStatus("SIGNED");
        field.setSignedAt(LocalDateTime.now());
        field.setUpdatedAt(LocalDateTime.now());
        signatureFieldMapper.updateById(field);
        
        // Update recipient
        recipient.setRecipientStatus("SIGNED");
        recipient.setSignedAt(LocalDateTime.now());
        recipient.setUpdatedAt(LocalDateTime.now());
        recipientMapper.updateById(recipient);
        
        // Check if all fields signed
        LambdaQueryWrapper<SignatureField> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SignatureField::getDocumentId, field.getDocumentId());
        List<SignatureField> fields = signatureFieldMapper.selectList(wrapper);
        
        boolean allSigned = fields.stream()
                .allMatch(f -> "SIGNED".equals(f.getFieldStatus()));
        
        if (allSigned) {
            Document document = documentMapper.selectById(field.getDocumentId());
            document.setDocumentStatus("COMPLETED");
            document.setUpdatedAt(LocalDateTime.now());
            documentMapper.updateById(document);
        }
        
        return getDocument(field.getDocumentId(), userId);
    }
    
    @Override
    public byte[] downloadDocument(Long documentId, Long userId) {
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new RuntimeException("Document not found");
        }
        
        // Check permission (simplified)
        File file = new File(uploadPath, document.getFilePath());
        if (!file.exists()) {
            throw new RuntimeException("File not found");
        }
        
        try {
            return java.nio.file.Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file", e);
        }
    }
    
    private DocumentResponse toDocumentResponse(Document document) {
        DocumentResponse response = new DocumentResponse();
        response.setId(document.getId());
        response.setTitle(document.getTitle());
        response.setDescription(document.getDescription());
        response.setFileName(document.getFileName());
        response.setFileType(document.getFileType());
        response.setFileSize(document.getFileSize());
        response.setDocumentStatus(document.getDocumentStatus());
        response.setCreatedAt(document.getCreatedAt());
        response.setUpdatedAt(document.getUpdatedAt());
        
        // Load recipients
        LambdaQueryWrapper<Recipient> rWrapper = new LambdaQueryWrapper<>();
        rWrapper.eq(Recipient::getDocumentId, document.getId());
        List<Recipient> recipients = recipientMapper.selectList(rWrapper);
        response.setRecipients(recipients.stream()
                .map(this::toRecipientResponse)
                .toList());
        
        // Load signature fields
        LambdaQueryWrapper<SignatureField> fWrapper = new LambdaQueryWrapper<>();
        fWrapper.eq(SignatureField::getDocumentId, document.getId());
        List<SignatureField> fields = signatureFieldMapper.selectList(fWrapper);
        response.setFields(fields.stream()
                .map(this::toSignatureFieldResponse)
                .toList());
        
        return response;
    }
    
    private RecipientResponse toRecipientResponse(Recipient recipient) {
        RecipientResponse response = new RecipientResponse();
        response.setId(recipient.getId());
        response.setEmail(recipient.getEmail());
        response.setName(recipient.getName());
        response.setRecipientType(recipient.getRecipientType());
        response.setSigningOrder(recipient.getSigningOrder());
        response.setRecipientStatus(recipient.getRecipientStatus());
        return response;
    }
    
    private SignatureFieldResponse toSignatureFieldResponse(SignatureField field) {
        SignatureFieldResponse response = new SignatureFieldResponse();
        response.setId(field.getId());
        response.setType(field.getType());
        response.setPage(field.getPage());
        response.setPositionX(field.getPositionX());
        response.setPositionY(field.getPositionY());
        response.setWidth(field.getWidth());
        response.setHeight(field.getHeight());
        response.setFieldStatus(field.getFieldStatus());
        response.setSignedAt(field.getSignedAt());
        return response;
    }
}
