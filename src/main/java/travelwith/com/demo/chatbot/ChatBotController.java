package travelwith.com.demo.chatbot;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1")
public class ChatBotController {
    private final ChatBotService chatBotService;
    private final CopyOnWriteArraySet<String> lockSet = new CopyOnWriteArraySet<>();
    private final int ASK_MAX = 10;
    private final String NO_ANSWER = "";

    /**
     * 챗봇 페이지 접근시 쿠키 발행.
     * 쿠키 저장소는 추후 redis 로 변경하여 12시간마다 초기화 되도록 설정.
     * 쿠키 유효성 체크. 통과 못하면 질문 못함.
     */
    @GetMapping("/chatbot")
    public String chattingPage(@CookieValue(value = "ask_token", required = false) Cookie cookie, HttpServletResponse response) {

        if (cookie == null) {// 사용자에게 쿠키가 없으면 쿠키 새롭게 발급
            UUID uuid = UUID.randomUUID();
            chatBotService.setTokenCookie(response, uuid);
            chatBotService.saveNewCookie(uuid.toString());
            chatBotService.saveConversation(uuid.toString(), new ConversationLog(uuid.toString(), LocalDateTime.now().toString(), "대화 시작", "대화를 시작합니다"));

        } else { // 사용자가 쿠키 보유하고 있을시 쿠키 유효성 검사
            String cookieValue = cookie.getValue();
            try {
                boolean cookieValid = chatBotService.validateCookie(cookie, chatBotService.getAskCount(cookieValue));
                if (!cookieValid) { // 쿠키가 이상하면 0 반환. 정상이면 1반환
                    return "0";
                }
            } catch (NullPointerException e) {
                return "0";
            }
        }
        return "1";
    }
}