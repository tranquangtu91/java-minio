package com.file.api.adapter;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
public class MinioFileAdapter implements FileAdapter {
    @Autowired
    private final MinioClient minioClient;

    private final String bucketName;

    public MinioFileAdapter(MinioClient minioClient,
                            @Value("${minio.bucketName}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    @Override
    public String getAdapterName() {
        return "MinioFsAdapter";
    }

    @Override
    public CompletableFuture<byte[]> readFileSync(Path path, String encoding, String flag) {
        return CompletableFuture.supplyAsync(() -> {
            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path.toString())
                            .build())) {
                return stream.readAllBytes();
            } catch (ErrorResponseException e) {
                throw new RuntimeException("MinIO Error: " + e.errorResponse().message(), e);
            } catch (Exception e) {
                throw new RuntimeException("Error reading file from MinIO: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> writeFileSync(Path file, byte[] data, String options) {
        return CompletableFuture.runAsync(() -> {
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(file.getFileName().toString())
                                .stream(inputStream, data.length, -1)
                                .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                                .build()
                );

            } catch (Exception e) {
                throw new RuntimeException("Error writing file to Minio", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> appendFileSync(Path path, byte[] data, String options) {
        throw new UnsupportedOperationException("Append operation is not directly supported in Minio");
    }

    @Override
    public CompletableFuture<Boolean> existsSync(Path path) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(bucketName)
                                .object(path.toString())
                                .build()
                );
                return true;
            } catch (ErrorResponseException e) {
                throw new RuntimeException("MinIO Error: " + e.errorResponse().message(), e);
            } catch (Exception e) {
                throw new RuntimeException("Error checking file existence in MinIO: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> copyFileSync(Path src, Path dest, int mode) {
        String srcPath = src.toString();
        String destPath = dest.toString();
        return CompletableFuture.runAsync(() -> {
            try {
                if (srcPath.equals(destPath)) {
                    throw new IllegalArgumentException("Source and destination cannot be the same");
                }

                minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(bucketName)
                                .object(destPath)
                                .source(CopySource.builder()
                                        .bucket(bucketName)
                                        .object(srcPath)
                                        .build())
                                .build()
                );
            } catch (MinioException e) {
                throw new RuntimeException("Error copying file in MinIO", e);
            } catch (Exception e) {
                throw new RuntimeException("Unexpected error", e);
            }
        });
    }

    @Override
    public CompletableFuture<File[]> opendirSync(Path path) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public CompletableFuture<Void> rmdirSync(Path path) {
        return CompletableFuture.runAsync(() -> {
            try {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(path.toString())
                                .build()
                );
            } catch (Exception e) {
                throw new RuntimeException("Unexpected error while deleting file: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> mkdirSync(Path path) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public CompletableFuture<Void> rmSync(Path path) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public CompletableFuture<String> getFileUrl(Path path) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .bucket(bucketName)
                                .object(path.toString())
                                .method(Method.GET)
                                .expiry(1, TimeUnit.HOURS) //expires in 1 hour
                                .build()
                );
            } catch (MinioException e) {
                throw new RuntimeException("Error generating file URL from MinIO", e);
            } catch (Exception e) {
                throw new RuntimeException("Unexpected error", e);
            }
        });
    }

    @Override
    public CompletableFuture<Path> getLatestFileByPattern(Path directory, Pattern pattern) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public CompletableFuture<Path> waitForFileByPattern(Path directory, Pattern pattern, long timeout) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public CompletableFuture<Void> renameFile(Path directory, String oldName, String newName) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Step 1: Copy the existing file to a new file with the desired name
                minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(bucketName)
                                .object(newName)
                                .source(CopySource.builder()
                                        .bucket(bucketName)
                                        .object(oldName)
                                        .build())
                                .build()
                );

                // Step 2: Delete the old file
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(oldName)
                                .build()
                );

            } catch (MinioException e) {
                throw new RuntimeException("Error renaming file in MinIO", e);
            } catch (Exception e) {
                throw new RuntimeException("Unexpected error", e);
            }
        });
    }
}
