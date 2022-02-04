package software.amazon.voiceid.domain;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.voiceid.VoiceIdClient;
import software.amazon.awssdk.services.voiceid.model.DescribeDomainRequest;
import software.amazon.awssdk.services.voiceid.model.DescribeDomainResponse;
import software.amazon.awssdk.services.voiceid.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.voiceid.model.ListTagsForResourceResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;

public class ReadHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<VoiceIdClient> proxyClient,
        final Logger logger) {

        this.logger = logger;
        final DescribeDomainRequest describeDomainRequest =
            Translator.translateToReadRequest(request.getDesiredResourceState());
        final DescribeDomainResponse describeDomainResponseResponse =
            describeDomain(describeDomainRequest, proxyClient, logger);
        final ListTagsForResourceRequest listTagsForResourceRequest =
            Translator.translateToListTagsRequest(describeDomainResponseResponse.domain().arn());
        final List<Tag> tags;
        try {
            final ListTagsForResourceResponse listTagsForResourceResponse = proxy.injectCredentialsAndInvokeV2(
                listTagsForResourceRequest,
                proxyClient.client()::listTagsForResource);
            tags = TagHelper.convertToCfnTags(listTagsForResourceResponse.tags());
        } catch (final AwsServiceException e) {
            throw Translator.translateToCfnException(e);
        }
        return ProgressEvent.defaultSuccessHandler(Translator.translateFromReadResponse(describeDomainResponseResponse, tags));
    }
}
