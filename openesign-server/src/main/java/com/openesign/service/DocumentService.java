package com.openesign.service;

import com.openesign.dto.request.DocumentCreateRequest;
import com.openesign.dto.request.SignFieldRequest;
import com.openesign.dto.response.DocumentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
    DocumentResponse uploadDocument(MultipartFile file, Long userId);
    DocumentResponse createDocument(DocumentCreateRequest request, Long userId);
    DocumentResponse getDocument(Long documentId, Long userId);
    List<DocumentResponse> listDocuments(Long userId);
    DocumentResponse sendDocument(Long documentId, Long userId);
    DocumentResponse signField(SignFieldRequest request, Long userId);
    byte[] downloadDocument(Long documentId, Long userId);
}
