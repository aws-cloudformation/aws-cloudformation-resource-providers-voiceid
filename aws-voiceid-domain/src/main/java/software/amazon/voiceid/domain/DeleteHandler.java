package software.amazon.voiceid.domain;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.voiceid.VoiceIdClient;
import software.amazon.awssdk.services.voiceid.model.DeleteDomainRequest;
import software.amazon.awssdk.services.voiceid.model.DeleteDomainResponse;
import software.amazon.awssdk.services.voiceid.model.DescribeDomainRequest;
import software.amazon.awssdk.services.voiceid.model.DescribeDomainResponse;
import software.amazon.awssdk.services.voiceid.model.DomainStatus;
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
                      proxy.initiate("AWS-VoiceID-Domain::Delete",
                                     proxyClient,
                                     progress.getResourceModel(),
                                     progress.getCallbackContext())
                          .translateToServiceRequest(Translator::translateToDeleteRequest)
                          .makeServiceCall((awsRequest, client) -> deleteDomain(awsRequest, client))
                          .stabilize((awsRequest, awsResponse, client, model, context) -> isStabilized(client, model))
                          .progress()
                 )
            .then(progress -> ProgressEvent.defaultSuccessHandler(null));
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

    private Boolean isStabilized(
        final ProxyClient<VoiceIdClient> client,
        final ResourceModel model) {

        final DescribeDomainRequest
            describeDomainRequest =
            DescribeDomainRequest.builder().domainId(model.getDomainId()).build();
        final DescribeDomainResponse describeDomainResponse = client.injectCredentialsAndInvokeV2(describeDomainRequest,
                                                                                                  client.client()::describeDomain);
        if (describeDomainResponse != null
            && describeDomainResponse.domain().domainStatus() == DomainStatus.SUSPENDED) {
            logger.log(String.format("%s [%s] has been stabilized.",
                                     ResourceModel.TYPE_NAME,
                                     model.getPrimaryIdentifier()));
            return true;
        }
        return false;
    }
}
