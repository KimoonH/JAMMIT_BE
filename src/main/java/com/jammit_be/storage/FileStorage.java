package com.jammit_be.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {
    String save(MultipartFile file, String subFolder);
}
