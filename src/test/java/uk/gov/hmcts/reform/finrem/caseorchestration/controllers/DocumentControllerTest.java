package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.feignError;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.APPLICATION_ISSUED;

@WebMvcTest(DocumentController.class)
public class DocumentControllerTest extends BaseControllerTest {

    private static final String GEN_DOC_URL = "/case-orchestration/documents/generate-mini-form-a";

    private JsonNode requestContent;

    @MockBean
    private OnlineFormDocumentService documentService;

    @Before
    public void setUp()  {
        super.setUp();
        try {
            doRequestSetUp();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private void doRequestSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/fee-lookup.json").toURI()));
    }

    @Test
    public void generateMiniFormA() throws Exception {
        when(documentService.generateMiniFormA(eq(AUTH_TOKEN), isA(CaseDetails.class))).thenReturn(caseDocument());

        mvc.perform(post(GEN_DOC_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state", is(APPLICATION_ISSUED.toString())))
                .andExpect(jsonPath("$.data.miniFormA.document_url", is(DOC_URL)))
                .andExpect(jsonPath("$.data.miniFormA.document_filename", is(FILE_NAME)))
                .andExpect(jsonPath("$.data.miniFormA.document_binary_url", is(BINARY_URL)))
                .andExpect(jsonPath("$.errors", isEmptyOrNullString()))
                .andExpect(jsonPath("$.warnings", isEmptyOrNullString()));
    }

    @Test
    public void generateMiniFormAHttpError400() throws Exception {
        mvc.perform(post(GEN_DOC_URL)
                .content("kwuilebge")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void generateMiniFormAHttpError500() throws Exception {
        when(documentService.generateMiniFormA(eq(AUTH_TOKEN), isA(CaseDetails.class))).thenThrow(feignError());

        mvc.perform(post(GEN_DOC_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}