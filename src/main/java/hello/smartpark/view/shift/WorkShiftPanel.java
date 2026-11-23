/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package hello.smartpark.view.shift;

import hello.smartpark.controller.WorkShiftController;
import hello.smartpark.dao.WorkShiftDAO;
import hello.smartpark.model.User;
import hello.smartpark.model.WorkShift;
import hello.smartpark.service.WorkShiftService;
import hello.smartpark.session.UserSession;

import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PC
 */
public class WorkShiftPanel extends javax.swing.JPanel {

    private WorkShiftController workShiftController;
    private List<WorkShift> danhSachCa = new ArrayList<>();

    /**
     * Creates new form WorkShiftPanel
     */
    public WorkShiftPanel() {
        initComponents();
        // Khoi tao DAO
        WorkShiftDAO dao = new WorkShiftDAO();
        // Khoi tao Service
        WorkShiftService service = new WorkShiftService(dao);
        // Khoi tao Controller
        workShiftController = new WorkShiftController(service);
        loadWorkShifts();
    }

    private void loadWorkShifts() {

        User user = UserSession.getCurrentUser();
        if (user == null) {
            System.out.println("Chưa có tài khoản đăng nhập!");
            return;
        }
        try {
            // Lấy danh sách ca theo tài khoản đăng nhập
            danhSachCa = workShiftController.getWorkShiftsForUser(user.getUserId());
            // Lấy Model của JTable
            DefaultTableModel model = (DefaultTableModel) jTableList.getModel();
            // Xóa dữ liệu cũ
            model.setRowCount(0);
            // Định dạng thời gian
            DateTimeFormatter dinhDang = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            // Đổ dữ liệu vào JTable
            for (WorkShift ca : danhSachCa) {
                String startAt = ca.getStartAt() == null
                        ? ""
                        : dinhDang.format(ca.getStartAt());
                String endAt = ca.getEndAt() == null
                        ? ""
                        : dinhDang.format(ca.getEndAt());
                model.addRow(new Object[]{
                    ca.getShiftId(),
                    ca.getShiftName(),
                    startAt,
                    endAt,
                    ca.getStatus(),
                    ca.getEmployeeNames(),
                    ca.getEmployeeCount()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi tải danh sách ca: " + e.getMessage()
            );
        }
    }

    private void hienThiChiTietCa() {
        int dongChon = jTableList.getSelectedRow();
        if (dongChon == -1) {
            jLabelMaCa.setText("---");
            jLabelName.setText("---");
            jLabelTime.setText("---");
            jLabelTgianLam.setText("---");
            jLabelStatus.setText("---");
            jLabelNhanVien.setText("---");
            return;
        }
        // Chuyển chỉ số dòng hiển thị sang chỉ số dòng trong Model
        int dongModel = jTableList.convertRowIndexToModel(dongChon);
        if (dongModel < 0 || dongModel >= danhSachCa.size()) {
            return;
        }
        WorkShift ca = danhSachCa.get(dongModel);
        jButtonStart.setEnabled("PLANNED".equals(ca.getStatus()));
        jButtonEnd.setEnabled("OPEN".equals(ca.getStatus()));
        jButtonBanGiao.setEnabled("HANDOVER_PENDING".equals(ca.getStatus()));
        DateTimeFormatter dinhDang = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        // Mã ca
        jLabelMaCa.setText(String.valueOf(ca.getShiftId()));
        // Tên ca
        jLabelName.setText(ca.getShiftName());
        // Lịch làm việc theo kế hoạch
        jLabelTime.setText(dinhDangThoiGian(ca.getStartAt(), dinhDang)
                + " - "
                + dinhDangThoiGian(ca.getEndAt(), dinhDang)
        );
        // Thời gian làm việc thực tế
        if (ca.getOpenedAt() == null) {
            jLabelTgianLam.setText("Chưa bắt đầu");
        } else if (ca.getEndedAt() == null) {
            jLabelTgianLam.setText(
                    dinhDang.format(ca.getOpenedAt()) + " - Đang làm"
            );
        } else {
            jLabelTgianLam.setText(
                    dinhDang.format(ca.getOpenedAt())
                    + " - "
                    + dinhDang.format(ca.getEndedAt())
            );
        }
        // Trạng thái
        jLabelStatus.setText(ca.getStatus());
        // Nhân viên
        jLabelNhanVien.setText(ca.getEmployeeNames());
    }
    private String dinhDangThoiGian(
            LocalDateTime thoiGian,
            DateTimeFormatter dinhDang
    ) 
    {
        if (thoiGian == null) {
            return "---";
        }
        return dinhDang.format(thoiGian);
    }
    private WorkShift getCaDangChon() {
        int dongChon = jTableList.getSelectedRow();
        if (dongChon == -1) {
            return null;
        }
        int dongModel = jTableList.convertRowIndexToModel(dongChon);
        if (dongModel < 0 || dongModel >= danhSachCa.size()) {
            return null;
        }
        return danhSachCa.get(dongModel);
    }
    private void banGiao() {
        User user = UserSession.getCurrentUser();
        WorkShift ca = getCaDangChon();
        if (ca == null) {
            JOptionPane.showMessageDialog(
                    this, "Vui lòng chọn ca cần bàn giao."
            );
            return;
        }
        if (user == null) {
            JOptionPane.showMessageDialog(
                    this, "Không xác định được tài khoản đăng nhập."
            );
            return;
        }
        if (!"HANDOVER_PENDING".equals(ca.getStatus())) {
            JOptionPane.showMessageDialog(
                    this, "Ca này không ở trạng thái chờ bàn giao."
            );
            return;
        }
        int xacNhan = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn bàn giao và đóng ca này không?",
                "Xác nhận bàn giao",
                JOptionPane.YES_NO_OPTION
        );
        if (xacNhan != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            boolean ketQua = workShiftController.handoverShift(
                    ca.getShiftId(),
                    user.getUserId()
            );
            if (ketQua) {
                JOptionPane.showMessageDialog(
                        this, "Bàn giao ca thành công!"
                );
                taiLaiVaChonCa(ca.getShiftId());
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Bạn không có quyền bàn giao ca này hoặc ca không còn ở trạng thái chờ bàn giao."
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this, "Lỗi bàn giao ca: " + e.getMessage()
            );
        }
    }

    private void taiLaiVaChonCa(long shiftId) {
        loadWorkShifts();
        for (int i = 0; i < danhSachCa.size(); i++) {
            if (danhSachCa.get(i).getShiftId() == shiftId) {
                int dongHienThi = jTableList.convertRowIndexToView(i);
                if (dongHienThi != -1) {
                    jTableList.setRowSelectionInterval(
                            dongHienThi, dongHienThi
                    );
                }
                break;
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelTitle = new javax.swing.JLabel();
        jButtonCreate = new javax.swing.JButton();
        jButtonStart = new javax.swing.JButton();
        jButtonEnd = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableList = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabelMaCa = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabelName = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabelTime = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabelTgianLam = new javax.swing.JLabel();
        jButtonBanGiao = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jLabelStatus = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabelNhanVien = new javax.swing.JLabel();
        jButtonLoad = new javax.swing.JButton();
        jButtonBG = new javax.swing.JButton();

        jLabelTitle.setText("jLabel1");

        jButtonCreate.setText("Thêm Ca");
        jButtonCreate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCreateActionPerformed(evt);
            }
        });

        jButtonStart.setText("Bắt đầu ca");
        jButtonStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStartActionPerformed(evt);
            }
        });

        jButtonEnd.setText("Kết thúc ca");
        jButtonEnd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEndActionPerformed(evt);
            }
        });

        jTableList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Mã Ca", "Tên Ca", "Lịch", "Thời Gian Làm", "Trạng Thái", "Nhân Viên"
            }
        ));
        jTableList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableListMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTableList);

        jLabel1.setText("Thông Tin Chi Tiết");

        jLabel2.setText("Mã Ca");

        jLabelMaCa.setText("jLabelMaCa");

        jLabel4.setText("Tên Ca");

        jLabelName.setText("jLabelName");

        jLabel6.setText("Lịch");

        jLabelTime.setText("jLabelTime");

        jLabel8.setText("Thời Gian Làm");

        jLabelTgianLam.setText("jLabelTgianLam");

        jButtonBanGiao.setText("Bàn Giao");
        jButtonBanGiao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBanGiaoActionPerformed(evt);
            }
        });

        jLabel10.setText("Trạng Thái");

        jLabelStatus.setText("jLabelStatus");

        jLabel12.setText("Nhân Viên");

        jLabelNhanVien.setText("jLabelNhanVien");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(54, 54, 54)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel12))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(60, 60, 60)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabelStatus)
                                            .addComponent(jLabelTgianLam)
                                            .addComponent(jLabelTime)
                                            .addComponent(jLabelName)
                                            .addComponent(jLabelMaCa)))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(52, 52, 52)
                                        .addComponent(jLabelNhanVien))))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(102, 102, 102)
                        .addComponent(jButtonBanGiao)))
                .addContainerGap(160, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1)
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabelMaCa))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelName)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabelTime))
                .addGap(34, 34, 34)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabelTgianLam))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabelStatus))
                .addGap(26, 26, 26)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jLabelNhanVien))
                .addGap(79, 79, 79)
                .addComponent(jButtonBanGiao)
                .addContainerGap(182, Short.MAX_VALUE))
        );

        jButtonLoad.setText("Load");
        jButtonLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadActionPerformed(evt);
            }
        });

        jButtonBG.setText("Bàn Giao");
        jButtonBG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBGActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(84, 84, 84)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelTitle)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jButtonBG)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jButtonCreate)
                                        .addGap(71, 71, 71)
                                        .addComponent(jButtonStart)
                                        .addGap(80, 80, 80)
                                        .addComponent(jButtonEnd)))
                                .addGap(53, 53, 53)
                                .addComponent(jButtonLoad))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(75, 75, 75)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 532, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(100, 100, 100)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(jLabelTitle)
                .addGap(34, 34, 34)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCreate)
                    .addComponent(jButtonStart)
                    .addComponent(jButtonEnd)
                    .addComponent(jButtonLoad))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(jButtonBG)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(64, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCreateActionPerformed
        // TODO add your handling code here:
        AddWorkShift add = new AddWorkShift();
        add.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                loadWorkShifts();
            }
        });

        add.setLocationRelativeTo(this);
        add.setVisible(true);
    }//GEN-LAST:event_jButtonCreateActionPerformed

    private void jButtonLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadActionPerformed
        // TODO add your handling code here:
        loadWorkShifts();
    }//GEN-LAST:event_jButtonLoadActionPerformed

    private void jTableListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableListMouseClicked
        // TODO add your handling code here:
        hienThiChiTietCa();
    }//GEN-LAST:event_jTableListMouseClicked

    private void jButtonBanGiaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBanGiaoActionPerformed
        // TODO add your handling code here:
        // Kiểm tra dòng đang chọn
        banGiao();
    }//GEN-LAST:event_jButtonBanGiaoActionPerformed

    private void jButtonBGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBGActionPerformed
        // TODO add your handling code here:
        banGiao();
    }//GEN-LAST:event_jButtonBGActionPerformed

    private void jButtonStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartActionPerformed
        // TODO add your handling code here:
        User user = UserSession.getCurrentUser();
        if (user == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không tìm thấy tài khoản đăng nhập."
            );
            return;
        }
        WorkShift ca = getCaDangChon();
        if (ca == null) {
            JOptionPane.showMessageDialog(
                    this, "Vui lòng chọn ca cần bắt đầu."
            );
            return;
        }
        if (!"PLANNED".equals(ca.getStatus())) {
            JOptionPane.showMessageDialog(
                    this, "Chỉ có thể bắt đầu ca đang ở trạng thái PLANNED."
            );
            return;
        }
        int xacNhan = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn bắt đầu ca " + ca.getShiftName() + " không?",
                "Xác nhận bắt đầu ca",
                JOptionPane.YES_NO_OPTION
        );
        if (xacNhan != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            boolean ketQua = workShiftController.openShift(
                    ca.getShiftId(),
                    user.getUserId()
            );
            if (ketQua) {
                JOptionPane.showMessageDialog(
                        this, "Bắt đầu ca thành công!"
                );
                taiLaiVaChonCa(ca.getShiftId());
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Không thể bắt đầu ca. Ca đã thay đổi trạng thái hoặc bạn không có quyền."
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this, "Lỗi bắt đầu ca: " + e.getMessage()
            );
        }

    }//GEN-LAST:event_jButtonStartActionPerformed

    private void jButtonEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEndActionPerformed
        // TODO add your handling code here:
        User user = UserSession.getCurrentUser();
        WorkShift ca = getCaDangChon();
        if (user == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không tìm thấy tài khoản đăng nhập."
            );
            return;
        }
        if (ca == null) {
            JOptionPane.showMessageDialog(
                    this, "Vui lòng chọn ca cần kết thúc."
            );
            return;
        }
        if (!"OPEN".equals(ca.getStatus())) {
            JOptionPane.showMessageDialog(
                    this, "Chỉ có thể kết thúc ca đang ở trạng thái OPEN."
            );
            return;
        }

        int xacNhan = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn kết thúc ca " + ca.getShiftName() + " không?",
                "Xác nhận kết thúc ca",
                JOptionPane.YES_NO_OPTION
        );

        if (xacNhan != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            boolean ketQua = workShiftController.endShift(
                    ca.getShiftId(),
                    user.getUserId()
            );
            if (ketQua) {
                JOptionPane.showMessageDialog(
                        this, "Kết thúc ca thành công! Ca đang chờ bàn giao."
                );
                taiLaiVaChonCa(ca.getShiftId());
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Không thể kết thúc ca. Ca đã thay đổi trạng thái hoặc bạn không có quyền."
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this, "Lỗi kết thúc ca: " + e.getMessage()
            );
        }

    }//GEN-LAST:event_jButtonEndActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonBG;
    private javax.swing.JButton jButtonBanGiao;
    private javax.swing.JButton jButtonCreate;
    private javax.swing.JButton jButtonEnd;
    private javax.swing.JButton jButtonLoad;
    private javax.swing.JButton jButtonStart;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabelMaCa;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JLabel jLabelNhanVien;
    private javax.swing.JLabel jLabelStatus;
    private javax.swing.JLabel jLabelTgianLam;
    private javax.swing.JLabel jLabelTime;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTableList;
    // End of variables declaration//GEN-END:variables
}
