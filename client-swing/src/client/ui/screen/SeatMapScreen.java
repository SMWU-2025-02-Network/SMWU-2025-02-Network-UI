package client.ui.screen;

import client.ui.SeatPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

public class SeatMapScreen extends JPanel {
    private final ArrayList<SeatPanel> seats = new ArrayList<>();
    private final JLabel infoLabel;
    private final JButton confirmButton, outButton, homeButton, returnButton, changeButton;

    private SeatPanel selectedSeat = null;
    private SeatPanel mySeat = null;
    private boolean changeMode = false;

    private Font ttfFont; // 커스텀 폰트

    public SeatMapScreen() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 폰트
        try {
            ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
        } catch (Exception e) {
            e.printStackTrace();
            ttfFont = new Font("SansSerif", Font.PLAIN, 16);
        }

        JPanel seatGrid = new JPanel(new GridLayout(0, 5, 10, 10));
        seatGrid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(seatGrid, BorderLayout.CENTER);

        for (int i = 1; i <= 20; i++) {
            SeatPanel p = new SeatPanel(i);
            seats.add(p);
            seatGrid.add(p);

            p.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onSeatClicked(p);
                }
            });
        }

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        add(bottom, BorderLayout.SOUTH);

        infoLabel = new JLabel("선택 좌석: -", SwingConstants.CENTER);
        infoLabel.setFont(ttfFont.deriveFont(Font.BOLD, 25f)); // 폰트 변경
        bottom.add(infoLabel, BorderLayout.NORTH);

        JPanel btns = new JPanel();
        bottom.add(btns, BorderLayout.SOUTH);

        confirmButton = new JButton("선택완료");
        confirmButton.setFont(ttfFont.deriveFont(Font.BOLD, 25f));
        confirmButton.setEnabled(false);
        confirmButton.setBackground(Color.decode("#1B76C0"));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setOpaque(true);
        confirmButton.setContentAreaFilled(true);
        btns.add(confirmButton);

        outButton = new JButton("외출");
        outButton.setFont(ttfFont.deriveFont(Font.BOLD, 25f));
        outButton.setBackground(Color.decode("#ee7b4c"));
        outButton.setForeground(Color.WHITE);
        outButton.setOpaque(true);
        outButton.setContentAreaFilled(true);
        outButton.setVisible(false);
        btns.add(outButton);

        homeButton = new JButton("귀가");
        homeButton.setFont(ttfFont.deriveFont(Font.BOLD, 25f));
        homeButton.setBackground(Color.decode("#ee7b4c"));
        homeButton.setForeground(Color.WHITE);
        homeButton.setOpaque(true);
        homeButton.setContentAreaFilled(true);
        homeButton.setVisible(false);
        btns.add(homeButton);

        returnButton = new JButton("좌석반납");
        returnButton.setFont(ttfFont.deriveFont(Font.BOLD, 25f));
        returnButton.setBackground(Color.decode("#ee7b4c"));
        returnButton.setForeground(Color.WHITE);
        returnButton.setOpaque(true);
        returnButton.setContentAreaFilled(true);
        returnButton.setVisible(false);
        btns.add(returnButton);

        changeButton = new JButton("좌석변경");
        changeButton.setFont(ttfFont.deriveFont(Font.BOLD, 25f));
        changeButton.setBackground(Color.decode("#ee7b4c"));
        changeButton.setForeground(Color.WHITE);
        changeButton.setOpaque(true);
        changeButton.setContentAreaFilled(true);
        changeButton.setVisible(false);
        btns.add(changeButton);

        confirmButton.addActionListener(e -> onConfirm());
        outButton.addActionListener(e -> { if (mySeat!=null) mySeat.startOutTimer(60*60); });
        homeButton.addActionListener(e -> { if (mySeat!=null) mySeat.stopOutTimer(); });
        returnButton.addActionListener(e -> onReturn());
        changeButton.addActionListener(e -> enterChangeMode());
    }

    // 좌석 클릭
    public void onSeatClicked(SeatPanel p) {
        if (p.getState() != SeatPanel.State.EMPTY) return;

        boolean allowSelect = (mySeat == null) || changeMode;
        if (!allowSelect) return;

        if (selectedSeat != null) selectedSeat.resetSeat();

        selectedSeat = p;
        selectedSeat.setSelected();
        confirmButton.setEnabled(true);
        confirmButton.setVisible(true);

        confirmButton.setText(changeMode ? "좌석 변경 완료" : "선택완료");

        updateInfoLabel();
    }

    private void onConfirm() {
        if (selectedSeat == null) return;

        if (changeMode && mySeat != null) {
            int remaining = mySeat.getRemainingSeconds();
            int outRem = mySeat.getOutRemainingSeconds();
            boolean wasOut = mySeat.isOutActive();

            mySeat.resetSeat();

            mySeat = selectedSeat;
            mySeat.setMineWithRemaining(remaining);
            if (wasOut && outRem > 0) mySeat.startOutTimer(outRem);

            changeMode = false;
            selectedSeat = null;

            confirmButton.setVisible(false);
            confirmButton.setEnabled(false);
            outButton.setVisible(true);
            homeButton.setVisible(true);
            returnButton.setVisible(true);
            changeButton.setVisible(true);
            confirmButton.setText("선택완료");

            infoLabel.setText("이용중 좌석: " + mySeat.getSeatNumber());
        } else {
            mySeat = selectedSeat;
            mySeat.setMineDefault();
            selectedSeat = null;

            confirmButton.setVisible(false);
            confirmButton.setEnabled(false);
            outButton.setVisible(true);
            homeButton.setVisible(true);
            returnButton.setVisible(true);
            changeButton.setVisible(true);

            infoLabel.setText("이용중 좌석: " + mySeat.getSeatNumber());
        }
    }

    private void onReturn() {
        if (mySeat == null) return;
        mySeat.resetSeat();
        mySeat = null;
        selectedSeat = null;

        infoLabel.setText("선택 좌석: -");
        confirmButton.setVisible(true);
        confirmButton.setEnabled(false);
        confirmButton.setText("선택완료");
        outButton.setVisible(false);
        homeButton.setVisible(false);
        returnButton.setVisible(false);
        changeButton.setVisible(false);
    }

    private void enterChangeMode() {
        if (mySeat == null) return;
        changeMode = true;
        infoLabel.setText("이용중 좌석: " + mySeat.getSeatNumber() + " / 선택 좌석: -");

        confirmButton.setVisible(true);
        confirmButton.setEnabled(false);
        confirmButton.setText("좌석 변경 완료");
        outButton.setVisible(false);
        homeButton.setVisible(false);
        returnButton.setVisible(false);
        changeButton.setVisible(false);
    }

    private void updateInfoLabel() {
        if (changeMode && mySeat != null && selectedSeat != null) {
            infoLabel.setText("이용중 좌석: " + mySeat.getSeatNumber()
                    + " / 선택 좌석: " + selectedSeat.getSeatNumber());
        } else if (mySeat != null) {
            infoLabel.setText("이용중 좌석: " + mySeat.getSeatNumber());
        } else if (selectedSeat != null) {
            infoLabel.setText("선택 좌석: " + selectedSeat.getSeatNumber());
        } else {
            infoLabel.setText("선택 좌석: -");
        }
    }
}



