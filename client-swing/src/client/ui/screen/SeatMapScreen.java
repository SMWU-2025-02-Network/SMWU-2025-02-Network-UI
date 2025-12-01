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

    private Font ttfFont; // 커스텀 폰트

    // 소켓 & 사용자 & 위치 정보 추가
    private final SocketClient socketClient;
    private final String userId;
    private final int floor;
    private final String room;

    // 생성자 변경: 네트워크 정보 주입
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
            e.printStackTrace();
            ttfFont = new Font("SansSerif", Font.PLAIN, 16);
        }

        // ====== 중앙 좌석 그리드 ======
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

        // ====== 하단 영역 ======
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
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

        // ====== 버튼 이벤트 ======

        // 좌석 선택 후 서버에 CHECKIN 전송
        confirmButton.addActionListener(e -> onConfirm());

        // 외출 시작 (AWAY_START)
        outButton.addActionListener(e -> onAwayStart());

        // 외출 복귀 (AWAY_BACK)
        homeButton.addActionListener(e -> onAwayBack());

        // 좌석 반납 (CHECKOUT)
        returnButton.addActionListener(e -> onReturn());

        // 좌석 변경 모드
        changeButton.addActionListener(e -> enterChangeMode());
    }

    // ================== 소켓과 연동되는 부분 ==================

    // 서버에서 오는 SEAT_UPDATE 적용
    // msg.getSeats() 타입에 맞춰서 제네릭 바꿔줘 (예: List<SocketMessage.SeatInfo>)
    public void applySeatUpdate(List<? /* 또는 SeatInfo 타입 */> seatInfos) {

        // 서버 DTO에 맞게 캐스팅해서 쓰면 됨 (여기서는 예시 타입 이름 SeatInfo로 가정)
        for (Object o : seatInfos) {
            // TODO: 실제 타입으로 캐스팅
            // 예: SocketMessage.SeatInfo info = (SocketMessage.SeatInfo) o;
            var info = (SocketMessage.SeatInfo) o; // 네 프로젝트에 맞게 수정

            int seatNo = info.getSeatNo();     // 1~20
            String status = info.getStatus();  // "EMPTY", "IN_USE", "AWAY"
            String seatUserId = info.getUserId(); // null or userId
            Integer remainSec = info.getRemainSeconds(); // 남은 시간 (있으면)

            if (seatNo < 1 || seatNo > seats.size()) continue;
            SeatPanel seatPanel = seats.get(seatNo - 1);

            // 기본 초기화
            seatPanel.resetSeat();

            if ("EMPTY".equals(status)) {
                // 비어있는 좌석
                continue;
            }

            // 내가 쓰는 좌석인지 확인
            boolean isMine = (seatUserId != null && seatUserId.equals(userId));

            if ("IN_USE".equals(status)) {
                if (isMine) {
                    mySeat = seatPanel;
                    if (remainSec != null && remainSec > 0) {
                        seatPanel.setMineWithRemaining(remainSec);
                    } else {
                        seatPanel.setMineDefault();
                    }
                } else {
                    seatPanel.setOccupied(); // 다른 사람 자리
                }
            } else if ("AWAY".equals(status)) {
                if (isMine) {
                    mySeat = seatPanel;
                    if (remainSec != null && remainSec > 0) {
                        // SeatPanel에서 외출 상태 표시 + 타이머 있는 버전으로 처리
                        seatPanel.setMineWithRemaining(remainSec);
                        seatPanel.startOutTimer(remainSec);
                    } else {
                        seatPanel.setMineDefault();
                    }
                } else {
                    seatPanel.setAway(); // 남의 외출좌석 (필요하면 색 다르게)
                }
            }
        }

        // infoLabel 갱신
        updateInfoLabel();
    }

    // ================== 좌석 클릭/버튼 로직 ==================

    // 좌석 클릭
    public void onSeatClicked(SeatPanel p) {
        // 이미 다른 사람이 쓰는 자리면 선택 불가
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

    // 🔥 좌석 선택 완료 → 서버에 CHECKIN (또는 변경이면 CHECKOUT + CHECKIN)
    private void onConfirm() {
        if (selectedSeat == null) return;

        int seatNo = selectedSeat.getSeatNumber();

        if (changeMode && mySeat != null) {
            // 기존 좌석 반납 + 새 좌석 체크인
            int oldSeatNo = mySeat.getSeatNumber();

            // 1) 기존 좌석 CHECKOUT
            socketClient.send(SocketMessage.builder()
                    .type("CHECKOUT")
                    .floor(floor)
                    .room(room)
                    .seatNo(oldSeatNo)
                    .userId(userId)
                    .build());

            // 2) 새 좌석 CHECKIN
            socketClient.send(SocketMessage.builder()
                    .type("CHECKIN")
                    .floor(floor)
                    .room(room)
                    .seatNo(seatNo)
                    .userId(userId)
                    .build());

            changeMode = false;
            selectedSeat = null;

            confirmButton.setVisible(false);
            confirmButton.setEnabled(false);
            confirmButton.setText("선택완료");

        } else {
            // 일반 CHECKIN
            socketClient.send(SocketMessage.builder()
                    .type("CHECKIN")
                    .floor(floor)
                    .room(room)
                    .seatNo(seatNo)
                    .userId(userId)
                    .build());

            selectedSeat = null;
            confirmButton.setVisible(false);
            confirmButton.setEnabled(false);
        }

        // 실제 좌석 반영은 서버에서 SEAT_UPDATE 오면 applySeatUpdate()에서 처리
    }

    // 🔥 외출 시작 (AWAY_START)
    private void onAwayStart() {
        if (mySeat == null) return;
        int seatNo = mySeat.getSeatNumber();

        socketClient.send(SocketMessage.builder()
                .type("AWAY_START")
                .floor(floor)
                .room(room)
                .seatNo(seatNo)
                .userId(userId)
                .build());
    }

    // 🔥 외출 복귀 (AWAY_BACK)
    private void onAwayBack() {
        if (mySeat == null) return;
        int seatNo = mySeat.getSeatNumber();

        socketClient.send(SocketMessage.builder()
                .type("AWAY_BACK")
                .floor(floor)
                .room(room)
                .seatNo(seatNo)
                .userId(userId)
                .build());
    }

    // 🔥 좌석 반납 (CHECKOUT)
    private void onReturn() {
        if (mySeat == null) return;
        int seatNo = mySeat.getSeatNumber();

        socketClient.send(SocketMessage.builder()
                .type("CHECKOUT")
                .floor(floor)
                .room(room)
                .seatNo(seatNo)
                .userId(userId)
                .build());

        // 실제 mySeat 초기화는 SEAT_UPDATE 이후 applySeatUpdate에서 처리
        selectedSeat = null;
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
