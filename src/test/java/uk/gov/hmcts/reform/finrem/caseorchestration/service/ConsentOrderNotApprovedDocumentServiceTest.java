package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderConsented;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderConsentedData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementUploadService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PAPER_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultConsentedCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultContestedCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.DOCUMENT_BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.DOCUMENT_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_ORDER_NOT_APPROVED_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_COLLECTION_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.UPLOAD_ORDER;

@ActiveProfiles("test-mock-feign-clients")
public class ConsentOrderNotApprovedDocumentServiceTest extends BaseServiceTest {

    private static final String COVER_LETTER_URL = "cover_letter_url";

    private static final String COVER_LETTER_BINARY_URL = "cover_letter_url/binary";
    private static final String GENERAL_ORDER_URL = "general_letter_url";

    @Autowired private ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;

    @Autowired private EvidenceManagementUploadService evidenceManagementUploadService;

    @Autowired private DocmosisPdfGenerationService docmosisPdfGenerationServiceMock;
    private CaseDetails caseDetails;

    @Before
    public void setupDocumentGenerationMocks() {

        when(evidenceManagementUploadService.upload(any(), any(), any()))
            .thenReturn(Collections.singletonList(
                FileUploadResponse.builder()
                    .fileName("app_docs.pdf")
                    .fileUrl(COVER_LETTER_URL)
                    .build()));

        when(docmosisPdfGenerationServiceMock.generateDocFrom(any(), any()))
            .thenReturn("".getBytes(StandardCharsets.UTF_8));
    }

    public void setupContestedCase() {
        caseDetails = defaultContestedCaseDetails();

        Map<String, Object> caseData = caseDetails.getData();
        caseData.put(GENERAL_ORDER_LATEST_DOCUMENT, caseDocument());
        caseData.put(PAPER_APPLICATION, YES_VALUE);
        caseData.put(UPLOAD_ORDER, Collections.singletonList(
            ImmutableMap.of("value", ImmutableMap.of(
                "DocumentLink", ImmutableMap.of(
                    DOCUMENT_URL, "mockUrl",
                    DOCUMENT_FILENAME, "mockFilename",
                    DOCUMENT_BINARY_URL, GENERAL_ORDER_URL)))));

        List<GeneralOrderConsentedData> generalOrders = new ArrayList<>();
        GeneralOrderConsented generalOrder = new GeneralOrderConsented();
        generalOrder.setGeneralOrder(caseDocument());
        generalOrders.add(new GeneralOrderConsentedData("123", generalOrder));
        caseData.put(GENERAL_ORDER_COLLECTION_CONSENTED, generalOrders);
    }

    @Test
    public void whenApplicantLetterPackIsPrepared_thenItHasExpectedDocuments_and_caseDataIsUpdated() {
        setupContestedCase();

        List<BulkPrintDocument> generatedDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, AUTH_TOKEN);

        assertThat(generatedDocuments, hasSize(2));
        assertThat(generatedDocuments.get(0).getBinaryFileUrl(), is(COVER_LETTER_BINARY_URL));
        assertThat(generatedDocuments.get(1).getBinaryFileUrl(), is(TestSetUpUtils.BINARY_URL));

        assertThat(caseDetails.getData().get(BULK_PRINT_COVER_SHEET_APP), is(nullValue()));
    }

    @Test
    public void whenNoNotApprovedConsentOrderIsFound_thenApplicantPackPrintsWithoutIt() {
        setupContestedCase();
        caseDetails.getData().put(UPLOAD_ORDER, null);

        List<BulkPrintDocument> generatedDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, AUTH_TOKEN);

        assertThat(generatedDocuments, hasSize(2));
        assertThat(generatedDocuments.get(0).getBinaryFileUrl(), is(COVER_LETTER_BINARY_URL));
        assertThat(generatedDocuments.get(1).getBinaryFileUrl(), is(TestSetUpUtils.BINARY_URL));

        assertThat(caseDetails.getData().get(BULK_PRINT_COVER_SHEET_APP), is(nullValue()));
    }

    @Test
    public void getApplicantLetterPackWithNoConsentOrderAndNoGeneralOrdersReturnsEmptyList_withConsentDocGenOn() {
        caseDetails = defaultConsentedCaseDetails();
        caseDetails.getData().put(UPLOAD_ORDER, null);

        List<BulkPrintDocument> generatedDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, AUTH_TOKEN);

        assertThat(generatedDocuments, hasSize(0));

        assertThat(caseDetails.getData().get(BULK_PRINT_COVER_SHEET_APP), is(nullValue()));
    }

    @Test
    public void givenConsentedInContestedCase_whenConsentOrderWasNotApproved_expectedDocumentsArePrinted() {
        caseDetails = defaultContestedCaseDetails();
        addConsentedInContestedConsentOrderNotApproved();

        List<BulkPrintDocument> generatedDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, AUTH_TOKEN);

        assertThat(generatedDocuments, hasSize(2));
        assertThat(caseDetails.getData().get(BULK_PRINT_COVER_SHEET_APP), is(nullValue()));
    }

    private void addConsentedInContestedConsentOrderNotApproved() {
        Map<String, Object> caseData = caseDetails.getData();

        caseData.put(CONTESTED_CONSENT_ORDER_NOT_APPROVED_COLLECTION, List.of(ImmutableMap.of(
            "id", UUID.randomUUID().toString(),
            "value", ImmutableMap.of(
                "consentOrder", ImmutableMap.of(
                    "document_binary_url", "test_url_"
                )))));

    }
}
