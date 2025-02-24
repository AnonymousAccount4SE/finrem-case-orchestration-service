package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.paymentDocumentCollection;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_ALLOCATED_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_ADDRESS_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_NAME_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_PHONE_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_A_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_C;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_G;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ADDITIONAL_DOC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OUT_OF_FAMILY_COURT_RESOLUTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;

public class HearingDocumentServiceTest extends BaseServiceTest {

    private static final String DATE_OF_HEARING = "2019-01-01";

    @Autowired
    private HearingDocumentService hearingDocumentService;
    @Autowired
    private DocumentConfiguration documentConfiguration;

    @MockBean
    private GenericDocumentService genericDocumentService;
    @MockBean
    BulkPrintService bulkPrintService;

    @Captor
    private ArgumentCaptor<List<BulkPrintDocument>> bulkPrintDocumentsCaptor;
    @Captor
    private ArgumentCaptor<CaseDetails> caseDetailsArgumentCaptor;
    @MockBean
    private NotificationService notificationService;

    @Before
    public void setUp() {
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(caseDocument());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fastTrackDecisionNotSupplied() {
        CaseDetails caseDetails = CaseDetails.builder().data(ImmutableMap.of()).build();
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetails);
    }

    @Test
    public void generateFastTrackFormCAndOutOfFamilyCourtResolution() {
        Map<String, CaseDocument> result = hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, makeItFastTrackDecisionCase());

        assertCaseDocument(result.get(FORM_C));
        assertCaseDocument(result.get(OUT_OF_FAMILY_COURT_RESOLUTION));
        verifyAdditionalFastTrackFields();
    }

    @Test
    public void generateJudiciaryBasedFastTrackFormCAndOutOfFamilyCourtResolution() {
        final Map<String, CaseDocument> result = hearingDocumentService.generateHearingDocuments(AUTH_TOKEN,
            makeItJudiciaryFastTrackDecisionCase());

        assertCaseDocument(result.get(FORM_C));
        assertCaseDocument(result.get(OUT_OF_FAMILY_COURT_RESOLUTION));
        verifyAdditionalFastTrackFields();
    }

    @Test
    public void generateNonFastTrackFormCAndFormGAndOutOfFamilyCourtResolution() {
        final Map<String, CaseDocument> result = hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, makeItNonFastTrackDecisionCase());

        assertCaseDocument(result.get(FORM_C));
        assertCaseDocument(result.get(FORM_G));
        assertCaseDocument(result.get(OUT_OF_FAMILY_COURT_RESOLUTION));
        verifyAdditionalNonFastTrackFields();
    }

    @Test
    public void sendToBulkPrint() {
        CaseDetails caseDetails = caseDetails(NO_VALUE);

        hearingDocumentService.sendInitialHearingCorrespondence(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(eq(caseDetails), eq(AUTH_TOKEN), bulkPrintDocumentsCaptor.capture());
        verify(bulkPrintService).printRespondentDocuments(eq(caseDetails), eq(AUTH_TOKEN), bulkPrintDocumentsCaptor.capture());

        assertThat(bulkPrintDocumentsCaptor.getValue().size(), is(5));
        bulkPrintDocumentsCaptor.getValue().forEach(obj -> assertThat(obj.getBinaryFileUrl(), is(BINARY_URL)));
    }

    @Test
    public void sendToBulkPrint_multipleFormA() {
        CaseDetails caseDetails = caseDetails(YES_VALUE);

        caseDetails.getData().put(FORM_A_COLLECTION, asList(paymentDocumentCollection(), paymentDocumentCollection(), paymentDocumentCollection()));

        hearingDocumentService.sendInitialHearingCorrespondence(caseDetails, AUTH_TOKEN);

        when(notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(any())).thenReturn(false);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(CaseDetails.class))).thenReturn(true);

        verify(bulkPrintService).printApplicantDocuments(eq(caseDetails), eq(AUTH_TOKEN), bulkPrintDocumentsCaptor.capture());

        assertThat(bulkPrintDocumentsCaptor.getValue().size(), is(7));
        bulkPrintDocumentsCaptor.getValue().forEach(obj -> assertThat(obj.getBinaryFileUrl(), is(BINARY_URL)));
    }

    @Test
    public void verifySwanseaCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST, "FR_swansea_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Swansea Civil & Family Justice Centre", "Carvella House, Quay West, Quay Parade, Swansea, SA1 1SD",
            "01792 485 800", "FRCswansea@justice.gov.uk");
    }

    @Test
    public void verifyNewportCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            WALES, WALES_FRC_LIST, NEWPORT, NEWPORT_COURTLIST, "FR_newport_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Newport Civil and Family Court", "Clarence House, Clarence Place, Newport, NP19 7AA",
            "01633 258946", "FRCNewport@justice.gov.uk");
    }

    @Test
    public void verifyNoWalesFrc() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            WALES, SOUTHEAST_FRC_LIST, SWANSEA, SWANSEA_COURTLIST, "FR_swansea_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyKentCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST, "FR_kent_surrey_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Canterbury Family Court Hearing Centre", "The Law Courts, Chaucer Road, Canterbury, CT1 1ZA",
            "0300 123 5577", "Family.canterbury.countycourt@justice.gov.uk");
    }

    @Test
    public void verifyNoSouthEastFrc() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            SOUTHEAST, WALES_FRC_LIST, KENT, KENTFRC_COURTLIST, "FR_kent_surrey_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyCleavelandCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST, "FR_cleaveland_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Newcastle Civil and Family Courts and Tribunals Centre", "Barras Bridge, Newcastle upon Tyne, NE18QF",
            "0191 2058750", "Family.newcastle.countycourt@justice.gov.uk");
    }

    @Test
    public void verifyNwYorkshireCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST, "FR_nw_yorkshire_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Harrogate Justice Centre", "The Court House, Victoria Avenue, Harrogate, HG1 1EL",
            "0113 306 2501", "leedsfamily@justice.gov.uk");
    }

    @Test
    public void verifyHumberCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            NORTHEAST, NORTHEAST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST, "FR_humber_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Sheffield Family Hearing Centre", "The Law Courts, 50 West Bar, Sheffield, S3 8PH",
            "0114 2812522", "FRCSheffield@justice.gov.uk");
    }

    @Test
    public void verifyNoNorthEastFrc() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            NORTHEAST, NORTHWEST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST, "FR_humber_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyLiverpoolCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            NORTHWEST, NORTHWEST_FRC_LIST, LIVERPOOL, LIVERPOOL_COURTLIST, "FR_liverpool_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Liverpool Civil And Family Court", "35 Vernon Street, Liverpool, L2 2BX",
            "0151 296 2225", "FRCLiverpool@Justice.gov.uk");
    }

    @Test
    public void verifyManchesterCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            NORTHWEST, NORTHWEST_FRC_LIST, MANCHESTER, MANCHESTER_COURTLIST, "FR_manchester_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Manchester County And Family Court", "1 Bridge Street West, Manchester, M60 9DJ",
            "0161 240 5430", "manchesterdivorce@justice.gov.uk");
    }

    @Test
    public void verifyNoNorthWestFrc() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            NORTHWEST, NORTHEAST_FRC_LIST, MANCHESTER, MANCHESTER_COURTLIST, "FR_manchester_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyCfcCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            LONDON, LONDON_FRC_LIST, CFC, CFC_COURTLIST, "FR_s_CFCList_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Bromley County Court And Family Court", "Bromley County Court, College Road, Bromley, BR1 3PX",
            "0208 290 9620", "family.bromley.countycourt@justice.gov.uk");
    }

    @Test
    public void verifyNoLondonFrc() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            LONDON, MIDLANDS_FRC_LIST, CFC, CFC_COURTLIST, "FR_s_CFCList_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyNottinghamCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST, "FR_s_NottinghamList_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Nottingham County Court And Family Court", "60 Canal Street, Nottingham NG1 7EJ",
            "0115 910 3504", "FRCNottingham@justice.gov.uk");
    }

    @Test
    public void verifyBirminghamCourtDetails() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST, "FR_birmingham_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFields(
            "Birmingham Civil And Family Justice Centre", "Priory Courts, 33 Bull Street, Birmingham, B4 6DS",
            "0300 123 5577", "FRCBirmingham@justice.gov.uk");
    }

    @Test
    public void verifyNoMidlandsFrc() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            MIDLANDS, LONDON_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST, "FR_birmingham_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyInvalidCourt() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST, "invalid_court"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyInvalidCourtList() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetailsWithCourtDetails(
            MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, NEWPORT_COURTLIST, "FR_birmingham_hc_list_1"));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFieldsNotSet();
    }

    @Test
    public void verifyNoRegionProvided() {
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetails(NO_VALUE));

        verifyAdditionalNonFastTrackFields();

        verifyCourtDetailsFieldsNotSet();
    }

    private CaseDetails makeItNonFastTrackDecisionCase() {
        return caseDetails(NO_VALUE);
    }

    private CaseDetails makeItFastTrackDecisionCase() {
        return caseDetails(YES_VALUE);
    }

    private CaseDetails makeItJudiciaryFastTrackDecisionCase() {
        Map<String, Object> caseData =
            ImmutableMap.of(FAST_TRACK_DECISION, NO_VALUE,
                CASE_ALLOCATED_TO, YES_VALUE, HEARING_DATE, DATE_OF_HEARING);
        return CaseDetails.builder().data(caseData).build();
    }

    private CaseDetails caseDetails(String isFastTrackDecision) {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(FAST_TRACK_DECISION, isFastTrackDecision);
        caseData.put(HEARING_DATE, DATE_OF_HEARING);
        caseData.put(FORM_A_COLLECTION, singletonList(paymentDocumentCollection()));
        caseData.put(FORM_C, caseDocument());
        caseData.put(FORM_G, caseDocument());
        caseData.put(OUT_OF_FAMILY_COURT_RESOLUTION, caseDocument());
        caseData.put(HEARING_ADDITIONAL_DOC, caseDocument());

        return CaseDetails.builder().data(caseData).build();
    }

    private CaseDetails caseDetailsWithCourtDetails(String region, String frcList, String frc, String courtList, String court) {
        Map<String, Object> caseData =
            ImmutableMap.of(FAST_TRACK_DECISION, NO_VALUE, HEARING_DATE, DATE_OF_HEARING, REGION, region, frcList, frc, courtList, court);
        return CaseDetails.builder().data(caseData).build();
    }

    void verifyAdditionalFastTrackFields() {
        verify(genericDocumentService).generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
            eq(documentConfiguration.getFormCFastTrackTemplate(CaseDetails.builder().build())),
            eq(documentConfiguration.getFormCFileName()));
        verify(genericDocumentService, never()).generateDocument(any(), any(),
            eq(documentConfiguration.getFormCNonFastTrackTemplate(CaseDetails.builder().build())), any());
        verify(genericDocumentService, never()).generateDocument(any(), any(),
            eq(documentConfiguration.getFormGTemplate(CaseDetails.builder().build())), any());

        Map<String, Object> data = caseDetailsArgumentCaptor.getValue().getData();
        assertThat(data.get("formCCreatedDate"), is(notNullValue()));
        assertThat(data.get("eventDatePlus21Days"), is(notNullValue()));
    }

    void verifyCourtDetailsFields(String courtName, String courtAddress, String phone, String email) {
        Map<String, Object> data = caseDetailsArgumentCaptor.getValue().getData();
        @SuppressWarnings("unchecked")
        Map<String, Object> courtDetails = (Map<String, Object>) data.get("courtDetails");
        assertThat(courtDetails.get(COURT_DETAILS_NAME_KEY), is(courtName));
        assertThat(courtDetails.get(COURT_DETAILS_ADDRESS_KEY), is(courtAddress));
        assertThat(courtDetails.get(COURT_DETAILS_EMAIL_KEY), is(email));
        assertThat(courtDetails.get(COURT_DETAILS_PHONE_KEY), is(phone));
    }

    void verifyCourtDetailsFieldsNotSet() {
        Map<String, Object> data = caseDetailsArgumentCaptor.getValue().getData();
        assertTrue(ObjectUtils.isEmpty(data.get("courtDetails")));
    }

    void verifyAdditionalNonFastTrackFields() {
        verify(genericDocumentService).generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
            eq(documentConfiguration.getFormCNonFastTrackTemplate(CaseDetails.builder().build())),
            eq(documentConfiguration.getFormCFileName()));
        verify(genericDocumentService, never())
            .generateDocument(any(), any(),
                eq(documentConfiguration.getFormCFastTrackTemplate(CaseDetails.builder().build())), any());
        verify(genericDocumentService)
            .generateDocument(eq(AUTH_TOKEN), any(), eq(documentConfiguration.getFormGTemplate(CaseDetails.builder().build())),
                eq(documentConfiguration.getFormGFileName()));

        Map<String, Object> data = caseDetailsArgumentCaptor.getValue().getData();
        assertThat(data.get("formCCreatedDate"), is(notNullValue()));
        assertThat(data.get("hearingDateLess35Days"), is(notNullValue()));
        assertThat(data.get("hearingDateLess14Days"), is(notNullValue()));
    }
}