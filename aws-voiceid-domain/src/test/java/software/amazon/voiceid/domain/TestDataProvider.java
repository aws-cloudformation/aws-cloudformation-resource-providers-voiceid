package software.amazon.voiceid.domain;

import software.amazon.awssdk.services.voiceid.model.DescribeDomainResponse;
import software.amazon.awssdk.services.voiceid.model.Domain;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class TestDataProvider {
    protected static final String DESCRIPTION = "Description";
    protected static final String DOMAIN_ID = "DomainId";
    protected static final String KMS_KEY_ID = "KmsKeyId";
    protected static final String NAME = "Name";
    protected static final ServerSideEncryptionConfiguration
        SERVER_SIDE_ENCRYPTION_CONFIGURATION =
        ServerSideEncryptionConfiguration.builder().kmsKeyId(KMS_KEY_ID).build();

    protected static Domain getDomain() {
        return Domain.builder().description(DESCRIPTION)
            .domainId(DOMAIN_ID)
            .name(NAME)
            .serverSideEncryptionConfiguration(software.amazon.awssdk.services.voiceid.model.ServerSideEncryptionConfiguration.builder()
                                                   .kmsKeyId(KMS_KEY_ID)
                                                   .build())
            .build();
    }

    protected static ResourceModel getResourceModel() {
        return ResourceModel.builder()
            .description(DESCRIPTION)
            .domainId(DOMAIN_ID)
            .name(NAME)
            .serverSideEncryptionConfiguration(SERVER_SIDE_ENCRYPTION_CONFIGURATION)
            .build();
    }

    protected static ResourceHandlerRequest<ResourceModel> getRequest() {
        return ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(getResourceModel())
            .build();
    }

    protected static DescribeDomainResponse describeDomainResponse() {
        return DescribeDomainResponse.builder()
            .domain(getDomain())
            .build();
    }
}
