package travelwith.com.demo.aws;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;

@Configuration
public class AwsConfig {
    @Value("${bedrock.aws_access_key_id}")
    private String accessKey;

    @Value("${bedrock.aws_secret_access_key}")
    private String secretKey;

    @Bean
    public Gson gson (){
        return new Gson();
    }

    @Bean
    public BedrockRuntimeAsyncClient bedrockRuntimeAsyncClient() {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        SdkAsyncHttpClient httpClient = NettyNioAsyncHttpClient.builder()
                .maxConcurrency(50)
                .build();

        return BedrockRuntimeAsyncClient.builder()
                .httpClient(httpClient)
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                .build();
    }
}
