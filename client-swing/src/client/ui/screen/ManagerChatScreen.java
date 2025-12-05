/*
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ManagerChatScreen extends JFrame {
    private final JTextArea chatArea;
    private final JTextField inputField;
    private final JButton sendBtn;
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
    private Font ttfFont; // 커스텀 폰트

    public ManagerChatScreen() {

        //창 띄우기
        setTitle("NetLibrary - 관리자 채팅 화면");*/
/**//*

        setSize(500, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        setContentPane(root);

        //폰트
        try {
            ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
        } catch (Exception e) {
            e.printStackTrace();
            ttfFont = new Font("SansSerif", Font.PLAIN, 18);
        }

        //상단 바
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
            ManagerScreen ms = new ManagerScreen();
            ms.setVisible(true);
            dispose();
        });
        topBar.add(backBtn, BorderLayout.WEST);

        // 상단바 제목
        JLabel loginTitle = new JLabel("관리자 채팅", SwingConstants.CENTER);
        loginTitle.setForeground(Color.BLACK);
        try {
            Font ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
            loginTitle.setFont(ttfFont.deriveFont(Font.BOLD, 24f));
        } catch (Exception ex) {
            loginTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
            ex.printStackTrace();
        }
        topBar.add(loginTitle, BorderLayout.CENTER);
        topBar.add(Box.createHorizontalStrut(backBtn.getPreferredSize().width), BorderLayout.EAST);

        JPanel content = new JPanel(null);
        content.setBackground(Color.WHITE);
        root.add(content, BorderLayout.CENTER);

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

*/
