/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hello.smartpark.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author PC
 */
public class ParkingTicket {

    private long ticketId;
    private String ticketCode;
    private long vehicleId;
    private int vehicleTypeId;
    private int parkingLotId;
    private long slotId;
    private LocalDateTime entryTime;
    private BigDecimal hourlyRateSnapshot;
    private String roundingMethodSnapshot;
    private int minBillableHoursSnapshot;
    private String status;
    private long entryShiftId;
    private long createdBy;

    public ParkingTicket() {
    }

    public long getTicketId() {
        return ticketId;
    }

    public void setTicketId(long ticketId) {
        this.ticketId = ticketId;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getVehicleTypeId() {
        return vehicleTypeId;
    }

    public void setVehicleTypeId(int vehicleTypeId) {
        this.vehicleTypeId = vehicleTypeId;
    }

    public int getParkingLotId() {
        return parkingLotId;
    }

    public void setParkingLotId(int parkingLotId) {
        this.parkingLotId = parkingLotId;
    }

    public long getSlotId() {
        return slotId;
    }

    public void setSlotId(long slotId) {
        this.slotId = slotId;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public BigDecimal getHourlyRateSnapshot() {
        return hourlyRateSnapshot;
    }

    public void setHourlyRateSnapshot(BigDecimal hourlyRateSnapshot) {
        this.hourlyRateSnapshot = hourlyRateSnapshot;
    }

    public String getRoundingMethodSnapshot() {
        return roundingMethodSnapshot;
    }

    public void setRoundingMethodSnapshot(String roundingMethodSnapshot) {
        this.roundingMethodSnapshot = roundingMethodSnapshot;
    }

    public int getMinBillableHoursSnapshot() {
        return minBillableHoursSnapshot;
    }

    public void setMinBillableHoursSnapshot(int minBillableHoursSnapshot) {
        this.minBillableHoursSnapshot = minBillableHoursSnapshot;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getEntryShiftId() {
        return entryShiftId;
    }

    public void setEntryShiftId(long entryShiftId) {
        this.entryShiftId = entryShiftId;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }
}
