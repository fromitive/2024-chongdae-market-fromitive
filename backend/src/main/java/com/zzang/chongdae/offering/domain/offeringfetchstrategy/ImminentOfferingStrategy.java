package com.zzang.chongdae.offering.domain.offeringfetchstrategy;

import com.zzang.chongdae.offering.repository.OfferingRepository;
import com.zzang.chongdae.offering.repository.entity.OfferingEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

public class ImminentOfferingStrategy extends OfferingFetchStrategy {

    public ImminentOfferingStrategy(OfferingRepository offeringRepository) {
        super(offeringRepository);
    } // TODO: 롬복 어노테이션으로 대체가능한거 찾아보기

    @Override
    protected List<OfferingEntity> fetchOfferingsWithoutLastId(String searchKeyword, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = LocalDateTime.now().plusHours(6);
        Long outOfRangeId = findOutOfRangeId();
        return offeringRepository.findImminentOfferingsWithKeyword(
                now, threshold, outOfRangeId, searchKeyword, pageable);
    }

    @Override
    protected List<OfferingEntity> fetchOfferingsWithLastOffering(
            OfferingEntity lastOffering, String searchKeyword, Pageable pageable) {
        LocalDateTime lastDeadline = lastOffering.getDeadline();
        LocalDateTime threshold = LocalDateTime.now().plusHours(6);
        return offeringRepository.findImminentOfferingsWithKeyword(
                lastDeadline, threshold, lastOffering.getId(), searchKeyword, pageable);
    }
}
