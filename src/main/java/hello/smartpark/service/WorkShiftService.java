/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hello.smartpark.service;

import hello.smartpark.dao.WorkShiftDAO;
import hello.smartpark.model.WorkShift;
import hello.smartpark.model.ShiftAssignment;
import hello.smartpark.model.User;
import hello.smartpark.model.ShiftSummary;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author PC
 */
public class WorkShiftService {

    private final WorkShiftDAO workShiftDAO;

    public WorkShiftService(WorkShiftDAO workShiftDAO) {
        this.workShiftDAO = Objects.requireNonNull(
                workShiftDAO, "WorkShiftDAO không được null"
        );
    }

    public List<WorkShift> getAllWorkShifts() throws SQLException {
        return workShiftDAO.getAllWorkShifts();
    }

    public boolean createShiftWithAssignments(
            WorkShift ca,
            List<ShiftAssignment> danhSachPhanCong
    ) throws SQLException {
        if (ca == null) {
            throw new IllegalArgumentException(
                    "Thông tin ca làm việc không hợp lệ."
            );
        }
        if (ca.getShiftName() == null
                || ca.getShiftName().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Tên ca làm việc không được để trống."
            );
        }
        if (ca.getStartAt() == null || ca.getEndAt() == null) {
            throw new IllegalArgumentException(
                    "Thời gian bắt đầu và kết thúc không được để trống."
            );
        }
        if (!ca.getStartAt().isBefore(ca.getEndAt())) {
            throw new IllegalArgumentException(
                    "Thời gian kết thúc phải sau thời gian bắt đầu."
            );
        }
        if (danhSachPhanCong == null
                || danhSachPhanCong.isEmpty()) {
            throw new IllegalArgumentException(
                    "Ca làm việc phải có ít nhất một nhân viên."
            );
        }
        return workShiftDAO.createShiftWithAssignments(
                ca, danhSachPhanCong
        );
    }

// Bàn giao ca làm việc
    public boolean handoverShift(long shiftId, long userId) throws SQLException {
        if (shiftId <= 0 || userId <= 0) {
            throw new IllegalArgumentException("Thông tin ca hoặc tài khoản không hợp lệ.");
        }
        return workShiftDAO.handoverShift(shiftId, userId);
    }
// Lấy danh sách ca theo tài khoản đăng nhập

    public List<WorkShift> getWorkShiftsForUser(long userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Tài khoản không hợp lệ.");
        }
        return workShiftDAO.getWorkShiftsForUser(userId);
    }
// Bắt đầu ca
    public boolean openShift(long shiftId, long userId) throws SQLException {
        if (shiftId <= 0 || userId <= 0) {
            throw new IllegalArgumentException("Thông tin không hợp lệ.");
        }
        return workShiftDAO.openShift(shiftId, userId);
    }
// Kết thúc ca
    public boolean endShift(long shiftId, long userId) throws SQLException {
        if (shiftId <= 0 || userId <= 0) {
            throw new IllegalArgumentException("Thông tin không hợp lệ.");
        }
        return workShiftDAO.endShift(shiftId, userId);
    }
}
