package software.amazon.voiceid.domain;

import java.util.Map;
import java.util.stream.Collectors;

class Configuration extends BaseConfiguration {

    public Configuration() {
        super("aws-voiceid-domain.json");
    }

    /**
     * Merges resource tags and stack tags in getDesiredResourceTags and getPreviousResourceTags
     */
    public Map<String, String> resourceDefinedTags(final ResourceModel resourceModel) {
        if (resourceModel.getTags() == null) {
            return null;
        } else {
            return resourceModel.getTags().stream().collect(Collectors.toMap(tag -> tag.getKey(), tag -> tag.getValue()));
        }
    }
}
