package hello.smartpark.dao;

import hello.smartpark.model.ViTriDo;
import hello.smartpark.model.ViTriDo.BaiDoXe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ViTriDAO {

    // Lấy danh sách bãi đỗ xe
    public List<BaiDoXe> findAllLots() throws SQLException {
        List<BaiDoXe> list = new ArrayList<>();
        String sql = "SELECT parking_lot_id, lot_name FROM parking_lots ORDER BY parking_lot_id";
        try (Connection conn = JDBCConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new BaiDoXe(
                        rs.getInt("parking_lot_id"),
                        rs.getString("lot_name")
                ));
            }
        }
        return list;
    }

    // Lấy toàn bộ ô đỗ của 1 bãi kèm thông tin xe đang gửi (nếu có)
    public List<ViTriDo> findSlotsByLot(int lotId) throws SQLException {
        List<ViTriDo> list = new ArrayList<>();
        String sql = "SELECT s.slot_id, s.parking_lot_id, s.slot_code, s.area_name, s.slot_status, " +
                     "t.ticket_code, t.entry_time, v.license_plate " +
                     "FROM parking_slots s " +
                     "LEFT JOIN parking_tickets t ON s.slot_id = t.slot_id AND t.status = 'PARKING' " +
                     "LEFT JOIN vehicles v ON t.vehicle_id = v.vehicle_id " +
                     "WHERE s.parking_lot_id = ? AND s.slot_status <> 'DISABLED' " +
                     "ORDER BY s.slot_code";
        try (Connection conn = JDBCConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lotId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ViTriDo vt = new ViTriDo();
                    vt.setSlotId(rs.getLong("slot_id"));
                    vt.setParkingLotId(rs.getInt("parking_lot_id"));
                    vt.setSlotCode(rs.getString("slot_code"));
                    vt.setAreaName(rs.getString("area_name"));
                    vt.setSlotStatus(rs.getString("slot_status"));
                    vt.setTicketCode(rs.getString("ticket_code"));
                    vt.setEntryTime(rs.getTimestamp("entry_time"));
                    vt.setLicensePlate(rs.getString("license_plate"));
                    list.add(vt);
                }
            }
        }
        return list;
    }

    // Chuyển đổi trạng thái ô đỗ (ACTIVE <-> MAINTENANCE)
    public void updateSlotStatus(long slotId, String newStatus) throws SQLException {
        String sql = "UPDATE parking_slots SET slot_status = ? WHERE slot_id = ?";
        try (Connection conn = JDBCConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setLong(2, slotId);
            ps.executeUpdate();
        }
    }
}