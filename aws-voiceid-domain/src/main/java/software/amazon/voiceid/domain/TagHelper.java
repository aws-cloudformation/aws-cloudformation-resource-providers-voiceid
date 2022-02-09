package software.amazon.voiceid.domain;

import org.apache.commons.collections.MapUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.voiceid.VoiceIdClient;
import software.amazon.awssdk.services.voiceid.model.Tag;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class TagHelper {

    /**
     * Converts a tag map to a list of SDK Tag objects, filtering out value-less tag entries
     *
     * @param tagMap Map of tags to convert
     *
     * @return List of Tag objects
     */
    protected static List<Tag> convertToList(final Map<String, String> tagMap) {
        if (MapUtils.isEmpty(tagMap)) {
            return Collections.emptyList();
        }
        return tagMap.entrySet().stream()
            .filter(tag -> tag.getValue() != null)
            .map(tag -> Tag.builder()
                .key(tag.getKey())
                .value(tag.getValue())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Converts a tag collection to a list of resource Tag objects
     *
     * @param tags Collection of tags to convert
     *
     * @return List of Tag objects
     */
    protected static List<software.amazon.voiceid.domain.Tag> convertToCfnTags(final Collection<Tag> tags) {
        return tags.stream()
            .map(tag -> software.amazon.voiceid.domain.Tag.builder()
                .key(tag.key()).value(tag.value()).build())
            .collect(Collectors.toList());
    }

    /**
     * Generate tags to put into resource creation request. This includes user defined tags and system tags as well.
     *
     * @param handlerRequest Create handler request
     *
     * @return Map of tags to add at domain creation
     */
    protected static Map<String, String> generateTagsForCreate(final ResourceHandlerRequest<ResourceModel> handlerRequest) {
        final Map<String, String> tagMap = new HashMap<>();
        if (handlerRequest.getSystemTags() != null) {
            tagMap.putAll(handlerRequest.getSystemTags());
        }
        if (handlerRequest.getDesiredResourceTags() != null) {
            tagMap.putAll(handlerRequest.getDesiredResourceTags());
        }
        return Collections.unmodifiableMap(tagMap);
    }

    /**
     * Determines the tags the customer desired to add
     *
     * @param previousTags
     * @param desiredTags
     *
     * @return Map of tags to add to domain
     */
    protected static Map<String, String> generateTagsToAdd(
        final Map<String, String> previousTags,
        final Map<String, String> desiredTags) {

        return desiredTags.entrySet().stream()
            .filter(e -> !previousTags.containsKey(e.getKey()) || !Objects.equals(previousTags.get(e.getKey()),
                                                                                  e.getValue()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue));
    }

    /**
     * Determines the tags the customer desired to remove
     *
     * @param previousTags
     * @param desiredTags
     *
     * @return Set of tag keys to remove from domain
     */
    protected static Set<String> generateTagsToRemove(
        final Map<String, String> previousTags,
        final Map<String, String> desiredTags) {

        final Set<String> desiredTagNames = desiredTags.keySet();
        return previousTags.keySet().stream()
            .filter(tagName -> !desiredTagNames.contains(tagName))
            .collect(Collectors.toSet());
    }

    protected static ProgressEvent<ResourceModel, CallbackContext> tagResource(
        final AmazonWebServicesClientProxy proxy,
        final ProxyClient<VoiceIdClient> proxyClient,
        final ResourceModel resourceModel,
        final ResourceHandlerRequest<ResourceModel> handlerRequest,
        final CallbackContext callbackContext,
        final Map<String, String> addedTags,
        final Logger logger,
        final String resourceArn) {

        logger.log(String.format("[UPDATE][IN PROGRESS] Going to add tags for domain resource: %s with AccountId: %s",
                                 resourceModel.getDomainId(), handlerRequest.getAwsAccountId()));
        return proxy.initiate("AWS-VoiceID-Domain::TagOps", proxyClient, resourceModel, callbackContext)
            .translateToServiceRequest(model -> Translator.translateToTagRequest(resourceArn, addedTags))
            .makeServiceCall((request, client) -> {
                try {
                    return proxy.injectCredentialsAndInvokeV2(request, client.client()::tagResource);
                } catch (final AwsServiceException e) {
                    throw Translator.translateToCfnException(e);
                }
            })
            .progress();
    }

    protected static ProgressEvent<ResourceModel, CallbackContext> untagResource(
        final AmazonWebServicesClientProxy proxy,
        final ProxyClient<VoiceIdClient> proxyClient,
        final ResourceModel resourceModel,
        final ResourceHandlerRequest<ResourceModel> handlerRequest,
        final CallbackContext callbackContext,
        final Set<String> removedTags,
        final Logger logger,
        final String resourceArn) {

        logger.log(String.format("[UPDATE][IN PROGRESS] Going to remove tags for domain resource: %s with AccountId: %s",
                                 resourceModel.getDomainId(), handlerRequest.getAwsAccountId()));
        return proxy.initiate("AWS-VoiceID-Domain::TagOps", proxyClient, resourceModel, callbackContext)
            .translateToServiceRequest(model -> Translator.translateToUntagRequest(resourceArn, removedTags))
            .makeServiceCall((request, client) -> {
                try {
                    return proxy.injectCredentialsAndInvokeV2(request, client.client()::untagResource);
                } catch (final AwsServiceException e) {
                    throw Translator.translateToCfnException(e);
                }
            })
            .progress();
    }

}
