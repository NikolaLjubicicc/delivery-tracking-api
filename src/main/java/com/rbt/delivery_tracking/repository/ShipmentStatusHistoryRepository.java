package com.rbt.delivery_tracking.repository;

import com.rbt.delivery_tracking.entity.ShipmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentStatusHistoryRepository extends JpaRepository<ShipmentStatusHistory, Long> {

    List<ShipmentStatusHistory> findByShipmentIdOrderByChangedAtAsc(Long shipmentId);
}
