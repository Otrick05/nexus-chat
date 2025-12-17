package com.example.nexuschat.nexuschat.DTO.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileSignatureRequestDTO {
    private String fileName;
    private String contentType;
}
