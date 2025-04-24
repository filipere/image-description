package com.image_description.core.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ImageDescriptionResponse {

    private String description;

    public ImageDescriptionResponse(String description) {
        this.description = description;
    }
}
