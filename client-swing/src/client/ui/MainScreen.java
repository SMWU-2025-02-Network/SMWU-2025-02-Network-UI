package client.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class MainScreen extends JFrame {
    private CardLayout cardLayout;
    private JPanel centerPanel;

    private ChatScreen chatScreen;
    private DashboardScreen dashScreen;
    private SeatMapScreen seatScreen;

    private final String selectedFloor;
    private JLabel floorTitle; // <-- 인스턴스 필드로 변경

    public MainScreen(String selectedFloor) {
        this.selectedFloor = selectedFloor;

        setTitle("Netlibrary - 메인화면");
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
            FloorSelectionScreen floorScreen = new FloorSelectionScreen();
            floorScreen.setVisible(true);
            dispose();
        });
        topBar.add(backBtn, BorderLayout.WEST);

        // 상단바 제목 (인스턴스 변수로)
        floorTitle = new JLabel(selectedFloor + " 대화방", SwingConstants.CENTER);
        floorTitle.setForeground(Color.BLACK);
        try {
            Font ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
            floorTitle.setFont(ttfFont.deriveFont(Font.BOLD, 24f));
        } catch (Exception ex) {
            floorTitle.setFont(new Font("SansSerif", Font.BOLD, 19));
            ex.printStackTrace();
        }
        topBar.add(floorTitle, BorderLayout.CENTER);
        topBar.add(Box.createHorizontalStrut(backBtn.getPreferredSize().width), BorderLayout.EAST);

        // 중앙 - CardLayout
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        root.add(centerPanel, BorderLayout.CENTER);

        chatScreen = new ChatScreen();
        dashScreen = new DashboardScreen();
        seatScreen = new SeatMapScreen();

        centerPanel.add(chatScreen, "CHAT");
        centerPanel.add(dashScreen, "DASH");
        centerPanel.add(seatScreen, "SEAT");

        cardLayout.show(centerPanel, "CHAT");

        // 하단 바
        JPanel bottomBar = new JPanel(null);
        bottomBar.setBackground(Color.decode("#DBDBDB"));
        bottomBar.setPreferredSize(new Dimension(0, 60));
        bottomBar.setBorder(new EmptyBorder(5, 5, 5, 5));
        root.add(bottomBar, BorderLayout.SOUTH);

        String[] btnNames = {"대화방", "대시보드", "좌석"};
        int[] btnX = {45, 195, 345};

        for (int i = 0; i < btnNames.length; i++) {
            JButton btn = new JButton(btnNames[i]);
            btn.setBounds(btnX[i], 5, 100, 50);
            btn.setBackground(Color.decode("#1B76C0"));
            btn.setForeground(Color.WHITE);
            try {
                Font ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
                btn.setFont(ttfFont.deriveFont(Font.BOLD, 20f));
            } catch (Exception ex) {
                btn.setFont(new Font("SansSerif", Font.BOLD, 15));
            }
            bottomBar.add(btn);

            int idx = i;
            btn.addActionListener(e -> {
                if (idx == 0) cardLayout.show(centerPanel, "CHAT");
                else if (idx == 1) cardLayout.show(centerPanel, "DASH");
                else cardLayout.show(centerPanel, "SEAT");

                //제목 변경
                switch (idx) {
                    case 0 -> floorTitle.setText(selectedFloor + " 대화방");
                    case 1 -> floorTitle.setText(selectedFloor + " 대시보드");
                    case 2 -> floorTitle.setText(selectedFloor + " 좌석");
                }
            });
        }
    }
}
