/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package hello.smartpark.view.panel;
import hello.smartpark.dao.ViTriDAO;
import hello.smartpark.model.ViTriDo;
import hello.smartpark.model.ViTriDo.BaiDoXe;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author owner
 */
public class QLyMap extends javax.swing.JPanel {
    private final ViTriDAO dao = new ViTriDAO();
    private final List<BaiDoXe> lotList = new ArrayList<>();
    private JPanel pnlGridSlots;
    private boolean isLoadingLots = false;
    
    private static final Color COLOR_EMPTY_BG = new Color(220, 252, 231);      
    private static final Color COLOR_EMPTY_BORDER = new Color(34, 197, 94);
    private static final Color COLOR_OCCUPIED_BG = new Color(37, 99, 235);     
    private static final Color COLOR_MAINT_BG = new Color(254, 243, 199);      
    private static final Color COLOR_MAINT_BORDER = new Color(245, 158, 11);

    /**
     * Creates new form QLyMap
     */
    public QLyMap() {
        initComponents();
        setupCustomUI();
        loadLots();
    }
    private void setupCustomUI() {
        // Tô màu nền chú thích trạng thái bên trái
        styleLegendLabel(jLabel4, new Color(34, 197, 94), Color.WHITE);   // Trống
        styleLegendLabel(jLabel5, new Color(37, 99, 235), Color.WHITE);   // Đã có xe
        styleLegendLabel(jLabel6, new Color(245, 158, 11), Color.WHITE);  // Bảo trì

        // Tạo khung lưới 5 cột đặt vào khoảng trống bên phải cột Trạng thái
        pnlGridSlots = new JPanel(new GridLayout(0, 5, 10, 10));
        pnlGridSlots.setBackground(Color.WHITE);
        pnlGridSlots.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollSlots = new JScrollPane(pnlGridSlots);
        scrollSlots.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollSlots.setBounds(190, 120, 435, 250);

        jPanel1.add(scrollSlots);
    }
    private void styleLegendLabel(JLabel lbl, Color bg, Color fg) {
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setForeground(fg);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
    }
    private void loadLots() {
        isLoadingLots = true;
        cboBaiXe.removeAllItems();
        lotList.clear();
        try {
            lotList.addAll(dao.findAllLots());
            for (BaiDoXe lot : lotList) {
                cboBaiXe.addItem(lot.getLotName());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách bãi xe:\n" + e.getMessage());
        } finally {
            isLoadingLots = false;
        }
        loadSlotsForSelectedLot();
    }
    private void loadSlotsForSelectedLot() {
        pnlGridSlots.removeAll();
        int idx = cboBaiXe.getSelectedIndex();
        if (idx < 0 || idx >= lotList.size()) {
            pnlGridSlots.revalidate();
            pnlGridSlots.repaint();
            return;
        }

        BaiDoXe selectedLot = lotList.get(idx);
        try {
            List<ViTriDo> slots = dao.findSlotsByLot(selectedLot.getLotId());
            for (ViTriDo slot : slots) {
                pnlGridSlots.add(createSlotButton(slot));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải sơ đồ vị trí:\n" + e.getMessage());
        }

        pnlGridSlots.revalidate();
        pnlGridSlots.repaint();
    }
    private JButton createSlotButton(ViTriDo slot) {
        JButton btn = new JButton(slot.getSlotCode());
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(70, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (slot.isOccupied()) {
            btn.setBackground(COLOR_OCCUPIED_BG);
            btn.setForeground(Color.WHITE);
            btn.setToolTipText("Đang có xe: " + slot.getLicensePlate());
        } else if (slot.isMaintenance()) {
            btn.setBackground(COLOR_MAINT_BG);
            btn.setForeground(new Color(146, 64, 14));
            btn.setBorder(BorderFactory.createLineBorder(COLOR_MAINT_BORDER, 2));
            btn.setToolTipText("Đang bảo trì (Bấm để mở lại)");
        } else {
            btn.setBackground(COLOR_EMPTY_BG);
            btn.setForeground(new Color(22, 101, 52));
            btn.setBorder(BorderFactory.createLineBorder(COLOR_EMPTY_BORDER, 1));
            btn.setToolTipText("Vị trí trống (Bấm để chuyển bảo trì)");
        }

        btn.addActionListener(e -> handleSlotClick(slot));
        return btn;
    }
    private void handleSlotClick(ViTriDo slot) {
        if (slot.isOccupied()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            String timeStr = slot.getEntryTime() != null ? sdf.format(slot.getEntryTime()) : "N/A";
            JOptionPane.showMessageDialog(this,
                    "Vị trí: " + slot.getSlotCode() + "\n" +
                    "Trạng thái: Đang có xe\n" +
                    "Biển số xe: " + slot.getLicensePlate() + "\n" +
                    "Mã vé: " + slot.getTicketCode() + "\n" +
                    "Giờ vào: " + timeStr,
                    "Thông tin vị trí đỗ", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        boolean currentlyMaint = slot.isMaintenance();
        String msg = currentlyMaint
                ? "Mở hoạt động lại cho vị trí " + slot.getSlotCode() + "?"
                : "Chuyển vị trí " + slot.getSlotCode() + " sang trạng thái BẢO TRÌ?";
        int confirm = JOptionPane.showConfirmDialog(this, msg, "Cập nhật trạng thái", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            dao.updateSlotStatus(slot.getSlotId(), currentlyMaint ? "ACTIVE" : "MAINTENANCE");
            loadSlotsForSelectedLot();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật trạng thái:\n" + e.getMessage());
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
        jLabel2 = new javax.swing.JLabel();
        cboBaiXe = new javax.swing.JComboBox<>();
        btnlammoi = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        jLabel1.setText("Quản lý vị trí đỗ");

        jLabel2.setText("Bãi xe: ");

        cboBaiXe.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboBaiXe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboBaiXeActionPerformed(evt);
            }
        });

        btnlammoi.setText("Làm mới");
        btnlammoi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnlammoiActionPerformed(evt);
            }
        });

        jLabel3.setText("Trạng thái:");

        jLabel4.setText("Trống");

        jLabel5.setText("Đã có xe");

        jLabel6.setText("Bảo trì");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cboBaiXe, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 155, Short.MAX_VALUE)
                        .addComponent(btnlammoi, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(83, 83, 83))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGap(25, 25, 25)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGap(62, 62, 62)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnlammoi, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
                    .addComponent(cboBaiXe))
                .addGap(30, 30, 30)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(102, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(34, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnlammoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnlammoiActionPerformed
        // TODO add your handling code here:
        loadSlotsForSelectedLot();
    }//GEN-LAST:event_btnlammoiActionPerformed

    private void cboBaiXeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboBaiXeActionPerformed
        // TODO add your handling code here:
        if (!isLoadingLots) {
            loadSlotsForSelectedLot();
        }
    }//GEN-LAST:event_cboBaiXeActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnlammoi;
    private javax.swing.JComboBox<String> cboBaiXe;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
