package com.file.api.adapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public interface FileAdapter {
    String getAdapterName();

    CompletableFuture<byte[]> readFileSync(Path path, String encoding, String flag) throws IOException;

    CompletableFuture<Void> writeFileSync(Path file, byte[] data, String path) throws IOException;

    // tam thoi chua lam
    CompletableFuture<Void> appendFileSync(Path path, byte[] data, String options) throws IOException;

    CompletableFuture<Boolean> existsSync(Path path);

    CompletableFuture<Void> copyFileSync(Path src, Path dest, int mode) throws IOException;

    // tam thoi chua lam
    CompletableFuture<File[]> opendirSync(Path path) throws IOException;

    // tam thoi chua lam
    CompletableFuture<Void> rmdirSync(Path path) throws IOException;

    // tam thoi chua lam
    CompletableFuture<Void> mkdirSync(Path path) throws IOException;

    CompletableFuture<Void> rmSync(Path path) throws IOException;

    CompletableFuture<String> getFileUrl(Path path);

    // tam thoi chua lam
    CompletableFuture<Path> getLatestFileByPattern(Path directory, Pattern pattern);

    // tam thoi chua lam
    CompletableFuture<Path> waitForFileByPattern(Path directory, Pattern pattern, long timeout);

    CompletableFuture<Void> renameFile(Path directory, String oldName, String newName) throws IOException;
}