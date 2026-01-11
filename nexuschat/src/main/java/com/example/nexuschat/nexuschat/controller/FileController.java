package com.example.nexuschat.nexuschat.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.nexuschat.nexuschat.DTO.request.FileSignatureRequestDTO;
import com.example.nexuschat.nexuschat.DTO.response.FileSignedUrlResponseDTO;
import com.example.nexuschat.nexuschat.service.StorageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final StorageService storageService;

    @PostMapping("/signed-url")
    public ResponseEntity<List<FileSignedUrlResponseDTO>> generateSignedUrls(
            @RequestBody List<FileSignatureRequestDTO> files,
            Principal principal) {

        List<FileSignedUrlResponseDTO> response = new ArrayList<>();

        for (FileSignatureRequestDTO file : files) {
            // Generate unique filename: uuid + originalName
            String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getFileName();

            String signedUrl = storageService.generarUrlFirmada(uniqueFileName, file.getContentType());

            response.add(FileSignedUrlResponseDTO.builder()
                    .fileName(uniqueFileName)
                    .signedUrl(signedUrl)
                    .contentType(file.getContentType())
                    .build());
        }
        System.out.println(response);
        return ResponseEntity.ok(response);
    }
}
