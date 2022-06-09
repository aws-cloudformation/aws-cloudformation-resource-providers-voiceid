package software.amazon.voiceid.domain;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.voiceid.VoiceIdClient;
import software.amazon.awssdk.services.voiceid.model.DescribeDomainRequest;
import software.amazon.awssdk.services.voiceid.model.DescribeDomainResponse;
import software.amazon.awssdk.services.voiceid.model.DomainStatus;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

    @Override
    public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {
        return handleRequest(proxy,
                             request,
                             callbackContext != null ? callbackContext : new CallbackContext(),
                             proxy.newProxy(ClientBuilder::getClient),
                             logger);
    }

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<VoiceIdClient> proxyClient,
        final Logger logger);

    protected DescribeDomainResponse describeDomain(
        final DescribeDomainRequest awsRequest,
        final ProxyClient<VoiceIdClient> client,
        final Logger logger) {

        final DescribeDomainResponse awsResponse;
        try {
            awsResponse = client.injectCredentialsAndInvokeV2(awsRequest, client.client()::describeDomain);
        } catch (final AwsServiceException e) {
            throw Translator.translateToCfnException(e);
        }
        // When a deleteDomain request is called, the domain is not immediately deleted and is temporarily put into a
        // SUSPENDED state to serve as a grace period.
        // Although a SUSPENDED domain successfully returns from a read request, resource handlers are expected to
        // return FAILED with a NotFound error code once
        // a delete operation successfully completes. Therefore, a CfnNotFoundException is manually thrown for
        // SUSPENDED domains.
        if (awsResponse.domain().domainStatus() == DomainStatus.SUSPENDED) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.domainId());
        }
        logger.log(String.format("%s has successfully been read.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }
}
