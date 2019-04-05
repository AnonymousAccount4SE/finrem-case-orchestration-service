package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalData;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.ObjectUtils.isEmpty;

public final class OrderRefusalTranslator {
    private static Map<String, String> REFUSAL_KEYS =
            ImmutableMap.of("Transferred to Applicant’s home Court", "Transferred to Applicant home Court - A",
                    "Transferred to Applicant's home Court", "Transferred to Applicant home Court - B"
            );

    private static Function<CaseDetails, Pair<CaseDetails, List<OrderRefusalData>>> pickLatestOrderRefusal =
            OrderRefusalTranslator::applyPickLatest;

    private static Function<Pair<CaseDetails, List<OrderRefusalData>>, CaseDetails> translate =
            OrderRefusalTranslator::applyTranslate;

    static UnaryOperator<Pair<CaseDetails, String>> translateOrderRefusalCollection =
            OrderRefusalTranslator::applyOrderRefusalCollectionTranslation;

    private static Pair<CaseDetails, String> applyOrderRefusalCollectionTranslation(Pair<CaseDetails, String> pair) {
        return ImmutablePair.of(translateOrderRefusalCollection(pair.getLeft()), pair.getRight());
    }

    private static Pair<CaseDetails, List<OrderRefusalData>> applyPickLatest(CaseDetails caseDetails) {
        List<OrderRefusalData> orderRefusalCollection = caseDetails.getCaseData().getOrderRefusalCollection();
        return ImmutablePair.of(caseDetails, refusalOrderList(orderRefusalCollection));
    }

    private static List<OrderRefusalData> refusalOrderList(List<OrderRefusalData> orderRefusalCollection) {
        return isEmpty(orderRefusalCollection) ? ImmutableList.of() : constructOrderRefusalList(orderRefusalCollection);
    }

    private static ImmutableList<OrderRefusalData> constructOrderRefusalList(
            List<OrderRefusalData> orderRefusalCollection) {
        return ImmutableList.of(orderRefusalCollection.get(orderRefusalCollection.size() - 1));
    }

    private static CaseDetails applyTranslate(Pair<CaseDetails, List<OrderRefusalData>> pair) {
        CaseDetails caseDetails = pair.getLeft();
        CaseData caseData = caseDetails.getCaseData();
        caseData.setOrderRefusalCollection(pair.getRight());

        caseData.getOrderRefusalCollection().forEach(orderRefusalData -> {
            List<String> orderRefusal = orderRefusalData.getOrderRefusal().getOrderRefusal();
            orderRefusalData.getOrderRefusal().setOrderRefusal(
                    orderRefusal.stream()
                            .map(s -> REFUSAL_KEYS.getOrDefault(s, s))
                            .collect(toList()));
        });

        return caseDetails;
    }

    public static CaseDetails translateOrderRefusalCollection(CaseDetails caseDetails) {
        return pickLatestOrderRefusal.andThen(translate).apply(caseDetails);
    }
}
