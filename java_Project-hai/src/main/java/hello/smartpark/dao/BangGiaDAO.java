package hello.smartpark.dao;

import hello.smartpark.model.BangGia;
import hello.smartpark.model.BangGia.VehicleTypeItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BangGiaDAO {

    public List<BangGia> findAll() throws SQLException {
        List<BangGia> list = new ArrayList<>();
        String sql = "SELECT r.rate_id, r.vehicle_type_id, vt.type_name, " +
                     "r.hourly_rate, r.lost_ticket_fee, r.rounding_method, " +
                     "r.min_billable_hours, r.is_active " +
                     "FROM parking_rates r " +
                     "JOIN vehicle_types vt ON r.vehicle_type_id = vt.vehicle_type_id " +
                     "ORDER BY r.rate_id";
        try (Connection conn = JDBCConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public List<VehicleTypeItem> findAllVehicleTypes() throws SQLException {
        List<VehicleTypeItem> list = new ArrayList<>();
        String sql = "SELECT vehicle_type_id, type_name FROM vehicle_types WHERE is_active = 1 ORDER BY vehicle_type_id";
        try (Connection conn = JDBCConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new VehicleTypeItem(
                        rs.getInt("vehicle_type_id"),
                        rs.getString("type_name")
                ));
            }
        }
        return list;
    }
    public VehicleTypeItem insertVehicleType(String typeName) throws SQLException {
        if (typeName == null || typeName.trim().isEmpty()) {
            throw new SQLException("Tên loại phương tiện không được để trống.");
        }
        String cleanName = typeName.trim();

        // Kiểm tra trùng tên loại xe (không phân biệt hoa thường)
        String checkSql = "SELECT vehicle_type_id FROM vehicle_types WHERE LOWER(type_name) = LOWER(?) LIMIT 1";
        try (Connection conn = JDBCConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setString(1, cleanName);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    throw new SQLException("Loại phương tiện \"" + cleanName + "\" đã tồn tại trong danh sách!");
                }
            }
        }

        String insertSql = "INSERT INTO vehicle_types (type_name, is_active) VALUES (?, 1)";
        try (Connection conn = JDBCConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cleanName);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new VehicleTypeItem(keys.getInt(1), cleanName);
                }
            }
        }
        throw new SQLException("Không thể lấy mã loại phương tiện vừa tạo.");
    }

    public void insert(BangGia bg) throws SQLException {
        String sql = "INSERT INTO parking_rates " +
                     "(vehicle_type_id, hourly_rate, lost_ticket_fee, rounding_method, min_billable_hours, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = JDBCConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bg.getVehicleTypeId());
            ps.setBigDecimal(2, bg.getDonGiaGio());
            ps.setBigDecimal(3, bg.getPhiMatVe());
            ps.setString(4, bg.getRoundingMethod());
            ps.setInt(5, bg.getMinBillableHours());
            ps.setBoolean(6, bg.isActive());
            ps.executeUpdate();
        }
    }

    public void update(BangGia bg) throws SQLException {
        String sql = "UPDATE parking_rates SET " +
                     "vehicle_type_id=?, hourly_rate=?, lost_ticket_fee=?, " +
                     "rounding_method=?, min_billable_hours=?, is_active=? " +
                     "WHERE rate_id=?";
        try (Connection conn = JDBCConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bg.getVehicleTypeId());
            ps.setBigDecimal(2, bg.getDonGiaGio());
            ps.setBigDecimal(3, bg.getPhiMatVe());
            ps.setString(4, bg.getRoundingMethod());
            ps.setInt(5, bg.getMinBillableHours());
            ps.setBoolean(6, bg.isActive());
            ps.setInt(7, bg.getRateId());
            ps.executeUpdate();
        }
    }

    public void delete(int rateId) throws SQLException {
        String sql = "DELETE FROM parking_rates WHERE rate_id=?";
        try (Connection conn = JDBCConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rateId);
            ps.executeUpdate();
        }
    }

    private BangGia map(ResultSet rs) throws SQLException {
        BangGia bg = new BangGia();
        bg.setRateId(rs.getInt("rate_id"));
        bg.setVehicleTypeId(rs.getInt("vehicle_type_id"));
        bg.setLoaiXe(rs.getString("type_name"));
        bg.setDonGiaGio(rs.getBigDecimal("hourly_rate"));
        bg.setPhiMatVe(rs.getBigDecimal("lost_ticket_fee"));
        bg.setRoundingMethod(rs.getString("rounding_method"));
        bg.setMinBillableHours(rs.getInt("min_billable_hours"));
        bg.setActive(rs.getBoolean("is_active"));
        return bg;
    }
}