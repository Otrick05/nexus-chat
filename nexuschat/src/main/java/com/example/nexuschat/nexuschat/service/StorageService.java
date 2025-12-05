package com.example.nexuschat.nexuschat.service;

public interface StorageService {
    String generarUrlFirmada(String fileName, String contentType);

    String generarUrlFirmadaLectura(String fileName);
}
