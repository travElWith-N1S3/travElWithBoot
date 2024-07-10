package travelwith.com.demo.chatbot;

import lombok.Data;

@Data
public class ChatBotPrompt {

    private final String token;
    private final String prompt;
    private final String historyLog;

    public ChatBotPrompt(String token,String prompt, String historyLog) {
        this.token = token;
        this.prompt = "<PROMPT>" + prompt + "</PROMPT>";
        this.historyLog = "<HISTORY_LOG>" + historyLog + "</HISTORY_LOG>";
    }
}
