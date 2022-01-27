package software.amazon.voiceid.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.voiceid.VoiceIdClient;
import software.amazon.awssdk.services.voiceid.model.DomainStatus;
import software.amazon.awssdk.services.voiceid.model.DomainSummary;
import software.amazon.awssdk.services.voiceid.model.ListDomainsRequest;
import software.amazon.awssdk.services.voiceid.model.ListDomainsResponse;
import software.amazon.awssdk.services.voiceid.model.VoiceIdException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock private ProxyClient<VoiceIdClient> proxyClient;

    @Mock VoiceIdClient voiceIdClient;

    private ListHandler handler = new ListHandler();

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        voiceIdClient = mock(VoiceIdClient.class);
        proxyClient = MOCK_PROXY(proxy, voiceIdClient);
    }

    @AfterEach
    public void tear_down() {
        verifyNoMoreInteractions(voiceIdClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .build();

        final ListDomainsResponse listDomainsResponse = ListDomainsResponse
            .builder().domainSummaries(TestDataProvider.getDomainSummaries()).build();

        when(voiceIdClient.listDomains(any(ListDomainsRequest.class)))
            .thenReturn(listDomainsResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, null, proxyClient, logger);

        final ResourceModel expectedModel1 = ResourceModel.builder().domainId("DomainId").build();
        final ResourceModel expectedModel2 = ResourceModel.builder().domainId("DomainId2").build();

        verify(proxyClient.client()).listDomains(any(ListDomainsRequest.class));
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getResourceModels()).containsAll(Arrays.asList(expectedModel1, expectedModel2));
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_PaginatedSuccess() {

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .nextToken("nextTokenRequest").build();

        final ListDomainsResponse listDomainsResponse = ListDomainsResponse.builder()
            .domainSummaries(TestDataProvider.getDomainSummaries()).nextToken("nextTokenResponse").build();

        when(voiceIdClient.listDomains(any(ListDomainsRequest.class)))
            .thenReturn(listDomainsResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, null, proxyClient, logger);

        final ResourceModel expectedModel1 = ResourceModel.builder().domainId("DomainId").build();
        final ResourceModel expectedModel2 = ResourceModel.builder().domainId("DomainId2").build();

        verify(proxyClient.client()).listDomains(any(ListDomainsRequest.class));
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getNextToken()).isEqualTo("nextTokenResponse");
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getResourceModels()).containsAll(Arrays.asList(expectedModel1, expectedModel2));
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_SuspendedNotReturnedSuccess() {

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .build();

        final DomainSummary domainSummary = DomainSummary.builder()
            .description("Description3")
            .domainId("DomainId3")
            .domainStatus(DomainStatus.SUSPENDED)
            .name("Name3")
            .description("Description3")
            .serverSideEncryptionConfiguration(software.amazon.awssdk.services.voiceid.model.ServerSideEncryptionConfiguration.builder()
                                                   .kmsKeyId("KmsKeyId3")
                                                   .build()).build();

        final List<DomainSummary> domainSummaries = TestDataProvider.getDomainSummaries();
        domainSummaries.add(domainSummary);

        final ListDomainsResponse listDomainsResponse = ListDomainsResponse.builder()
            .domainSummaries(domainSummaries).build();

        when(voiceIdClient.listDomains(any(ListDomainsRequest.class)))
            .thenReturn(listDomainsResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, null, proxyClient, logger);

        final ResourceModel expectedModel1 = ResourceModel.builder().domainId("DomainId").build();
        final ResourceModel expectedModel2 = ResourceModel.builder().domainId("DomainId2").build();

        verify(proxyClient.client()).listDomains(any(ListDomainsRequest.class));
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getResourceModels()).containsExactly(expectedModel1, expectedModel2);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_EmptySuccess() {

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .build();

        when(voiceIdClient.listDomains(any(ListDomainsRequest.class)))
            .thenReturn(ListDomainsResponse.builder().build());

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, null, proxyClient, logger);

        verify(proxyClient.client()).listDomains(any(ListDomainsRequest.class));
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getResourceModels()).isEmpty();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_Exception() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .build();

        when(voiceIdClient.listDomains(any(ListDomainsRequest.class)))
            .thenThrow(VoiceIdException.class);

        assertThrows(CfnGeneralServiceException.class,
                     () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        verify(proxyClient.client()).listDomains(any(ListDomainsRequest.class));
    }
}
