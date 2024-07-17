package travelwith.com.demo.chatbot;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatBotService {
    private final RedisTemplate<String, ConversationLog> redisTemplate;
    private final RedisTemplate<String, String> redisStrTemplate;
    private final Gson gson;

    private final AmazonSQS amazonSQS;

    @Value("${bedrock.aws_access_key_id}")
    private String accessKey;

    @Value("${bedrock.aws_secret_access_key}")
    private String secretKey;
    private String apiGatewayUrl = "https://wrrvutyink.execute-api.us-west-2.amazonaws.com/dev/prompt";
    private String queueName = "travelwith-chatbot-queue";
    private final String tableName = "travelwith-chatbot-db";
    private final DynamoDB dynamoDB;

    @Async()
    public CompletableFuture<String> chatWithBedrock(String token, ChatBotPrompt prompt) {
        log.info("prompt = {}", prompt.getPrompt());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ChatBotJson chatBotJson = new ChatBotJson(token, objectMapper.writeValueAsString(prompt));
            String json = objectMapper.writeValueAsString(chatBotJson);

            DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(apiGatewayUrl);
            factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

            WebClient client = WebClient.builder()
                    .baseUrl(apiGatewayUrl)
                    .build();

            CompletableFuture<String> stringMono = client.post()
                    .body(BodyInserters.fromValue(json))
                    .retrieve()
                    .bodyToMono(String.class).toFuture();

            return stringMono.thenApply(response -> {
                return response;
            }).exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });

        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.failedFuture(e);
        }
    }

    public void saveNewCookie(String cookie) {
        redisStrTemplate.opsForZSet().add("cookies", cookie, 0);
        redisStrTemplate.expire("cookies", 12, TimeUnit.HOURS);
    }

    public Double getAskCount(String coookie) {
        return redisStrTemplate.opsForZSet().score("cookies", coookie);
    }

    public void increaseAskCount(String cookie) {
        redisStrTemplate.opsForZSet().incrementScore("cookies", cookie, 1);
    }

    public void saveConversation(String key, ConversationLog conversation) {
        ListOperations<String, ConversationLog> listOps = redisTemplate.opsForList();
        listOps.leftPush(key, conversation);  // 최신 대화를 왼쪽으로 추가
        listOps.trim(key, 0, 9);  // 리스트의 길이를 최대 10개로 유지
        redisTemplate.expire(key, 12, TimeUnit.HOURS);
    }

    public String getConversations(String key) {
        ListOperations<String, ConversationLog> listOps = redisTemplate.opsForList();
        return gson.toJson(listOps.range(key, 0, -1));
    }

    public boolean validateCookie(Cookie cookie, Double StorageValue) {
        try {
            UUID uuid = UUID.fromString(cookie.getValue());
            if (StorageValue == null) {
                log.info("저장되지 않은 쿠키");
                return false;
            }
        } catch (IllegalArgumentException e) {
            log.error("유효하지 않은 쿠키");
            return false;
        }
        return true;
    }

    public void setTokenCookie(HttpServletResponse response, UUID uuid) {
//        Cookie token = new Cookie("ask_token", uuid.toString());
//        token.setPath("/");
//        token.setMaxAge(43200);  // 12시간
//        response.addCookie(token);
        ResponseCookie token = ResponseCookie.from("ask_token", uuid.toString())
                .path("/")
                .maxAge(Duration.ofHours(12))
                .secure(false) // secure를 false로 설정하여 HTTP에서도 사용 가능하게 설정
                .sameSite("Lax")
                .httpOnly(false) // JavaScript에서 접근 가능하게 설정
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, token.toString());
    }

    public String pullingSQSMessage(String cookieId) {

        String queueUrl = getOrCreateQueueUrl(amazonSQS, queueName);
        String aiAnswer = "";

        // Receive messages from the queue
        ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest(queueUrl)
                .withWaitTimeSeconds(5) // Long polling for messages (10 seconds)
                .withMaxNumberOfMessages(10) // Max messages to receive in one request
                .withMessageAttributeNames("All");

        ReceiveMessageResult receiveMessageResult = amazonSQS.receiveMessage(receiveRequest);
        List<Message> messages = receiveMessageResult.getMessages();

        for (Message message : messages) {
            Map<String, MessageAttributeValue> messageAttributes = message.getMessageAttributes();
            JsonParser jsonParser = new JsonParser();

            String messageId = messageAttributes.get("s_id").getStringValue();

            if (messageId == null) {
                continue;
            }

            if (messageId.equals(cookieId)) {
                JsonObject object = (JsonObject) jsonParser.parse(message.getBody());
                JsonObject body = (JsonObject) object.get("body");
                aiAnswer = body.get("p_text").toString().replace("\"", "");
                DeleteMessageRequest deleteRequest = new DeleteMessageRequest(queueUrl, message.getReceiptHandle());
                amazonSQS.deleteMessage(deleteRequest);
                break;
            }
        }
        return aiAnswer;
    }

    public String getAnswerFormDdb(String s_id, String c_date) {
        List<String> pTextList = new ArrayList<>();

        Table table = dynamoDB.getTable(tableName);

        String pText = "";
        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("s_id = :v_s_id and #date > :v_date")
                .withNameMap(new HashMap() {{
                    put("#date", "date");
                }})
                .withValueMap(new ValueMap()
                        .withString(":v_s_id", s_id)
                        .withString(":v_date", c_date));
        try {
            ItemCollection<QueryOutcome> items = table.query(spec);
            for (Item item : items) {
                pText = item.getString("p_text");
                pTextList.add(pText);
            }

        } catch (Exception e) {
            // 로깅 프레임워크를 사용하는 것이 좋습니다. 예: log.error("Unable to query items", e);
            System.err.println("Unable to query items: " + e.getMessage());
        }
        return pText;
    }


    private static String getOrCreateQueueUrl(AmazonSQS sqsClient, String queueName) {
        ListQueuesResult listQueuesResult = sqsClient.listQueues(queueName);
        if (!listQueuesResult.getQueueUrls().isEmpty()) {
            return listQueuesResult.getQueueUrls().get(0);
        } else {
            CreateQueueRequest createRequest = new CreateQueueRequest(queueName);
            return sqsClient.createQueue(createRequest).getQueueUrl();
        }
    }
}