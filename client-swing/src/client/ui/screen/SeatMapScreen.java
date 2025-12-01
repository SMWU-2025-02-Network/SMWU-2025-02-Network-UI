package client.ui.screen;

import client.socket.SocketClient;
import client.socket.SocketMessage;
import client.ui.SeatPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SeatMapScreen extends JPanel {

    private final ArrayList<SeatPanel> seats = new ArrayList<>();
    private final JLabel infoLabel;
    private final JButton confirmButton, outButton, homeButton, returnButton, changeButton;

    private SeatPanel selectedSeat = null;
    private SeatPanel mySeat = null;
    private boolean changeMode = false;

    private Font ttfFont;

    // 소켓 정보
    private final SocketClient socketClient;
    private final String userId;
    private final int floor;
    private final String room;

    public SeatMapScreen(SocketClient socketClient, String userId, int floor, String room) {
        this.socketClient = socketClient;
        this.userId = userId;
        this.floor = floor;
        this.room = room;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 폰트
        try {
            ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
        } catch (Exception e) {
            ttfFont = new Font("SansSerif", Font.PLAIN, 16);
        }

        // ====================== 좌석 그리드 ======================
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

        // ====================== 하단 패널 ======================
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(bottom, BorderLayout.SOUTH);

        infoLabel = new JLabel("선택 좌석: -", SwingConstants.CENTER);
        infoLabel.setFont(ttfFont.deriveFont(Font.BOLD, 25f));
        bottom.add(infoLabel, BorderLayout.NORTH);

        JPanel btns = new JPanel();
        bottom.add(btns, BorderLayout.SOUTH);

        confirmButton = styledButton("선택완료", "#1B76C0");
        confirmButton.setEnabled(false);
        btns.add(confirmButton);

        outButton = styledButton("외출", "#ee7b4c");
        outButton.setVisible(false);
        btns.add(outButton);

        homeButton = styledButton("귀가", "#ee7b4c");
        homeButton.setVisible(false);
        btns.add(homeButton);

        returnButton = styledButton("좌석반납", "#ee7b4c");
        returnButton.setVisible(false);
        btns.add(returnButton);

        changeButton = styledButton("좌석변경", "#ee7b4c");
        changeButton.setVisible(false);
        btns.add(changeButton);

        // 버튼 이벤트
        confirmButton.addActionListener(e -> onConfirm());
        outButton.addActionListener(e -> onAwayStart());
        homeButton.addActionListener(e -> onAwayBack());
        returnButton.addActionListener(e -> onReturn());
        changeButton.addActionListener(e -> enterChangeMode());
    }


    private JButton styledButton(String text, String colorHex) {
        JButton btn = new JButton(text);
        btn.setFont(ttfFont.deriveFont(Font.BOLD, 25f));
        btn.setBackground(Color.decode(colorHex));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        return btn;
    }



    // ============================================================
    //  서버 → UI 좌석 업데이트 (SEAT_UPDATE)
    // ============================================================
    public void applySeatUpdate(List<SocketMessage.SeatInfo> seatInfos) {
        // 전체 초기화
        for (SeatPanel sp : seats) sp.resetSeat();
        mySeat = null;

        for (SocketMessage.SeatInfo info : seatInfos) {
            SeatPanel sp = seats.get(info.getSeatNo() - 1);

            String state = info.getState();
            String uid = info.getUserId();
            Integer remain = info.getRemainSeconds();

            boolean isMine = userId.equals(uid);

            //비었을 때
            if ("EMPTY".equals(state)) {
                continue;
            }

            //사용중
            if ("IN_USE".equals(state)) {
                if (isMine) {
                    mySeat = sp;
                    if (remain != null) sp.setMineWithRemaining(remain);
                    else sp.setMineDefault();
                } else {
                    sp.setState(SeatPanel.State.OCCUPIED);
                }
            }

            //외출
            if ("AWAY".equals(state)) {
                int awaySec = (remain != null ? remain : 60 * 60);
                //내 자리 + 외출
                if (isMine) {
                    mySeat = sp;
                    sp.startOutTimer(remain);
                }
                //다른 사람 + 외출
                else {
                    sp.setState(SeatPanel.State.OCCUPIED);
                    sp.startOutTimer(awaySec);
                }
            }
        }

        updateInfoLabel();
    }


    // ============================================================
    //  좌석 선택
    // ============================================================
    private void onSeatClicked(SeatPanel p) {
        // 다른사람 자리 클릭 불가
        if (p.getState() != SeatPanel.State.EMPTY) return;

        // 내 좌석 존재 + 변경 모드가 아니면 선택 불가
        if (mySeat != null && !changeMode) return;

        if (selectedSeat != null) selectedSeat.resetSeat();

        selectedSeat = p;
        selectedSeat.setSelected();

        confirmButton.setVisible(true);
        confirmButton.setEnabled(true);

        confirmButton.setText(changeMode ? "좌석 변경 완료" : "선택완료");

        updateInfoLabel();
    }


    // ============================================================
    //  좌석 선택 완료 → CHECKIN / 좌석 변경
    // ============================================================
    private void onConfirm() {
        if (selectedSeat == null) return;

        int seatNo = selectedSeat.getSeatNumber();

        if (changeMode && mySeat != null) {
            // 기존 좌석 CHECKOUT
            SocketMessage outMsg = new SocketMessage();
            outMsg.setType("CHECKOUT");
            outMsg.setFloor(floor);
            outMsg.setRoom(room);
            outMsg.setSeatNo(mySeat.getSeatNumber());
            outMsg.setUserId(userId);
            socketClient.send(outMsg);

            // 새 좌석 CHECKIN
            SocketMessage inMsg = new SocketMessage();
            inMsg.setType("CHECKIN");
            inMsg.setFloor(floor);
            inMsg.setRoom(room);
            inMsg.setSeatNo(seatNo);
            inMsg.setUserId(userId);
            socketClient.send(inMsg);

            changeMode = false;
        } else {
            SocketMessage msg = new SocketMessage();
            msg.setType("CHECKIN");
            msg.setFloor(floor);
            msg.setRoom(room);
            msg.setSeatNo(seatNo);
            msg.setUserId(userId);
            socketClient.send(msg);
        }

        selectedSeat = null;
        confirmButton.setVisible(false);
        confirmButton.setEnabled(false);
    }


    // ============================================================
    //  외출 시작
    // ============================================================
    private void onAwayStart() {
        if (mySeat == null) return;

        SocketMessage msg = new SocketMessage();
        msg.setType("AWAY_START");
        msg.setFloor(floor);
        msg.setRoom(room);
        msg.setSeatNo(mySeat.getSeatNumber());
        msg.setUserId(userId);

        socketClient.send(msg);
    }


    // ============================================================
    //  외출 복귀
    // ============================================================
    private void onAwayBack() {
        if (mySeat == null) return;

        SocketMessage msg = new SocketMessage();
        msg.setType("AWAY_BACK");
        msg.setFloor(floor);
        msg.setRoom(room);
        msg.setSeatNo(mySeat.getSeatNumber());
        msg.setUserId(userId);

        socketClient.send(msg);
    }


    // ============================================================
    //  좌석 반납
    // ============================================================
    private void onReturn() {
        if (mySeat == null) return;

        SocketMessage msg = new SocketMessage();
        msg.setType("CHECKOUT");
        msg.setFloor(floor);
        msg.setRoom(room);
        msg.setSeatNo(mySeat.getSeatNumber());
        msg.setUserId(userId);

        socketClient.send(msg);

        selectedSeat = null;
    }


    // ============================================================
    //  좌석 변경 모드
    // ============================================================
    private void enterChangeMode() {
        if (mySeat == null) return;

        changeMode = true;
        selectedSeat = null;

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
