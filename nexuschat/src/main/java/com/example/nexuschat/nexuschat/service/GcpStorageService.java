package com.example.nexuschat.nexuschat.service;

import org.springframework.stereotype.Service;
import java.util.UUID;

// import com.google.cloud.storage.BlobInfo;
// import com.google.cloud.storage.Storage;
// import com.google.cloud.storage.StorageOptions;
// import java.net.URL;
// import java.util.concurrent.TimeUnit;
// import org.springframework.beans.factory.annotation.Value;

@Service
public class GcpStorageService implements StorageService {

    // @Value("${gcp.bucket.name}")
    // private String bucketName;

    // private final Storage storage;

    public GcpStorageService() {
        // this.storage = StorageOptions.getDefaultInstance().getService();
    }

    @Override
    public String generarUrlFirmada(String fileName, String contentType) {
        // --- IMPLEMENTACIÓN MOCK (ACTIVA PARA PRUEBAS) ---
        // Genera una URL simulada que parece real pero no funciona
        String mockUrl = "https://storage.googleapis.com/mock-bucket/" + fileName +
                "?GoogleAccessId=mock-service-account&Expires=123456789&Signature=mockSignature123";
        return mockUrl;

        // --- IMPLEMENTACIÓN REAL (COMENTADA) ---
        /*
         * try {
         * BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
         * .setContentType(contentType)
         * .build();
         * 
         * URL url = storage.signUrl(blobInfo, 15, TimeUnit.MINUTES,
         * Storage.SignUrlOption.httpMethod(Storage.SignUrlOption.HttpMethod.PUT),
         * Storage.SignUrlOption.withContentType());
         * 
         * return url.toString();
         * } catch (Exception e) {
         * throw new RuntimeException("Error al generar URL firmada de GCS", e);
         * }
         */
    }

    @Override
    public String generarUrlFirmadaLectura(String fileName) {
        // --- IMPLEMENTACIÓN MOCK (ACTIVA PARA PRUEBAS) ---
        // Genera una URL simulada de lectura con firma
        String mockUrl = "https://storage.googleapis.com/mock-bucket/" + fileName +
                "?GoogleAccessId=mock-service-account&Expires=123456789&Signature=mockReadSignature123";
        return mockUrl;

        // --- IMPLEMENTACIÓN REAL (COMENTADA) ---
        /*
         * try {
         * BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();
         * 
         * URL url = storage.signUrl(blobInfo, 15, TimeUnit.MINUTES,
         * Storage.SignUrlOption.httpMethod(Storage.SignUrlOption.HttpMethod.GET),
         * Storage.SignUrlOption.withV4Signature());
         * 
         * return url.toString();
         * } catch (Exception e) {
         * throw new RuntimeException("Error al generar URL firmada de lectura de GCS",
         * e);
         * }
         */
    }
}
