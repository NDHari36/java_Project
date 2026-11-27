package hello.smartpark.model;

import java.sql.Timestamp;

public class ViTriDo {
    private long slotId;
    private int parkingLotId;
    private String slotCode;
    private String areaName;
    private String slotStatus;    
    private String ticketCode;   
    private String licensePlate;  
    private Timestamp entryTime;  

    public long getSlotId() { return slotId; }
    public void setSlotId(long slotId) { this.slotId = slotId; }

    public int getParkingLotId() { return parkingLotId; }
    public void setParkingLotId(int parkingLotId) { this.parkingLotId = parkingLotId; }

    public String getSlotCode() { return slotCode; }
    public void setSlotCode(String slotCode) { this.slotCode = slotCode; }

    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }

    public String getSlotStatus() { return slotStatus; }
    public void setSlotStatus(String slotStatus) { this.slotStatus = slotStatus; }

    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public Timestamp getEntryTime() { return entryTime; }
    public void setEntryTime(Timestamp entryTime) { this.entryTime = entryTime; }

    public boolean isOccupied() {
        return ticketCode != null && !ticketCode.isEmpty();
    }

    public boolean isMaintenance() {
        return "MAINTENANCE".equalsIgnoreCase(slotStatus);
    }

    // Lớp đại diện cho từng bãi xe trong bảng parking_lots
    public static class BaiDoXe {
        private final int lotId;
        private final String lotName;

        public BaiDoXe(int lotId, String lotName) {
            this.lotId = lotId;
            this.lotName = lotName;
        }

        public int getLotId() { return lotId; }
        public String getLotName() { return lotName; }
    }
}