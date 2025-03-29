package com.image.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RenameImageResponseDTO extends BaseResponseDTO {
    private String newName;

    public RenameImageResponseDTO(String status, String message, String newName) {
        super(status, message);
        this.newName = newName;
    }
}