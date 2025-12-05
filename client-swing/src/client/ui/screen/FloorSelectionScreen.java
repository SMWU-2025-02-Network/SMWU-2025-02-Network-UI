package client.ui.screen;

import client.socket.SocketClient;
import client.socket.SocketMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class FloorSelectionScreen extends JFrame {

    private final SocketClient socketClient;
    private final String userId;
    private final String role;

    // LoginScreen에서 socketClient + userId를 넘겨받음
    public FloorSelectionScreen(SocketClient socketClient, String userId, String role) {
        this.socketClient = socketClient;
        this.userId = userId;
        this.role = role;

        setTitle("Netlibrary - 층 선택");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 800);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        setContentPane(root);

        // 상단 바
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
            new FloorSelectionScreen(socketClient, userId, role).setVisible(true);
            dispose();
        });
        topBar.add(backBtn, BorderLayout.WEST);

        // 상단바 제목
        JLabel floorTitle = new JLabel("층 및 구역 선택", SwingConstants.CENTER);
        floorTitle.setForeground(Color.BLACK);
        try {
            Font ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
            floorTitle.setFont(ttfFont.deriveFont(Font.BOLD, 24f));
        } catch (Exception ex) {
            floorTitle.setFont(new Font("SansSerif", Font.BOLD, 19));
        }
        topBar.add(floorTitle, BorderLayout.CENTER);
        topBar.add(Box.createHorizontalStrut(backBtn.getPreferredSize().width), BorderLayout.EAST);

        JPanel content = new JPanel(null);
        content.setBackground(Color.WHITE);
        root.add(content, BorderLayout.CENTER);

        // 제목 박스
        JPanel title = new JPanel(new FlowLayout(FlowLayout.CENTER));
        title.setBackground(Color.WHITE);
        title.setBounds(75, 75, 350, 75);
        content.add(title);

        JLabel titleLabel = new JLabel("층과 구역을 선택해주세요");
        try {
            Font ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
            titleLabel.setFont(ttfFont.deriveFont(Font.BOLD, 28f));
        } catch (Exception ex) {
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        }
        title.add(titleLabel);

        JPanel floor = new JPanel(null);
        floor.setBackground(Color.WHITE);
        floor.setBounds(75, 150, 350, 450);
        content.add(floor);

        // 층/구역 버튼
        String[] btnNames = {"6층", "5층 A", "5층 B", "4층", "3층", "2층 A", "2층 B", "1층 A", "1층 B"};
        int[][] btnPositions = {
                {50, 25, 250, 50}, {50, 100, 120, 50}, {180, 100, 120, 50},
                {50, 175, 250, 50}, {50, 250, 250, 50}, {50, 325, 120, 50},
                {180, 325, 120, 50}, {50, 400, 120, 50}, {180, 400, 120, 50}
        };

        for (int i = 0; i < btnNames.length; i++) {
            final int index = i;

            JButton btn = new JButton(btnNames[i]);
            btn.setBounds(btnPositions[i][0], btnPositions[i][1],
                    btnPositions[i][2], btnPositions[i][3]);

            btn.setBackground(Color.decode("#1B76C0"));
            btn.setForeground(Color.WHITE);

            try {
                Font ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
                btn.setFont(ttfFont.deriveFont(Font.BOLD, 25f));
            } catch (Exception ex) {
                btn.setFont(new Font("SansSerif", Font.BOLD, 20));
            }

            // 버튼 클릭 시 JOIN_ROOM 전송
            btn.addActionListener(e -> {
                String text = btnNames[index];   // 예: "6층", "2층 A"
                int floorNum;
                String room = null;

                if (text.contains("층")) {
                    String[] arr = text.split(" ");           // ["6층"] 또는 ["2층","A"]
                    floorNum = Integer.parseInt(arr[0].replace("층", ""));

                    if (arr.length == 1) {
                        // 3,4,6층처럼 구역이 없는 층 → room 없이 사용 (DB seats.room = NULL)
                        room = null;
                    } else {
                        // 1,2,5층처럼 A/B 구역 있는 층 → 두 번째 토큰 사용
                        room = arr[1];                        // "A" or "B"
                    }
                } else {
                    JOptionPane.showMessageDialog(
                            FloorSelectionScreen.this,
                            "층 정보를 읽을 수 없습니다."
                    );
                    return;
                }

                // JOIN_ROOM 메시지 생성
                SocketMessage joinRoom = new SocketMessage();
                joinRoom.setType("JOIN_ROOM");
                joinRoom.setFloor(floorNum);
                joinRoom.setRoom(room);       // 3·4·6층이면 null, 1·2·5층이면 "A"/"B"
                joinRoom.setSender(userId);
                joinRoom.setRole(role);

                socketClient.send(joinRoom);

                // 다음 화면으로 이동
                MainScreen chat = new MainScreen(socketClient, userId, floorNum, room,role);
                chat.setVisible(true);
                dispose();
            });


            floor.add(btn);
        }

        setVisible(true);
    }
}
