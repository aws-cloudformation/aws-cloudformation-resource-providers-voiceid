package software.amazon.voiceid.domain;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.voiceid.VoiceIdClient;
import software.amazon.awssdk.services.voiceid.model.DescribeDomainResponse;
import software.amazon.awssdk.services.voiceid.model.UpdateDomainRequest;
import software.amazon.awssdk.services.voiceid.model.UpdateDomainResponse;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<VoiceIdClient> proxyClient,
        final Logger logger) {

        this.logger = logger;
        final AtomicReference<String> resourceArn = new AtomicReference<>();
        final Map<String, String>
            previousTags =
            request.getPreviousResourceTags() == null ? Collections.emptyMap() : request.getPreviousResourceTags();
        final Map<String, String>
            desiredTags =
            request.getDesiredResourceTags() == null ? Collections.emptyMap() : request.getDesiredResourceTags();

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
            .then(progress ->
                      proxy.initiate("AWS-VoiceID-Domain::Update::PreUpdateCheck",
                                     proxyClient,
                                     progress.getResourceModel(),
                                     progress.getCallbackContext())
                          .translateToServiceRequest(Translator::translateToReadRequest)
                          .makeServiceCall((awsRequest, client) -> {
                              if (awsRequest.domainId() == null) {
                                  throw new CfnNotFoundException(ResourceModel.TYPE_NAME, null);
                              }
                              final DescribeDomainResponse describeDomainResponse = describeDomain(awsRequest,
                                                                                                   client,
                                                                                                   logger);
                              resourceArn.set(describeDomainResponse.domain().arn());
                              return describeDomainResponse;
                          })
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
            .then(progress -> {
                final Map<String, String> tagsToAdd = TagHelper.generateTagsToAdd(previousTags, desiredTags);
                if (tagsToAdd.isEmpty()) {
                    return ProgressEvent.progress(progress.getResourceModel(), callbackContext);
                }
                return TagHelper.tagResource(proxy, proxyClient, request.getDesiredResourceState(), request,
                                             callbackContext, tagsToAdd, logger, resourceArn.get());
            })
            .then(progress -> {
                final Set<String> tagsToRemove = TagHelper.generateTagsToRemove(previousTags, desiredTags);
                if (tagsToRemove.isEmpty()) {
                    return ProgressEvent.progress(progress.getResourceModel(), callbackContext);
                }
                return TagHelper.untagResource(proxy, proxyClient, request.getDesiredResourceState(), request,
                                               callbackContext, tagsToRemove, logger, resourceArn.get());
            })
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
