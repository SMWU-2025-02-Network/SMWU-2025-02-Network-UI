package client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatScreen extends JPanel {
    private final JTextArea chatArea;
    private final JTextField inputField;
    private final JButton sendBtn;
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
    private Font ttfFont; // 커스텀 폰트

    public ChatScreen() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setMaximumSize(new Dimension(400, 400));

        //폰트
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
                String time = LocalTime.now().format(timeFmt);
                chatArea.append(String.format("[%s] 나: %s%n", time, text));
                inputField.setText("");
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            }
        };

        sendBtn.addActionListener(sendAction);
        inputField.addActionListener(sendAction); // 엔터로도 전송
    }

    // 외부에서 메시지를 추가할 때(서버 메시지) 사용
    public void appendMessage(String who, String msg) {
        String time = LocalTime.now().format(timeFmt);
        chatArea.append(String.format("[%s] %s: %s%n", time, who, msg));
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}

