package uk.gov.hmcts.reform.finrem.caseorchestration.config.client;

@FeignClient(name = "document-generator-client", url = "${document.generator.service.api.baseurl}")
public interface DocumentGeneratorClient {
}
