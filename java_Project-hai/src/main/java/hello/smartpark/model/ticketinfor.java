package hello.smartpark.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ticketinfor {

    private long ticketId;
    private String ticketCode;
    private String licensePlate;
    private String vehicleTypeName;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private String status;

    private BigDecimal parkingFee;
    private BigDecimal lostTicketFee;
    private BigDecimal amount;
    private String paymentMethod;
    private LocalDateTime paidAt;

    public ticketinfor() {
    }

    public long getTicketId() { return ticketId; }
    public void setTicketId(long ticketId) { this.ticketId = ticketId; }

    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getVehicleTypeName() { return vehicleTypeName; }
    public void setVehicleTypeName(String vehicleTypeName) { this.vehicleTypeName = vehicleTypeName; }

    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }

    public LocalDateTime getExitTime() { return exitTime; }
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getParkingFee() { return parkingFee; }
    public void setParkingFee(BigDecimal parkingFee) { this.parkingFee = parkingFee; }

    public BigDecimal getLostTicketFee() { return lostTicketFee; }
    public void setLostTicketFee(BigDecimal lostTicketFee) { this.lostTicketFee = lostTicketFee; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}