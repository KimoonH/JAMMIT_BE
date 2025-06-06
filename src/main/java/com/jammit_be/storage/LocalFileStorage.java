package com.jammit_be.storage;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

public class LocalFileStorage implements FileStorage {

    private final String basePath = System.getProperty("user.dir") + "/src/main/resources/static/uploads";

    @Override
    public String save(MultipartFile file, String subFolder) {
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);// 확장자만 추출
        String newFilename = UUID.randomUUID() +"."+ext;

        // 여러 종류의 파일(프로필, 모임썸네일 등)을 목적별로 폴더를 분리해 저장, 필요 없으면 uploads 폴더에만 저장
        File dir = (subFolder != null && !subFolder.isBlank())
                ? new File(basePath, subFolder) // 예시: uploads/profile
                : new File(basePath); // 예시: uploads/
        // 폴더가 없으면 폴더 생성
        if (!dir.exists()) {
            boolean ok = dir.mkdirs();
            if (!ok) throw new RuntimeException("디렉토리 생성 실패: " + dir.getAbsolutePath());
        }

        System.out.println("실제 파일 저장 경로: " + dir.getAbsolutePath());

        // 실제 저장할 파일 객체(경로 + 이름) 생성
        File dest = new File(dir, newFilename);
        try {
            // 실제 파일 저장
            file.transferTo(dest);
        } catch (Exception e) {
            throw new RuntimeException("퍼알 저장 실패", e);
        }

        String urlPath = "/static/" +
                (subFolder != null && !subFolder.isBlank() ? subFolder + "/" : "") +
                newFilename;

        return urlPath;
    }
}
