package io.quarkus.amazon.sns;

import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class AmazonSnsService {

    @Inject
    SnsAsyncClient snsAsyncClient;

    @Inject
    SnsClient snsClient;

    public CompletableFuture<CreateTopicResponse> asnycCreateTopic(String topic) {
        CreateTopicRequest createTopicRequest = CreateTopicRequest.builder().name(topic).build();
        return snsAsyncClient.createTopic(createTopicRequest);
    }

    public CompletableFuture<PublishResponse> asyncPublishMessage(String topicArn, String message) {
        PublishRequest publishRequest = PublishRequest.builder().topicArn(topicArn).message(message).build();
        return snsAsyncClient.publish(publishRequest);
    }

    public CreateTopicResponse createTopic(String topic) {
        CreateTopicRequest createTopicRequest = CreateTopicRequest.builder().name(topic).build();
        return snsClient.createTopic(createTopicRequest);

    }

    public PublishResponse publishMessage(String topicArn, String message) {
        PublishRequest publishRequest = PublishRequest.builder().topicArn(topicArn).message(message).build();
        return snsClient.publish(publishRequest);
    }

}
