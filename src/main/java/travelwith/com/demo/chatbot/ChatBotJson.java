package travelwith.com.demo.chatbot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatBotJson {

    private String s_id;
    private String p_text;

    public ChatBotJson(String s_id, String p_text) {
        this.s_id = s_id;
        this.p_text = p_text;
    }
}
