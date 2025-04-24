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



//package com.image_description.core.service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ArrayNode;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import lombok.AllArgsConstructor;
//import lombok.NoArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Base64;
//
//@Service
//public class ImageDescriptionService {
//
//    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
//
//    private final RestTemplate restTemplate;
//    private final String apiKey;
//    private final ObjectMapper objectMapper;
//
//    @Autowired
//    public ImageDescriptionService(RestTemplate restTemplate, String openaiApiKey) {
//        this.restTemplate = restTemplate;
//        this.apiKey = openaiApiKey;
//        this.objectMapper = new ObjectMapper();
//    }
//
//    public String analyzeImage(MultipartFile image) throws Exception {
//        // Converter a imagem para base64
//        byte[] imageBytes = image.getBytes();
//        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
//
//        // Criar o corpo da requisição
//        ObjectNode rootNode = objectMapper.createObjectNode();
//        rootNode.put("model", "gpt-4");
//        rootNode.put("max_tokens", 300);
//
//        ArrayNode messagesArray = objectMapper.createArrayNode();
//        ObjectNode messageNode = objectMapper.createObjectNode();
//        messageNode.put("role", "user");
//
//        ArrayNode contentArray = objectMapper.createArrayNode();
//
//        ObjectNode textPart = objectMapper.createObjectNode();
//        textPart.put("type", "text");
//        textPart.put("text", "Por favor, descreva detalhadamente esta imagem.");
//        contentArray.add(textPart);
//
//        ObjectNode imagePart = objectMapper.createObjectNode();
//        imagePart.put("type", "image_url");
//
//        ObjectNode imageUrl = objectMapper.createObjectNode();
//        imageUrl.put("url", "data:image/" + getImageExtension(image) + ";base64," + base64Image);
//        imagePart.set("image_url", imageUrl);
//
//        contentArray.add(imagePart);
//        messageNode.set("content", contentArray);
//        messagesArray.add(messageNode);
//        rootNode.set("messages", messagesArray);
//
//        // Configurar headers
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setBearerAuth(apiKey);
//        headers.add("OpenAI-Beta", "vision-preview");
//
//        // Fazer a requisição para a API do OpenAI
//        HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(rootNode), headers);
//        ResponseEntity<String> response = restTemplate.exchange(OPENAI_API_URL, HttpMethod.POST, requestEntity, String.class);
//
//        // Processar a resposta
//        JsonNode responseBody = objectMapper.readTree(response.getBody());
//        String description = responseBody
//                .path("choices")
//                .path(0)
//                .path("message")
//                .path("content")
//                .asText();
//
//        return description;
//    }
//
//    private String getImageExtension(MultipartFile file) {
//        String contentType = file.getContentType();
//        if (contentType != null) {
//            return contentType.split("/")[1];
//        }
//        return "jpeg";  // default
//    }
//}
