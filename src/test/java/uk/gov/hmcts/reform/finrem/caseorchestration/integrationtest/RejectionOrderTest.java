package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.PdfDocumentRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OrderRefusalTranslatorService;

import java.io.IOException;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.REJECTED_ORDER_TYPE;

public class RejectionOrderTest extends AbstractDocumentTest {

    @Autowired OrderRefusalTranslatorService orderRefusalTranslatorService;

    private static final String API_URL = "/case-orchestration/documents/consent-order-not-approved";

    @Override
    protected PdfDocumentRequest pdfRequest() {
        return PdfDocumentRequest.builder()
            .accessKey("TESTPDFACCESS")
            .outputName("result.pdf")
            .templateName(documentConfiguration.getRejectedOrderTemplate(CaseDetails.builder().build()))
            .data(request.getCaseDetails().getData())
            .build();
    }

    @Override
    protected String apiUrl() {
        return API_URL;
    }

    @Test
    public void generateConsentOrder() throws Exception {
        generateEvidenceUploadServiceSuccessStub();
        idamServiceStub();
        generateDocumentServiceSuccessStub();

        webClient.perform(MockMvcRequestBuilders.post(apiUrl())
            .content(objectMapper.writeValueAsString(request))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.uploadOrder[1].id", is(notNullValue())))
            .andExpect(jsonPath("$.data.uploadOrder[1].value.DocumentType", is(REJECTED_ORDER_TYPE)))
            .andExpect(jsonPath("$.data.uploadOrder[1].value.DocumentDateAdded", is(notNullValue())))
            .andExpect(jsonPath("$.data.uploadOrder[1].value.DocumentLink.document_url", is(DOC_URL)))
            .andExpect(jsonPath("$.data.uploadOrder[1].value.DocumentLink.document_filename", is(FILE_NAME)))
            .andExpect(jsonPath("$.data.uploadOrder[1].value.DocumentLink.document_binary_url", is(BINARY_URL)))
            .andExpect(jsonPath("$.errors", hasSize(0)))
            .andExpect(jsonPath("$.warnings", hasSize(0)));
    }

    private CaseDetails copyOf(CaseDetails caseDetails) {
        try {
            CaseDetails deepCopy = objectMapper
                .readValue(objectMapper.writeValueAsString(caseDetails), CaseDetails.class);
            return orderRefusalTranslatorService.translateOrderRefusals(deepCopy);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }
}
