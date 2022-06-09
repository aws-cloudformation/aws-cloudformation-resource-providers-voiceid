package software.amazon.voiceid.domain;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.voiceid.VoiceIdClient;
import software.amazon.awssdk.services.voiceid.model.DescribeDomainRequest;
import software.amazon.awssdk.services.voiceid.model.DescribeDomainResponse;
import software.amazon.awssdk.services.voiceid.model.ServerSideEncryptionUpdateStatus;
import software.amazon.awssdk.services.voiceid.model.UpdateDomainRequest;
import software.amazon.awssdk.services.voiceid.model.UpdateDomainResponse;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.delay.Constant;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;
    private Constant STABILIZATION_DELAY;

    public UpdateHandler() {
        // Setting stabilization timeout to 30 minutes to ensure that asynchronous KMS key update gets three attempts:
        // the initial attempt, a retry after 5 minutes, and a second retry after 15 minutes.
        // In the rare case that stabilization times out, the stack will attempt to roll back to the old KMS key. This
        // operation will likely fail because updates to KMS keys are not allowed during ongoing encryption updates.
        // There is little to do to prevent this, but it is an extremely rare event that should not occur often.
        this.STABILIZATION_DELAY = Constant.of()
            .timeout(Duration.ofMinutes(30L))
            .delay(Duration.ofMinutes(1L))
            .build();
    }

    // This constructor is used to set a shorter stabilization delay to test stabilization in unit tests without
    // requiring the full timeout to complete.
    public UpdateHandler(final Constant stabilizationDelay) {
        this.STABILIZATION_DELAY = stabilizationDelay;
    }

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
                          .backoffDelay(STABILIZATION_DELAY)
                          .makeServiceCall((awsRequest, client) -> updateDomain(awsRequest, client))
                          .stabilize((awsRequest, awsResponse, client, model, context) ->
                                         isStabilized(client, model, logger, resourceArn))
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

    /**
     * Stabilization is required for asynchronous API calls. If false is returned, the stabilization is repeatedly
     * attempted until a terminal state is reached. If true is returned, the resource is considered stabilized
     * and can proceed with the callback chain.
     */
    private Boolean isStabilized(final ProxyClient<VoiceIdClient> client,
                                 final ResourceModel model,
                                 final Logger logger,
                                 final AtomicReference<String> resourceArn) {
        final DescribeDomainRequest describeDomainRequest = Translator.translateToReadRequest(model);
        final DescribeDomainResponse describeDomainResponse = describeDomain(describeDomainRequest, client, logger);
        // The serverSideEncryptionUpdateDetails are only provided for domains whose encryption will change
        // or has changed in the past. If it is null, stabilization doesn't apply for the domain.
        if (describeDomainResponse.domain().serverSideEncryptionUpdateDetails() == null) {
            return true;
        }

        final ServerSideEncryptionUpdateStatus encryptionUpdateState =
            describeDomainResponse.domain().serverSideEncryptionUpdateDetails().updateStatus();
        switch (encryptionUpdateState) {
            case COMPLETED:
                logger.log(String.format("%s [%s] has been stabilized.",
                                         ResourceModel.TYPE_NAME,
                                         model.getPrimaryIdentifier()));
                // The resource arn is set after stabilization because the value isn't saved across stabilization
                // attempts.
                resourceArn.set(describeDomainResponse.domain().arn());
                return true;
            case IN_PROGRESS:
                // While encryption is IN_PROGRESS, false is returned to continue attempting to stabilize.
                return false;
            case FAILED:
                throw new CfnResourceConflictException(ResourceModel.TYPE_NAME,
                                                       model.getDomainId(),
                                                       "Failed to update the domain due to KMS key failure");
            default:
                logger.log(String.format(
                    "The server side encryption update status enum value, %s, is unrecognized. Continuing to attempt "
                        + "stabilization.",
                    encryptionUpdateState));
                return false;
        }
    }
}
