package com.file.api.controllers;

import com.file.api.adapter.FileAdapter;
import com.file.api.dto.request.CopyFileRequestDTO;
import com.file.api.dto.response.BaseResponseDTO;
import com.file.api.dto.response.GetFileUrlResponseDTO;
import com.file.api.dto.response.FileUploadResponseDTO;
import com.file.api.dto.request.RenameFileRequestDTO;
import com.file.api.dto.response.RenameFileResponseDTO;
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
public class FileController {

    @Autowired
    private final FileAdapter imageAdapter;

    public FileController(FileAdapter imageAdapter) {
        this.imageAdapter = imageAdapter;
    }


    @PostMapping("/upload")
    public CompletableFuture<ResponseEntity<FileUploadResponseDTO>> uploadFile(@RequestBody MultipartFile file, @RequestParam(value = "path", required = false) String path) throws IOException {
        // Ensure path ends with "/"
        String filePath = (path != null && !path.isEmpty()) ? path.replaceAll("/+$", "") + "/" : "";

        return imageAdapter.writeFileSync(Paths.get(file.getOriginalFilename()), file.getBytes(), filePath)
                .thenApply(voidResult -> ResponseEntity.ok(new FileUploadResponseDTO("success", "File uploaded successfully", file.getOriginalFilename())))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new FileUploadResponseDTO("error", "Error uploading file: " + ex.getMessage(), null)));
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
            @Valid @RequestBody CopyFileRequestDTO copyImageRequestDTO) throws IOException {

        Path sourcePath = Paths.get(copyImageRequestDTO.getSource());
        Path destPath = Paths.get(copyImageRequestDTO.getDestination());
        return imageAdapter.copyFileSync(sourcePath, destPath, 0)
                .thenApply(voidResult -> ResponseEntity.ok(new BaseResponseDTO("success","File copied successfully from " + sourcePath.toString() + " → " + destPath.toString())))
                .exceptionally(ex -> ResponseEntity.status(500).body(new BaseResponseDTO("error","Error: " + ex.getMessage())));
    }

    @DeleteMapping("/delete")
    public CompletableFuture<ResponseEntity<BaseResponseDTO>> deleteFile(@RequestParam String fileName) throws IOException {
        Path filePath = Paths.get(fileName);

        return imageAdapter.rmSync(filePath)
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
    public CompletableFuture<ResponseEntity<RenameFileResponseDTO>> renameFile(@Valid @RequestBody RenameFileRequestDTO renameImageRequestDTO) throws IOException {
        return imageAdapter.renameFile(null, renameImageRequestDTO.getOldFileName(), renameImageRequestDTO.getNewFileName())
                .thenApply(voidResult -> ResponseEntity.ok(
                        new RenameFileResponseDTO("success", "File renamed successfully", renameImageRequestDTO.getNewFileName())
                ))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new RenameFileResponseDTO("error", "Error renaming file: " + ex.getMessage(), null)));
    }
}
