package travelwith.com.demo.chatbot;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrock.BedrockClient;
import software.amazon.awssdk.services.bedrock.model.BedrockException;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;
import software.amazon.awssdk.services.bedrockruntime.model.Message;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
@Slf4j
@RequiredArgsConstructor
public class Test {

    private final BedrockRuntimeAsyncClient bedrockRuntimeAsyncClient;
    private final ChatBotService chatBotService;



    @RequestMapping("/ssss")
    public String test() throws JsonProcessingException {
        String prompt = "부산 여행 가고 싶어";

        try {
            Message message = Message.builder()
                    .content(ContentBlock.fromText(prompt))
                    .role(ConversationRole.USER)
                    .build();

            CompletableFuture<InvokeModelResponse> invokeModelResponseCompletableFuture = bedrockRuntimeAsyncClient.invokeModel(InvokeModelRequest.builder()
                    .modelId("arn:aws:bedrock:us-west-2:992382591529:agent/IDBMPPAGRE")
                    .build());

            System.out.println("호출");
            CompletableFuture<ConverseResponse> response = bedrockRuntimeAsyncClient.converse(request -> request
                    .modelId("IDBMPPAGRE")
                    .messages(message)
                    .inferenceConfig(config -> config
                            .maxTokens(512)
                            .temperature(0.5F)
                            .topP(0.9F)));


//            return responseText;
        } catch (Exception e) {
            e.printStackTrace();
//            return CompletableFuture.failedFuture(e);
        }
        return "dd";
    }

    @RequestMapping("/sqs")
    @ResponseBody
    public String sqsTest(){

        String s = chatBotService.pullingSQSMessage("xxxx");

        return s;
    }


}
