package com.visulaw.legal_service.application.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {

    String uploadFile(MultipartFile file) throws IOException;

}
