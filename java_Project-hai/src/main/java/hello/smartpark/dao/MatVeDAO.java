package hello.smartpark.dao;

import hello.smartpark.model.MatVe;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;

public class MatVeDAO {

    // Tìm vé đang gửi (status = 'PARKING') theo biển số xe
    public MatVe findParkingTicketByPlate(String plate) throws SQLException {
        String cleanPlate = plate.trim().replaceAll("[\\s.-]", "").toUpperCase();
        String sql = "SELECT t.ticket_id, t.ticket_code, t.entry_time, " +
                     "v.license_plate, s.slot_code, " +
                     "COALESCE(r.hourly_rate, 0) AS hourly_rate, " +
                     "COALESCE(r.lost_ticket_fee, 0) AS lost_ticket_fee, " +
                     "COALESCE(r.rounding_method, 'ROUND_UP') AS rounding_method, " +
                     "COALESCE(r.min_billable_hours, 1) AS min_billable_hours " +
                     "FROM parking_tickets t " +
                     "JOIN vehicles v ON t.vehicle_id = v.vehicle_id " +
                     "LEFT JOIN parking_slots s ON t.slot_id = s.slot_id " +
                     "LEFT JOIN parking_rates r ON v.vehicle_type_id = r.vehicle_type_id AND r.is_active = 1 " +
                     "WHERE t.status = 'PARKING' " +
                     "AND REPLACE(REPLACE(REPLACE(UPPER(v.license_plate), '-', ''), '.', ''), ' ', '') = ? " +
                     "ORDER BY t.entry_time DESC LIMIT 1";

        try (Connection conn = JDBCConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cleanPlate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MatVe info = new MatVe();
                    info.setTicketId(rs.getLong("ticket_id"));
                    info.setTicketCode(rs.getString("ticket_code"));
                    info.setEntryTime(rs.getTimestamp("entry_time"));
                    info.setLicensePlate(rs.getString("license_plate"));
                    info.setSlotCode(rs.getString("slot_code"));
                    info.setHourlyRate(rs.getBigDecimal("hourly_rate"));
                    info.setLostTicketFee(rs.getBigDecimal("lost_ticket_fee"));
                    info.setRoundingMethod(rs.getString("rounding_method"));
                    info.setMinBillableHours(rs.getInt("min_billable_hours"));
                    return info;
                }
            }
        }
        return null;
    }

    // Tính tiền gửi xe dựa theo bảng giá (làm tròn lên hoặc tính theo phút)
    public BigDecimal calculateParkingFee(MatVe info) {
        if (info.getEntryTime() == null || info.getHourlyRate() == null) {
            return BigDecimal.ZERO;
        }
        LocalDateTime entry = info.getEntryTime().toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();
        long minutes = Math.max(1, Duration.between(entry, now).toMinutes());

        BigDecimal minHours = BigDecimal.valueOf(Math.max(1, info.getMinBillableHours()));
        BigDecimal billableHours;

        if ("PROPORTIONAL".equalsIgnoreCase(info.getRoundingMethod())) {
            BigDecimal actualHours = BigDecimal.valueOf(minutes)
                    .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
            billableHours = actualHours.max(minHours);
        } else {
            long ceilHours = (long) Math.ceil(minutes / 60.0);
            billableHours = BigDecimal.valueOf(Math.max(ceilHours, info.getMinBillableHours()));
        }

        return info.getHourlyRate()
                .multiply(billableHours)
                .setScale(0, RoundingMode.HALF_UP);
    }

    // Xác nhận xử lý mất vé: cập nhật giờ ra, tổng tiền và chuyển status sang LOST
    public void confirmLostTicket(long ticketId, BigDecimal totalFee) throws SQLException {
        String sql = "UPDATE parking_tickets " +
                     "SET exit_time = NOW(), total_fee = ?, status = 'LOST' " +
                     "WHERE ticket_id = ? AND status = 'PARKING'";
        try (Connection conn = JDBCConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, totalFee);
            ps.setLong(2, ticketId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Vé này đã được xử lý trước đó hoặc không còn ở trạng thái đang gửi!");
            }
        }
    }
}