package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_ADDRESS_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_BODY_TEXT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_JUDGE_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_PREVIEW_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_RECITALS;

@WebMvcTest(GeneralOrderStartController.class)
public class GeneralOrderStartControllerTest extends BaseControllerTest {

    @MockBean
    private IdamService idamService;

    private String bearerToken = "some-access-token";

    @Test
    public void initialiseGeneralOrderPropertiesSuccess() throws Exception {
        generalOrderStartControllerSetUp();
        when(idamService.getIdamFullName(bearerToken)).thenReturn("Integration Test");
        when(idamService.getIdamSurname(bearerToken)).thenReturn("Integration Test");


        mvc.perform(post("/case-orchestration/general-order-start")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data." + GENERAL_ORDER_ADDRESS_TO, is(nullValue())))
            .andExpect(jsonPath("$.data." + GENERAL_ORDER_DATE, is(notNullValue())))
            .andExpect(jsonPath("$.data." + GENERAL_ORDER_BODY_TEXT, is(nullValue())))
            .andExpect(jsonPath("$.data." + GENERAL_ORDER_CREATED_BY, is("Integration Test")))
            .andExpect(jsonPath("$.data." + GENERAL_ORDER_PREVIEW_DOCUMENT, is(nullValue())))
            .andExpect(jsonPath("$.data." + GENERAL_ORDER_RECITALS, is(nullValue())))
            .andExpect(jsonPath("$.data." + GENERAL_ORDER_JUDGE_NAME, is("Integration Test")))
            .andExpect(jsonPath("$.data." + GENERAL_ORDER_JUDGE_TYPE, is(nullValue())));
    }

    @Test
    public void initialiseGeneralOrderPropertiesBadRequest() throws Exception {
        doEmptyCaseDataSetUp();

        mvc.perform(post("/case-orchestration/general-order-start")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void initialiseGeneralOrderPropertiesInternalServerError() throws Exception {
        generalOrderStartControllerSetUp();
        when(idamService.getIdamFullName(bearerToken))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        mvc.perform(post("/case-orchestration/general-order-start")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, bearerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    private void generalOrderStartControllerSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/general-order-consented.json").toURI()));
    }
}