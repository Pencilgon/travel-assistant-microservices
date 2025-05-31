package com.aichat.chatbot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*") // если фронт на другом порту
public class ChatController {

    @Autowired
    private OpenAiService openAiService;
    @Autowired
    private OpenAiVoiceChatService openAiVoiceChatService;

    @PostMapping("/ask")
    public ResponseEntity<String> ask(
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "message", required = false) String message
    ) {
        String response = openAiService.askSmart(image, message);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/voice-chat")
    public ResponseEntity<?> voiceChat(@RequestParam("audio") MultipartFile audio) {
        try {
            String text = openAiVoiceChatService.transcribeAudio(audio);
            if (text == null || text.isBlank()) {
                return ResponseEntity.badRequest().body("Failed to recognize audio.");
            }

            System.out.println("Transcribed text: " + text);

            String reply = openAiVoiceChatService.askChat(text);
            byte[] audioReply = openAiVoiceChatService.textToSpeech(reply);

            if (audioReply == null || audioReply.length == 0) {
                return ResponseEntity.internalServerError().body("TTS generation failed.");
            }

            System.out.println("Final audio byte length: " + audioReply.length);

            return ResponseEntity.ok()
                    .header("Content-Type", "audio/mpeg")
                    .header("Content-Disposition", "inline; filename=response.mp3")
                    .body(audioReply);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal error: " + e.getMessage());
        }
    }
}