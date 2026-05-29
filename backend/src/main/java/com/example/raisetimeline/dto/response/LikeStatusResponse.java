package com.example.raisetimeline.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeStatusResponse {
    private boolean liked;
    private long count;
}
