package travelwith.com.demo.chatbot;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1")
public class ChatBotController {
    private final ChatBotService chatBotService;
    private final CopyOnWriteArraySet<String> lockSet = new CopyOnWriteArraySet<>();

    // redis 로 변경 필요.
    private final ConcurrentHashMap<String, Integer> askTokenStorage = new ConcurrentHashMap<>();
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
            askTokenStorage.put(uuid.toString(), 0);

        } else { // 사용자가 쿠키 보유하고 있을시 쿠키 유효성 검사
            boolean cookieValid = chatBotService.validateCookie(cookie, askTokenStorage.get(cookie.getValue()));

            if (!cookieValid) { // 쿠키가 이상하면 0 반환. 정상이면 1반환
                return "0";
            }
        }
        return "1";
    }

    @GetMapping("/chatbot/chatting")
    public DeferredResult<String> chatWithBedrock(@RequestParam("prompt") String prompt,
                                                  @CookieValue("ask_token") Cookie token) {
        // 주의점
        // 1. 자바에서 외부 api 호출은 기본적으로 동기식. 따로 설정해줘야 함.
        // 2. 비동기로 구현했으나, prompt가 같은 요청은 동기식으로 작동함. 이유는 모르겠음.

        log.info("token value = {}", token.getValue());
        DeferredResult<String> deferredResult = new DeferredResult<>();

        int askCount = 0;

        try {// askTokenStorage 에 저장된 쿠키값인지 확인
            askCount = askTokenStorage.get(token.getValue());
        } catch (NullPointerException e) {
            log.info("유효하지 않은 쿠키로 채팅 시도");
            deferredResult.setResult("유효하지 않은 접근입니다.");
        }

        String countCheck = checkAskCount(askCount);
        if (countCheck != null) { // 질문 횟수 체크
            deferredResult.setResult(countCheck);
            return deferredResult;
        }

        if (prompt.isEmpty()) { // 질문 내용 공백 체크
            deferredResult.setResult(NO_ANSWER);
            return deferredResult;
        }

        if (lockSet.contains(token.getValue())) { // lock 이 잠겼는지 체크
            log.info("잠긴 상태 호출. 무시.");
            deferredResult.setResult(NO_ANSWER);
            return deferredResult;
        }

        // 챗봇 질문시 lock 잠그기
        log.info("잠김");
        lockSet.add(token.getValue());

        // 챗봇 호출
        chatBotService.chatWithBedrock(prompt).whenComplete((result, throwable) -> {
            if (throwable != null) {
                deferredResult.setErrorResult(throwable);
            } else {
                deferredResult.setResult(result);
                lockSet.remove(token.getValue());
                log.info("잠김 해제");
            }
        });

        askTokenStorage.put(token.getValue(), ++askCount);

        return deferredResult;
    }

    private String checkAskCount(int askCount) {
        if (askCount >= ASK_MAX) {
            return "질문 횟수가 끝났습니다. 12시간 후에 다시 질문해주세요.";
        }
        return null;
    }

}
