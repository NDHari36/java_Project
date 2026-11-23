/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hello.smartpark.dao;

import hello.smartpark.model.ShiftAssignment;
import hello.smartpark.model.ShiftSummary;
import hello.smartpark.model.WorkShift;
import hello.smartpark.model.User;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PC
 */
public class WorkShiftDAO {

    public List<WorkShift> getAllWorkShifts() throws SQLException {
        Connection conn = JDBCConnection.getConnection();
        List<WorkShift> shifts = new ArrayList<>();
        String sql = """
        SELECT ws.*,
            COALESCE(a.employee_names, 'Chưa phân công') AS employee_names,
            COALESCE(a.employee_count, 0) AS employee_count              
            FROM work_shifts ws                
            LEFT JOIN (SELECT sa.shift_id,GROUP_CONCAT( u.full_name ORDER BY u.full_name SEPARATOR ', ') AS employee_names,
                 COUNT(DISTINCT sa.user_id) AS employee_count FROM shift_assignments sa
                 JOIN users u ON sa.user_id = u.user_id
                 JOIN roles r ON u.role_id = r.role_id
                 WHERE r.role_name = 'EMPLOYEE' GROUP BY sa.shift_id) a ON ws.shift_id = a.shift_id
                 ORDER BY ws.start_at DESC;
                 """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                WorkShift shift = new WorkShift();
                shift.setShiftId(rs.getInt("shift_id"));
                shift.setShiftName(rs.getString("shift_name"));
                Timestamp startAt = rs.getTimestamp("start_at");
                shift.setStartAt(startAt != null ? startAt.toLocalDateTime() : null);
                Timestamp endAt = rs.getTimestamp("end_at");
                shift.setEndAt(endAt != null ? endAt.toLocalDateTime() : null);
                Timestamp openedAt = rs.getTimestamp("opened_at");
                shift.setOpenedAt(openedAt != null ? openedAt.toLocalDateTime() : null);
                Timestamp endedAt = rs.getTimestamp("ended_at");
                shift.setEndedAt(endedAt != null ? endedAt.toLocalDateTime() : null);
                Timestamp closedAt = rs.getTimestamp("closed_at");
                shift.setClosedAt(closedAt != null ? closedAt.toLocalDateTime() : null);
                shift.setOpeningCash(rs.getBigDecimal("opening_cash"));
                shift.setClosingCash(rs.getBigDecimal("closing_cash"));
                shift.setStatus(rs.getString("status"));
                shift.setOpenedBy(rs.getInt("opened_by"));
                shift.setClosedBy(rs.getInt("closed_by"));
                shift.setNote(rs.getString("note"));
                shift.setEmployeeNames(rs.getString("employee_names"));
                shift.setEmployeeCount(rs.getInt("employee_count"));
                shifts.add(shift);
            }
        }
        return shifts;
    }

    // 3. TẠO CA + PHÂN CÔNG NHÂN VIÊN
    public boolean createShiftWithAssignments(WorkShift ca, List<ShiftAssignment> danhSachPhanCong) throws SQLException {
        String sqlTaoCa = """
            INSERT INTO work_shifts
                (shift_name, start_at, end_at,
                 opening_cash, status, note)
            VALUES (?, ?, ?, 0, 'PLANNED', ?)
            """;
        String sqlPhanCong = """
            INSERT INTO shift_assignments
                (shift_id, user_id, duty, assigned_at)
            VALUES (?, ?, ?, NOW())
            """;
        Connection conn = JDBCConnection.getConnection();
        if (conn == null) {
            throw new SQLException("Không thể kết nối database.");
        }
        try {
            conn.setAutoCommit(false);
            long shiftId;
            // Tạo ca làm việc
            try (PreparedStatement ps = conn.prepareStatement(sqlTaoCa, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, ca.getShiftName());
                ps.setTimestamp(2, Timestamp.valueOf(ca.getStartAt()));
                ps.setTimestamp(3, Timestamp.valueOf(ca.getEndAt()));
                ps.setString(4, ca.getNote());
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    shiftId = rs.getLong(1);
                } else {
                    throw new SQLException("Không lấy được ID ca.");
                }
            }
            // Phân công nhân viên
            if (danhSachPhanCong == null || danhSachPhanCong.isEmpty()) {
                throw new SQLException("Ca phải có nhân viên.");
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlPhanCong)) {
                for (ShiftAssignment pc : danhSachPhanCong) {
                    ps.setLong(1, shiftId);
                    ps.setLong(2, pc.getUserId());
                    ps.setString(3, pc.getDuty() == null ? "Nhân viên" : pc.getDuty());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            ca.setShiftId(shiftId);
            return true;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
// Bàn giao và đóng ca làm việc

    // Bàn giao và đóng ca
    public boolean handoverShift(long shiftId, long userId) throws SQLException {
        String sql = """
        UPDATE work_shifts ws
        SET status = 'CLOSED',
            closed_at = NOW(),
            closed_by = ?
        WHERE ws.shift_id = ?
          AND ws.status = 'HANDOVER_PENDING'
          AND (
              EXISTS (
                  SELECT 1
                  FROM shift_assignments sa
                  WHERE sa.shift_id = ws.shift_id
                    AND sa.user_id = ?
              )
              OR EXISTS (
                  SELECT 1
                  FROM users u
                  JOIN roles r ON u.role_id = r.role_id
                  WHERE u.user_id = ?
                    AND r.role_name = 'ADMIN'
              )
          )
        """;
        try (Connection conn = JDBCConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Không thể kết nối database.");           
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, userId);
                ps.setLong(2, shiftId);
                ps.setLong(3, userId);
                ps.setLong(4, userId);
                return ps.executeUpdate() == 1;
            }
        }
    }
// Lấy danh sách ca theo tài khoản đăng nhập
// Admin xem tất cả, nhân viên chỉ xem ca được phân công

    public List<WorkShift> getWorkShiftsForUser(long userId) throws SQLException {
        String sql = """
        SELECT ws.*,
            COALESCE(a.employee_names, 'Chưa phân công') AS employee_names,
            COALESCE(a.employee_count, 0) AS employee_count
        FROM work_shifts ws

        LEFT JOIN (
            SELECT sa.shift_id,
                GROUP_CONCAT(
                    u.full_name ORDER BY u.full_name SEPARATOR ', '
                ) AS employee_names,
                COUNT(DISTINCT sa.user_id) AS employee_count
            FROM shift_assignments sa
            JOIN users u ON sa.user_id = u.user_id
            JOIN roles r ON u.role_id = r.role_id
            WHERE r.role_name = 'EMPLOYEE'
            GROUP BY sa.shift_id
        ) a ON ws.shift_id = a.shift_id
        WHERE
            EXISTS (
                SELECT 1
                FROM users u
                JOIN roles r ON u.role_id = r.role_id
                WHERE u.user_id = ?
                  AND r.role_name = 'ADMIN'
            )
            OR EXISTS (
                SELECT 1
                FROM shift_assignments sa
                WHERE sa.shift_id = ws.shift_id
                  AND sa.user_id = ?
            )
        ORDER BY ws.start_at DESC
        """;

        List<WorkShift> shifts = new ArrayList<>();
        try (Connection conn = JDBCConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Không thể kết nối database.");
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, userId);
                ps.setLong(2, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        WorkShift shift = new WorkShift();
                        shift.setShiftId(rs.getInt("shift_id"));
                        shift.setShiftName(rs.getString("shift_name"));
                        Timestamp startAt = rs.getTimestamp("start_at");
                        shift.setStartAt(startAt != null ? startAt.toLocalDateTime() : null);
                        Timestamp endAt = rs.getTimestamp("end_at");
                        shift.setEndAt(endAt != null ? endAt.toLocalDateTime() : null);
                        Timestamp openedAt = rs.getTimestamp("opened_at");
                        shift.setOpenedAt(openedAt != null ? openedAt.toLocalDateTime() : null);
                        Timestamp endedAt = rs.getTimestamp("ended_at");
                        shift.setEndedAt(endedAt != null ? endedAt.toLocalDateTime() : null);
                        Timestamp closedAt = rs.getTimestamp("closed_at");
                        shift.setClosedAt(closedAt != null ? closedAt.toLocalDateTime() : null);
                        shift.setOpeningCash(rs.getBigDecimal("opening_cash"));
                        shift.setClosingCash(rs.getBigDecimal("closing_cash"));
                        shift.setStatus(rs.getString("status"));
                        shift.setOpenedBy(rs.getInt("opened_by"));
                        shift.setClosedBy(rs.getInt("closed_by"));
                        shift.setNote(rs.getString("note"));
                        shift.setEmployeeNames(rs.getString("employee_names"));
                        shift.setEmployeeCount(rs.getInt("employee_count"));
                        shifts.add(shift);
                    }
                }
            }
        }

        return shifts;
    }
// Bắt đầu ca làm việc
    public boolean openShift(long shiftId, long userId) throws SQLException {
        String sql = """
        UPDATE work_shifts ws
        SET status = 'OPEN',
            opened_at = NOW(),
            opened_by = ?
        WHERE ws.shift_id = ?
          AND ws.status = 'PLANNED'
          AND (
              EXISTS (
                  SELECT 1
                  FROM shift_assignments sa
                  WHERE sa.shift_id = ws.shift_id
                    AND sa.user_id = ?
              )
              OR EXISTS (
                  SELECT 1
                  FROM users u
                  JOIN roles r ON u.role_id = r.role_id
                  WHERE u.user_id = ?
                    AND r.role_name = 'ADMIN'
              )
          )
        """;
        try (Connection conn = JDBCConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Không thể kết nối database.");
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, userId);
                ps.setLong(2, shiftId);
                ps.setLong(3, userId);
                ps.setLong(4, userId);
                return ps.executeUpdate() == 1;
            }
        }
    }
    // Kết thúc ca làm việc

    // Kết thúc ca làm việc
    public boolean endShift(long shiftId, long userId) throws SQLException {
        String sql = """
        UPDATE work_shifts ws
        SET status = 'HANDOVER_PENDING',
            ended_at = NOW()
        WHERE ws.shift_id = ?
          AND ws.status = 'OPEN'
          AND (
              EXISTS (
                  SELECT 1
                  FROM shift_assignments sa
                  WHERE sa.shift_id = ws.shift_id
                    AND sa.user_id = ?
              )
              OR EXISTS (
                  SELECT 1
                  FROM users u
                  JOIN roles r ON u.role_id = r.role_id
                  WHERE u.user_id = ?
                    AND r.role_name = 'ADMIN'
              )
          )
        """;

        try (Connection conn = JDBCConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Không thể kết nối database.");
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, shiftId);
                ps.setLong(2, userId);
                ps.setLong(3, userId);
                return ps.executeUpdate() == 1;
            }
        }
    }

}
