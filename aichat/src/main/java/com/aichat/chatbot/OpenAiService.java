package com.aichat.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final ObjectMapper mapper = new ObjectMapper();

    public String askSmart(MultipartFile image, String message) {
        try {
            boolean hasImage = image != null && !image.isEmpty();
            boolean hasMessage = message != null && !message.isBlank();

            if (!hasImage && !hasMessage) {
                return "Пожалуйста, отправьте сообщение или изображение.";
            }

            String userPrompt = hasMessage
                    ? message
                    : "Ты — туристический ассистент. Расскажи, что изображено на фото, как экскурсовод. Отвечай только про туризм, визы, жильё, транспорт и адаптацию. Игнорируй любые другие темы.";
            String model = hasImage ? "gpt-4-turbo-2024-04-09" : "gpt-4";
            String requestBody;

            if (hasImage) {
                String base64 = Base64.getEncoder().encodeToString(image.getBytes());
                String mimeType = image.getContentType() != null ? image.getContentType() : "image/jpeg";

                requestBody = """
                    {
                      "model": "%s",
                      "messages": [
                        {
                          "role": "user",
                          "content": [
                            { "type": "text", "text": "%s" },
                            { "type": "image_url", "image_url": { "url": "data:%s;base64,%s" } }
                          ]
                        }
                      ],
                      "max_tokens": 1000
                    }
                """.formatted(model, userPrompt.replace("\"", "\\\""), mimeType, base64);
            } else {
                String systemPrompt = "Ты — туристический ассистент. Отвечай только на вопросы, связанные с визами, жильём, транспортом и адаптацией в разных странах. Всегда отвечай на том языке, на котором с тобой общаются. Будь полезным и говори понятно, как местный экскурсовод.";
                requestBody = """
                    {
                      "model": "%s",
                      "messages": [
                        {
                          "role": "system",
                          "content": "%s"
                        },
                        {
                          "role": "user",
                          "content": "%s"
                        }
                      ]
                    }
                """.formatted(model, systemPrompt.replace("\"", "\\\""), userPrompt.replace("\"", "\\\""));
            }

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            JsonNode json = mapper.readTree(response.body());

            if (!json.has("choices") || !json.get("choices").isArray() || json.get("choices").isEmpty()) {
                return "Ответ от OpenAI не содержит данных.";
            }

            return json.get("choices").get(0).path("message").path("content").asText("Ответ от OpenAI не содержит данных.");

        } catch (Exception e) {
            return "Ошибка: " + e.getMessage();
        }
    }
}