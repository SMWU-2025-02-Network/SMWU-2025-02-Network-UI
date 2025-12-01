package client.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class StartScreen extends JFrame{
    public StartScreen() {

        //창 띄우기
        setTitle("NetLibrary - 시작화면");
        setSize(500, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(null);
        add(panel);

        try {
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"))
                    .deriveFont(Font.BOLD, 50f);

            JLabel titleLabel = new JLabel("NetLibrary", SwingConstants.CENTER);
            titleLabel.setFont(customFont); // TTF 적용
            titleLabel.setBounds(100, 100, 300, 50);
            panel.add(titleLabel);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("폰트 로드 실패. 기본 폰트 사용됨.");
        }

        //이미지
        ImageIcon icon = new ImageIcon("src/resources/networklogo.png");
        Image img = icon.getImage();

        int originW = icon.getIconWidth();
        int originH = icon.getIconHeight();

        int targetW = 300;
        int targetH = (originH * targetW) / originW;

        Image scaledImg = img.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImg);

        JLabel imageLabel = new JLabel(scaledIcon);
        imageLabel.setBounds(100, 250, targetW, targetH);
        panel.add(imageLabel);

        //로그인 버튼
        try {
            Font buttonFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"))
                    .deriveFont(Font.BOLD, 20f);

            JButton startButton = new JButton("사용자 로그인") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // 배경 색
                    if (getModel().isPressed()) {
                        g2.setColor(getBackground().darker());
                    } else {
                        g2.setColor(getBackground());
                    }
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30); // 둥근 배경
                    g2.dispose();
                    super.paintComponent(g);
                }

                @Override
                protected void paintBorder(Graphics g) {
                    g.setColor(getForeground());
                    g.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30); // 둥근 테두리
                }
            };

            startButton.setFont(buttonFont);
            startButton.setBounds(150, 550, 200, 60);
            startButton.setForeground(Color.WHITE);
            startButton.setBackground(Color.decode("#1B76C0"));
            startButton.setFocusPainted(false);
            startButton.setContentAreaFilled(false); // 기본 배경 안 그리도록

            panel.add(startButton);

            startButton.addActionListener(e -> {
                try {
                    LoginScreen login = new LoginScreen();
                    login.setVisible(true);
                    StartScreen.this.dispose();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "로그인 화면 생성 실패");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("버튼 폰트 로드 실패. 기본 폰트 사용됨.");
        }


        setVisible(true);
    }
}
