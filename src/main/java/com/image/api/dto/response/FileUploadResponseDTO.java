package com.file.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileUploadResponseDTO extends BaseResponseDTO {
    private String fileName;

    public FileUploadResponseDTO(String status, String message, String fileName) {
        super(status, message);
        this.fileName = fileName;
    }
}
