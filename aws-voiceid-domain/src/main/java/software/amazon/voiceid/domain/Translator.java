package software.amazon.voiceid.domain;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.voiceid.model.AccessDeniedException;
import software.amazon.awssdk.services.voiceid.model.ConflictException;
import software.amazon.awssdk.services.voiceid.model.CreateDomainRequest;
import software.amazon.awssdk.services.voiceid.model.DeleteDomainRequest;
import software.amazon.awssdk.services.voiceid.model.DescribeDomainRequest;
import software.amazon.awssdk.services.voiceid.model.DescribeDomainResponse;
import software.amazon.awssdk.services.voiceid.model.Domain;
import software.amazon.awssdk.services.voiceid.model.DomainStatus;
import software.amazon.awssdk.services.voiceid.model.ListDomainsRequest;
import software.amazon.awssdk.services.voiceid.model.ListDomainsResponse;
import software.amazon.awssdk.services.voiceid.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.voiceid.model.ResourceNotFoundException;
import software.amazon.awssdk.services.voiceid.model.ServerSideEncryptionConfiguration;
import software.amazon.awssdk.services.voiceid.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.voiceid.model.TagResourceRequest;
import software.amazon.awssdk.services.voiceid.model.ThrottlingException;
import software.amazon.awssdk.services.voiceid.model.UntagResourceRequest;
import software.amazon.awssdk.services.voiceid.model.UpdateDomainRequest;
import software.amazon.awssdk.services.voiceid.model.ValidationException;
import software.amazon.awssdk.services.voiceid.model.VoiceIdException;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is a centralized placeholder for
 * - api request construction
 * - object translation to/from aws sdk
 * - resource model construction for read/list handlers
 */

public class Translator {

    /**
     * Request to create a domain
     *
     * @param model resource model
     *
     * @return awsRequest the voiceid service request to create a domain
     */
    static CreateDomainRequest translateToCreateRequest(final ResourceModel model, final Map<String, String> tags) {
        return CreateDomainRequest.builder()
            .description(model.getDescription())
            .name(model.getName())
            .serverSideEncryptionConfiguration(ServerSideEncryptionConfiguration.builder()
                                                   .kmsKeyId(model.getServerSideEncryptionConfiguration().getKmsKeyId())
                                                   .build())
            .tags(TagHelper.convertToList(tags))
            .build();
    }

    /**
     * Request to read a domain
     *
     * @param model resource model
     *
     * @return awsRequest the voiceid service request to describe a domain
     */
    static DescribeDomainRequest translateToReadRequest(final ResourceModel model) {
        return DescribeDomainRequest.builder().domainId(model.getDomainId()).build();
    }

    /**
     * Translates domain object from sdk into a resource model
     *
     * @param awsResponse the voiceid service describe domain response
     *
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final DescribeDomainResponse awsResponse, final List<Tag> tags) {
        final Domain domain = awsResponse.domain();
        return ResourceModel.builder()
            .description(domain.description())
            .domainId(domain.domainId())
            .name(domain.name())
            .serverSideEncryptionConfiguration(software.amazon.voiceid.domain.ServerSideEncryptionConfiguration.builder()
                                                   .kmsKeyId(domain.serverSideEncryptionConfiguration().kmsKeyId())
                                                   .build())
            .tags(tags)
            .build();
    }

    /**
     * Request to delete a domain
     *
     * @param model resource model
     *
     * @return awsRequest the voiceid service request to delete a domain
     */
    static DeleteDomainRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteDomainRequest.builder().domainId(model.getDomainId()).build();
    }

    /**
     * Request to update properties of a previously created domain
     *
     * @param model resource model
     *
     * @return awsRequest the voiceid service request to modify a domain
     */
    static UpdateDomainRequest translateToUpdateRequest(final ResourceModel model) {
        return UpdateDomainRequest.builder()
            .description(model.getDescription())
            .domainId(model.getDomainId())
            .name(model.getName())
            .serverSideEncryptionConfiguration(ServerSideEncryptionConfiguration.builder()
                                                   .kmsKeyId(model.getServerSideEncryptionConfiguration().getKmsKeyId())
                                                   .build())
            .build();
    }

    /**
     * Request to list domains
     *
     * @param nextToken token passed to the voiceid service list domains request
     *
     * @return awsRequest the voiceid service request to list domains within aws account
     */
    static ListDomainsRequest translateToListRequest(final String nextToken) {
        return ListDomainsRequest.builder().nextToken(nextToken).build();
    }

    /**
     * Translates domain objects from sdk into resource models (primary identifier only)
     * Domains with a SUSPENDED status are filtered out to abide by the handler contract.
     *
     * @param awsResponse the voiceid service list domains response
     *
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListRequest(final ListDomainsResponse awsResponse) {
        return streamOfOrEmpty(awsResponse.domainSummaries())
            .filter(domainSummary -> domainSummary.domainStatus() == DomainStatus.ACTIVE)
            .map(domainSummary -> ResourceModel.builder().domainId(domainSummary.domainId()).build())
            .collect(Collectors.toList());
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
            .map(Collection::stream)
            .orElseGet(Stream::empty);
    }

    /**
     * Request to list tags for a domain resource
     *
     * @param resourceArn resource arn
     *
     * @return awsRequest the voice id service request to list tags for a domain
     */
    static ListTagsForResourceRequest translateToListTagsRequest(final String resourceArn) {
        return ListTagsForResourceRequest.builder().resourceArn(resourceArn).build();
    }

    /**
     * Request to add tags to a domain resource
     *
     * @param resourceArn resource arn
     *
     * @return awsRequest the voice id service request to tag a domain
     */
    static TagResourceRequest translateToTagRequest(final String resourceArn, final Map<String, String> addedTags) {
        return TagResourceRequest.builder().resourceArn(resourceArn).tags(TagHelper.convertToList(addedTags)).build();
    }

    /**
     * Request to remove tags from a domain resource
     *
     * @param resourceArn resource arn
     *
     * @return awsRequest the voice id service request to untag a domain
     */
    static UntagResourceRequest translateToUntagRequest(final String resourceArn, final Set<String> removedTags) {
        return UntagResourceRequest.builder().resourceArn(resourceArn).tagKeys(removedTags).build();
    }

    /**
     * Translates voice id exception to corresponding cfn exception
     * The handler contract states that the handler must always return a progress event,
     * but any instance of BaseHandlerException may be thrown as the wrapper maps it to a progress event.
     *
     * @param awsException exception
     *
     * @return the cfn exception most closely mapped from the service exception
     */
    static BaseHandlerException translateToCfnException(final AwsServiceException awsException) {
        if (awsException instanceof AccessDeniedException) {
            return new CfnAccessDeniedException(awsException);
        } else if (awsException instanceof ConflictException) {
            return new CfnResourceConflictException(awsException);
        } else if (awsException instanceof ResourceNotFoundException) {
            return new CfnNotFoundException(awsException);
        } else if (awsException instanceof ServiceQuotaExceededException) {
            return new CfnServiceLimitExceededException(awsException);
        } else if (awsException instanceof ThrottlingException) {
            return new CfnThrottlingException(awsException);
        } else if (awsException instanceof ValidationException) {
            return new CfnInvalidRequestException(awsException);
        } else if (awsException instanceof VoiceIdException) {
            return new CfnGeneralServiceException(awsException);
        } else {
            return new CfnInternalFailureException(awsException);
        }
    }
}
