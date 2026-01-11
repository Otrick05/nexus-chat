package com.example.nexuschat.nexuschat.service;

import org.springframework.stereotype.Service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.HttpMethod;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@Service
public class GcpStorageService implements StorageService {

    private final Storage storage;

    public GcpStorageService() {
        this.storage = StorageOptions.getDefaultInstance().getService();
    }

    @Override
    public String generarUrlFirmada(String fileName, String contentType) {
        try {
            BlobInfo blobInfo = BlobInfo.newBuilder("nexus-chat", fileName)
                    .setContentType(contentType)
                    .build();

            URL url = storage.signUrl(blobInfo, 15, TimeUnit.MINUTES,
                    Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                    Storage.SignUrlOption.withContentType());

            return url.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar URL firmada de GCS: " + e.getMessage(), e);
        }
    }

    @Override
    public String generarUrlFirmadaLectura(String fileName) {
        try {
            BlobInfo blobInfo = BlobInfo.newBuilder("nexus-chat", fileName).build();

            URL url = storage.signUrl(blobInfo, 15, TimeUnit.MINUTES,
                    Storage.SignUrlOption.httpMethod(HttpMethod.GET));

            return url.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar URL firmada de lectura de GCS: " + e.getMessage(),
                    e);
        }
    }
}
