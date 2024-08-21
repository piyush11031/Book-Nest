package org.network.backend.file;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.network.backend.book.Book;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${application.file.uploads.photos-output-path}")
    private String fileUploadPath;

    public String saveFile(@Nonnull MultipartFile file,@Nonnull Integer userId) {
        final String fileUploadSubPath = "users" + File.separator + userId;
        return uploadFile(file, fileUploadSubPath);
    }

    private String uploadFile(@Nonnull MultipartFile sourceFile,@Nonnull String fileUploadSubPath) {

        final String finalUploadPath = fileUploadPath + File.separator + fileUploadSubPath;
        File targetFolder = new File(finalUploadPath);
        if(!targetFolder.exists()){
            boolean folderCreated = targetFolder.mkdirs();
            if(!folderCreated){
                log.warn("failed to create the target folder");
                return null;
            }
        }

        final String fileExtension = getFileExtension(sourceFile.getOriginalFilename());

        // ./uploads/users/1/23232323232.jpg
        String targetFilePath = finalUploadPath + File.separator + System.currentTimeMillis() + "." +fileExtension;

        Path targetPath = Paths.get(targetFilePath).toAbsolutePath();
        targetFilePath = targetPath.toAbsolutePath().toString();
        try {
            Files.write(targetPath, sourceFile.getBytes());
            log.info("File save to " + targetFilePath);
            return targetFilePath;
        }catch (IOException e){
            log.error("File was not saved", e);
        }

        return null;
    }

    private String getFileExtension(String fileName) {
        if(fileName == null || fileName.isEmpty()) return "";

        //something.jpg -> get index of "."
        int lastDotIndex = fileName.lastIndexOf(".");

        if(lastDotIndex == -1) return "";

        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}
