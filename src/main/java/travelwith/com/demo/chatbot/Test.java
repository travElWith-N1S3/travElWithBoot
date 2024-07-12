package travelwith.com.demo.chatbot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Controller
@Slf4j
public class Test {

//    private String apiKey ="NOIuhKglPd5RfLiAL4yOP4BwcRjfN2kF270Wlllx";
private String apiGatewayUrl = "https://wrrvutyink.execute-api.us-west-2.amazonaws.com/dev/prompt";


    @RequestMapping("/ssss")
    public String test() throws JsonProcessingException {
//        log.info("prompt = {}", prompt);
        try {
            ChatBotJson chatBotJson = new ChatBotJson("21321","서울에 대해 알려줘");
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(chatBotJson);

            DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(apiGatewayUrl);
            factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

            WebClient client = WebClient.builder()
                    .baseUrl(apiGatewayUrl)
//                .defaultHeader("x-api-key", apiKey)
                    .build();

            CompletableFuture<String> stringMono = client.post()
                    .body(BodyInserters.fromValue(json))
                    .retrieve()
                    .bodyToMono(String.class).toFuture();

            stringMono.thenApply(response->{
                return response;
            });

//            return stringMono.thenApply(response -> {
//                String responseText = response;
//                return responseText;
//            }).exceptionally(ex -> {
//                ex.printStackTrace();
//                return null;
//            });

        } catch (Exception e) {
            e.printStackTrace();
//            return CompletableFuture.failedFuture(e);
        }
        return "dd";
    }
}
