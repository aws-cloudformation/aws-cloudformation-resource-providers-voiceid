package software.amazon.voiceid.domain;

import software.amazon.awssdk.services.voiceid.model.CreateDomainResponse;
import software.amazon.awssdk.services.voiceid.model.DeleteDomainResponse;
import software.amazon.awssdk.services.voiceid.model.DescribeDomainResponse;
import software.amazon.awssdk.services.voiceid.model.Domain;
import software.amazon.awssdk.services.voiceid.model.DomainStatus;
import software.amazon.awssdk.services.voiceid.model.DomainSummary;
import software.amazon.awssdk.services.voiceid.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.voiceid.model.UpdateDomainResponse;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestDataProvider {
    protected static final String DESCRIPTION = "Description";
    protected static final String DOMAIN_ID = "DomainId";
    protected static final String KMS_KEY_ID = "KmsKeyId";
    protected static final String NAME = "Name";
    protected static final ServerSideEncryptionConfiguration
        SERVER_SIDE_ENCRYPTION_CONFIGURATION =
        ServerSideEncryptionConfiguration.builder().kmsKeyId(KMS_KEY_ID).build();
    protected static final List<Tag> TAGS = Arrays.asList(Tag.builder().key("Key1").value("Value1").build(),
                                                          Tag.builder().key("Key2").value("Value2").build());

    protected static Domain getDomain(final DomainStatus domainStatus) {
        return Domain.builder()
            .description(DESCRIPTION)
            .domainId(DOMAIN_ID)
            .domainStatus(domainStatus)
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
            .tags(TAGS)
            .build();
    }

    protected static ResourceHandlerRequest<ResourceModel> getRequest() {
        return ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(getResourceModel())
            .build();
    }

    protected static DescribeDomainResponse describeDomainResponse() {
        return DescribeDomainResponse.builder()
            .domain(getDomain(DomainStatus.ACTIVE))
            .build();
    }

    protected static CreateDomainResponse createDomainResponse() {
        return CreateDomainResponse.builder()
            .domain(getDomain(DomainStatus.ACTIVE))
            .build();
    }

    protected static DescribeDomainResponse describeDeletedDomainResponse() {
        return DescribeDomainResponse.builder()
            .domain(getDomain(DomainStatus.SUSPENDED))
            .build();
    }

    protected static DeleteDomainResponse deleteDomainResponse() {
        return DeleteDomainResponse.builder().build();
    }

    protected static UpdateDomainResponse updateDomainResponse() {
        return UpdateDomainResponse.builder()
            .domain(Domain.builder().build())
            .build();
    }

    protected static List<DomainSummary> getDomainSummaries() {
        final DomainSummary domainSummary = DomainSummary.builder()
            .domainId(DOMAIN_ID)
            .domainStatus(DomainStatus.ACTIVE)
            .name(NAME)
            .description(DESCRIPTION)
            .serverSideEncryptionConfiguration(software.amazon.awssdk.services.voiceid.model.ServerSideEncryptionConfiguration.builder()
                                                   .kmsKeyId(KMS_KEY_ID)
                                                   .build()).build();

        final DomainSummary domainSummary2 = DomainSummary.builder()
            .description("Description2")
            .domainId("DomainId2")
            .domainStatus(DomainStatus.ACTIVE)
            .name("Name2")
            .description("Description2")
            .serverSideEncryptionConfiguration(software.amazon.awssdk.services.voiceid.model.ServerSideEncryptionConfiguration.builder()
                                                   .kmsKeyId("KmsKeyId2")
                                                   .build()).build();

        return Stream.of(domainSummary, domainSummary2).collect(Collectors.toList());
    }

    protected static ListTagsForResourceResponse listTagsForResourceResponse() {
        return ListTagsForResourceResponse.builder()
                .tags(Arrays.asList(software.amazon.awssdk.services.voiceid.model.Tag.builder()
                                        .key("Key1")
                                        .value("Value1")
                                        .build(),
                                    software.amazon.awssdk.services.voiceid.model.Tag.builder()
                                        .key("Key2")
                                        .value("Value2")
                                        .build()))
                .build();
    }

    protected static Map<String, String> getTags() {
        return new HashMap<String, String>() {{
            put("key-1", "value-1");
            put("key-2", "value-2");
        }};
    }
}
