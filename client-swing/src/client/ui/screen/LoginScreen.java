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

        // 로그인 버튼 (둥글게 적용 안함)
        JPanel box3 = new JPanel(null);
        box3.setBackground(Color.WHITE);
        box3.setBounds(175, 450, 150, 75);
        content.add(box3);

        JButton loginBtn = new JButton("로그인");
        loginBtn.setBounds(0, 0, 150, 60);
        try {
            Font ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
            loginBtn.setFont(ttfFont.deriveFont(Font.BOLD, 20f));
        } catch (Exception ex) {
            loginBtn.setFont(new Font("SansSerif", Font.BOLD, 20));
        }
        loginBtn.setBackground(Color.decode("#1B76C0"));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        box3.add(loginBtn);


        // 로그인 버튼 동작: 서버에 JOIN 전송 → 층 선택 페이지 이동
        loginBtn.addActionListener(e -> {
            String userId = idField.getText().trim();

            // 0) 아이디 입력 검증
            if (userId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "아이디를 입력해주세요");
                return;
            }

            int idNum;
            try {
                idNum = Integer.parseInt(userId);   // 숫자 아이디만 허용
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "숫자 아이디만 입력 가능합니다.");
                return;
            }

            // 1) role 결정 (1~3: USER, 4이상: ADMIN)
            String role = (idNum >= 4) ? "ADMIN" : "USER";

            // 2) 관리자라면 담당 층/구역 매핑
            Integer floor = null;
            String room = null;

            if ("ADMIN".equals(role)) {
                switch (idNum) {
                    case 4:  // 관리자A
                        floor = 1; room = "A"; break;
                    case 5:  // 관리자B
                        floor = 1; room = "B"; break;
                    case 6:  // 관리자C
                        floor = 2; room = "A"; break;
                    case 7:  // 새 관리자D
                        floor = 2; room = "B"; break;
                    case 8:  // 새 관리자E
                        floor = 3; room = null; break;
                    case 9:  // 새 관리자F
                        floor = 4; room = null; break;
                    case 10: // 새 관리자G
                        floor = 5; room = "A"; break;
                    case 11: // 새 관리자H
                        floor = 5; room = "B"; break;
                    case 12: // 새 관리자I
                        floor = 6; room = null; break;
                    default:
                        JOptionPane.showMessageDialog(this, "등록되지 않은 관리자입니다.");
                        return;
                }
            }

            // 3) JOIN 메시지 생성 + 서버 전송
            SocketMessage joinMsg = new SocketMessage();
            joinMsg.setType("JOIN");
            joinMsg.setSender(userId);
            joinMsg.setRole(role);

            // 관리자면 floor/room도 같이 보냄
            if (floor != null) joinMsg.setFloor(floor);
            if (room != null)  joinMsg.setRoom(room);

            socketClient.send(joinMsg);

            // 4) 화면 전환
            if ("ADMIN".equals(role)) {
                // 관리자 → 관리자 메인 화면
                ManagerScreen next =new ManagerScreen(socketClient, userId, floor, room, "ADMIN");
                next.setVisible(true);
            } else {
                // 일반 사용자 → 기존 층 선택 화면
                FloorSelectionScreen next =
                        new FloorSelectionScreen(socketClient, userId, role);
                next.setVisible(true);
            }

            dispose();
        });


        setVisible(true);
    }
}
