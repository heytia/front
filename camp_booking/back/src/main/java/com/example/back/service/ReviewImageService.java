package com.example.back.service;

import com.example.back.model.ReviewImage;
import com.example.back.repository.ReviewImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class ReviewImageService {

    @Autowired
    private ReviewImageRepository imageRepository;
    
    // 圖片上傳的基礎路徑
    private final String UPLOAD_DIR = "./uploads/reviews/";
    
    // 初始化上傳目錄
    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Created upload directory: " + uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }
    
    // 上傳單個評價圖片
    public Map<String, String> uploadImage(MultipartFile file, Integer reviewId) {
        try {
            // 創建上傳目錄（雖然已在 init 方法中創建，但再次檢查以確保安全）
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 驗證文件
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("文件不能為空");
            }
            
            validateFileSize(file);
            if (!isValidImageFile(file)) {
                throw new RuntimeException("只接受 JPEG、PNG 或 GIF 圖片");
            }
            
            // 生成唯一檔案名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + fileExtension;
            
            // 保存檔案
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);
            
            // 保存到資料庫
            String imageUrl = "/uploads/reviews/" + newFilename;
            
            if (reviewId != null) {
                ReviewImage image = new ReviewImage();
                image.setReviewId(reviewId);
                image.setImageUrl(imageUrl);
                imageRepository.save(image);
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            return response;
            
        } catch (IOException e) {
            throw new RuntimeException("無法儲存圖片: " + e.getMessage(), e);
        }
    }
    
    // 批次上傳評價圖片
    public List<Map<String, String>> uploadMultipleImages(MultipartFile[] files, Integer reviewId) {
        List<Map<String, String>> responses = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                Map<String, String> response = uploadImage(file, reviewId);
                responses.add(response);
            }
        }
        
        return responses;
    }
    
    // 獲取評價的所有圖片
    public List<ReviewImage> getImagesByReviewId(Integer reviewId) {
        return imageRepository.findByReviewId(reviewId);
    }
    
    // 刪除評價圖片
    public void deleteImage(Integer imageId) {
        Optional<ReviewImage> imageOptional = imageRepository.findById(imageId);
        
        if (imageOptional.isPresent()) {
            ReviewImage image = imageOptional.get();
            
            // 嘗試從檔案系統中刪除檔案
            try {
                String filename = image.getImageUrl().substring(image.getImageUrl().lastIndexOf("/") + 1);
                Path filePath = Paths.get(UPLOAD_DIR).resolve(filename);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // 即使檔案刪除失敗，也繼續刪除資料庫記錄
                System.err.println("無法刪除檔案: " + e.getMessage());
            }
            
            // 從資料庫中刪除記錄
            imageRepository.deleteById(imageId);
        }
    }
    
    // 刪除評價的所有圖片
    @Transactional
    public void deleteByReviewId(Integer reviewId) {
        List<ReviewImage> images = imageRepository.findByReviewId(reviewId);
        for (ReviewImage image : images) {
            deleteImage(image.getId());
        }
    }

    // 驗證檔案類型的方法
    private boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
            contentType.startsWith("image/jpeg") || 
            contentType.startsWith("image/png") || 
            contentType.startsWith("image/gif")
        );
    }

    // 檢查檔案大小的方法
    private void validateFileSize(MultipartFile file) {
        long maxFileSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("檔案大小不能超過 10MB");
        }
    }
}