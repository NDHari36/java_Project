/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package hello.smartpark.view.panel;
import hello.smartpark.dao.BangGiaDAO;
import hello.smartpark.model.BangGia;
import hello.smartpark.model.BangGia.VehicleTypeItem;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;

public class QlyBangGia extends javax.swing.JPanel {
    private final BangGiaDAO dao = new BangGiaDAO();
    private final DecimalFormat moneyFmt = new DecimalFormat("#,##0");
    private DefaultTableModel tableModel;
    private Integer selectedId = null;
    
    public QlyBangGia() {
        initComponents();
        setupTable();
        loadData();
    }
    private void setupTable() {
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Loại phương tiện", "Đơn giá/giờ", "Phí mất vé", "Cách làm tròn", "Tối thiểu", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblBangGia.setModel(tableModel);
        tblBangGia.getColumnModel().getColumn(0).setMinWidth(0);
        tblBangGia.getColumnModel().getColumn(0).setMaxWidth(0);
        tblBangGia.getColumnModel().getColumn(0).setWidth(0);
    }
    private void loadData() {
        tableModel.setRowCount(0);
        selectedId = null;
        try {
            List<BangGia> list = dao.findAll();
            for (BangGia bg : list) {
                tableModel.addRow(new Object[]{
                        bg.getRateId(),
                        bg.getLoaiXe(),
                        moneyFmt.format(bg.getDonGiaGio()) + " đ",
                        moneyFmt.format(bg.getPhiMatVe()) + " đ",
                        bg.getRoundingMethodLabel(),
                        bg.getMinBillableHours() + " giờ",
                        bg.isActive() ? "Đang áp dụng" : "Ngừng áp dụng"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi tải dữ liệu:\n" + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    private BangGia showInputDialog(BangGia existing) {
        List<VehicleTypeItem> vehicleTypes;
        try {
            vehicleTypes = dao.findAllVehicleTypes();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Không tải được danh sách loại xe:\n" + e.getMessage());
            return null;
        }

        DefaultComboBoxModel<VehicleTypeItem> comboModel =
                new DefaultComboBoxModel<>(vehicleTypes.toArray(new VehicleTypeItem[0]));
        JComboBox<VehicleTypeItem> cboLoaiXe = new JComboBox<>(comboModel);

        // Nút '+' đặt ngay cạnh JComboBox để thêm nhanh loại xe mới
        JButton btnAddType = new JButton("+");
        btnAddType.setToolTipText("Thêm loại phương tiện mới");
        btnAddType.setPreferredSize(new java.awt.Dimension(45, 26));
        btnAddType.addActionListener(e -> {
            String newTypeName = JOptionPane.showInputDialog(
                    this,
                    "Nhập tên loại phương tiện mới:",
                    "Thêm loại xe",
                    JOptionPane.PLAIN_MESSAGE
            );
            if (newTypeName == null) return; // Người dùng bấm Cancel

            try {
                VehicleTypeItem created = dao.insertVehicleType(newTypeName);
                comboModel.addElement(created);
                cboLoaiXe.setSelectedItem(created);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        });

        JPanel loaiXeRow = new JPanel(new java.awt.BorderLayout(6, 0));
        loaiXeRow.add(cboLoaiXe, java.awt.BorderLayout.CENTER);
        loaiXeRow.add(btnAddType, java.awt.BorderLayout.EAST);

        JTextField txtDonGia = new JTextField(existing != null ? existing.getDonGiaGio().toPlainString() : "");
        JTextField txtPhiMatVe = new JTextField(existing != null ? existing.getPhiMatVe().toPlainString() : "0");
        JComboBox<String> cboLamTron = new JComboBox<>(new String[]{
                "Làm tròn lên", "Tính theo phút"
        });
        JSpinner spnGioToiThieu = new JSpinner(new SpinnerNumberModel(
                existing != null ? Math.max(1, existing.getMinBillableHours()) : 1, 1, 24, 1
        ));
        JCheckBox chkActive = new JCheckBox("Đang áp dụng", existing == null || existing.isActive());

        if (existing != null) {
            cboLoaiXe.setSelectedItem(new VehicleTypeItem(existing.getVehicleTypeId(), existing.getLoaiXe()));
            cboLamTron.setSelectedIndex("PROPORTIONAL".equals(existing.getRoundingMethod()) ? 1 : 0);
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Loại phương tiện:"));
        panel.add(loaiXeRow);
        panel.add(Box.createVerticalStrut(8));
        panel.add(new JLabel("Đơn giá/giờ (VNĐ):"));
        panel.add(txtDonGia);
        panel.add(Box.createVerticalStrut(8));
        panel.add(new JLabel("Phí mất vé (VNĐ):"));
        panel.add(txtPhiMatVe);
        panel.add(Box.createVerticalStrut(8));
        panel.add(new JLabel("Cách làm tròn thời gian:"));
        panel.add(cboLamTron);
        panel.add(Box.createVerticalStrut(8));
        panel.add(new JLabel("Số giờ tính phí tối thiểu:"));
        panel.add(spnGioToiThieu);
        panel.add(Box.createVerticalStrut(8));
        panel.add(chkActive);

        int result = JOptionPane.showConfirmDialog(
                this, panel,
                existing == null ? "Thêm bảng giá" : "Sửa bảng giá",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return null;

        VehicleTypeItem selectedType = (VehicleTypeItem) cboLoaiXe.getSelectedItem();
        if (selectedType == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hoặc tạo loại phương tiện!");
            return null;
        }

        try {
            BigDecimal donGia = new BigDecimal(txtDonGia.getText().trim().replace(",", ""));
            BigDecimal phiMatVe = new BigDecimal(txtPhiMatVe.getText().trim().replace(",", ""));

            if (donGia.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Đơn giá theo giờ phải lớn hơn 0!");
                return null;
            }
            if (phiMatVe.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "Phí mất vé không được nhỏ hơn 0!");
                return null;
            }

            BangGia bg = new BangGia();
            if (existing != null) bg.setRateId(existing.getRateId());
            bg.setVehicleTypeId(selectedType.getId());
            bg.setLoaiXe(selectedType.getName());
            bg.setDonGiaGio(donGia);
            bg.setPhiMatVe(phiMatVe);
            bg.setRoundingMethod(cboLamTron.getSelectedIndex() == 1 ? "PROPORTIONAL" : "ROUND_UP");
            bg.setMinBillableHours((Integer) spnGioToiThieu.getValue());
            bg.setActive(chkActive.isSelected());
            return bg;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Đơn giá / Phí mất vé phải là số hợp lệ!");
            return null;
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

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        btnthem = new javax.swing.JButton();
        btnsua = new javax.swing.JButton();
        btnxoa = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBangGia = new javax.swing.JTable();

        jLabel1.setText("Quản lý bảng giá");

        btnthem.setText("Thêm");
        btnthem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnthemActionPerformed(evt);
            }
        });

        btnsua.setText("Sửa");
        btnsua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnsuaActionPerformed(evt);
            }
        });

        btnxoa.setText("Xóa");
        btnxoa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnxoaActionPerformed(evt);
            }
        });

        tblBangGia.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Loại phương tiện", "Đơn giá/giờ", "Phí vé", "Quy tắc"
            }
        ));
        tblBangGia.setShowHorizontalLines(true);
        tblBangGia.setShowVerticalLines(true);
        tblBangGia.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblBangGiaMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblBangGia);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(79, 79, 79)
                        .addComponent(btnthem, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnsua, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnxoa, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 531, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(36, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnthem, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnsua, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnxoa, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(50, 50, 50)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(89, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(55, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(146, 146, 146))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnthemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnthemActionPerformed
        // TODO add your handling code here:
        BangGia bg = showInputDialog(null);
        if (bg == null) return;
        try {
            dao.insert(bg);
            loadData();
            JOptionPane.showMessageDialog(this, "Thêm thành công!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btnthemActionPerformed

    private void btnsuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnsuaActionPerformed
        // TODO add your handling code here:
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để sửa!");
            return;
        }
        try {
            BangGia existing = null;
            for (BangGia b : dao.findAll()) {
                if (b.getRateId() == selectedId) {
                    existing = b;
                    break;
                }
            }
            if (existing == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu!");
                return;
            }
            BangGia updated = showInputDialog(existing);
            if (updated == null) return;
            dao.update(updated);
            loadData();
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btnsuaActionPerformed

    private void btnxoaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnxoaActionPerformed
        // TODO add your handling code here:
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để xóa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa dòng này?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            dao.delete(selectedId);
            loadData();
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi xóa:\n" + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }//GEN-LAST:event_btnxoaActionPerformed
    }//GEN-LAST:event_btnxoaActionPerformed

    private void tblBangGiaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblBangGiaMouseClicked
        // TODO add your handling code here:
        int row = tblBangGia.getSelectedRow();
        if (row >= 0) {
            int modelRow = tblBangGia.convertRowIndexToModel(row);
            selectedId = (Integer) tableModel.getValueAt(modelRow, 0);
        }
    }//GEN-LAST:event_tblBangGiaMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnsua;
    private javax.swing.JButton btnthem;
    private javax.swing.JButton btnxoa;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblBangGia;
    // End of variables declaration//GEN-END:variables
}
