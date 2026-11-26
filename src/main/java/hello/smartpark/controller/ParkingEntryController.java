/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hello.smartpark.controller;

import hello.smartpark.service.ParkingEntryService;
import hello.smartpark.model.Vehicle;
import hello.smartpark.model.VehicleType;
import hello.smartpark.model.ParkingSlot;
import hello.smartpark.model.ParkingTicket;

import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author PC
 */
public class ParkingEntryController {

    private final ParkingEntryService parkingEntryService;

    public ParkingEntryController(ParkingEntryService parkingEntryService) {
        this.parkingEntryService = parkingEntryService;
    }

    public ParkingTicket createVehicleEntry(
            String licensePlate,
            int vehicleTypeId,
            long slotId,
            long userId,
            long shiftId
    ) throws SQLException {

        return parkingEntryService.createVehicleEntry(
                licensePlate,
                vehicleTypeId,
                slotId,
                userId,
                shiftId
        );
    }

    public ParkingTicket createVehicleEntry(
            String licensePlate,
            int vehicleTypeId,
            long slotId,
            long userId,
            Long shiftId,
            boolean isAdmin
    ) throws SQLException {

        return parkingEntryService.createVehicleEntry(
                licensePlate,
                vehicleTypeId,
                slotId,
                userId,
                shiftId,
                isAdmin
        );
    }

    // Lấy loại phương tiện
    public List<VehicleType> getActiveVehicleTypes() throws SQLException {
        return parkingEntryService.getActiveVehicleTypes();
    }

    // Tìm xe theo biển số
    public Vehicle findVehicleByLicensePlate(String licensePlate)
            throws SQLException {

        return parkingEntryService.findVehicleByLicensePlate(licensePlate);
    }

    // Lấy vị trí đỗ
    public List<ParkingSlot> getParkingSlots(int vehicleTypeId)
            throws SQLException {

        return parkingEntryService.getParkingSlots(vehicleTypeId);
    }

    // Ghi nhận xe vào bãi
    public ParkingTicket createVehicleEntry(
            String licensePlate,
            int vehicleTypeId,
            long slotId,
            long userId
    ) throws SQLException {

        return parkingEntryService.createVehicleEntry(
                licensePlate, vehicleTypeId, slotId, userId
        );
    }
}
