package com.file.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetFileUrlResponseDTO extends BaseResponseDTO {
    private String fileUrl;

    public GetFileUrlResponseDTO(String status, String message, String fileUrl) {
        super(status, message);
        this.fileUrl = fileUrl;
    }
}
