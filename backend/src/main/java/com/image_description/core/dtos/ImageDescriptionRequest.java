package com.image_description.core.dtos;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImageDescriptionRequest {

    private MultipartFile image;
}
