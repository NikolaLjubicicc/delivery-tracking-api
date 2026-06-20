package com.rbt.delivery_tracking.specification;

import com.rbt.delivery_tracking.entity.Shipment;
import com.rbt.delivery_tracking.enums.ShipmentStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class ShipmentSpecifications {

    private ShipmentSpecifications() {
    }

    public static Specification<Shipment> withFilters(Long userId,
                                                      ShipmentStatus status,
                                                      LocalDateTime createdFrom,
                                                      LocalDateTime createdTo) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("currentStatus"), status));
            }
            if (createdFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
            }
            if (createdTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdTo));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
