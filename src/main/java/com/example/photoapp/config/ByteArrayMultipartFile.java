package com.example.photoapp.config;

import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ByteArrayMultipartFile implements MultipartFile {

    private final byte[] content;
    private final String filename;

    public ByteArrayMultipartFile(byte[] content, String filename) {
        this.content = content;
        this.filename = filename;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getOriginalFilename() {
        return filename;
    }

    @Override
    public String getContentType() {
        // You can adjust content type as needed
        return "image/jpeg";
    }

    @Override
    public boolean isEmpty() {
        return content == null || content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return IOUtils.toInputStream(new String(content), "UTF-8");
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        IOUtils.write(content, new FileOutputStream(dest));
    }
}