package hello.smartpark.model;

import java.math.BigDecimal;

public class BangGia {
    private int rateId;
    private int vehicleTypeId;
    private String loaiXe;           // Lấy từ vehicle_types.type_name khi JOIN
    private BigDecimal donGiaGio;    // hourly_rate
    private BigDecimal phiMatVe;     // lost_ticket_fee
    private String roundingMethod;   // 'ROUND_UP' hoặc 'PROPORTIONAL'
    private int minBillableHours;    // min_billable_hours
    private boolean active;          // is_active

    public BangGia() {
        this.minBillableHours = 1;
        this.active = true;
    }

    public int getRateId() { return rateId; }
    public void setRateId(int rateId) { this.rateId = rateId; }

    public int getVehicleTypeId() { return vehicleTypeId; }
    public void setVehicleTypeId(int vehicleTypeId) { this.vehicleTypeId = vehicleTypeId; }

    public String getLoaiXe() { return loaiXe; }
    public void setLoaiXe(String loaiXe) { this.loaiXe = loaiXe; }

    public BigDecimal getDonGiaGio() { return donGiaGio; }
    public void setDonGiaGio(BigDecimal donGiaGio) { this.donGiaGio = donGiaGio; }

    public BigDecimal getPhiMatVe() { return phiMatVe; }
    public void setPhiMatVe(BigDecimal phiMatVe) { this.phiMatVe = phiMatVe; }

    public String getRoundingMethod() { return roundingMethod; }
    public void setRoundingMethod(String roundingMethod) { this.roundingMethod = roundingMethod; }

    public int getMinBillableHours() { return minBillableHours; }
    public void setMinBillableHours(int minBillableHours) { this.minBillableHours = minBillableHours; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getRoundingMethodLabel() {
        if ("PROPORTIONAL".equals(roundingMethod)) {
            return "Tính theo phút";
        }
        return "Làm tròn lên";
    }

    // Class hỗ trợ hiển thị danh sách loại xe trong JComboBox
    public static class VehicleTypeItem {
        private final int id;
        private final String name;

        public VehicleTypeItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof VehicleTypeItem)) return false;
            return this.id == ((VehicleTypeItem) obj).id;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(id);
        }
    }
}