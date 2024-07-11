package travelwith.com.demo.chatbot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationLog {
    private String userId;
    private String timestamp;
    private String message;
    private String response;
    // Getters and setters
}
