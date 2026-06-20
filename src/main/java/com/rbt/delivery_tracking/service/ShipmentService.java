package com.rbt.delivery_tracking.service;

import com.rbt.delivery_tracking.dto.request.ChangeStatusRequest;
import com.rbt.delivery_tracking.dto.request.CreateShipmentRequest;
import com.rbt.delivery_tracking.dto.request.UpdateShipmentRequest;
import com.rbt.delivery_tracking.dto.response.ShipmentResponse;
import com.rbt.delivery_tracking.entity.Shipment;
import com.rbt.delivery_tracking.entity.ShipmentStatusHistory;
import com.rbt.delivery_tracking.entity.User;
import com.rbt.delivery_tracking.enums.ShipmentStatus;
import com.rbt.delivery_tracking.exception.BusinessException;
import com.rbt.delivery_tracking.exception.NotFoundException;
import com.rbt.delivery_tracking.repository.ShipmentRepository;
import com.rbt.delivery_tracking.repository.ShipmentStatusHistoryRepository;
import com.rbt.delivery_tracking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;

    public ShipmentService(ShipmentRepository shipmentRepository,
                           ShipmentStatusHistoryRepository historyRepository,
                           UserRepository userRepository) {
        this.shipmentRepository = shipmentRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ShipmentResponse createShipment(CreateShipmentRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User with id=" + request.getUserId() + " not found"));

        String trackingNumber = generateTrackingNumber();

        Shipment shipment = new Shipment(trackingNumber, request.getDescription(), ShipmentStatus.CREATED, user);
        Shipment saved = shipmentRepository.save(shipment);

        ShipmentStatusHistory history = new ShipmentStatusHistory(saved, ShipmentStatus.CREATED, "Shipment created");
        historyRepository.save(history);

        return toResponse(saved);
    }

    public ShipmentResponse getById(Long id) {
        Shipment shipment = findShipmentOrThrow(id);
        return toResponse(shipment);
    }

    public ShipmentResponse getByTrackingNumber(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new NotFoundException("Shipment with tracking number '" + trackingNumber + "' not found"));
        return toResponse(shipment);
    }

    @Transactional
    public ShipmentResponse updateDescription(Long id, UpdateShipmentRequest request) {
        Shipment shipment = findShipmentOrThrow(id);
        shipment.setDescription(request.getDescription());
        Shipment saved = shipmentRepository.save(shipment);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Shipment shipment = findShipmentOrThrow(id);
        shipmentRepository.delete(shipment);
    }

    @Transactional
    public ShipmentResponse changeStatus(Long id, ChangeStatusRequest request) {
        Shipment shipment = findShipmentOrThrow(id);
        ShipmentStatus currentStatus = shipment.getCurrentStatus();
        ShipmentStatus newStatus = request.getStatus();

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new BusinessException("Cannot change status from " + currentStatus + " to " + newStatus);
        }

        shipment.setCurrentStatus(newStatus);
        Shipment saved = shipmentRepository.save(shipment);

        ShipmentStatusHistory history = new ShipmentStatusHistory(saved, newStatus, request.getNote());
        historyRepository.save(history);

        return toResponse(saved);
    }

    private Shipment findShipmentOrThrow(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shipment with id=" + id + " not found"));
    }

    private String generateTrackingNumber() {
        Long sequenceValue = shipmentRepository.getNextTrackingSequence();
        return String.format("TRK-%010d", sequenceValue);
    }

    private ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getTrackingNumber(),
                shipment.getDescription(),
                shipment.getCurrentStatus(),
                shipment.getUser().getId(),
                shipment.getCreatedAt(),
                shipment.getUpdatedAt()
        );
    }
}
