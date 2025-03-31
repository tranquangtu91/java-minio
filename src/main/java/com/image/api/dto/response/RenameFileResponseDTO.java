package com.file.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RenameFileResponseDTO extends BaseResponseDTO {
    private String newName;

    public RenameFileResponseDTO(String status, String message, String newName) {
        super(status, message);
        this.newName = newName;
    }
}