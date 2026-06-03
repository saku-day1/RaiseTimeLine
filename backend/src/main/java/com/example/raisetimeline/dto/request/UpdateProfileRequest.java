package com.example.raisetimeline.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @NotBlank
    @Size(min = 1, max = 50)
    private String displayName;

    @Size(max = 160)
    private String bio;
}
