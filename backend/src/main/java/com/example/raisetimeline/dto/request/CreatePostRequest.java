package com.example.raisetimeline.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePostRequest {

    @Size(max = 140)
    private String content;

    private String imageUrl;
}
