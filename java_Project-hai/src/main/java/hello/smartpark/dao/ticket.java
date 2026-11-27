package hello.smartpark.dao;

import hello.smartpark.model.ticketinfor;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;

public class ticket {

    public ticketinfor findByTicketCode(String ticketCode) throws SQLException {
        String sql = """
            SELECT
                t.ticket_id, t.ticket_code, v.license_plate, vt.type_name,
                t.entry_time, t.exit_time, t.status,
                p.parking_fee, p.lost_ticket_fee, p.amount, p.payment_method, p.paid_at
            FROM parking_tickets t
            INNER JOIN vehicles v ON t.vehicle_id = v.vehicle_id
            INNER JOIN vehicle_types vt ON t.vehicle_type_id = vt.vehicle_type_id
            LEFT JOIN payments p ON t.ticket_id = p.ticket_id
            WHERE t.ticket_code = ?
            """;
        return queryOne(sql, ticketCode.trim());
    }

    public ticketinfor findByLicensePlate(String licensePlate) throws SQLException {
        String sql = """
            SELECT
                t.ticket_id, t.ticket_code, v.license_plate, vt.type_name,
                t.entry_time, t.exit_time, t.status,
                p.parking_fee, p.lost_ticket_fee, p.amount, p.payment_method, p.paid_at
            FROM parking_tickets t
            INNER JOIN vehicles v ON t.vehicle_id = v.vehicle_id
            INNER JOIN vehicle_types vt ON t.vehicle_type_id = vt.vehicle_type_id
            LEFT JOIN payments p ON t.ticket_id = p.ticket_id
            WHERE v.license_plate = ?
            ORDER BY
                CASE WHEN t.status = 'PARKING' THEN 0 ELSE 1 END,
                t.entry_time DESC
            LIMIT 1
            """;
        return queryOne(sql, licensePlate.trim().toUpperCase());
    }

    private ticketinfor queryOne(String sql, String param) throws SQLException {
        try (Connection conn = JDBCConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (conn == null) throw new SQLException("Không thể kết nối database");

            ps.setString(1, param);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
            return null;
        }
    }

    private ticketinfor mapRow(ResultSet rs) throws SQLException {
        ticketinfor info = new ticketinfor();
        info.setTicketId(rs.getLong("ticket_id"));
        info.setTicketCode(rs.getString("ticket_code"));
        info.setLicensePlate(rs.getString("license_plate"));
        info.setVehicleTypeName(rs.getString("type_name"));
        info.setEntryTime(toLDT(rs.getTimestamp("entry_time")));
        info.setExitTime(toLDT(rs.getTimestamp("exit_time")));
        info.setStatus(rs.getString("status"));

        BigDecimal amount = rs.getBigDecimal("amount");
        if (amount != null) {
            info.setParkingFee(rs.getBigDecimal("parking_fee"));
            info.setLostTicketFee(rs.getBigDecimal("lost_ticket_fee"));
            info.setAmount(amount);
            info.setPaymentMethod(rs.getString("payment_method"));
            info.setPaidAt(toLDT(rs.getTimestamp("paid_at")));
        }
        return info;
    }

    private LocalDateTime toLDT(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}