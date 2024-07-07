package com.tour.chatbot;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    /**
     * 챗봇 페이지 접근시 쿠키 발행.
     * 쿠키 저장소는 추후 redis 로 변경. 12시간마다 초기화 되도록 설정.
     * 쿠키 유효성 체크. 통과 못하면 질문 못함.
     */
    @GetMapping("/chatbot")
    public String chattingPage(@CookieValue(value = "ask_token", required = false) Cookie cookie, HttpServletResponse response) {
        System.out.println("ChatBotController.chattingPage");

        if (cookie == null) {
            UUID uuid = UUID.randomUUID();
            Cookie token = new Cookie("ask_token", uuid.toString());
            token.setMaxAge(43200);  // 12시간
            response.addCookie(token);

            if (!askTokenStorage.contains(uuid.toString())) {
                askTokenStorage.put(uuid.toString(), 0);
            }
        } else {
            boolean cookieValid = chatBotService.validateCookie(cookie, askTokenStorage.get(cookie.getValue()));
            if (!cookieValid) {
                return "0";
            }
        }
        return "1";
    }

    @GetMapping("/chatbot/chatting")
    public DeferredResult<String> chatWithBedrock(@RequestParam("prompt") String prompt,
                                                  @CookieValue("ask_token") Cookie token,
                                                  HttpServletResponse response) {

        log.info("token value = {}", token.getValue());
        DeferredResult<String> deferredResult = new DeferredResult<>();

        if(lockSet.contains(token.getValue())){
            log.info("잠긴 상태 호출. 무시.");
            deferredResult.setResult("");
            return deferredResult;
        }else {
            log.info("잠김");
            lockSet.add(token.getValue());
        }

        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        if (prompt.isEmpty()) {
            deferredResult.setResult("");
            return deferredResult;
        }

        try {
            int askCount = askTokenStorage.get(token.getValue());
            if (askCount >= ASK_MAX) {
                deferredResult.setResult("질문 횟수가 끝났습니다. 12시간 후에 다시 질문해주세요.");
                return deferredResult;
            }

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

        } catch (NullPointerException e) { // askTokenStorage 에 저장되지 않은 쿠키값으로 접근시
            log.info("유효하지 않은 쿠키로 채팅 시도");
            deferredResult.setResult("유효하지 않은 접근입니다.");
        }
        return deferredResult;
    }

}
