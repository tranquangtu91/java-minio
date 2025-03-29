package com.image.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ImageUploadResponseDTO extends BaseResponseDTO {
    private String fileName;

    public ImageUploadResponseDTO(String status, String message, String fileName) {
        super(status, message);
        this.fileName = fileName;
    }
}
