package software.amazon.voiceid.domain;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.voiceid.VoiceIdClient;
import software.amazon.awssdk.services.voiceid.model.UpdateDomainRequest;
import software.amazon.awssdk.services.voiceid.model.UpdateDomainResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandlerStd {
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
                      proxy.initiate("AWS-VoiceID-Domain::Update::PreUpdateCheck",
                                     proxyClient,
                                     progress.getResourceModel(),
                                     progress.getCallbackContext())
                          .translateToServiceRequest(Translator::translateToReadRequest)
                          .makeServiceCall((awsRequest, client) -> describeDomain(awsRequest, client, logger))
                          .progress()
                 )
            .then(progress ->
                      proxy.initiate("AWS-VoiceID-Domain::Update",
                                     proxyClient,
                                     progress.getResourceModel(),
                                     progress.getCallbackContext())
                          .translateToServiceRequest(Translator::translateToUpdateRequest)
                          .makeServiceCall((awsRequest, client) -> updateDomain(awsRequest, client))
                          .progress()
                 )
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private UpdateDomainResponse updateDomain(
        final UpdateDomainRequest awsRequest,
        final ProxyClient<VoiceIdClient> client) {

        final UpdateDomainResponse awsResponse;
        try {
            awsResponse = client.injectCredentialsAndInvokeV2(awsRequest, client.client()::updateDomain);
        } catch (final AwsServiceException e) {
            throw Translator.translateToCfnException(e);
        }
        logger.log(String.format("%s has successfully been updated.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }
}
