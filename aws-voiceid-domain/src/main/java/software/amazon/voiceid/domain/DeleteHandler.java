package software.amazon.voiceid.domain;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.voiceid.VoiceIdClient;
import software.amazon.awssdk.services.voiceid.model.DeleteDomainRequest;
import software.amazon.awssdk.services.voiceid.model.DeleteDomainResponse;
import software.amazon.awssdk.services.voiceid.model.DescribeDomainRequest;
import software.amazon.awssdk.services.voiceid.model.DescribeDomainResponse;
import software.amazon.awssdk.services.voiceid.model.DomainStatus;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<VoiceIdClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
            .then(progress ->
                      proxy.initiate("AWS-VoiceID-Domain::PreDeletionCheck",
                                     proxyClient,
                                     progress.getResourceModel(),
                                     progress.getCallbackContext())
                          .translateToServiceRequest(Translator::translateToReadRequest)
                          .makeServiceCall((awsRequest, client) -> preDeletionCheck(awsRequest, client))
                          .progress()
                 )
            .then(progress ->
                      proxy.initiate("AWS-VoiceID-Domain::Delete",
                                     proxyClient,
                                     progress.getResourceModel(),
                                     progress.getCallbackContext())
                          .translateToServiceRequest(Translator::translateToDeleteRequest)
                          .makeServiceCall((awsRequest, client) -> deleteDomain(awsRequest, client))
                          .progress()
                 )
            .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }

    private DescribeDomainResponse preDeletionCheck(
        final DescribeDomainRequest awsRequest,
        final ProxyClient<VoiceIdClient> client) {

        final DescribeDomainResponse awsResponse;
        try {
            awsResponse = client.injectCredentialsAndInvokeV2(awsRequest, client.client()::describeDomain);
        } catch (final AwsServiceException e) {
            throw Translator.translateToCfnException(e);
        }
        if (awsResponse.domain().domainStatus() == DomainStatus.SUSPENDED) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.domainId());
        }
        return awsResponse;
    }

    private DeleteDomainResponse deleteDomain(
        final DeleteDomainRequest awsRequest,
        final ProxyClient<VoiceIdClient> client) {

        final DeleteDomainResponse awsResponse;
        try {
            awsResponse = client.injectCredentialsAndInvokeV2(awsRequest, client.client()::deleteDomain);
        } catch (final AwsServiceException e) {
            throw Translator.translateToCfnException(e);
        }
        logger.log(String.format("%s successfully deleted.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }
}
