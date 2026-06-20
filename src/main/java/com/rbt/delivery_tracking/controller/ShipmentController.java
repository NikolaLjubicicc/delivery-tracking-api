package com.rbt.delivery_tracking.controller;

import com.rbt.delivery_tracking.dto.request.ChangeStatusRequest;
import com.rbt.delivery_tracking.dto.request.CreateShipmentRequest;
import com.rbt.delivery_tracking.dto.request.UpdateShipmentRequest;
import com.rbt.delivery_tracking.dto.response.PageResponse;
import com.rbt.delivery_tracking.dto.response.ShipmentResponse;
import com.rbt.delivery_tracking.dto.response.StatusHistoryResponse;
import com.rbt.delivery_tracking.enums.ShipmentStatus;
import com.rbt.delivery_tracking.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    public ResponseEntity<ShipmentResponse> create(@Valid @RequestBody CreateShipmentRequest request) {
        ShipmentResponse response = shipmentService.createShipment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<ShipmentResponse>> getAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(shipmentService.getAll(userId, status, createdFrom, createdTo, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentService.getById(id));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<StatusHistoryResponse>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentService.getHistory(id));
    }

    @GetMapping("/by-tracking/{trackingNumber}")
    public ResponseEntity<ShipmentResponse> getByTrackingNumber(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(shipmentService.getByTrackingNumber(trackingNumber));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShipmentResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateShipmentRequest request) {
        return ResponseEntity.ok(shipmentService.updateDescription(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ShipmentResponse> changeStatus(@PathVariable Long id,
                                                         @Valid @RequestBody ChangeStatusRequest request) {
        return ResponseEntity.ok(shipmentService.changeStatus(id, request));
    }
}
