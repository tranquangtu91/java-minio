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
public class CopyFileRequestDTO {
    @NotNull(message = "source file is not null")
    @NotEmpty(message = "source file not empty")
    private String source;

    @NotNull(message = "destination file is not null")
    @NotEmpty(message = "source file not empty")
    private String destination;

    @AssertTrue(message = "source and destination paths must be different")
    private boolean isSamePath() {
        return !source.equals(destination);
    }
}

