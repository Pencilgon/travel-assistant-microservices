package com.aichat.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiVoiceChatService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final ObjectMapper mapper = new ObjectMapper();

    public String transcribeAudio(MultipartFile audio) {
        try {
            String boundary = "---011000010111000001101001";
            String contentType = audio.getContentType() != null ? audio.getContentType() : "audio/webm";

            String metadataPart = "--%s\r\nContent-Disposition: form-data; name=\"model\"\r\n\r\nwhisper-1\r\n"
                    .formatted(boundary);

            String filePartHeader = "--%s\r\nContent-Disposition: form-data; name=\"file\"; filename=\"audio.webm\"\r\nContent-Type: %s\r\n\r\n"
                    .formatted(boundary, contentType);

            byte[] metadataBytes = metadataPart.getBytes(StandardCharsets.UTF_8);
            byte[] fileHeaderBytes = filePartHeader.getBytes(StandardCharsets.UTF_8);
            byte[] fileContentBytes = audio.getBytes();
            byte[] endBytes = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);

            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofByteArrays(List.of(
                    metadataBytes,
                    fileHeaderBytes,
                    fileContentBytes,
                    endBytes
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/audio/transcriptions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(body)
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Whisper raw response: " + response.body());

            JsonNode json = mapper.readTree(response.body());
            return json.path("text").asText();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String askChat(String message) {
        try {
            String systemPrompt = "Ты — туристический помощник. Отвечай кратко, чётко и только по теме: визы, жильё, транспорт, культура, адаптация.";
            String requestBody = """
                {
                  "model": "gpt-4",
                  "messages": [
                    { "role": "system", "content": "%s" },
                    { "role": "user", "content": "%s" }
                  ]
                }
            """.formatted(systemPrompt.replace("\"", "\\\""), message.replace("\"", "\\\""));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode json = mapper.readTree(response.body());
            return json.get("choices").get(0).path("message").path("content").asText();

        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка генерации ответа: " + e.getMessage();
        }
    }

    public byte[] textToSpeech(String text) {
        try {
            // Создаём JSON корректно
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(Map.of(
                    "model", "tts-1",
                    "input", text,
                    "voice", "alloy"
            ));

            System.out.println("TTS JSON Body: " + jsonBody); // лог запроса

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/audio/speech"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            System.out.println("TTS Response Code: " + response.statusCode());
            if (response.statusCode() != 200) {
                System.out.println("TTS Error Body: " + new String(response.body(), StandardCharsets.UTF_8));
                return null;
            }

            return response.body();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private HttpRequest.BodyPublisher ofMimeMultipartData(MultipartFile file) throws Exception {
        String boundary = "---011000010111000001101001";
        String contentType = file.getContentType() != null ? file.getContentType() : "audio/webm";

        String filePartHeader = "--%s\r\nContent-Disposition: form-data; name=\"file\"; filename=\"audio.webm\"\r\nContent-Type: %s\r\n\r\n"
                .formatted(boundary, contentType);

        byte[] fileHeaderBytes = filePartHeader.getBytes(StandardCharsets.UTF_8);
        byte[] fileContentBytes = file.getBytes();
        byte[] endBytes = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);

        return HttpRequest.BodyPublishers.ofByteArrays(List.of(fileHeaderBytes, fileContentBytes, endBytes));
    }
}
