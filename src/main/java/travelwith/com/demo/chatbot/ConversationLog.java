package travelwith.com.demo.chatbot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationLog implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userId;
    private String timestamp;
    private String message;
    private String response;
    // Getters and setters
}
