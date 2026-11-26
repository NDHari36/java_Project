/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hello.smartpark.service;
import hello.smartpark.dao.ParkingEntryDAO;
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
public class ParkingEntryService {
    
    private final ParkingEntryDAO parkingEntryDAO;

    public ParkingEntryService(ParkingEntryDAO parkingEntryDAO) {
        this.parkingEntryDAO = parkingEntryDAO;
    }

    // Lấy danh sách loại phương tiện
    public List<VehicleType> getActiveVehicleTypes() throws SQLException {
        return parkingEntryDAO.getActiveVehicleTypes();
    }

    // Tìm phương tiện theo biển số
    public Vehicle findVehicleByLicensePlate(String licensePlate)
            throws SQLException {

        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập biển số xe.");
        }

        return parkingEntryDAO.findVehicleByLicensePlate(
                licensePlate.trim().toUpperCase()
        );
    }

    // Lấy danh sách vị trí đỗ phù hợp với loại xe
    public List<ParkingSlot> getParkingSlots(int vehicleTypeId)
            throws SQLException {

        if (vehicleTypeId <= 0) {
            throw new IllegalArgumentException("Loại phương tiện không hợp lệ.");
        }

        return parkingEntryDAO.getParkingSlots(vehicleTypeId);
    }

    // Ghi nhận xe vào bãi
    public ParkingTicket createVehicleEntry(
            String licensePlate,
            int vehicleTypeId,
            long slotId,
            long userId
    ) throws SQLException {

        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập biển số xe.");
        }

        if (vehicleTypeId <= 0 || slotId <= 0 || userId <= 0) {
            throw new IllegalArgumentException(
                    "Thông tin phương tiện, vị trí hoặc tài khoản không hợp lệ."
            );
        }

        // Tìm ca đang mở của nhân viên hoặc ca đang mở để Admin thao tác
        Long shiftId = parkingEntryDAO.getOpenShiftIdForUser(userId);

        if (shiftId == null) {
            throw new SQLException(
                    "Bạn chưa có ca làm việc đang mở. Vui lòng bắt đầu ca trước."
            );
        }

        return parkingEntryDAO.createVehicleEntry(
                licensePlate.trim().toUpperCase(),
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
        long shiftId
) throws SQLException {

    if (licensePlate == null || licensePlate.trim().isEmpty()) {
        throw new IllegalArgumentException("Biển số xe không hợp lệ.");
    }

    if (vehicleTypeId <= 0 || slotId <= 0
            || userId <= 0 || shiftId <= 0) {

        throw new IllegalArgumentException(
                "Thông tin ghi nhận xe không hợp lệ."
        );
    }

    return parkingEntryDAO.createVehicleEntry(
            licensePlate.trim().toUpperCase(),
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

        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("Biển số xe không hợp lệ.");
        }

        if (vehicleTypeId <= 0 || slotId <= 0 || userId <= 0
                || (!isAdmin && (shiftId == null || shiftId <= 0))
                || (shiftId != null && shiftId <= 0)) {
            throw new IllegalArgumentException(
                    "Thông tin ghi nhận xe không hợp lệ."
            );
        }

        return parkingEntryDAO.createVehicleEntry(
                licensePlate.trim().toUpperCase(),
                vehicleTypeId,
                slotId,
                userId,
                shiftId,
                isAdmin
        );
    }
}