package travelwith.com.demo.chatbot;


import com.google.gson.JsonElement;
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
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


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
    private final CopyOnWriteArraySet<String> lockSet = new CopyOnWriteArraySet<>();
    private final int ASK_MAX = 10;
    private final String NO_ANSWER = "";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionsSet.add(session);
        log.info("{} 연결됨", session.getId());
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {

        String answer = "";
        Double askCount = 0d;

        JsonObject parse = (JsonObject) jsonParser.parse(message.getPayload());
        String id = parse.get("id").toString().replaceAll("\"","")
                .replaceAll("\"","");
        String prompt = parse.get("text").toString();

        try {// askTokenStorage 에 저장된 쿠키값인지 확인
            askCount = chatBotService.getAskCount(id);
            if(askCount == null){
                throw new NullPointerException();
            }
        } catch (NullPointerException e) {
            log.info("유효하지 않은 쿠키로 채팅 시도");
            session.sendMessage(new TextMessage(NO_ANSWER));
            return;
        }

        String countCheck = checkAskCount(askCount);
        if (countCheck != null) { // 질문 횟수 체크
            session.sendMessage(new TextMessage(countCheck));
            return;
        }

        if (prompt.isEmpty()) { // 질문 내용 공백 체크'
            session.sendMessage(new TextMessage(NO_ANSWER));
            return;
        }

        if (lockSet.contains(id)) { // lock 이 잠겼는지 체크
            log.info("잠긴 상태 호출. 무시.");
            return ;
        }

        String conversations = chatBotService.getConversations(id);
        ChatBotPrompt chatBotPrompt = new ChatBotPrompt(id, prompt, conversations);
        chatBotService.increaseAskCount(id);

        // 챗봇 질문시 lock 잠그기
        log.info("잠김");
        lockSet.add(id);
        chatBotService.chatWithBedrock(id, chatBotPrompt);
        while (true){
            answer = chatBotService.pullingSQSMessage(id);
            if(!answer.isEmpty()) break;
        }
        JsonObject parsed = (JsonObject) jsonParser.parse(answer);
        JsonObject body =(JsonObject)parsed.get("body");
        answer = body.get("p_text").toString();
        ConversationLog conversationLog = new ConversationLog(id, LocalDateTime.now().toString(), prompt, answer);
        chatBotService.saveConversation(id, conversationLog);
        session.sendMessage(new TextMessage(answer));
        lockSet.remove(id);
        log.info("잠김 해제");
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

    private String checkAskCount(Double askCount) {
        if ( askCount >= ASK_MAX) {
            return "질문 횟수가 끝났습니다. 12시간 후에 다시 질문해주세요.";
        }
        return null;
    }
}