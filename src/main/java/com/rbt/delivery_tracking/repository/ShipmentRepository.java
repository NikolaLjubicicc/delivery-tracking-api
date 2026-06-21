package com.rbt.delivery_tracking.repository;

import com.rbt.delivery_tracking.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long>, JpaSpecificationExecutor<Shipment> {

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    boolean existsByTrackingNumber(String trackingNumber);

    @Query(value = "SELECT nextval('shipment_tracking_seq')", nativeQuery = true)
    Long getNextTrackingSequence();

    @Query(value = "SELECT nextval('shipment_tracking_seq') FROM generate_series(1, :count)", nativeQuery = true)
    List<Long> getNextTrackingSequences(@Param("count") int count);
}
