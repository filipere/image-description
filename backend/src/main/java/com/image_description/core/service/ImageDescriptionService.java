package com.image_description.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@Service
public class ImageDescriptionService {

    private static final String GOOGLE_VISION_API_URL = "https://vision.googleapis.com/v1/images:annotate";

    private final RestTemplate restTemplate;
    private final String googleVisionApiKey;
    private final ObjectMapper objectMapper;

    @Autowired
    public ImageDescriptionService(RestTemplate restTemplate, String googleVisionApiKey) {
        this.restTemplate = restTemplate;
        this.googleVisionApiKey = googleVisionApiKey;
        this.objectMapper = new ObjectMapper();
    }

    public String analyzeImage(MultipartFile image) throws Exception {
        // Converter imagem para base64
        byte[] imageBytes = image.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // Criar JSON da requisição
        ObjectNode requestJson = objectMapper.createObjectNode();
        ArrayNode requestsArray = objectMapper.createArrayNode();

        ObjectNode imageNode = objectMapper.createObjectNode();
        imageNode.put("content", base64Image);

        ObjectNode featureNode = objectMapper.createObjectNode();
        featureNode.put("type", "LABEL_DETECTION");
        featureNode.put("maxResults", 5);

        ObjectNode requestItem = objectMapper.createObjectNode();
        requestItem.set("image", imageNode);

        ArrayNode featuresArray = objectMapper.createArrayNode();
        featuresArray.add(featureNode);
        requestItem.set("features", featuresArray);

        requestsArray.add(requestItem);
        requestJson.set("requests", requestsArray);

        // Configurar headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestJson.toString(), headers);

        // Fazer a requisição para a API do Google Vision
        String url = GOOGLE_VISION_API_URL + "?key=" + googleVisionApiKey;
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        // Interpretar resposta
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        JsonNode labels = jsonResponse.path("responses").path(0).path("labelAnnotations");

        if (!labels.isArray() || labels.size() == 0) {
            return "Nenhuma descrição foi encontrada para a imagem.";
        }

        StringBuilder descriptionBuilder = new StringBuilder("Descrição da imagem:\n");
        for (JsonNode label : labels) {
            String desc = label.path("description").asText();
            float score = (float) label.path("score").asDouble();
            descriptionBuilder.append("- ").append(desc).append(" (confiança: ").append(String.format("%.2f", score * 100)).append("%)\n");
        }

        return descriptionBuilder.toString();
    }
}