package client.ui.screen;

import client.socket.SocketClient;
import client.socket.SocketMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class MainScreen extends JFrame {

    private CardLayout cardLayout;
    private JPanel centerPanel;

    private ChatScreen chatScreen;
    private DashboardScreen dashScreen;

    // 사용자용 좌석 화면 / 관리자용 좌석 화면
    private SeatMapScreen userSeatScreen;
    private ManagerSeatMapScreen managerSeatScreen;

    private JLabel floorTitle;

    // 네트워크/사용자/위치 정보
    private final SocketClient socketClient;
    private final String userId;
    private final int floor;
    private final String room;
    private final String role;
    private final String displayFloorName;   // "2층 A" 같은 표시용 텍스트

    // FloorSelectionScreen 또는 ManagerScreen에서 socketClient + userId + floor + room + role을 넘겨받음
    public MainScreen(SocketClient socketClient, String userId, int floor, String room, String role) {
        this.socketClient = socketClient;
        this.userId = userId;
        this.floor = floor;
        this.room = room;
        this.role = role;

        // "2층", "2층 A" 같은 표시용 이름
        if (floor == 3 || floor == 4 || floor == 6) {
            this.displayFloorName = floor + "층";
        } else if (room != null && !room.isBlank()) {
            this.displayFloorName = floor + "층 " + room;
        } else {
            this.displayFloorName = floor + "층";
        }

        setTitle("Netlibrary - 메인화면");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 800);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        setContentPane(root);

        // ───────────────── 상단 바 ─────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.decode("#DBDBDB"));
        topBar.setPreferredSize(new Dimension(0, 50));
        topBar.setBorder(new EmptyBorder(6, 8, 6, 8));
        root.add(topBar, BorderLayout.NORTH);

        // 뒤로가기 버튼
        ImageIcon icon = new ImageIcon("src/resources/backBtn.png");
        Image img = icon.getImage().getScaledInstance(45, 30, Image.SCALE_SMOOTH);
        icon = new ImageIcon(img);
        JButton backBtn = new JButton(icon);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setFocusPainted(false);
        backBtn.setOpaque(false);

        backBtn.addActionListener(e -> {
            if ("ADMIN".equals(role)) {
                // 관리자라면 → ManagerScreen 으로 복귀
                ManagerScreen ms = new ManagerScreen(socketClient, userId, floor, room, role);
                ms.setVisible(true);
            } else {
                // 일반 사용자라면 → 층 선택 화면으로 복귀
                FloorSelectionScreen fs =
                        new FloorSelectionScreen(socketClient, userId, role);
                fs.setVisible(true);
            }
            dispose();
        });

        topBar.add(backBtn, BorderLayout.WEST);

        // 상단바 제목
        floorTitle = new JLabel(displayFloorName + " 대화방", SwingConstants.CENTER);
        floorTitle.setForeground(Color.BLACK);
        try {
            Font ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
            floorTitle.setFont(ttfFont.deriveFont(Font.BOLD, 24f));
        } catch (Exception ex) {
            floorTitle.setFont(new Font("SansSerif", Font.BOLD, 19));
            ex.printStackTrace();
        }
        topBar.add(floorTitle, BorderLayout.CENTER);
        topBar.add(Box.createHorizontalStrut(backBtn.getPreferredSize().width), BorderLayout.EAST);

        // ───────────────── 중앙 (CardLayout) ─────────────────
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        root.add(centerPanel, BorderLayout.CENTER);

        // 대화방 화면
        this.chatScreen = new ChatScreen(socketClient, userId, floor, room, role);
        // 대시보드 화면
        this.dashScreen = new DashboardScreen();

        // 좌석 화면 (USER / ADMIN 분기)
        if ("ADMIN".equals(role)) {
            // 관리자 전용 좌석 모니터링 화면
            this.managerSeatScreen = new ManagerSeatMapScreen(socketClient, floor, room);
            this.userSeatScreen = null;
            centerPanel.add(managerSeatScreen, "SEAT");
        } else {
            // 사용자 좌석 화면 (체크인/외출/변경)
            this.userSeatScreen = new SeatMapScreen(socketClient, userId, floor, room);
            this.managerSeatScreen = null;
            centerPanel.add(userSeatScreen, "SEAT");
        }

        centerPanel.add(chatScreen, "CHAT");
        centerPanel.add(dashScreen, "DASH");

        cardLayout.show(centerPanel, "CHAT");

        // ───────────────── 소켓 리스너 설정 ─────────────────
        if (socketClient != null) {
            socketClient.setListener(msg -> {

                if ("CHAT".equals(msg.getType())) {

                    String sender = msg.getSender();   // 보낸 userId
                    String text   = msg.getMsg();
                    String senderRole = msg.getRole(); // 관리자/사용자 구분

                    chatScreen.appendChatFromServer(sender, senderRole, text);
                }

                // 좌석 상태 업데이트
                else if ("SEAT_UPDATE".equals(msg.getType())) {
                    var seatInfos = msg.getSeats();
                    System.out.println("[CLIENT] SEAT_UPDATE 수신, seats="
                            + (seatInfos == null ? "null" : seatInfos.size()));

                    if (seatInfos != null) {
                        SwingUtilities.invokeLater(() -> {
                            if ("ADMIN".equals(role) && managerSeatScreen != null) {
                                managerSeatScreen.applySeatUpdate(seatInfos);
                            } else if (userSeatScreen != null) {
                                userSeatScreen.applySeatUpdate(seatInfos);
                            }
                        });
                    }
                }

                // 센서 대시보드 업데이트
                else if ("DASHBOARD_UPDATE".equals(msg.getType())) {
                    Double temp = msg.getTemp();
                    Double lux  = msg.getLux();
                    Double co2  = msg.getCo2();

                    System.out.println("[CLIENT] DASHBOARD_UPDATE 수신: "
                            + "temp=" + temp + ", lux=" + lux + ", co2=" + co2);

                    if (temp != null && lux != null && co2 != null) {
                        dashScreen.updateSensorData(temp, lux, co2);
                    }
                }

                // 좌석 체크인 관련 에러
                else if ("ERROR".equals(msg.getType())) {
                    JOptionPane.showMessageDialog(
                            MainScreen.this,
                            msg.getMsg(),
                            "좌석 체크인 오류",
                            JOptionPane.ERROR_MESSAGE
                    );

                    // 사용자만 좌석 에러 처리 (관리자는 체크인 안 함)
                    if (!"ADMIN".equals(role) && userSeatScreen != null) {
                        userSeatScreen.handleCheckinError();
                    }
                }
            });

            // 리스너 세팅이 끝난 뒤, 현재 좌석 상태를 요청
            SocketMessage req = new SocketMessage();
            req.setType("SEAT_STATUS_REQUEST");
            req.setFloor(floor);
            req.setRoom(room);     // 3,4,6층이면 null
            socketClient.send(req);
        }

        // ───────────────── 하단 탭 바 ─────────────────
        JPanel bottomBar = new JPanel(null);
        bottomBar.setBackground(Color.decode("#DBDBDB"));
        bottomBar.setPreferredSize(new Dimension(0, 60));
        bottomBar.setBorder(new EmptyBorder(5, 5, 5, 5));
        root.add(bottomBar, BorderLayout.SOUTH);

        String[] btnNames = {"대화방", "대시보드", "좌석"};
        int[] btnX = {45, 195, 345};

        for (int i = 0; i < btnNames.length; i++) {
            JButton btn = new JButton(btnNames[i]);
            btn.setBounds(btnX[i], 5, 100, 50);
            btn.setBackground(Color.decode("#1B76C0"));
            btn.setForeground(Color.WHITE);
            try {
                Font ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
                btn.setFont(ttfFont.deriveFont(Font.BOLD, 20f));
            } catch (Exception ex) {
                btn.setFont(new Font("SansSerif", Font.BOLD, 15));
            }
            bottomBar.add(btn);

            final int idx = i;
            btn.addActionListener(e -> {
                if (idx == 0) cardLayout.show(centerPanel, "CHAT");
                else if (idx == 1) cardLayout.show(centerPanel, "DASH");
                else cardLayout.show(centerPanel, "SEAT");

                // 상단 제목 변경
                switch (idx) {
                    case 0 -> floorTitle.setText(displayFloorName + " 대화방");
                    case 1 -> floorTitle.setText(displayFloorName + " 대시보드");
                    case 2 -> floorTitle.setText(displayFloorName + " 좌석");
                }
            });
        }
    }
}
