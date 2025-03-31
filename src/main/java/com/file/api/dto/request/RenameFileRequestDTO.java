package com.file.api.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RenameFileRequestDTO {
    @NotNull
    @NotEmpty
    private String oldFileName;

    @NotNull
    @NotEmpty
    private String newFileName;

    @AssertTrue(message = "old and new names must be different")
    private boolean isSameName() {
        return !newFileName.equals(oldFileName);
    }
}