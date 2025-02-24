package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import javax.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_ADDRESS_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_BODY_TEXT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_CREATED_BY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_JUDGE_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_PREVIEW_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_RECITALS;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class GeneralOrderStartController extends BaseController {

    @Autowired
    private IdamService service;

    @PostMapping(path = "/general-order-start", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Clears previous entered field values. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> initialiseGeneralOrderProperties(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") CallbackRequest callback) {

        CaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request to clear general order fields for Case ID: {}", caseDetails.getId());

        validateCaseData(callback);

        Map<String, Object> caseData = caseDetails.getData();
        caseData.put(GENERAL_ORDER_ADDRESS_TO, null);
        caseData.put(GENERAL_ORDER_DATE, LocalDate.now());
        caseData.put(GENERAL_ORDER_BODY_TEXT, null);
        caseData.put(GENERAL_ORDER_PREVIEW_DOCUMENT, null);
        caseData.put(GENERAL_ORDER_RECITALS, null);
        caseData.put(GENERAL_ORDER_JUDGE_NAME, service.getIdamSurname(authorisationToken));
        caseData.put(GENERAL_ORDER_JUDGE_TYPE, null);
        caseData.put(GENERAL_ORDER_CREATED_BY, service.getIdamFullName(authorisationToken));

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }
}
