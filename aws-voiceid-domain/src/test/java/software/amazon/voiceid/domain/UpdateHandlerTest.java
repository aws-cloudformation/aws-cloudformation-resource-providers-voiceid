package software.amazon.voiceid.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.voiceid.VoiceIdClient;
import software.amazon.awssdk.services.voiceid.model.AccessDeniedException;
import software.amazon.awssdk.services.voiceid.model.ConflictException;
import software.amazon.awssdk.services.voiceid.model.DescribeDomainRequest;
import software.amazon.awssdk.services.voiceid.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.voiceid.model.ServerSideEncryptionConfiguration;
import software.amazon.awssdk.services.voiceid.model.TagResourceRequest;
import software.amazon.awssdk.services.voiceid.model.TagResourceResponse;
import software.amazon.awssdk.services.voiceid.model.UntagResourceRequest;
import software.amazon.awssdk.services.voiceid.model.UntagResourceResponse;
import software.amazon.awssdk.services.voiceid.model.UpdateDomainRequest;
import software.amazon.awssdk.services.voiceid.model.ValidationException;
import software.amazon.awssdk.services.voiceid.model.VoiceIdException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.delay.Constant;

import java.time.Duration;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<VoiceIdClient> proxyClient;

    @Mock
    VoiceIdClient voiceIdClient;

    private Constant stabilizationDelay = Constant.of()
        .timeout(Duration.ofSeconds(10L))
        .delay(Duration.ofSeconds(5L))
        .build();

    final UpdateHandler handlerWithSetDelay = new UpdateHandler(stabilizationDelay);

    final UpdateHandler handler = new UpdateHandler();

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

        when(voiceIdClient.describeDomain(any(DescribeDomainRequest.class)))
            .thenReturn(TestDataProvider.describeDomainResponse());

        when(voiceIdClient.updateDomain(any(UpdateDomainRequest.class)))
            .thenReturn(TestDataProvider.updateDomainResponse());

        when(voiceIdClient.listTagsForResource(any(ListTagsForResourceRequest.class)))
            .thenReturn(TestDataProvider.listTagsForResourceResponse());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy,
                                                                                             request,
                                                                                             new CallbackContext(),
                                                                                             proxyClient,
                                                                                             logger);

        verify(proxyClient.client()).updateDomain(any(UpdateDomainRequest.class));
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_SuccessWithTagging() {
        final ResourceHandlerRequest<ResourceModel> request = TestDataProvider.getRequest();
        request.setPreviousResourceTags(TestDataProvider.getTags());
        request.setDesiredResourceTags(new HashMap<String, String>() {{
            put("Key1", "Value1");
            put("Key2", "Value2");
        }});

        when(voiceIdClient.describeDomain(any(DescribeDomainRequest.class)))
            .thenReturn(TestDataProvider.describeDomainResponse());

        when(voiceIdClient.updateDomain(any(UpdateDomainRequest.class)))
            .thenReturn(TestDataProvider.updateDomainResponse());

        when(voiceIdClient.tagResource(any(TagResourceRequest.class)))
            .thenReturn(TagResourceResponse.builder().build());

        when(voiceIdClient.untagResource(any(UntagResourceRequest.class)))
            .thenReturn(UntagResourceResponse.builder().build());

        when(voiceIdClient.listTagsForResource(any(ListTagsForResourceRequest.class)))
            .thenReturn(TestDataProvider.listTagsForResourceResponse());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy,
                                                                                             request,
                                                                                             new CallbackContext(),
                                                                                             proxyClient,
                                                                                             logger);

        verify(proxyClient.client()).updateDomain(any(UpdateDomainRequest.class));
        verify(proxyClient.client()).tagResource(any(TagResourceRequest.class));
        verify(proxyClient.client()).untagResource(any(UntagResourceRequest.class));
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_StabilizationSucceeds() {
        final ResourceHandlerRequest<ResourceModel> request = TestDataProvider.getRequest();

        // There are four describeDomain calls: first is for pre-existence check, second and third during stabilization,
        // and the last is the returned result from the ReadHandler which calls describeDomain itself.
        when(voiceIdClient.describeDomain(any(DescribeDomainRequest.class)))
            .thenReturn(TestDataProvider.describeDomainResponse(), TestDataProvider.describeStabilizingDomainResponse(),
                        TestDataProvider.describeStabilizedDomainResponse());

        when(voiceIdClient.updateDomain(any(UpdateDomainRequest.class)))
            .thenReturn(TestDataProvider.updateDomainResponse());

        when(voiceIdClient.listTagsForResource(any(ListTagsForResourceRequest.class)))
            .thenReturn(TestDataProvider.listTagsForResourceResponse());

        final ProgressEvent<ResourceModel, CallbackContext> response = handlerWithSetDelay.handleRequest(proxy,
                                                                                                         request,
                                                                                                         new CallbackContext(),
                                                                                                         proxyClient,
                                                                                                         logger);

        final ResourceModel resourceModel = request.getDesiredResourceState();

        final UpdateDomainRequest expectedUpdateDomainRequest = UpdateDomainRequest.builder()
            .description(resourceModel.getDescription())
            .domainId(resourceModel.getDomainId())
            .name(resourceModel.getName())
            .serverSideEncryptionConfiguration(ServerSideEncryptionConfiguration.builder()
                                                   .kmsKeyId(resourceModel.getServerSideEncryptionConfiguration()
                                                                 .getKmsKeyId())
                                                   .build())
            .build();
        final DescribeDomainRequest expectedDescribeDomainRequest = DescribeDomainRequest.builder()
            .domainId(resourceModel.getDomainId())
            .build();

        final ArgumentCaptor<UpdateDomainRequest> updateDomainRequestArgumentCaptor = ArgumentCaptor.forClass(
            UpdateDomainRequest.class);
        final ArgumentCaptor<DescribeDomainRequest> describeDomainRequestArgumentCaptor = ArgumentCaptor.forClass(
            DescribeDomainRequest.class);

        verify(proxyClient.client()).updateDomain(updateDomainRequestArgumentCaptor.capture());
        assertThat(updateDomainRequestArgumentCaptor.getValue()).isEqualToIgnoringNullFields(expectedUpdateDomainRequest);

        verify(proxyClient.client(),
               times(4)).describeDomain(describeDomainRequestArgumentCaptor.capture());
        assertThat(describeDomainRequestArgumentCaptor.getValue())
            .isEqualToIgnoringNullFields(expectedDescribeDomainRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_StabilizationTimesOut() {
        final ResourceHandlerRequest<ResourceModel> request = TestDataProvider.getRequest();

        when(voiceIdClient.describeDomain(any(DescribeDomainRequest.class)))
            .thenReturn(TestDataProvider.describeDomainResponse(),
                        TestDataProvider.describeUnknownStabilizationDomainResponse(),
                        TestDataProvider.describeStabilizingDomainResponse());

        when(voiceIdClient.updateDomain(any(UpdateDomainRequest.class)))
            .thenReturn(TestDataProvider.updateDomainResponse());

        final ProgressEvent<ResourceModel, CallbackContext> response = handlerWithSetDelay.handleRequest(proxy,
                                                                                                         request,
                                                                                                         new CallbackContext(),
                                                                                                         proxyClient,
                                                                                                         logger);

        verify(proxyClient.client()).updateDomain(any(UpdateDomainRequest.class));
        verify(proxyClient.client(), atLeast(3)).describeDomain(any(DescribeDomainRequest.class));
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotStabilized);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
    }

    @Test
    public void handleRequest_StabilizationFails() {
        final ResourceHandlerRequest<ResourceModel> request = TestDataProvider.getRequest();

        when(voiceIdClient.describeDomain(any(DescribeDomainRequest.class)))
            .thenReturn(TestDataProvider.describeDomainResponse(),
                        TestDataProvider.describeFailedStabilizationDomainResponse());

        when(voiceIdClient.updateDomain(any(UpdateDomainRequest.class)))
            .thenReturn(TestDataProvider.updateDomainResponse());

        assertThrows(CfnResourceConflictException.class,
                     () -> handlerWithSetDelay.handleRequest(proxy,
                                                             request,
                                                             new CallbackContext(),
                                                             proxyClient,
                                                             logger));
        verify(proxyClient.client()).updateDomain(any(UpdateDomainRequest.class));
        verify(proxyClient.client(), times(2)).describeDomain(any(DescribeDomainRequest.class));
    }

    @Test
    public void handleRequest_TagException() {
        final ResourceHandlerRequest<ResourceModel> request = TestDataProvider.getRequest();

        request.setDesiredResourceTags(TestDataProvider.getTags());

        when(voiceIdClient.describeDomain(any(DescribeDomainRequest.class)))
            .thenReturn(TestDataProvider.describeDomainResponse());

        when(voiceIdClient.updateDomain(any(UpdateDomainRequest.class)))
            .thenReturn(TestDataProvider.updateDomainResponse());

        when(voiceIdClient.tagResource(any(TagResourceRequest.class)))
            .thenThrow(VoiceIdException.class);

        assertThrows(CfnGeneralServiceException.class,
                     () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        verify(proxyClient.client()).updateDomain(any(UpdateDomainRequest.class));
    }

    @Test
    public void handleRequest_UntagException() {
        final ResourceHandlerRequest<ResourceModel> request = TestDataProvider.getRequest();

        request.setPreviousResourceTags(TestDataProvider.getTags());

        when(voiceIdClient.describeDomain(any(DescribeDomainRequest.class)))
            .thenReturn(TestDataProvider.describeDomainResponse());

        when(voiceIdClient.updateDomain(any(UpdateDomainRequest.class)))
            .thenReturn(TestDataProvider.updateDomainResponse());

        when(voiceIdClient.untagResource(any(UntagResourceRequest.class)))
            .thenThrow(VoiceIdException.class);

        assertThrows(CfnGeneralServiceException.class,
                     () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        verify(proxyClient.client()).updateDomain(any(UpdateDomainRequest.class));
        verify(proxyClient.client(), never()).tagResource(any(TagResourceRequest.class));
    }

    @Test
    public void handleRequest_TagAccessDenied() {
        final ResourceHandlerRequest<ResourceModel> request = TestDataProvider.getRequest();

        request.setDesiredResourceTags(TestDataProvider.getTags());

        when(voiceIdClient.describeDomain(any(DescribeDomainRequest.class)))
            .thenReturn(TestDataProvider.describeDomainResponse());

        when(voiceIdClient.updateDomain(any(UpdateDomainRequest.class)))
            .thenReturn(TestDataProvider.updateDomainResponse());

        when(voiceIdClient.tagResource(any(TagResourceRequest.class)))
            .thenThrow(AccessDeniedException.class);

        assertThrows(CfnAccessDeniedException.class,
                     () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        verify(proxyClient.client()).updateDomain(any(UpdateDomainRequest.class));
    }

    @Test
    public void handleRequest_Suspended() {
        final ResourceHandlerRequest<ResourceModel> request = TestDataProvider.getRequest();

        when(voiceIdClient.describeDomain(any(DescribeDomainRequest.class)))
            .thenReturn(TestDataProvider.describeDeletedDomainResponse());

        assertThrows(CfnNotFoundException.class,
                     () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        verify(proxyClient.client()).describeDomain(any(DescribeDomainRequest.class));
    }

    @Test
    public void handleRequest_Invalid() {
        final ResourceHandlerRequest<ResourceModel> request = TestDataProvider.getRequest();

        when(voiceIdClient.describeDomain(any(DescribeDomainRequest.class)))
            .thenReturn(TestDataProvider.describeDomainResponse());

        when(voiceIdClient.updateDomain(any(UpdateDomainRequest.class)))
            .thenThrow(ValidationException.class);

        assertThrows(CfnInvalidRequestException.class,
                     () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        verify(proxyClient.client()).updateDomain(any(UpdateDomainRequest.class));
    }

    @Test
    public void handleRequest_Conflict() {
        final ResourceHandlerRequest<ResourceModel> request = TestDataProvider.getRequest();

        when(voiceIdClient.describeDomain(any(DescribeDomainRequest.class)))
            .thenReturn(TestDataProvider.describeDomainResponse());

        when(voiceIdClient.updateDomain(any(UpdateDomainRequest.class)))
            .thenThrow(ConflictException.class);

        assertThrows(CfnResourceConflictException.class,
                     () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        verify(proxyClient.client()).updateDomain(any(UpdateDomainRequest.class));
    }

    @Test
    public void handleRequest_Exception() {
        final ResourceHandlerRequest<ResourceModel> request = TestDataProvider.getRequest();

        when(voiceIdClient.describeDomain(any(DescribeDomainRequest.class)))
            .thenReturn(TestDataProvider.describeDomainResponse());

        when(voiceIdClient.updateDomain(any(UpdateDomainRequest.class)))
            .thenThrow(VoiceIdException.class);

        assertThrows(CfnGeneralServiceException.class,
                     () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        verify(proxyClient.client()).updateDomain(any(UpdateDomainRequest.class));
    }
}
