/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hello.smartpark.dao;

import hello.smartpark.model.Vehicle;
import hello.smartpark.model.VehicleType;
import hello.smartpark.model.ParkingSlot;
import hello.smartpark.model.ParkingTicket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Types;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author PC
 */
public class ParkingEntryDAO {

    // 1. Lấy danh sách loại phương tiện đang hoạt động
    public List<VehicleType> getActiveVehicleTypes() throws SQLException {

        String sql = """
            SELECT vt.vehicle_type_id, vt.type_name,
                   vt.description, vt.is_active
            FROM vehicle_types vt
            JOIN parking_rates pr
                ON vt.vehicle_type_id = pr.vehicle_type_id
            WHERE vt.is_active = TRUE
              AND pr.is_active = TRUE
            ORDER BY vt.type_name
            """;

        List<VehicleType> danhSach = new ArrayList<>();

        try (Connection conn = JDBCConnection.getConnection()) {

            if (conn == null) {
                throw new SQLException("Không thể kết nối database.");
            }

            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    VehicleType type = new VehicleType();

                    type.setVehicleTypeId(rs.getInt("vehicle_type_id"));
                    type.setTypeName(rs.getString("type_name"));
                    type.setDescription(rs.getString("description"));
                    type.setActive(rs.getBoolean("is_active"));

                    danhSach.add(type);
                }
            }
        }

        return danhSach;
    }

    // 2. Tìm phương tiện theo biển số
    public Vehicle findVehicleByLicensePlate(String licensePlate)
            throws SQLException {

        String sql = """
            SELECT v.vehicle_id, v.license_plate,
                   v.vehicle_type_id, vt.type_name
            FROM vehicles v
            JOIN vehicle_types vt
                ON v.vehicle_type_id = vt.vehicle_type_id
            WHERE UPPER(v.license_plate) = UPPER(?)
            """;

        try (Connection conn = JDBCConnection.getConnection()) {

            if (conn == null) {
                throw new SQLException("Không thể kết nối database.");
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, licensePlate);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        Vehicle vehicle = new Vehicle();

                        vehicle.setVehicleId(rs.getLong("vehicle_id"));
                        vehicle.setLicensePlate(rs.getString("license_plate"));
                        vehicle.setVehicleTypeId(rs.getInt("vehicle_type_id"));
                        vehicle.setVehicleTypeName(rs.getString("type_name"));

                        return vehicle;
                    }
                }
            }
        }

        return null;
    }

    // 3. Lấy vị trí đỗ theo loại phương tiện
    public List<ParkingSlot> getParkingSlots(int vehicleTypeId)
            throws SQLException {

        String sql = """
            SELECT ps.slot_id, ps.parking_lot_id,
                   pl.lot_name, ps.slot_code, ps.area_name,
                   ps.allowed_vehicle_type_id, ps.slot_status,

                   CASE
                       WHEN ps.slot_status = 'MAINTENANCE'
                           THEN 'MAINTENANCE'
                       WHEN ps.slot_status = 'DISABLED'
                           THEN 'DISABLED'
                       WHEN t.ticket_id IS NOT NULL
                           THEN 'OCCUPIED'
                       ELSE 'AVAILABLE'
                   END AS availability_status

            FROM parking_slots ps
            JOIN parking_lots pl
                ON ps.parking_lot_id = pl.parking_lot_id

            LEFT JOIN parking_tickets t
                ON t.slot_id = ps.slot_id
               AND t.parking_lot_id = ps.parking_lot_id
               AND t.status = 'PARKING'

            WHERE pl.status = 'OPEN'
              AND (
                    ps.allowed_vehicle_type_id IS NULL
                    OR ps.allowed_vehicle_type_id = ?
              )

            ORDER BY pl.lot_name, ps.area_name, ps.slot_code
            """;

        List<ParkingSlot> danhSach = new ArrayList<>();

        try (Connection conn = JDBCConnection.getConnection()) {

            if (conn == null) {
                throw new SQLException("Không thể kết nối database.");
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, vehicleTypeId);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        ParkingSlot slot = new ParkingSlot();

                        slot.setSlotId(rs.getLong("slot_id"));
                        slot.setParkingLotId(rs.getInt("parking_lot_id"));
                        slot.setLotName(rs.getString("lot_name"));
                        slot.setSlotCode(rs.getString("slot_code"));
                        slot.setAreaName(rs.getString("area_name"));

                        int allowedTypeId
                                = rs.getInt("allowed_vehicle_type_id");

                        slot.setAllowedVehicleTypeId(
                                rs.wasNull() ? null : allowedTypeId
                        );

                        slot.setSlotStatus(rs.getString("slot_status"));
                        slot.setAvailabilityStatus(
                                rs.getString("availability_status")
                        );

                        danhSach.add(slot);
                    }
                }
            }
        }

        return danhSach;
    }

    // 4. Lấy ca đang mở của tài khoản
    public Long getOpenShiftIdForUser(long userId) throws SQLException {

        String sql = """
        SELECT ws.shift_id
        FROM work_shifts ws
        JOIN shift_assignments sa
            ON sa.shift_id = ws.shift_id
        JOIN users u
            ON u.user_id = sa.user_id
        WHERE ws.status = 'OPEN'
          AND sa.user_id = ?
          AND u.status = 'ACTIVE'
        LIMIT 1
        """;

        try (Connection conn = JDBCConnection.getConnection()) {

            if (conn == null) {
                throw new SQLException("Không thể kết nối database.");
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setLong(1, userId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong("shift_id");
                    }
                }
            }
        }

        return null;
    }

    // 5. Ghi nhận xe vào bãi
    public ParkingTicket createVehicleEntry(String licensePlate, int vehicleTypeId, long slotId, long userId, long shiftId) throws SQLException {
        return createVehicleEntry(
                licensePlate, vehicleTypeId, slotId, userId,
                Long.valueOf(shiftId), false
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
        String sqlKiemTraCa = """
    SELECT ws.shift_id
    FROM work_shifts ws
    JOIN shift_assignments sa
        ON sa.shift_id = ws.shift_id
    JOIN users u
        ON u.user_id = sa.user_id
    WHERE ws.shift_id = ?
      AND ws.status = 'OPEN'
      AND sa.user_id = ?
      AND u.status = 'ACTIVE'
    FOR UPDATE
    """;

        String sqlKiemTraAdmin = """
            SELECT u.user_id
            FROM users u
            JOIN roles r ON r.role_id = u.role_id
            WHERE u.user_id = ?
              AND u.status = 'ACTIVE'
              AND r.role_name = 'ADMIN'
            """;

        String sqlKiemTraViTri = """
            SELECT ps.parking_lot_id,
                   ps.allowed_vehicle_type_id
            FROM parking_slots ps
            JOIN parking_lots pl
                ON ps.parking_lot_id = pl.parking_lot_id
            WHERE ps.slot_id = ?
              AND ps.slot_status = 'ACTIVE'
              AND pl.status = 'OPEN'
            FOR UPDATE
            """;

        String sqlTimXe = """
            SELECT vehicle_id, vehicle_type_id
            FROM vehicles
            WHERE UPPER(license_plate) = UPPER(?)
            FOR UPDATE
            """;

        String sqlThemXe = """
            INSERT INTO vehicles (license_plate, vehicle_type_id)
            VALUES (?, ?)
            """;

        String sqlKiemTraXeDangGui = """
            SELECT ticket_id
            FROM parking_tickets
            WHERE active_vehicle_id = ?
            LIMIT 1
            """;

        String sqlKiemTraViTriDangDung = """
            SELECT ticket_id
            FROM parking_tickets
            WHERE active_slot_id = ?
            LIMIT 1
            """;

        String sqlLayGia = """
            SELECT hourly_rate, rounding_method, min_billable_hours
            FROM parking_rates
            WHERE vehicle_type_id = ?
              AND is_active = TRUE
            """;

        String sqlTaoVe = """
            INSERT INTO parking_tickets (
                ticket_code,
                vehicle_id,
                vehicle_type_id,
                parking_lot_id,
                slot_id,
                entry_time,
                hourly_rate_snapshot,
                rounding_method_snapshot,
                min_billable_hours_snapshot,
                status,
                entry_shift_id,
                created_by
            )
            VALUES (?, ?, ?, ?, ?, NOW(), ?, ?, ?, 'PARKING', ?, ?)
            """;

        try (Connection conn = JDBCConnection.getConnection()) {

            if (conn == null) {
                throw new SQLException("Không thể kết nối database.");
            }

            conn.setAutoCommit(false);

            try {

                // Nhân viên phải thuộc ca đang mở; Admin được bỏ qua ca.
                if (isAdmin) {
                    try (PreparedStatement ps = conn.prepareStatement(sqlKiemTraAdmin)) {
                        ps.setLong(1, userId);

                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                throw new SQLException(
                                        "Tài khoản không có quyền Admin để ghi nhận xe."
                                );
                            }
                        }
                    }
                } else {
                    if (shiftId == null) {
                        throw new SQLException(
                                "Nhân viên phải có ca làm việc đang mở."
                        );
                    }

                    try (PreparedStatement ps = conn.prepareStatement(sqlKiemTraCa)) {
                        ps.setLong(1, shiftId);
                        ps.setLong(2, userId);

                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                throw new SQLException(
                                        "Bạn không có ca đang mở hoặc không được phép ghi nhận xe."
                                );
                            }
                        }
                    }
                }

                // Kiểm tra vị trí đỗ
                int parkingLotId;
                Integer allowedTypeId;

                try (PreparedStatement ps = conn.prepareStatement(sqlKiemTraViTri)) {

                    ps.setLong(1, slotId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "Vị trí không tồn tại, đã bảo trì hoặc bãi xe đã đóng."
                            );
                        }

                        parkingLotId = rs.getInt("parking_lot_id");

                        int allowed = rs.getInt("allowed_vehicle_type_id");
                        allowedTypeId = rs.wasNull() ? null : allowed;
                    }
                }

                if (allowedTypeId != null && allowedTypeId != vehicleTypeId) {
                    throw new SQLException(
                            "Vị trí này không phù hợp với loại phương tiện."
                    );
                }

                // Tìm phương tiện đã đăng ký
                long vehicleId = 0;
                boolean xeDaTonTai = false;

                try (PreparedStatement ps = conn.prepareStatement(sqlTimXe)) {

                    ps.setString(1, licensePlate);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (rs.next()) {
                            xeDaTonTai = true;
                            vehicleId = rs.getLong("vehicle_id");

                            if (rs.getInt("vehicle_type_id") != vehicleTypeId) {
                                throw new SQLException(
                                        "Biển số này đã được đăng ký với loại phương tiện khác."
                                );
                            }
                        }
                    }
                }

                // Nếu là xe mới thì thêm vào bảng vehicles
                if (!xeDaTonTai) {

                    try (PreparedStatement ps = conn.prepareStatement(
                            sqlThemXe, Statement.RETURN_GENERATED_KEYS)) {

                        ps.setString(1, licensePlate);
                        ps.setInt(2, vehicleTypeId);

                        ps.executeUpdate();

                        try (ResultSet rs = ps.getGeneratedKeys()) {

                            if (rs.next()) {
                                vehicleId = rs.getLong(1);
                            } else {
                                throw new SQLException(
                                        "Không tạo được thông tin phương tiện."
                                );
                            }
                        }
                    }
                }

                // Kiểm tra xe đã có vé đang gửi hay chưa
                try (PreparedStatement ps = conn.prepareStatement(
                        sqlKiemTraXeDangGui)) {

                    ps.setLong(1, vehicleId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            throw new SQLException(
                                    "Phương tiện này hiện đang ở trong bãi."
                            );
                        }
                    }
                }

                // Kiểm tra vị trí đã có xe hay chưa
                try (PreparedStatement ps = conn.prepareStatement(
                        sqlKiemTraViTriDangDung)) {

                    ps.setLong(1, slotId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            throw new SQLException(
                                    "Vị trí này đã có xe khác."
                            );
                        }
                    }
                }

                // Lấy giá vé hiện hành
                java.math.BigDecimal hourlyRate;
                String roundingMethod;
                int minBillableHours;

                try (PreparedStatement ps = conn.prepareStatement(sqlLayGia)) {

                    ps.setInt(1, vehicleTypeId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "Loại phương tiện chưa có bảng giá hoạt động."
                            );
                        }

                        hourlyRate = rs.getBigDecimal("hourly_rate");
                        roundingMethod = rs.getString("rounding_method");
                        minBillableHours = rs.getInt("min_billable_hours");
                    }
                }

                // Tạo mã vé
                String ticketCode = "SP"
                        + LocalDateTime.now().format(
                                DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                        )
                        + "-"
                        + UUID.randomUUID().toString()
                                .substring(0, 8).toUpperCase();

                // Tạo vé xe
                long ticketId;

                try (PreparedStatement ps = conn.prepareStatement(
                        sqlTaoVe, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, ticketCode);
                    ps.setLong(2, vehicleId);
                    ps.setInt(3, vehicleTypeId);
                    ps.setInt(4, parkingLotId);
                    ps.setLong(5, slotId);
                    ps.setBigDecimal(6, hourlyRate);
                    ps.setString(7, roundingMethod);
                    ps.setInt(8, minBillableHours);
                    if (shiftId == null) {
                        ps.setNull(9, Types.BIGINT);
                    } else {
                        ps.setLong(9, shiftId);
                    }
                    ps.setLong(10, userId);

                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {

                        if (rs.next()) {
                            ticketId = rs.getLong(1);
                        } else {
                            throw new SQLException(
                                    "Không lấy được mã vé vừa tạo."
                            );
                        }
                    }
                }

                conn.commit();

                ParkingTicket ticket = new ParkingTicket();

                ticket.setTicketId(ticketId);
                ticket.setTicketCode(ticketCode);
                ticket.setVehicleId(vehicleId);
                ticket.setVehicleTypeId(vehicleTypeId);
                ticket.setParkingLotId(parkingLotId);
                ticket.setSlotId(slotId);
                ticket.setEntryTime(LocalDateTime.now());
                ticket.setHourlyRateSnapshot(hourlyRate);
                ticket.setRoundingMethodSnapshot(roundingMethod);
                ticket.setMinBillableHoursSnapshot(minBillableHours);
                ticket.setStatus("PARKING");
                if (shiftId != null) {
                    ticket.setEntryShiftId(shiftId);
                }
                ticket.setCreatedBy(userId);

                return ticket;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

}
