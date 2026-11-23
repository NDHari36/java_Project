/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hello.smartpark.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author PC
 */
public class WorkShift {
    private long shiftId;
    private String shiftName;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime openedAt;
    private LocalDateTime endedAt;
    private LocalDateTime closedAt;
    private BigDecimal openingCash;
    private BigDecimal closingCash;
    private String status;
    private Integer openedBy;
    private Integer closedBy;
    private String note;
    private int employeeCount;
    private String employeeNames;


    public WorkShift() {
    }

    public WorkShift(long shiftId, String shiftName, LocalDateTime startAt, LocalDateTime endAt, LocalDateTime openedAt, LocalDateTime closedAt, BigDecimal openingCash, BigDecimal closingCash, String status, Integer openedBy, Integer closedBy, String note, int employeeCount) {
        this.shiftId = shiftId;
        this.shiftName = shiftName;
        this.startAt = startAt;
        this.endAt = endAt;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
        this.openingCash = openingCash;
        this.closingCash = closingCash;
        this.status = status;
        this.openedBy = openedBy;
        this.closedBy = closedBy;
        this.note = note;
        this.employeeCount = employeeCount;
    }

    public long getShiftId() {
        return shiftId;
    }

    public String getShiftName() {
        return shiftName;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public BigDecimal getOpeningCash() {
        return openingCash;
    }

    public BigDecimal getClosingCash() {
        return closingCash;
    }

    public String getStatus() {
        return status;
    }

    public Integer getOpenedBy() {
        return openedBy;
    }

    public Integer getClosedBy() {
        return closedBy;
    }

    public String getNote() {
        return note;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public void setShiftId(long shiftId) {
        this.shiftId = shiftId;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public void setOpenedAt(LocalDateTime openedAt) {
        this.openedAt = openedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public void setOpeningCash(BigDecimal openingCash) {
        this.openingCash = openingCash;
    }

    public void setClosingCash(BigDecimal closingCash) {
        this.closingCash = closingCash;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setOpenedBy(Integer openedBy) {
        this.openedBy = openedBy;
    }

    public void setClosedBy(Integer closedBy) {
        this.closedBy = closedBy;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setEmployeeCount(int employeeCount) {
        this.employeeCount = employeeCount;
    }

    public String getEmployeeNames() {
        return employeeNames;
    }

    public void setEmployeeNames(String employeeNames) {
        this.employeeNames = employeeNames;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }
    
    
}
