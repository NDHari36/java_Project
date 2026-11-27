package hello.smartpark.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class MatVe {
    private long ticketId;
    private String ticketCode;
    private String licensePlate;
    private String slotCode;
    private Timestamp entryTime;
    private BigDecimal hourlyRate;
    private BigDecimal lostTicketFee;
    private String roundingMethod;
    private int minBillableHours;

    public long getTicketId() { return ticketId; }
    public void setTicketId(long ticketId) { this.ticketId = ticketId; }

    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getSlotCode() { return slotCode; }
    public void setSlotCode(String slotCode) { this.slotCode = slotCode; }

    public Timestamp getEntryTime() { return entryTime; }
    public void setEntryTime(Timestamp entryTime) { this.entryTime = entryTime; }

    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }

    public BigDecimal getLostTicketFee() { return lostTicketFee; }
    public void setLostTicketFee(BigDecimal lostTicketFee) { this.lostTicketFee = lostTicketFee; }

    public String getRoundingMethod() { return roundingMethod; }
    public void setRoundingMethod(String roundingMethod) { this.roundingMethod = roundingMethod; }

    public int getMinBillableHours() { return minBillableHours; }
    public void setMinBillableHours(int minBillableHours) { this.minBillableHours = minBillableHours; }
}