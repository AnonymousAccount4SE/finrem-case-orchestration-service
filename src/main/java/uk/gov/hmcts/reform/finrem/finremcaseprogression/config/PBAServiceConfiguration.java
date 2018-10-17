package uk.gov.hmcts.reform.finrem.finremcaseprogression.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "pba.api")
public class PBAServiceConfiguration {
    private String url;
    private String api;
}
