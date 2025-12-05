package client.ui.screen;

import client.socket.SocketClient;
import client.socket.SocketMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.text.*;

public class ChatScreen extends JPanel {

    private final SocketClient socketClient;
    private final String userId;
    private final int floor;
    private final String room;
    private final String myRole;

    private final JTextPane chatArea;
    private final JTextField inputField;
    private final JButton sendBtn;
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
    private Font ttfFont; // 커스텀 폰트

    // MainScreen에서 socketClient + userId + floor + room을 넘겨받는 버전
    public ChatScreen(SocketClient socketClient, String userId, int floor, String room, String myRole) {
        this.socketClient = socketClient;
        this.userId = userId;
        this.floor = floor;
        this.room = room;
        this.myRole = myRole;

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
        // 채팅 표시 영역
        chatArea = new JTextPane();
        chatArea.setEditable(false);
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

                try {
                    SocketMessage chatMsg = new SocketMessage();
                    chatMsg.setType("CHAT");
                    chatMsg.setFloor(floor);
                    chatMsg.setRoom(room);
                    chatMsg.setRole(myRole);

                    // 여기서 sender = 내 userId (또는 닉네임으로 쓰는 값)
                    chatMsg.setSender(userId);
                    chatMsg.setMsg(text);

                    socketClient.send(chatMsg);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    appendMessage("SYSTEM", "메시지 전송 실패: " + ex.getMessage(), false);

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
    public void appendMessage(String who, String msg, boolean isAdmin) {
        String time = LocalTime.now().format(timeFmt);

        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = chatArea.getStyledDocument();
            SimpleAttributeSet attr = new SimpleAttributeSet();

            // 관리자면 파란색, 아니면 기본(검정)
            if (isAdmin) {
                StyleConstants.setForeground(attr, Color.BLUE);
            } else {
                StyleConstants.setForeground(attr, Color.BLACK);
            }

            String line = String.format("[%s] %s: %s%n", time, who, msg);

            try {
                doc.insertString(doc.getLength(), line, attr);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            chatArea.setCaretPosition(doc.getLength());
        });
    }

    // 서버에서 온 채팅을 추가할 때 사용하는 편의 메서드
    public void appendChatFromServer(String sender, String role, String msg) {

        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
        String who;

        if (isAdmin) {
            // 관리자면 항상 이렇게 (본인이 봐도 동일)
            who = "관리자";
        } else if (sender == null || sender.isBlank()) {
            who = "SYSTEM";
        } else if (sender.equals(this.userId)) {
            who = "나";
        } else {
            who = sender;
        }

        appendMessage(who, msg, isAdmin);
    }
}
