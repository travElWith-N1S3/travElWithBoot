package com.tour.chatbot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Controller
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @RequestMapping("/test")
    @ResponseBody
    public String test() throws ExecutionException, InterruptedException {
        System.out.println("TestController.test");
        CompletableFuture<String> test = testService.getTest();
        System.out.println(test.get());
        return test.get();
    }
}
