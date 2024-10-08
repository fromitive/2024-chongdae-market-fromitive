package com.zzang.chongdae.offering.service.dto;

import com.zzang.chongdae.offering.domain.OfferingCondition;
import com.zzang.chongdae.offering.domain.OfferingPrice;
import com.zzang.chongdae.offering.domain.OfferingStatus;
import com.zzang.chongdae.offering.repository.entity.OfferingEntity;
import java.time.LocalDateTime;

public record OfferingDetailResponse(Long id,
                                     String title,
                                     String productUrl,
                                     String meetingAddress,
                                     String meetingAddressDetail,
                                     String description,
                                     LocalDateTime deadline,
                                     Integer currentCount,
                                     Integer totalCount,
                                     String thumbnailUrl,
                                     Integer dividedPrice,
                                     Integer totalPrice,
                                     OfferingCondition condition,
                                     Long memberId,
                                     String nickname,
                                     Boolean isParticipated) {

    public OfferingDetailResponse(OfferingEntity offering,
                                  OfferingPrice offeringPrice,
                                  OfferingStatus offeringStatus,
                                  Boolean isParticipated) {
        this(offering.getId(),
                offering.getTitle(),
                offering.getProductUrl(),
                offering.getMeetingAddress(),
                offering.getMeetingAddressDetail(),
                offering.getDescription(),
                offering.getDeadline(),
                offeringStatus.getCurrentCount(),
                offering.getTotalCount(),
                offering.getThumbnailUrl(),
                offeringPrice.calculateDividedPrice(),
                offering.getTotalPrice(),
                offeringStatus.decideOfferingCondition(),
                offering.getMember().getId(),
                offering.getMember().getNickname(),
                isParticipated
        );
    }
}
