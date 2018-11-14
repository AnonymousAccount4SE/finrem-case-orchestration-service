package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.NotificationServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;

import java.net.URI;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationServiceConfiguration notificationServiceConfiguration;
    private final RestTemplate restTemplate;

    public void sendHWFSuccessfulConfirmationEmail(CCDRequest ccdRequest, String authToken) {
        NotificationRequest notificationRequest = buildNotificationRequest(ccdRequest);
        HttpEntity<NotificationRequest> request = new HttpEntity<>(notificationRequest, buildHeaders(authToken));
        URI uri = buildUri(notificationServiceConfiguration.getHwfSuccessful());
        restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
    }

    private NotificationRequest buildNotificationRequest(CCDRequest ccdRequest) {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setCaseReferenceNumber(ccdRequest.getCaseId());
        notificationRequest.setSolicitorReferenceNumber(
                ccdRequest.getCaseDetails().getCaseData().getSolicitorReference());
        notificationRequest.setName(ccdRequest.getCaseDetails().getCaseData().getSolicitorName());
        notificationRequest.setNotificationEmail(ccdRequest.getCaseDetails().getCaseData().getSolicitorEmail());
        return notificationRequest;
    }

    private URI buildUri(String endPoint) {
        return UriComponentsBuilder.fromHttpUrl(notificationServiceConfiguration.getUrl()
                + notificationServiceConfiguration.getApi()
                + endPoint)
                .build()
                .toUri();
    }

    private HttpHeaders buildHeaders(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authToken);
        headers.add("Content-Type", "application/json");
        return headers;
    }
}
