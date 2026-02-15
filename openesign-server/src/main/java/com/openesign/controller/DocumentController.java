package com.openesign.controller;

import com.openesign.dto.request.DocumentCreateRequest;
import com.openesign.dto.request.SignFieldRequest;
import com.openesign.dto.response.DocumentResponse;
import com.openesign.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
    
    private final DocumentService documentService;
    
    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(documentService.uploadDocument(file, userId));
    }
    
    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(
            @RequestBody DocumentCreateRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(documentService.createDocument(request, userId));
    }
    
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> listDocuments(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(documentService.listDocuments(userId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocument(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(documentService.getDocument(id, userId));
    }
    
    @PostMapping("/{id}/send")
    public ResponseEntity<DocumentResponse> sendDocument(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(documentService.sendDocument(id, userId));
    }
    
    @PostMapping("/sign")
    public ResponseEntity<DocumentResponse> signField(
            @RequestBody SignFieldRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(documentService.signField(request, userId));
    }
    
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDocument(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        byte[] content = documentService.downloadDocument(id, userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "document.pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }
}
