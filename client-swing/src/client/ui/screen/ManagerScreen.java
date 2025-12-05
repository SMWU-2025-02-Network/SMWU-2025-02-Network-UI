package client.ui.screen;

import client.socket.SocketClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class ManagerScreen extends JFrame {

    private final SocketClient socketClient;
    private final String userId;
    private final int floor;
    private final String room; // 예: "A", "B" / 단일 구역이면 null
    private final String role;


    public ManagerScreen(SocketClient socketClient, String userId, int floor, String room, String role) {
        this.socketClient = socketClient;
        this.userId = userId;
        this.floor = floor;
        this.room = room;
        this.role = role;

        setTitle("NetLibrary - 관리자 화면");
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
            // 다시 로그인 화면으로 (같은 소켓 사용)
            new LoginScreen(socketClient).setVisible(true);
            dispose();
        });
        topBar.add(backBtn, BorderLayout.WEST);

        // 상단바 제목
        JLabel loginTitle = new JLabel("관리자 화면", SwingConstants.CENTER);
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

        // 관리자 채팅 버튼
        JPanel box2 = new JPanel(null);
        box2.setBackground(Color.WHITE);
        box2.setBounds(175, 200, 150, 75);
        content.add(box2);

        JButton mchatBtn = new JButton("관리자 채팅");
        mchatBtn.setBounds(0, 0, 150, 75);
        try {
            Font ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
            mchatBtn.setFont(ttfFont.deriveFont(Font.BOLD, 20f));
        } catch (Exception ex) {
            mchatBtn.setFont(new Font("SansSerif", Font.BOLD, 20));
        }
        mchatBtn.setBackground(Color.decode("#1B76C0"));
        mchatBtn.setForeground(Color.WHITE);
        mchatBtn.setFocusPainted(false);
        box2.add(mchatBtn);

        mchatBtn.addActionListener(e -> {
            try {
                ManagerChatScreen mchat =
                        new ManagerChatScreen(socketClient, userId, floor, room);
                mchat.setVisible(true);
                ManagerScreen.this.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "관리자 채팅 화면 생성 실패");
            }
        });

        // 담당층 메인 화면 버튼
        JPanel box3 = new JPanel(null);
        box3.setBackground(Color.WHITE);
        box3.setBounds(175, 400, 150, 75);
        content.add(box3);

        JButton mfloorBtn = new JButton("담당층 메인 화면");
        mfloorBtn.setBounds(0, 0, 150, 75);
        try {
            Font ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
            mfloorBtn.setFont(ttfFont.deriveFont(Font.BOLD, 20f));
        } catch (Exception ex) {
            mfloorBtn.setFont(new Font("SansSerif", Font.BOLD, 20));
        }
        mfloorBtn.setBackground(Color.decode("#1B76C0"));
        mfloorBtn.setForeground(Color.WHITE);
        mfloorBtn.setFocusPainted(false);
        box3.add(mfloorBtn);

        mfloorBtn.addActionListener(e -> {
            try {
                // 담당층/구역 MainScreen 으로 이동
                MainScreen main = new MainScreen(socketClient, userId, floor, room, role);
                main.setVisible(true);
                ManagerScreen.this.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "담당층 메인 화면 생성 실패");
            }
        });
    }
}
