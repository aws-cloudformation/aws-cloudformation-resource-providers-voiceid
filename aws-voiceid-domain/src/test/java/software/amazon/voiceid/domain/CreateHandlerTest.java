package software.amazon.voiceid.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.voiceid.VoiceIdClient;
import software.amazon.awssdk.services.voiceid.model.CreateDomainRequest;
import software.amazon.awssdk.services.voiceid.model.DescribeDomainRequest;
import software.amazon.awssdk.services.voiceid.model.ValidationException;
import software.amazon.awssdk.services.voiceid.model.VoiceIdException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<VoiceIdClient> proxyClient;

    @Mock
    VoiceIdClient voiceIdClient;

    private CreateHandler handler = new CreateHandler();

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        voiceIdClient = mock(VoiceIdClient.class);
        proxyClient = MOCK_PROXY(proxy, voiceIdClient);
    }

    @AfterEach
    public void tear_down() {
        verify(voiceIdClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(voiceIdClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ResourceHandlerRequest<ResourceModel> request = TestDataProvider.getRequest();

        when(voiceIdClient.createDomain(any(CreateDomainRequest.class)))
            .thenReturn(TestDataProvider.createDomainResponse());

        when(voiceIdClient.describeDomain(any(DescribeDomainRequest.class)))
            .thenReturn(TestDataProvider.describeDomainResponse());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy,
                                                                                             request,
                                                                                             new CallbackContext(),
                                                                                             proxyClient,
                                                                                             logger);

        verify(proxyClient.client()).createDomain(any(CreateDomainRequest.class));
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_Invalid() {
        final ResourceHandlerRequest<ResourceModel> request = TestDataProvider.getRequest();

        when(voiceIdClient.createDomain(any(CreateDomainRequest.class)))
            .thenThrow(ValidationException.class);

        assertThrows(CfnInvalidRequestException.class,
                     () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        verify(proxyClient.client()).createDomain(any(CreateDomainRequest.class));
    }

    @Test
    public void handleRequest_Exception() {
        final ResourceHandlerRequest<ResourceModel> request = TestDataProvider.getRequest();

        when(voiceIdClient.createDomain(any(CreateDomainRequest.class)))
            .thenThrow(VoiceIdException.class);

        assertThrows(CfnGeneralServiceException.class,
                     () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        verify(proxyClient.client()).createDomain(any(CreateDomainRequest.class));
    }
}
