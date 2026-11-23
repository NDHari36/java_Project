/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hello.smartpark.model;

/**
 *
 * @author PC
 */
public class ShiftAssignment {
    private long assignmentId;
    private long shiftId;
    private int userId;
    private String username;
    private String fullName;
    private String duty;

    public ShiftAssignment() {
    }

    public ShiftAssignment(long assignmentId, long shiftId, int userId, String username, String fullName, String duty) {
        this.assignmentId = assignmentId;
        this.shiftId = shiftId;
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.duty = duty;
    }
    

    public long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public long getShiftId() {
        return shiftId;
    }

    public void setShiftId(long shiftId) {
        this.shiftId = shiftId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDuty() {
        return duty;
    }

    public void setDuty(String duty) {
        this.duty = duty;
    }
}
