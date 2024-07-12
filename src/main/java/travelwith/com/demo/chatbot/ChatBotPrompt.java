package travelwith.com.demo.chatbot;

import lombok.Data;

@Data
public class ChatBotPrompt {

    private final String token;
    private final String prompt;
    private final String historyLog;

    public ChatBotPrompt(String token,String prompt, String historyLog) {
        this.token = token;
        this.prompt = "<PROMPT>" + prompt + "</PROMPT>\n\n <HISTORY_LOG>를 고려해서 <PROMPT> 에 대해 대답해라.";
        this.historyLog = "<HISTORY_LOG>" + historyLog + "</HISTORY_LOG>";
    }
}
