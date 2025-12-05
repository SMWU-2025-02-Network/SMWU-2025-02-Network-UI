package client.ui.screen;

import client.socket.SocketClient;
import client.socket.SocketMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ManagerChatScreen extends JFrame {

    // ───────── 네트워크 / 관리자 정보 ─────────
    private final SocketClient socketClient;
    private final String userId;
    private final int floor;     // 담당 층
    private final String room;   // 담당 구역 ("A"/"B" 또는 null)
    private final String role;   // "ADMIN"

    // ───────── UI 컴포넌트 ─────────
    private final JTextArea chatArea;
    private final JTextField inputField;
    private final JButton sendBtn;
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
    private Font ttfFont; // 커스텀 폰트

    public ManagerChatScreen(SocketClient socketClient,
                             String userId,
                             int floor,
                             String room,
                             String role) {

        this.socketClient = socketClient;
        this.userId = userId;
        this.floor = floor;
        this.room = room;
        this.role = role;   // "ADMIN"

        // ───────── 기본 프레임 설정 ─────────
        setTitle("NetLibrary - 관리자 채팅 화면");
        setSize(500, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        setContentPane(root);

        // ───────── 폰트 로딩 ─────────
        try {
            ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
        } catch (Exception e) {
            e.printStackTrace();
            ttfFont = new Font("SansSerif", Font.PLAIN, 18);
        }

        // ───────── 상단 바 ─────────
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
            // 관리자 메인으로 복귀
            ManagerScreen ms = new ManagerScreen(socketClient, userId, floor, room, role);
            ms.setVisible(true);
            dispose();
        });
        topBar.add(backBtn, BorderLayout.WEST);

        // 상단바 제목
        JLabel loginTitle = new JLabel("관리자 채팅", SwingConstants.CENTER);
        loginTitle.setForeground(Color.BLACK);
        try {
            Font ttfFontTitle = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
            loginTitle.setFont(ttfFontTitle.deriveFont(Font.BOLD, 24f));
        } catch (Exception ex) {
            loginTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
            ex.printStackTrace();
        }
        topBar.add(loginTitle, BorderLayout.CENTER);
        topBar.add(Box.createHorizontalStrut(backBtn.getPreferredSize().width), BorderLayout.EAST);

        // ───────── 중앙: 채팅 영역 ─────────
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(ttfFont.deriveFont(Font.PLAIN, 21f));
        chatArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(
                chatArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // ───────── 하단: 입력창 + 전송 버튼 ─────────
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setPreferredSize(new Dimension(0, 80));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        inputField = new JTextField();
        inputField.setFont(ttfFont.deriveFont(Font.PLAIN, 18f));
        inputPanel.add(inputField, BorderLayout.CENTER);

        sendBtn = new JButton("전송");
        sendBtn.setFont(ttfFont.deriveFont(Font.BOLD, 21f));
        sendBtn.setBackground(Color.decode("#1B76C0"));
        sendBtn.setForeground(Color.WHITE);
        inputPanel.add(sendBtn, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);

        // ───────── 전송 액션 (버튼 + 엔터) ─────────
        Action sendAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendChat();
            }
        };
        sendBtn.addActionListener(sendAction);
        inputField.addActionListener(sendAction);

        // ───────── 소켓 리스너: ADMIN_CHAT 수신 ─────────
        if (socketClient != null) {
            socketClient.setListener(msg -> {
                if ("ADMIN_CHAT".equals(msg.getType())) {
                    SwingUtilities.invokeLater(() -> onAdminChat(msg));
                }
            });
        }
    }

    // ───────────────── 전송 로직 ─────────────────
    private void sendChat() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        // 서버로만 보내고, 화면 출력은 서버에서 받은 메시지로 통일
        SocketMessage msg = new SocketMessage();
        msg.setType("ADMIN_CHAT");
        msg.setRole(role);       // "ADMIN"
        msg.setSender(userId);
        msg.setFloor(floor);
        msg.setRoom(room);
        msg.setMsg(text);

        socketClient.send(msg);
        inputField.setText("");
    }

    // ───────────────── 서버 수신 처리 ─────────────────

    // ADMIN_CHAT 수신 시 호출
    private void onAdminChat(SocketMessage msg) {
        boolean isMine = userId.equals(msg.getSender());
        String who = buildAdminDisplayName(isMine, msg.getFloor(), msg.getRoom());
        appendMessage(who, msg.getMsg());
    }

    // "나(4층-A 관리자)" / "1층-A 관리자" / "6층 관리자" 형식으로 이름 만들기
    private String buildAdminDisplayName(boolean isMine, int floorFromMsg, String roomFromMsg) {
        String base;
        if (roomFromMsg == null || roomFromMsg.isBlank()) {
            base = floorFromMsg + "층 관리자";
        } else {
            base = floorFromMsg + "층-" + roomFromMsg + " 관리자";
        }

        if (isMine) {
            return "나(" + base + ")";
        } else {
            return base;
        }
    }

    // 실제로 채팅창에 한 줄 append
    public void appendMessage(String who, String msg) {
        String time = LocalTime.now().format(timeFmt);
        chatArea.append(String.format("[%s] %s: %s%n", time, who, msg));
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}
