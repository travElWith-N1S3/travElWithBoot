package travelwith.com.demo.chatbot;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


/*
 * WebSocket Handler 작성
 * 소켓 통신은 서버와 클라이언트가 1:n으로 관계를 맺는다. 따라서 한 서버에 여러 클라이언트 접속 가능
 * 서버에는 여러 클라이언트가 발송한 메세지를 받아 처리해줄 핸들러가 필요
 * TextWebSocketHandler를 상속받아 핸들러 작성
 * 클라이언트로 받은 메세지를 log로 출력하고 클라이언트로 환영 메세지를 보내줌
 * */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketChatHandler extends TextWebSocketHandler {
    private final Set<WebSocketSession> sessionsSet = new HashSet<>();
    private final ChatBotService chatBotService;
    private final JsonParser jsonParser;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionsSet.add(session);
        log.info("{} 연결됨", session.getId());
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        System.out.println(message.getPayload());
        JsonObject parse = (JsonObject) jsonParser.parse(message.getPayload());
        String id = parse.get("id").toString();
        String prompt = parse.get("text").toString();
        String conversations = chatBotService.getConversations(id);
        ChatBotPrompt chatBotPrompt = new ChatBotPrompt(id, prompt, conversations);
        chatBotService.chatWithBedrock(id, chatBotPrompt);
        String answer = "";
        while (true){
            System.out.println("찾는중");
            answer = chatBotService.pullingSQSMessage(id);
            if(!answer.isEmpty()) break;
        }
        System.out.println(answer);
        System.out.println(session);
        session.sendMessage(new TextMessage(answer));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("{} 연결 끊김", session.getId());
        sessionsSet.remove(session);
    }


//    private void sendMessageToChatRoom(WebSocketSession sessions, String answer) {
//        sendMessage(sessions, answer);
//    }
//
//    /**
//     * [websocket 에 메세지 전송]
//     *
//     * @param session (메세지를 받을 websocket 세션)
//     * @param message (전송될 메세지)
//     */
//    public <T> void sendMessage(WebSocketSession session, String message) {
//        try {
//            session.sendMessage(new TextMessage(message));
//        } catch (IOException e) {
//            log.error(e.getMessage(), e);
//        }
//    }
}