package com.image.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RenameImageRequestDTO {
    @NotNull
    @NotEmpty
    private String oldFileName;

    @NotNull
    @NotEmpty
    private String newFileName;
}