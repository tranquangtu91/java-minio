package com.image.api.controllers;

import com.image.api.adapter.ImageAdapter;
import com.image.api.dto.request.CopyImageRequestDTO;
import com.image.api.dto.request.RenameImageRequestDTO;
import com.image.api.dto.response.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/minio")
public class ImageController {

    @Autowired
    private final ImageAdapter imageAdapter;

    public ImageController(ImageAdapter imageAdapter) {
        this.imageAdapter = imageAdapter;
    }


    @PostMapping("/upload")
    public CompletableFuture<ResponseEntity<ImageUploadResponseDTO>> uploadFile(@RequestBody MultipartFile file) throws IOException {
        return imageAdapter.writeFileSync(Paths.get(file.getOriginalFilename()), file.getBytes(), null)
                .thenApply(voidResult -> ResponseEntity.ok(new ImageUploadResponseDTO("success", "File uploaded successfully", file.getOriginalFilename())))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ImageUploadResponseDTO("error", "Error uploading file: " + ex.getMessage(), null)));
    }

    @GetMapping("/download")
    public CompletableFuture<ResponseEntity<byte[]>> downloadFile(@RequestParam String fileName) throws IOException {
        Path filePath = Paths.get(fileName);

        return imageAdapter.readFileSync(filePath, null, null)
                .thenApply(content -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(content))
                .exceptionally(ex -> ResponseEntity.status(500)
                        .body(("Download failed: " + ex.getMessage()).getBytes()));
    }

    @PostMapping("/copy")
    public CompletableFuture<ResponseEntity<BaseResponseDTO>> copyFile(
            @Valid @RequestBody CopyImageRequestDTO copyImageRequestDTO) throws IOException {

        Path sourcePath = Paths.get(copyImageRequestDTO.getSource());
        Path destPath = Paths.get(copyImageRequestDTO.getDestination());
        return imageAdapter.copyFileSync(sourcePath, destPath, 0)
                .thenApply(voidResult -> ResponseEntity.ok(new BaseResponseDTO("success","File copied successfully from " + sourcePath.toString() + " → " + destPath.toString())))
                .exceptionally(ex -> ResponseEntity.status(500).body(new BaseResponseDTO("error","Error: " + ex.getMessage())));
    }

    @DeleteMapping("/delete")
    public CompletableFuture<ResponseEntity<BaseResponseDTO>> deleteFile(@RequestParam String fileName) throws IOException {
        Path filePath = Paths.get(fileName);

        return imageAdapter.rmdirSync(filePath)
                .thenApply(url -> ResponseEntity.ok(new BaseResponseDTO("success", "Delete File successfully: " + fileName)))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new BaseResponseDTO("error", ex.getMessage())));
    }

    @GetMapping("/url")
    public CompletableFuture<ResponseEntity<GetFileUrlResponseDTO>> getFileUrl(@RequestParam String fileName) {
        Path filePath = Paths.get(fileName);
        return imageAdapter.getFileUrl(filePath)
                .thenApply(url -> ResponseEntity.ok(new GetFileUrlResponseDTO("success", "Get File url successfully", url)))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new GetFileUrlResponseDTO("error", "Error fetching file URL: " + ex.getMessage(), null)));
    }

    @PutMapping("/rename")
    public CompletableFuture<ResponseEntity<RenameImageResponseDTO>> renameFile(@Valid @RequestBody RenameImageRequestDTO renameImageRequestDTO) throws IOException {
        return imageAdapter.renameFile(null, renameImageRequestDTO.getOldFileName(), renameImageRequestDTO.getNewFileName())
                .thenApply(voidResult -> ResponseEntity.ok(
                        new RenameImageResponseDTO("success", "File renamed successfully", renameImageRequestDTO.getNewFileName())
                ))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new RenameImageResponseDTO("error", "Error renaming file: " + ex.getMessage(), null)));
    }
}
