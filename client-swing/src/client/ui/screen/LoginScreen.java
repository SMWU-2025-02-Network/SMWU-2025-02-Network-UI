package client.ui.screen;

import client.socket.SocketClient;
import client.socket.SocketMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class LoginScreen extends JFrame {

    private final SocketClient socketClient;

    public LoginScreen(SocketClient socketClient) {
        this.socketClient = socketClient;

        setTitle("NetLibrary - 로그인");
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
            new StartScreen();
            dispose();
        });
        topBar.add(backBtn, BorderLayout.WEST);

        // 상단 제목
        JLabel loginTitle = new JLabel("로그인", SwingConstants.CENTER);
        loginTitle.setForeground(Color.BLACK);
        try {
            Font ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
            loginTitle.setFont(ttfFont.deriveFont(Font.BOLD, 24f));
        } catch (Exception ex) {
            loginTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        }
        topBar.add(loginTitle, BorderLayout.CENTER);
        topBar.add(Box.createHorizontalStrut(backBtn.getPreferredSize().width), BorderLayout.EAST);

        JPanel content = new JPanel(null);
        content.setBackground(Color.WHITE);
        root.add(content, BorderLayout.CENTER);

        // 제목 박스
        JPanel box1 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        box1.setBackground(Color.WHITE);
        box1.setBounds(100, 130, 300, 100);
        content.add(box1);
        JLabel titleLabel = new JLabel("NetLibrary 로그인");
        try {
            Font ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
            titleLabel.setFont(ttfFont.deriveFont(Font.BOLD, 40f));
        } catch (Exception ex) {
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        }
        box1.add(titleLabel);

        // 아이디/비밀번호 입력 영역
        JPanel box2 = new JPanel(null);
        box2.setBackground(Color.WHITE);
        box2.setBounds(100, 230, 300, 175);
        content.add(box2);

        JLabel idLabel = new JLabel("아이디");
        idLabel.setBounds(5, 25, 75, 40);
        try {
            Font ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
            idLabel.setFont(ttfFont.deriveFont(Font.BOLD, 20f));
        } catch (Exception ex) {
            idLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        }
        box2.add(idLabel);

        JTextField idField = new JTextField();
        idField.setBounds(80, 25, 210, 40);
        box2.add(idField);

        JLabel pwLabel = new JLabel("비밀번호");
        pwLabel.setBounds(5, 110, 75, 40);
        try {
            Font ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
            pwLabel.setFont(ttfFont.deriveFont(Font.BOLD, 20f));
        } catch (Exception ex) {
            pwLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        }
        box2.add(pwLabel);

        JPasswordField pwField = new JPasswordField();
        pwField.setBounds(80, 110, 210, 40);
        box2.add(pwField);

        // 로그인 버튼
        JPanel box3 = new JPanel(null);
        box3.setBackground(Color.WHITE);
        box3.setBounds(175, 450, 150, 75);
        content.add(box3);

        JButton loginBtn = new JButton("로그인");
        loginBtn.setBounds(0, 0, 150, 75);
        loginBtn.setBackground(Color.decode("#1B76C0"));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        box3.add(loginBtn);


        // 로그인 버튼 동작: 서버에 JOIN 전송 → 층 선택 페이지 이동
        loginBtn.addActionListener(e -> {
            String userId = idField.getText().trim();

            if (userId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "아이디를 입력해주세요");
                return;
            }

            // (1) JOIN 메시지 생성
            SocketMessage joinMsg = new SocketMessage();
            joinMsg.setType("JOIN");
            joinMsg.setSender(userId);
            joinMsg.setRole("USER");

            // floor/room은 아직 선택 안 했으므로 null

            // (2) 서버에 JOIN 전송
            socketClient.send(joinMsg);

            // (3) 다음 화면으로 소켓 + userId 넘기기
            FloorSelectionScreen next = new FloorSelectionScreen(socketClient, userId);
            next.setVisible(true);
            dispose();
        });

        setVisible(true);
    }
}
