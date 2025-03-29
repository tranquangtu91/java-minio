package com.image.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CopyImageRequestDTO {
    @NotNull(message = "source file is not null")
    @NotEmpty(message = "source file not empty")
    private String source;

    @NotNull(message = "destination file is not null")
    @NotEmpty(message = "source file not empty")
    private String destination;
}

