package com.example.nexuschat.nexuschat.service;

import org.springframework.stereotype.Service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.HttpMethod;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;

@Service
public class GcpStorageService implements StorageService {

    @Value("${gcp.bucket.name}")
    private String bucketName;

    @Value("${gcp.credentials.location:}")
    private String credentialsLocation;

    private final Storage storage;

    public GcpStorageService(@Value("${gcp.credentials.location:}") String credentialsLocation) {
        try {
            if (credentialsLocation != null && !credentialsLocation.isBlank()) {
                // Local Dev: Cargar desde archivo explícito
                GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsLocation));
                this.storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
            } else {
                // Prod (Cloud Run): Usar ADC (Metadata Server)
                this.storage = StorageOptions.getDefaultInstance().getService();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar credenciales de GCP desde: " + credentialsLocation, e);
        }
    }

    @Override
    public String generarUrlFirmada(String fileName, String contentType) {
        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
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
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();

            URL url = storage.signUrl(blobInfo, 15, TimeUnit.MINUTES,
                    Storage.SignUrlOption.httpMethod(HttpMethod.GET),
                    Storage.SignUrlOption.withV4Signature());

            return url.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar URL firmada de lectura de GCS: " + e.getMessage(),
                    e);
        }
    }
}
