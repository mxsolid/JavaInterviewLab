package com.javainterviewlab.system.e2e;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/** 将 classpath 内置 Seed 适配到既有上传导入入口，确保 E2E 不复制解析和导入算法。 */
final class E2eSeedMultipartFile implements MultipartFile {

    private static final String CONTENT_TYPE = "application/json";

    private final String filename;
    private final byte[] content;

    E2eSeedMultipartFile(String filename, byte[] content) {
        this.filename = filename;
        this.content = content.clone();
    }

    @Override
    public String getName() {
        return "file";
    }

    @Override
    public String getOriginalFilename() {
        return filename;
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public boolean isEmpty() {
        return content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() {
        return content.clone();
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File destination) throws IOException {
        Files.write(destination.toPath(), content);
    }
}
