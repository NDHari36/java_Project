/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package hello.smartpark.view.parking;

import hello.smartpark.controller.ParkingEntryController;
import hello.smartpark.controller.WorkShiftController;
import hello.smartpark.dao.JDBCConnection;
import hello.smartpark.dao.ParkingEntryDAO;
import hello.smartpark.dao.WorkShiftDAO;
import hello.smartpark.model.ParkingSlot;
import hello.smartpark.model.ParkingTicket;
import hello.smartpark.model.User;
import hello.smartpark.model.Vehicle;
import hello.smartpark.model.VehicleType;
import hello.smartpark.service.ParkingEntryService;
import hello.smartpark.service.WorkShiftService;
import hello.smartpark.session.UserSession;
import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JOptionPane;

/**
 *
 * @author PC
 */
public class ParkingEntryPanel extends javax.swing.JPanel {

    private final ParkingEntryController controller = new ParkingEntryController(new ParkingEntryService(new ParkingEntryDAO()));
    private final WorkShiftController workShiftController = new WorkShiftController(new WorkShiftService(new WorkShiftDAO()));

    private List<VehicleType> danhSachLoaiXe = new ArrayList<>();
    private List<ParkingSlot> danhSachViTri = new ArrayList<>();
    private final List<ParkingSlot> viTriTheoBai = new ArrayList<>();
    private final Map<String, Integer> danhSachBaiXe = new LinkedHashMap<>();
    private final JButton[] nutViTri = new JButton[12];
    private final ParkingSlot[] slotTrenNut = new ParkingSlot[12];

    private ParkingSlot viTriDangChon;
    private Vehicle vehicleFound;
    private Long caDangMoId;
    private boolean dangNapLoaiXe = false;
    private boolean dangNapViTri = false;
    private boolean dangNapBaiXe = false;
    private boolean daTimKiem = false;
    private String trangThaiLoc = "ALL";
    private String bienSoDaTim;

    /**
     * Creates new form NewJPanel
     */
    public ParkingEntryPanel() {
        initComponents();
        jTextFieldBienSo.setText("");

        nutViTri[0] = jButton1;
        nutViTri[1] = jButton2;
        nutViTri[2] = jButton3;
        nutViTri[3] = jButton4;
        nutViTri[4] = jButton5;
        nutViTri[5] = jButton6;
        nutViTri[6] = jButton7;
        nutViTri[7] = jButton8;
        nutViTri[8] = jButton9;
        nutViTri[9] = jButton10;
        nutViTri[10] = jButton11;
        nutViTri[11] = jButton12;

        for (JButton button : nutViTri) {
            button.setVisible(false);
        }

        // Gắn sự kiện cho các nút từ nút 3 đến nút 12
        for (int i = 2; i < nutViTri.length; i++) {
            final int index = i;

            nutViTri[i].addActionListener(e -> chonViTri(index));
        }
        jButtonTrong.addActionListener(e -> locTheoTrangThai("AVAILABLE"));
        jButtonCoXe.addActionListener(e -> locTheoTrangThai("OCCUPIED"));
        jButtonBaoTri.addActionListener(e -> locTheoTrangThai("MAINTENANCE"));
        jComboBoxBaiDo.addActionListener(e -> jComboBoxBaiDoActionPerformed());
        loadVehicleTypes();
        kiemTraCaDangMo();
        jPanel1.setVisible(true);
    }

    private void locTheoTrangThai(String trangThai) {
        trangThaiLoc = trangThai.equals(trangThaiLoc) ? "ALL" : trangThai;
        viTriDangChon = null;
        jComboBoxViTri.setSelectedIndex(-1);
        jLabelViTri.setText("Chưa chọn vị trí");
        capNhatNutXacNhan();
        hienThiViTriTheoBai();
    }

    private void loadVehicleTypes() {

        try {
            dangNapLoaiXe = true;

            danhSachLoaiXe.clear();
            jComboBoxType.removeAllItems();
            jComboBoxBaiDo.removeAllItems();

            danhSachLoaiXe = controller.getActiveVehicleTypes();

            for (VehicleType type : danhSachLoaiXe) {
                jComboBoxType.addItem(type.getTypeName());
                jComboBoxBaiDo.addItem(type.getTypeName());
            }

            // Tự chọn loại xe đầu tiên
            if (!danhSachLoaiXe.isEmpty()) {
                jComboBoxType.setSelectedIndex(0);
                jComboBoxBaiDo.setSelectedIndex(0);
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi tải loại xe: " + e.getMessage()
            );

        } finally {
            dangNapLoaiXe = false;
        }

        // Tải vị trí sau khi đã chọn loại xe
        if (!danhSachLoaiXe.isEmpty()) {
            loadParkingSlots();
        }
    }

    private VehicleType getLoaiXeDangChon() {

        int index = jComboBoxType.getSelectedIndex();

        if (index == -1 || index >= danhSachLoaiXe.size()) {
            return null;
        }

        return danhSachLoaiXe.get(index);
    }

    private void kiemTraCaDangMo() {

        User user = UserSession.getCurrentUser();
        caDangMoId = null;

        if (user == null) {
            jLabelCaLamViec.setText("Chưa đăng nhập");
            capNhatNutXacNhan();
            return;
        }

        // Admin không bắt buộc phải có ca
        if (user.getRoleId() == 1) {
            jLabelCaLamViec.setText("Quản trị viên - Không yêu cầu ca");
            capNhatNutXacNhan();
            return;
        }

        try {
            caDangMoId = workShiftController.getOpenShiftAssignedToUser(
                    user.getUserId()
            );

            if (caDangMoId == null) {
                jLabelCaLamViec.setText("Bạn chưa có ca làm việc");
            } else {
                jLabelCaLamViec.setText("Mã ca đang làm: " + caDangMoId);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi kiểm tra ca làm việc: " + e.getMessage()
            );
        }

        capNhatNutXacNhan();
    }

    private void capNhatNutXacNhan() {
        User user = UserSession.getCurrentUser();

        boolean laAdmin = user != null && user.getRoleId() == 1;
        boolean coCaLam = caDangMoId != null;

        boolean duocPhepGhiNhan = laAdmin || coCaLam;
        jTextFieldBienSo.setEnabled(duocPhepGhiNhan);
        jComboBoxType.setEnabled(duocPhepGhiNhan);
        jComboBoxViTri.setEnabled(duocPhepGhiNhan);

        // Kiểm tra thông tin ghi nhận xe vào
        boolean coBienSo = !jTextFieldBienSo.getText().trim().isEmpty();
        boolean coLoaiXe = jComboBoxType.getSelectedIndex() != -1;
        boolean coViTri = viTriDangChon != null;

        boolean viTriTrong = coViTri
                && "AVAILABLE".equals(viTriDangChon.getAvailabilityStatus());

        // Nút chỉ được bật khi đủ điều kiện
        jButtonXacNhan.setEnabled(
                duocPhepGhiNhan && coBienSo && coLoaiXe && coViTri && viTriTrong
        );
    }

    private void timXe(String bienSo) throws SQLException {
        bienSo = bienSo.trim().toUpperCase();

        if (bienSo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập biển số xe cần tìm.");
            return;
        }

        vehicleFound = controller.findVehicleByLicensePlate(bienSo);
        jTextFieldBienSo.setText(bienSo);

        // Reset trạng thái tìm kiếm và vị trí đang chọn
        daTimKiem = false;
        bienSoDaTim = null;
        viTriDangChon = null;

        jComboBoxType.setEnabled(true);
        jComboBoxType.setSelectedIndex(-1);

        jLabelBienSo.setText("");
        jLabelType.setText("");
        jLabelViTri.setText("");
        jLabelTime.setText("");

        // Nếu tìm thấy xe đã đăng ký
        if (vehicleFound != null) {

            jLabelBienSo.setText(vehicleFound.getLicensePlate());
            jLabelType.setText(vehicleFound.getVehicleTypeName());

            // Chọn loại xe tương ứng trong CmboBox
            // Chọn loại xe tương ứng trong ComboBox
            for (int i = 0; i < jComboBoxType.getItemCount(); i++) {
                String tenLoaiXe = jComboBoxType.getItemAt(i);

                if (tenLoaiXe.equalsIgnoreCase(vehicleFound.getVehicleTypeName())) {
                    jComboBoxType.setSelectedIndex(i);
                    break;
                }
            }

        } else {
            // Xe chưa đăng ký
            jLabelBienSo.setText(bienSo);
            jLabelType.setText("Xe chưa đăng ký");
        }

        // Lưu biển số đã tìm kiếm
        bienSoDaTim = bienSo;
        daTimKiem = true;

        // Tải danh sách vị trí theo loại xe
        if (jComboBoxType.getSelectedIndex() != -1) {
            loadParkingSlots();
        }

        // Cập nhật trạng thái nút xác nhận
        capNhatNutXacNhan();
    }

    private void loadParkingSlots() {

        VehicleType type = getLoaiXeDangChon();

        if (type == null) {
            danhSachViTri.clear();
            anTatCaNutViTri();
            return;
        }

        try {
            danhSachViTri = controller.getParkingSlots(
                    type.getVehicleTypeId()
            );

            System.out.println("Loại xe: " + type.getTypeName());
            System.out.println("ID loại xe: " + type.getVehicleTypeId());
            System.out.println("Số vị trí lấy được: " + danhSachViTri.size());

            viTriDangChon = null;
            jLabelViTri.setText("Chưa chọn vị trí");

            dangNapBaiXe = true;

            jComboBoxViTriDo.removeAllItems();
            danhSachBaiXe.clear();

            for (ParkingSlot slot : danhSachViTri) {

                String tenBai = slot.getLotName()
                        + " (#" + slot.getParkingLotId() + ")";

                if (!danhSachBaiXe.containsKey(tenBai)) {

                    danhSachBaiXe.put(
                            tenBai,
                            slot.getParkingLotId()
                    );

                    jComboBoxViTriDo.addItem(tenBai);
                }
            }

            jComboBoxViTriDo.setSelectedIndex(
                    danhSachBaiXe.isEmpty() ? -1 : 0
            );

            dangNapBaiXe = false;

            hienThiViTriTheoBai();

        } catch (SQLException e) {

            dangNapBaiXe = false;

            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi tải danh sách vị trí: " + e.getMessage()
            );
        }
    }

    private void hienThiViTriTheoBai() {
        String tenBaiXe = (String) jComboBoxViTriDo.getSelectedItem();

        viTriTheoBai.clear();
        dangNapViTri = true;
        jComboBoxViTri.removeAllItems();

        if (tenBaiXe == null) {
            dangNapViTri = false;
            anTatCaNutViTri();
            capNhatNutXacNhan();
            return;
        }

        Integer baiXeId = danhSachBaiXe.get(tenBaiXe);

        if (baiXeId == null) {
            dangNapViTri = false;
            anTatCaNutViTri();
            capNhatNutXacNhan();
            return;
        }

        for (ParkingSlot slot : danhSachViTri) {
            if (slot.getParkingLotId() == baiXeId) {
                viTriTheoBai.add(slot);
            }
        }

        for (ParkingSlot slot : viTriTheoBai) {
            if ("AVAILABLE".equals(slot.getAvailabilityStatus())) {
                jComboBoxViTri.addItem(slot.getSlotCode());
            }
        }
        jComboBoxViTri.setSelectedIndex(-1);
        dangNapViTri = false;

        for (int i = 0; i < nutViTri.length; i++) {
            nutViTri[i].setVisible(false);
            nutViTri[i].setEnabled(false);
            slotTrenNut[i] = null;
        }

        int indexNut = 0;
        for (ParkingSlot slot : viTriTheoBai) {
            String trangThai = slot.getAvailabilityStatus();

            if (!"ALL".equals(trangThaiLoc)
                    && !trangThaiLoc.equals(trangThai)) {
                continue;
            }
            if (indexNut >= nutViTri.length) {
                break;
            }

            JButton button = nutViTri[indexNut];
            slotTrenNut[indexNut] = slot;
            button.setText(slot.getSlotCode());
            button.setVisible(true);
            button.setEnabled(true);

            switch (trangThai) {
                case "AVAILABLE":
                    button.setBackground(new Color(46, 204, 113));
                    break;
                case "OCCUPIED":
                    button.setBackground(new Color(52, 152, 219));
                    break;
                case "MAINTENANCE":
                    button.setBackground(new Color(230, 126, 34));
                    break;
                default:
                    button.setBackground(Color.LIGHT_GRAY);
                    break;
            }

            button.setOpaque(true);
            button.setBorderPainted(false);
            indexNut++;
        }

        capNhatMauNutViTri();
        capNhatNutXacNhan();
        jPanel3.revalidate();
        jPanel3.repaint();
    }
    private void anTatCaNutViTri() {

        for (int i = 0; i < 12; i++) {

            nutViTri[i].setVisible(false);
            nutViTri[i].setEnabled(false);

            slotTrenNut[i] = null;
        }

        jPanel3.revalidate();
        jPanel3.repaint();
    }

    private void chonViTri(int index) {
        if (index < 0 || index >= slotTrenNut.length) {
            return;
        }

        ParkingSlot slot = slotTrenNut[index];
        if (slot == null) {
            return;
        }

        switch (slot.getAvailabilityStatus()) {
            case "AVAILABLE":
                xoaThongTinXeQuanSat();
                viTriDangChon = slot;
                dangNapViTri = true;
                jComboBoxViTri.setSelectedItem(slot.getSlotCode());
                dangNapViTri = false;
                jLabelViTri.setText(slot.getSlotCode());
                capNhatMauNutViTri();
                capNhatNutXacNhan();
                break;
            case "OCCUPIED":
                boChonViTriNhapXe();
                hienThiXeDangDo(slot);
                break;
            case "MAINTENANCE":
                boChonViTriNhapXe();
                xoaThongTinXeQuanSat();
                jLabelViTri.setText(slot.getSlotCode());
                capNhatNutXacNhan();
                JOptionPane.showMessageDialog(
                        this,
                        "Vị trí " + slot.getSlotCode() + " đang bảo trì."
                );
                break;
            default:
                break;
        }
    }

    private void boChonViTriNhapXe() {
        viTriDangChon = null;
        dangNapViTri = true;
        jComboBoxViTri.setSelectedIndex(-1);
        dangNapViTri = false;
        capNhatMauNutViTri();
        capNhatNutXacNhan();
    }

    private void xoaThongTinXeQuanSat() {
        jLabelBienSo.setText("");
        jLabelType.setText("");
        jLabelViTri.setText("");
        jLabelTime.setText("");
    }

    private void hienThiXeDangDo(ParkingSlot slot) {
        String sql = """
            SELECT v.license_plate, vt.type_name, t.entry_time
            FROM parking_tickets t
            JOIN vehicles v ON v.vehicle_id = t.vehicle_id
            JOIN vehicle_types vt ON vt.vehicle_type_id = t.vehicle_type_id
            WHERE t.slot_id = ?
              AND t.status = 'PARKING'
            """;

        try (Connection connection = JDBCConnection.getConnection()) {
            if (connection == null) {
                throw new SQLException("Không thể kết nối database.");
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, slot.getSlotId());

                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        xoaThongTinXeQuanSat();
                        jLabelViTri.setText(slot.getSlotCode());
                        JOptionPane.showMessageDialog(
                                this,
                                "Không tìm thấy thông tin xe đang đỗ tại vị trí "
                                + slot.getSlotCode() + "."
                        );
                        return;
                    }

                    jLabelBienSo.setText(result.getString("license_plate"));
                    jLabelType.setText(result.getString("type_name"));
                    jLabelViTri.setText(slot.getSlotCode());

                    Timestamp entryTime = result.getTimestamp("entry_time");
                    jLabelTime.setText(entryTime == null
                            ? "---"
                            : entryTime.toLocalDateTime().format(
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                            ));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi tải thông tin xe đang đỗ: " + e.getMessage()
            );
        }
    }
    private void capNhatMauNutViTri() {

        for (int i = 0; i < nutViTri.length; i++) {

            ParkingSlot slot = slotTrenNut[i];

            if (slot == null) {
                continue;
            }

            switch (slot.getAvailabilityStatus()) {

                case "AVAILABLE":
                    nutViTri[i].setBackground(
                            new Color(190, 235, 205)
                    );
                    nutViTri[i].setForeground(Color.BLACK);
                    break;

                case "OCCUPIED":
                    nutViTri[i].setBackground(
                            new Color(90, 160, 230)
                    );
                    nutViTri[i].setForeground(Color.BLACK);
                    break;

                case "MAINTENANCE":
                    nutViTri[i].setBackground(
                            new Color(255, 190, 100)
                    );
                    nutViTri[i].setForeground(Color.BLACK);
                    break;

                default:
                    nutViTri[i].setBackground(Color.LIGHT_GRAY);
                    nutViTri[i].setForeground(Color.BLACK);
                    break;
            }

            if (viTriDangChon != null
                    && viTriDangChon.getSlotId() == slot.getSlotId()) {

                nutViTri[i].setBackground(
                        new Color(45, 110, 210)
                );

                nutViTri[i].setForeground(Color.WHITE);
            }
        }

        jPanel3.repaint();
    }

    private void xeVaoBai() {

        // 1. Lấy tài khoản đăng nhập
        User user = UserSession.getCurrentUser();

        if (user == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không xác định được tài khoản đăng nhập."
            );
            return;
        }

        boolean laAdmin = user.getRoleId() == 1;
        if (!laAdmin) {
            try {
                caDangMoId = workShiftController.getOpenShiftAssignedToUser(
                        user.getUserId()
                );
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Lỗi kiểm tra ca làm việc: " + e.getMessage()
                );
                return;
            }

            if (caDangMoId == null) {
                capNhatNutXacNhan();
                JOptionPane.showMessageDialog(
                        this,
                        "Bạn chưa được phân công vào ca đang hoạt động. "
                        + "Không thể ghi nhận xe vào bãi."
                );
                return;
            }
        } else {
            caDangMoId = null;
        }

        // 2. Kiểm tra biển số đã tìm kiếm
        String bienSo = jTextFieldBienSo.getText()
                .trim().toUpperCase();
        if (bienSo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập biển số xe.");
            return;
        }

        // 3. Kiểm tra loại phương tiện
        VehicleType type = getLoaiXeDangChon();

        if (type == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn loại phương tiện."
            );
            return;
        }

        // 4. Kiểm tra vị trí đỗ
        if (viTriDangChon == null
                || !"AVAILABLE".equals(
                        viTriDangChon.getAvailabilityStatus())) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn một vị trí đỗ còn trống."
            );
            return;
        }

        // 6. Xác nhận trước khi ghi nhận
        int xacNhan = JOptionPane.showConfirmDialog(
                this,
                "Thông tin xe vào bãi:"
                + "\nBiển số: " + bienSo
                + "\nLoại xe: " + type.getTypeName()
                + "\nBãi xe: " + viTriDangChon.getLotName()
                + "\nVị trí: " + viTriDangChon.getSlotCode()
                + "\nMã ca: " + (caDangMoId == null
                        ? "Không áp dụng (Admin)"
                        : caDangMoId)
                + "\n\nBạn có chắc chắn muốn xác nhận xe vào bãi không?",
                "Xác nhận xe vào bãi",
                JOptionPane.YES_NO_OPTION
        );

        if (xacNhan != JOptionPane.YES_OPTION) {
            return;
        }

        // 7. Ghi nhận xe vào database
        try {
            String maViTriDaChon = viTriDangChon.getSlotCode();

            ParkingTicket ticket = controller.createVehicleEntry(
                    bienSo,
                    type.getVehicleTypeId(),
                    viTriDangChon.getSlotId(),
                    user.getUserId(),
                    caDangMoId,
                    laAdmin
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Ghi nhận xe vào bãi thành công!"
                    + "\nMã vé: " + ticket.getTicketCode()
                    + "\nBiển số: " + bienSo
                    + "\nVị trí: " + viTriDangChon.getSlotCode()
            );

            // 8. Xóa thông tin xe vừa ghi nhận
            jTextFieldBienSo.setText("");

            jLabelBienSo.setText("---");
            jLabelType.setText("---");
            jLabelViTri.setText("Chưa chọn vị trí");

            vehicleFound = null;
            viTriDangChon = null;
            bienSoDaTim = null;
            daTimKiem = false;

            jComboBoxType.setEnabled(true);

            // 9. Tải lại sơ đồ để cập nhật vị trí vừa có xe
            loadParkingSlots();

            jLabelBienSo.setText(bienSo);
            jLabelType.setText(type.getTypeName());
            jLabelViTri.setText(maViTriDaChon);
            jLabelTime.setText(ticket.getEntryTime() == null
                    ? "---"
                    : ticket.getEntryTime().format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                    ));

            capNhatNutXacNhan();

        } catch (SQLException | IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Ghi nhận xe vào thất bại: " + e.getMessage()
            );

            // Tải lại trạng thái vị trí nếu có thay đổi
            loadParkingSlots();
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

        jPanel2 = new javax.swing.JPanel();
        jTextFieldTimkiem = new javax.swing.JTextField();
        jButtonTimKiem = new javax.swing.JButton();
        jButtonTrong = new javax.swing.JButton();
        jButtonCoXe = new javax.swing.JButton();
        jButtonBaoTri = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jComboBoxViTriDo = new javax.swing.JComboBox<>();
        jComboBoxBaiDo = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButtonXacNhan = new javax.swing.JButton();
        jComboBoxViTri = new javax.swing.JComboBox<>();
        jComboBoxType = new javax.swing.JComboBox<>();
        jTextFieldBienSo = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabelTime = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabelBienSo = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabelType = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabelViTri = new javax.swing.JLabel();
        jLabelCaLamViec = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));

        jTextFieldTimkiem.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTextFieldTimkiem.setText("Tìm kiếm");

        jButtonTimKiem.setBackground(new java.awt.Color(51, 102, 255));
        jButtonTimKiem.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jButtonTimKiem.setForeground(new java.awt.Color(255, 255, 255));
        jButtonTimKiem.setText("Tìm Kiếm");
        jButtonTimKiem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTimKiemActionPerformed(evt);
            }
        });

        jButtonTrong.setBackground(new java.awt.Color(190, 235, 205));
        jButtonTrong.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButtonTrong.setText("Trống");

        jButtonCoXe.setBackground(new java.awt.Color(90, 160, 230));
        jButtonCoXe.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButtonCoXe.setText("Đã có xe");

        jButtonBaoTri.setBackground(new java.awt.Color(255, 190, 100));
        jButtonBaoTri.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButtonBaoTri.setText("BaoTri");

        jButton1.setText("jButton1");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("jButton2");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("jButton3");

        jButton4.setText("jButton4");

        jButton5.setText("jButton5");

        jButton6.setText("jButton6");

        jButton7.setText("jButton7");

        jButton8.setText("jButton8");

        jButton9.setText("jButton9");

        jButton10.setText("jButton10");

        jButton11.setText("jButton11");

        jButton12.setText("jButton12");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton9)
                    .addComponent(jButton5)
                    .addComponent(jButton1))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGap(62, 62, 62)
                            .addComponent(jButton2))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton6)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(62, 62, 62)
                        .addComponent(jButton10)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 53, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton7, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton11, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(34, 34, 34)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton8)
                    .addComponent(jButton4)
                    .addComponent(jButton12))
                .addGap(25, 25, 25))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3)
                    .addComponent(jButton4))
                .addGap(36, 36, 36)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton5)
                    .addComponent(jButton6)
                    .addComponent(jButton7)
                    .addComponent(jButton8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton9)
                    .addComponent(jButton10)
                    .addComponent(jButton11)
                    .addComponent(jButton12))
                .addGap(21, 21, 21))
        );

        jComboBoxViTriDo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jComboBoxViTriDo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxViTriDo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxViTriDoActionPerformed(evt);
            }
        });

        jComboBoxBaiDo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jComboBoxBaiDo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(44, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jTextFieldTimkiem, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(jButtonTimKiem, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jComboBoxViTriDo, 0, 194, Short.MAX_VALUE)
                                .addComponent(jComboBoxBaiDo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGap(28, 28, 28)
                            .addComponent(jButtonTrong)
                            .addGap(26, 26, 26)
                            .addComponent(jButtonCoXe)
                            .addGap(18, 18, 18)
                            .addComponent(jButtonBaoTri))))
                .addGap(53, 53, 53))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonTimKiem)
                    .addComponent(jTextFieldTimkiem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonBaoTri)
                            .addComponent(jButtonCoXe)
                            .addComponent(jButtonTrong)
                            .addComponent(jComboBoxViTriDo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jComboBoxBaiDo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 61, Short.MAX_VALUE)))
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28))
        );

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("GHI NHẬN XE VÀO");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Biển Số");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("Loại Xe");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Vị Trí Đỗ");

        jButtonXacNhan.setBackground(new java.awt.Color(51, 102, 255));
        jButtonXacNhan.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jButtonXacNhan.setForeground(new java.awt.Color(255, 255, 255));
        jButtonXacNhan.setText("Xác Nhận");
        jButtonXacNhan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonXacNhanActionPerformed(evt);
            }
        });

        jComboBoxViTri.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jComboBoxViTri.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxViTri.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxViTriActionPerformed(evt);
            }
        });

        jComboBoxType.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jComboBoxType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxTypeActionPerformed(evt);
            }
        });

        jTextFieldBienSo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTextFieldBienSo.setText("jTextField2");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(28, 28, 28)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextFieldBienSo, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                            .addComponent(jComboBoxType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBoxViTri, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(94, 94, 94)
                        .addComponent(jButtonXacNhan, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 106, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jComboBoxType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextFieldBienSo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jComboBoxViTri, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addComponent(jButtonXacNhan)
                .addGap(41, 41, 41))
        );

        jLabelTime.setBackground(new java.awt.Color(255, 255, 255));
        jLabelTime.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabelTime.setText("jLabelTime");

        jLabel5.setBackground(new java.awt.Color(255, 255, 255));
        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("Thời Gian");

        jLabelBienSo.setBackground(new java.awt.Color(255, 255, 255));
        jLabelBienSo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabelBienSo.setText("Biển Số");

        jLabel7.setBackground(new java.awt.Color(255, 255, 255));
        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setText("Biển Số");

        jLabel6.setBackground(new java.awt.Color(255, 255, 255));
        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setText("Thông Tin Xe");

        jLabelType.setBackground(new java.awt.Color(255, 255, 255));
        jLabelType.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabelType.setText("jLabel8");

        jLabel8.setBackground(new java.awt.Color(255, 255, 255));
        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setText("Loại Xe");

        jLabel9.setBackground(new java.awt.Color(255, 255, 255));
        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setText("Vị Trí Đỗ");

        jLabelViTri.setBackground(new java.awt.Color(255, 255, 255));
        jLabelViTri.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabelViTri.setText("jLabel10");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(254, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(41, 41, 41)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabelTime, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                            .addComponent(jLabelBienSo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabelType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabelViTri, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(46, 46, 46))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(133, 133, 133))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(jLabel6)
                .addGap(29, 29, 29)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabelType))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabelBienSo))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jLabelViTri))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabelTime))
                .addContainerGap(63, Short.MAX_VALUE))
        );

        jLabelCaLamViec.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabelCaLamViec.setText("jLabel10");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(70, 70, 70)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabelCaLamViec, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(86, 86, 86)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(60, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(43, 43, 43))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(48, 48, 48)
                        .addComponent(jLabelCaLamViec)
                        .addGap(63, 63, 63)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonTimKiemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTimKiemActionPerformed
        // TODO add your handling code here:
        String bienSo = jTextFieldTimkiem.getText().trim();

        if (bienSo.isEmpty() || bienSo.equalsIgnoreCase("Tìm kiếm")) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập biển số cần tìm.");
            return;
        }

        try {
            timXe(bienSo);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi tìm kiếm xe: " + e.getMessage());
        }

    }//GEN-LAST:event_jButtonTimKiemActionPerformed

    private void jButtonXacNhanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonXacNhanActionPerformed
        // TODO add your handling code here:
        xeVaoBai();

    }//GEN-LAST:event_jButtonXacNhanActionPerformed

    private void jComboBoxTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTypeActionPerformed
        // TODO add your handling code here:
        if (dangNapLoaiXe) {
            return;
        }

        VehicleType type = getLoaiXeDangChon();

        if (type == null) {
            return;
        }

        dangNapLoaiXe = true;
        jComboBoxBaiDo.setSelectedIndex(jComboBoxType.getSelectedIndex());
        dangNapLoaiXe = false;

        jLabelType.setText(type.getTypeName());

        loadParkingSlots();

    }//GEN-LAST:event_jComboBoxTypeActionPerformed

    private void jComboBoxBaiDoActionPerformed() {
        if (dangNapLoaiXe) {
            return;
        }

        int selectedIndex = jComboBoxBaiDo.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= danhSachLoaiXe.size()) {
            return;
        }

        dangNapLoaiXe = true;
        jComboBoxType.setSelectedIndex(selectedIndex);
        dangNapLoaiXe = false;

        VehicleType type = danhSachLoaiXe.get(selectedIndex);
        jLabelType.setText(type.getTypeName());
        loadParkingSlots();
        capNhatNutXacNhan();
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        chonViTri(0);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        chonViTri(1);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jComboBoxViTriDoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxViTriDoActionPerformed
        // TODO add your handling code here:
        if (dangNapBaiXe) {
            return;
        }

        viTriDangChon = null;

        jLabelViTri.setText("Chưa chọn vị trí");

        hienThiViTriTheoBai();
    }//GEN-LAST:event_jComboBoxViTriDoActionPerformed

    private void jComboBoxViTriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxViTriActionPerformed
        // TODO add your handling code here:

        if (dangNapViTri) {
            return;
        }

        String maViTri = (String) jComboBoxViTri.getSelectedItem();

        if (maViTri == null) {
            return;
        }

        for (ParkingSlot slot : viTriTheoBai) {

            if (slot.getSlotCode().equals(maViTri)
                    && "AVAILABLE".equals(slot.getAvailabilityStatus())) {

                viTriDangChon = slot;

                jLabelViTri.setText(slot.getSlotCode());

                // Chỉ cập nhật màu nút, KHÔNG nạp lại ComboBox
                capNhatMauNutViTri();

                capNhatNutXacNhan();

                return;
            }
        }

    }//GEN-LAST:event_jComboBoxViTriActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jButtonBaoTri;
    private javax.swing.JButton jButtonCoXe;
    private javax.swing.JButton jButtonTimKiem;
    private javax.swing.JButton jButtonTrong;
    private javax.swing.JButton jButtonXacNhan;
    private javax.swing.JComboBox<String> jComboBoxBaiDo;
    private javax.swing.JComboBox<String> jComboBoxType;
    private javax.swing.JComboBox<String> jComboBoxViTri;
    private javax.swing.JComboBox<String> jComboBoxViTriDo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelBienSo;
    private javax.swing.JLabel jLabelCaLamViec;
    private javax.swing.JLabel jLabelTime;
    private javax.swing.JLabel jLabelType;
    private javax.swing.JLabel jLabelViTri;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTextField jTextFieldBienSo;
    private javax.swing.JTextField jTextFieldTimkiem;
    // End of variables declaration//GEN-END:variables
}
