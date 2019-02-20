package uk.gov.hmcts.reform.finrem.functional.payments;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SerenityRunner.class)
public class PaymentServiceTests extends IntegrationTestBase {

    @Value("${cos.payment.fee.lookup.api}")
    private String feeLookup;

    @Value("${cos.payment.pba.validate.api}")
    private String pbaValidate;

    @Value("${cos.payment.pba.api}")
    private String pbaPayment;

    @Value("${pba.account.active}")
    private String pbaAccountActive;

    @Value("${pba.account.inactive}")
    private String pbaAccountInActive;

    private HashMap<String, String> pbaAccounts = new HashMap<>();


    @Test
    public void verifyPBAAccountStatus() {
        pbaAccounts.put(pbaAccountActive, "Active");
        pbaAccounts.put(pbaAccountInActive, "Inactive");

        validatePBAAccountNumber(pbaValidate, pbaAccounts);

    }


    @Test
    public void verifyGetFeeLoopUpTest() {

        validatePostSuccess(feeLookup);
    }

    @Test
    public void verifyPBAValidationTest() {
        validatePostSuccessForPBAValidation(pbaValidate);
    }

    @Test
    public void verifyPBAPaymentSuccessTest() {
        validatePostSuccessForPBAPayment(pbaPayment);

    }

    @Test
    public void verifyPBAPaymentFailureTest() {
        validateFailurePBAPayment(pbaPayment);

    }

    private void validatePostSuccess(String url) {

        System.out.println("Fee LookUp : " + url);

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .when().get( url)
                .then()
                .assertThat().statusCode(200);
    }


    public void validatePostSuccessForPBAValidation(String url) {

        System.out.println("PBA Validation : " + url);

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeader())
                .param("pbaNumber", "PBA222")
                .when().get(url)
                .then()
                .assertThat().statusCode(200);
    }


    private void validateFailurePBAPayment(String url) {

        System.out.println("PBA Payment : " + url);
        Response response = getPBAPaymentResponse("FailurePaymentRequestPayload.json",  url);
        int statusCode = response.getStatusCode();
        JsonPath jsonPathEvaluator = response.jsonPath();

        assertEquals(statusCode, 200);

        assertTrue(jsonPathEvaluator.get("paymentError").toString()
                .equalsIgnoreCase("Account information could not be found"));

        assertTrue(jsonPathEvaluator.get("error").toString()
                .equalsIgnoreCase("404"));
    }

    private void validatePostSuccessForPBAPayment(String url) {
        System.out.println("PBA Payment : " + url);
        Response response = getPBAPaymentResponse("SuccessPaymentRequestPayload.json",  url);
        int statusCode = response.getStatusCode();
        JsonPath jsonPathEvaluator = response.jsonPath();

        assertEquals(statusCode, 200);

        assertTrue(jsonPathEvaluator.get("status").toString()
                .equalsIgnoreCase("Success"));
    }

    private Response getPBAPaymentResponse(String payload, String url) {

        return SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeader())
                .contentType("application/json")
                .body(utils.getJsonFromFile(payload))
                .when().post(url)
                .andReturn();

    }

    private void validatePBAAccountNumber(String url, HashMap<String, String> pbaAccount) {

        pbaAccount.forEach((account, status) -> {

            Response response = SerenityRest.given()
                    .relaxedHTTPSValidation()
                    .headers(utils.getHeader())
                    .when().get(url + account).andReturn();

            JsonPath jsonPathEvaluator = response.jsonPath();

            if (status.equalsIgnoreCase("Active")) {

                assertTrue(jsonPathEvaluator.get("pbaNumberValid").toString()
                        .equalsIgnoreCase("true"));

            } else if (status.equalsIgnoreCase("Inactive")) {

                assertTrue(jsonPathEvaluator.get("pbaNumberValid").toString()
                        .equalsIgnoreCase("false"));
            }

        });
    }

}
