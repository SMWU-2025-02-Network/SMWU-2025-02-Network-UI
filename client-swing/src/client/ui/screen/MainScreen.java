package client.ui.screen;

import client.socket.SocketClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class MainScreen extends JFrame {

    private CardLayout cardLayout;
    private JPanel centerPanel;

    private ChatScreen chatScreen;
    private DashboardScreen dashScreen;
    private SeatMapScreen seatScreen;

    private JLabel floorTitle;

    // 네트워크/사용자/위치 정보
    private final SocketClient socketClient;
    private final String userId;
    private final int floor;
    private final String room;
    private final String displayFloorName;   // "2층 A" 같은 표시용 텍스트

    // FloorSelectionScreen에서 socketClient + userId + floor + room을 넘겨받음
    public MainScreen(SocketClient socketClient, String userId, int floor, String room) {
        this.socketClient = socketClient;
        this.userId = userId;
        this.floor = floor;
        this.room = room;

        // "2층", "2층 A" 같은 표시용 이름
        this.displayFloorName = floor + "층" + (room != null ? " " + room : "");

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
            //  층 선택 화면으로 돌아갈 때도 socketClient, userId 유지
            new FloorSelectionScreen(socketClient, userId).setVisible(true);
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

        // TODO: 나중에 DashboardScreen/SeatMapScreen도
        //       socketClient, userId, floor, room을 넘겨서 생성하면 됨
        chatScreen = new ChatScreen(socketClient, userId, floor, room);
        dashScreen = new DashboardScreen();
        seatScreen = new SeatMapScreen();

        centerPanel.add(chatScreen, "CHAT");
        centerPanel.add(dashScreen, "DASH");
        centerPanel.add(seatScreen, "SEAT");

        cardLayout.show(centerPanel, "CHAT");

        if (socketClient != null) {
            socketClient.setListener(msg -> {
                // CHAT 메시지면 채팅창에 추가
                if ("CHAT".equals(msg.getType())) {
                    // SYSTEM, 본인, 다른 유저 모두 여기로 옴
                    String sender = msg.getSender() != null ? msg.getSender() : "SYSTEM";
                    String text = msg.getMsg();
                    chatScreen.appendMessage(sender, text);
                }

                // 센서 대시보드 업데이트
                else if ("DASHBOARD_UPDATE".equals(msg.getType())) {
                    Double temp = msg.getTemp();
                    Double lux  = msg.getLux();
                    Double co2  = msg.getCo2();

                    if (temp != null && lux != null && co2 != null) {
                        dashScreen.updateSensorData(temp, lux, co2);
                    }
                }

                // 나중에 SEAT_UPDATE 같은 것도 여기서 처리 가능
                // else if ("SEAT_UPDATE".equals(msg.getType())) { ... }
            });
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
