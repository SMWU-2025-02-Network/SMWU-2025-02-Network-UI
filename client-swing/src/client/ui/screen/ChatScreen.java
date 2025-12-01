package client.ui.screen;

import client.socket.SocketClient;
import client.socket.SocketMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatScreen extends JPanel {

    private final SocketClient socketClient;
    private final String userId;
    private final int floor;
    private final String room;

    private final JTextArea chatArea;
    private final JTextField inputField;
    private final JButton sendBtn;
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
    private Font ttfFont; // 커스텀 폰트

    // MainScreen에서 socketClient + userId + floor + room을 넘겨받는 버전
    public ChatScreen(SocketClient socketClient, String userId, int floor, String room) {
        this.socketClient = socketClient;
        this.userId = userId;
        this.floor = floor;
        this.room = room;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setMaximumSize(new Dimension(400, 400));

        // 폰트
        try {
            ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
        } catch (Exception e) {
            e.printStackTrace();
            ttfFont = new Font("SansSerif", Font.PLAIN, 18);
        }

        // 채팅 표시 영역
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

        // 입력창 + 전송 버튼
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

        // 전송 동작 (버튼 + 엔터)
        Action sendAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = inputField.getText().trim();
                if (text.isEmpty()) return;

                // 1) 화면에 먼저 내 메시지 추가
                appendMessage("나", text);

                // 2) 서버로 CHAT 패킷 전송
                try {
                    SocketMessage chatMsg = new SocketMessage();
                    chatMsg.setType("CHAT");
                    chatMsg.setFloor(floor);
                    chatMsg.setRoom(room);
                    chatMsg.setSender(userId);
                    chatMsg.setRole("USER");
                    chatMsg.setMsg(text);

                    socketClient.send(chatMsg);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    appendMessage("SYSTEM", "메시지 전송 실패: " + ex.getMessage());
                }

                inputField.setText("");
            }
        };

        sendBtn.addActionListener(sendAction);
        inputField.addActionListener(sendAction); // 엔터로도 전송
    }

    /**
     * 외부(소켓 수신 쓰레드 등)에서 서버 메시지를 추가할 때 사용
     */
    public void appendMessage(String who, String msg) {
        String time = LocalTime.now().format(timeFmt);

        // Swing UI 스레드에서 안전하게 실행
        SwingUtilities.invokeLater(() -> {
            chatArea.append(String.format("[%s] %s: %s%n", time, who, msg));
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
}
