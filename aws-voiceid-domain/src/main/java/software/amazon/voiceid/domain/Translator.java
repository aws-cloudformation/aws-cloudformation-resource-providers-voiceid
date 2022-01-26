package software.amazon.voiceid.domain;

import com.google.common.collect.Lists;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.voiceid.model.AccessDeniedException;
import software.amazon.awssdk.services.voiceid.model.ConflictException;
import software.amazon.awssdk.services.voiceid.model.CreateDomainRequest;
import software.amazon.awssdk.services.voiceid.model.DeleteDomainRequest;
import software.amazon.awssdk.services.voiceid.model.DescribeDomainRequest;
import software.amazon.awssdk.services.voiceid.model.DescribeDomainResponse;
import software.amazon.awssdk.services.voiceid.model.Domain;
import software.amazon.awssdk.services.voiceid.model.ResourceNotFoundException;
import software.amazon.awssdk.services.voiceid.model.ServerSideEncryptionConfiguration;
import software.amazon.awssdk.services.voiceid.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.voiceid.model.ThrottlingException;
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
    static CreateDomainRequest translateToCreateRequest(final ResourceModel model) {
        return CreateDomainRequest.builder()
            .description(model.getDescription())
            .name(model.getName())
            .serverSideEncryptionConfiguration(ServerSideEncryptionConfiguration.builder()
                                                   .kmsKeyId(model.getServerSideEncryptionConfiguration().getKmsKeyId())
                                                   .build())
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
    static ResourceModel translateFromReadResponse(final DescribeDomainResponse awsResponse) {
        final Domain domain = awsResponse.domain();
        return ResourceModel.builder()
            .description(domain.description())
            .domainId(domain.domainId())
            .name(domain.name())
            .serverSideEncryptionConfiguration(software.amazon.voiceid.domain.ServerSideEncryptionConfiguration.builder()
                                                   .kmsKeyId(domain.serverSideEncryptionConfiguration().kmsKeyId())
                                                   .build())
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
     * Request to update properties of a previously created resource
     *
     * @param model resource model
     *
     * @return awsRequest the aws service request to modify a resource
     */
    static AwsRequest translateToFirstUpdateRequest(final ResourceModel model) {
        final AwsRequest awsRequest = null;
        // TODO: construct a request
        // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L45-L50
        return awsRequest;
    }

    /**
     * Request to update some other properties that could not be provisioned through first update request
     *
     * @param model resource model
     *
     * @return awsRequest the aws service request to modify a resource
     */
    static AwsRequest translateToSecondUpdateRequest(final ResourceModel model) {
        final AwsRequest awsRequest = null;
        // TODO: construct a request
        return awsRequest;
    }

    /**
     * Request to list resources
     *
     * @param nextToken token passed to the aws service list resources request
     *
     * @return awsRequest the aws service request to list resources within aws account
     */
    static AwsRequest translateToListRequest(final String nextToken) {
        final AwsRequest awsRequest = null;
        // TODO: construct a request
        // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L26-L31
        return awsRequest;
    }

    /**
     * Translates resource objects from sdk into a resource model (primary identifier only)
     *
     * @param awsResponse the aws service describe resource response
     *
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListRequest(final AwsResponse awsResponse) {
        // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L75-L82
        return streamOfOrEmpty(Lists.newArrayList())
            .map(resource -> ResourceModel.builder()
                // include only primary identifier
                .build())
            .collect(Collectors.toList());
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
            .map(Collection::stream)
            .orElseGet(Stream::empty);
    }

    /**
     * Request to add tags to a resource
     *
     * @param model resource model
     *
     * @return awsRequest the aws service request to create a resource
     */
    static AwsRequest tagResourceRequest(final ResourceModel model, final Map<String, String> addedTags) {
        final AwsRequest awsRequest = null;
        // TODO: construct a request
        // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L39-L43
        return awsRequest;
    }

    /**
     * Request to add tags to a resource
     *
     * @param model resource model
     *
     * @return awsRequest the aws service request to create a resource
     */
    static AwsRequest untagResourceRequest(final ResourceModel model, final Set<String> removedTags) {
        final AwsRequest awsRequest = null;
        // TODO: construct a request
        // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L39-L43
        return awsRequest;
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
