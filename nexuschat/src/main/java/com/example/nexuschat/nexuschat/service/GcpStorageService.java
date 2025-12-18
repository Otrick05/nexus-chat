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
            java.io.File credentialsFile = new java.io.File(credentialsLocation);
            if (credentialsLocation != null && !credentialsLocation.isBlank() && credentialsFile.exists()) {
                // Local Dev: Cargar desde archivo explícito solo si existe
                System.out.println("Cargando credenciales desde archivo: " + credentialsLocation);
                GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsLocation));
                this.storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
            } else {
                // Prod (Cloud Run) o si no existe el archivo: Usar ADC (Metadata Server)
                System.out.println(
                        "No se encontró archivo de credenciales o no se especificó. Usando Application Default Credentials (ADC).");
                this.storage = StorageOptions.getDefaultInstance().getService();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar credenciales de GCP.", e);
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
                    Storage.SignUrlOption.httpMethod(HttpMethod.GET));

            return url.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar URL firmada de lectura de GCS: " + e.getMessage(),
                    e);
        }
    }
}
