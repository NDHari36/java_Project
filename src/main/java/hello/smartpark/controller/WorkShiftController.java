/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hello.smartpark.controller;

import hello.smartpark.dao.WorkShiftDAO;
import hello.smartpark.service.WorkShiftService;
import hello.smartpark.model.WorkShift;
import hello.smartpark.model.ShiftAssignment;
import hello.smartpark.model.ShiftSummary;
import hello.smartpark.model.User;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author PC
 */
public class WorkShiftController {

    private final WorkShiftService workShiftService;

    public WorkShiftController(WorkShiftService workShiftService) {
        this.workShiftService = workShiftService;
    }

    // Lấy danh sách tất cả ca làm việc
    public List<WorkShift> getAllWorkShifts() throws SQLException {
        return workShiftService.getAllWorkShifts();
    }

    public boolean createShiftWithAssignments(
            WorkShift ca,
            List<ShiftAssignment> danhSachPhanCong
    ) throws SQLException {
        return workShiftService.createShiftWithAssignments(
                ca, danhSachPhanCong
        );
    }

// Bàn giao ca làm việc
    public boolean handoverShift(long shiftId, long userId) throws SQLException {
        return workShiftService.handoverShift(shiftId, userId);
    }// Lấy danh sách ca theo tài khoản đăng nhập

    public List<WorkShift> getWorkShiftsForUser(long userId) throws SQLException {
        return workShiftService.getWorkShiftsForUser(userId);
    }
    // Bắt đầu ca

    public boolean openShift(long shiftId, long userId) throws SQLException {
        return workShiftService.openShift(shiftId, userId);
    }

// Kết thúc ca
    public boolean endShift(long shiftId, long userId) throws SQLException {
        return workShiftService.endShift(shiftId, userId);
    }
}
