package com.image_description.adapters.controller;

import com.image_description.core.dtos.ImageDescriptionResponse;
import com.image_description.core.service.ImageDescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/ai/images")
public class ImageDescriptionController {

    private final ImageDescriptionService service;

    @Autowired
    public ImageDescriptionController(ImageDescriptionService service) {
        this.service = service;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageDescriptionResponse> analyzeImage(@RequestParam("image") MultipartFile image) {
        try {
            // Chama o serviço para analisar a imagem
            String description = service.analyzeImage(image);
            // Retorna a descrição da imagem no corpo da resposta
            return ResponseEntity.ok(new ImageDescriptionResponse(description));
        } catch (Exception e) {
            // Retorna um erro, caso haja uma falha na análise
            return ResponseEntity.badRequest().body(new ImageDescriptionResponse("Erro ao analisar imagem: " + e.getMessage()));
        }
    }
}