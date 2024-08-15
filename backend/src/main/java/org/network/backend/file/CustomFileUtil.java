package org.network.backend.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.engine.MethodValidationConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class CustomFileUtil {
    public static byte[] readFileFromLocation(String fileUrl) {

        if(StringUtils.isBlank(fileUrl)) return null;

        try {
            Path filePath = new File(fileUrl).toPath();
            return Files.readAllBytes(filePath);
        }catch (IOException e){
            log.warn("No file found in path {}", fileUrl);
        }
        return null;
    }
}
