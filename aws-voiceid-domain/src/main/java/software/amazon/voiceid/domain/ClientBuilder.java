package software.amazon.voiceid.domain;

import software.amazon.awssdk.services.voiceid.VoiceIdClient;
import software.amazon.cloudformation.LambdaWrapper;

public class ClientBuilder {
    public static VoiceIdClient getClient() {
        return VoiceIdClient.builder().httpClient(LambdaWrapper.HTTP_CLIENT).build();
    }
}
