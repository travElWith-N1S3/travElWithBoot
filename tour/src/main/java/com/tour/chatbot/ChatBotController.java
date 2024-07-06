package com.tour.chatbot;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1")
public class ChatBotController {
    private final ChatBotService chatBotService;
    private final ChatBotLock chatBotLock;

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

//    @GetMapping("/chatbot/chatting")
//    public String chatWithBedrock(@RequestParam("prompt") String prompt,
//                                  @CookieValue("ask_token") Cookie token)
//            throws ExecutionException, InterruptedException {

//        log.info("token value = {}", token.getValue());
//        String answer = "";
//
//        if (prompt.isEmpty()) {
//            return  "";
//        }
//
//        try {
//            int askCount = askTokenStorage.get(token.getValue());
//            if (askCount >= ASK_MAX) {
//                return "질문 횟수가 끝났습니다. 12시간 후에 다시 질문해주세요.";
//            }
//        CompletableFuture<String> futureAnswer = chatBotService.chatWithBedrock(prompt);
//        answer = futureAnswer.get();
//            askTokenStorage.put(token.getValue(), ++askCount);
//            log.info("response = {}", answer);
//
//        } catch (NullPointerException e) { // askTokenStorage 에 저장되지 않은 쿠키값으로 접근시
//            log.info("유효하지 않은 쿠키로 채팅 시도");
//            answer = "유효하지 않은 접근입니다.";
//        }
//
//        String a = answer;
//        return answer;
//    }

    @GetMapping("/chatbot/chatting")
    public DeferredResult<String> chatWithBedrock(@RequestParam("prompt") String prompt,
                                  @CookieValue("ask_token") Cookie token)
            throws ExecutionException, InterruptedException {
//        System.out.println("ChatBotController.chatWithBedrock");
//        return chatBotService.chatWithBedrock(prompt).get();
//
        System.out.println("ChatBotController.chatWithBedrock");

        DeferredResult<String> deferredResult = new DeferredResult<>();
        chatBotService.chatWithBedrock(prompt).whenComplete((result, throwable) -> {
            if (throwable != null) {
                deferredResult.setErrorResult(throwable);
            } else {
                deferredResult.setResult(result);
            }
        });

        return deferredResult;
    }
}
