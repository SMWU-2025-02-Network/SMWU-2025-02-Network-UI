package client.ui.screen;
//사용자 전용 좌석 화면

import client.socket.SocketClient;
import client.socket.SocketMessage;
import client.ui.SeatPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SeatMapScreen extends JPanel {

    // 좌석 UI
    private final List<SeatPanel> seats = new ArrayList<>();
    private final JLabel infoLabel;
    private final JButton confirmButton;
    private final JButton outButton;
    private final JButton homeButton;
    private final JButton returnButton;
    private final JButton changeButton;

    private SeatPanel selectedSeat = null;
    private SeatPanel mySeat = null;
    private boolean changeMode = false;

    private Font ttfFont; // 커스텀 폰트

    // 소켓 관련
    private final SocketClient socketClient;
    private final String userId;
    private final int floor;
    private final String room;

    // ───────────────── 생성자 ─────────────────
    public SeatMapScreen(SocketClient socketClient, String userId, int floor, String room) {
        this.socketClient = socketClient;
        this.userId = userId;
        this.floor = floor;
        this.room = room;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initFont();

        // ===== 좌석 그리드 =====
        JPanel seatGrid = initSeatGrid();
        add(seatGrid, BorderLayout.CENTER);

        // ===== 하단 패널 =====
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(bottom, BorderLayout.SOUTH);

        infoLabel = new JLabel("선택 좌석: -", SwingConstants.CENTER);
        infoLabel.setFont(ttfFont.deriveFont(Font.BOLD, 25f));
        bottom.add(infoLabel, BorderLayout.NORTH);

        JPanel btns = new JPanel();
        bottom.add(btns, BorderLayout.SOUTH);

        // 버튼들 (UI 스타일 그대로 유지)
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

        // 버튼 리스너 (UI 로직은 그대로, + 소켓 메시지 전송 추가)
        confirmButton.addActionListener(e -> onConfirm());
        outButton.addActionListener(e -> onAwayStart());
        homeButton.addActionListener(e -> onAwayBack());
        returnButton.addActionListener(e -> onReturn());
        changeButton.addActionListener(e -> enterChangeMode());
    }

    // ───────────────── 초기 설정 ─────────────────

    private void initFont() {
        try {
            ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
        } catch (Exception e) {
            e.printStackTrace();
            ttfFont = new Font("SansSerif", Font.PLAIN, 16);
        }
    }

    private JPanel initSeatGrid() {
        JPanel seatGrid = new JPanel(new GridLayout(0, 5, 10, 10));
        seatGrid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

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

        return seatGrid;
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

    // ───────────────── 서버 → UI 좌석 업데이트 (SEAT_UPDATE) ─────────────────
    public void applySeatUpdate(List<SocketMessage.SeatInfo> seatInfos) {
        // 전체 초기화
        for (SeatPanel sp : seats) {
            sp.resetSeat();
        }
        mySeat = null;

        for (SocketMessage.SeatInfo info : seatInfos) {
            int seatNo = info.getSeatNo();
            if (seatNo < 1 || seatNo > seats.size()) continue;

            SeatPanel sp = seats.get(seatNo - 1);

            String state = info.getState();
            String uid = info.getUserId();
            Integer remain = info.getRemainSeconds();

            boolean isMine = userId.equals(uid);

            if ("EMPTY".equals(state)) {
                continue;
            }

            if ("IN_USE".equals(state)) {
                if (isMine) {
                    mySeat = sp;
                    if (remain != null) sp.setMineWithRemaining(remain);
                    else sp.setMineDefault();
                } else {
                    sp.setState(SeatPanel.State.OCCUPIED);
                    if (remain != null && remain > 0) {
                        sp.startSharedTimer(remain);   // 다른 사람 좌석도 남은 시간 표시
                    }
                }
            } else if ("AWAY".equals(state)) {
                int awaySec = (remain != null ? remain : 60 * 60);
                if (isMine) {
                    mySeat = sp;
                    sp.startOutTimer(awaySec);
                } else {
                    sp.setState(SeatPanel.State.OCCUPIED); // prevState로 저장됨
                    sp.startOutTimer(awaySec);              // 외출 남은시간 모두에게 표시
                }
            }
        }


        // 서버 상태 기준으로 버튼/라벨 동기화
        if (mySeat != null) {
            selectedSeat = null;
            changeMode = false;

            confirmButton.setVisible(false);
            confirmButton.setEnabled(false);
            confirmButton.setText("선택완료");

            outButton.setVisible(true);
            homeButton.setVisible(true);
            returnButton.setVisible(true);
            changeButton.setVisible(true);
        } else {
            selectedSeat = null;
            changeMode = false;

            infoLabel.setText("선택 좌석: -");
            confirmButton.setVisible(true);
            confirmButton.setEnabled(false);
            confirmButton.setText("선택완료");

            outButton.setVisible(false);
            homeButton.setVisible(false);
            returnButton.setVisible(false);
            changeButton.setVisible(false);
        }

        updateInfoLabel();
    }

    // ───────────────── 좌석 클릭 ─────────────────

    public void onSeatClicked(SeatPanel p) {
        // 내 자리가 아닌데 이미 사용중/외출이면: 상태만 보여주고 리턴
        if (p.getState() != SeatPanel.State.EMPTY && !p.isMine()) {
            if (p.getState() == SeatPanel.State.OUT) {
                infoLabel.setText("외출 중 좌석: " + p.getSeatNumber());
            } else {
                infoLabel.setText("이용 중 좌석: " + p.getSeatNumber());
            }
            // 버튼은 그대로 (내 자리 기준으로만 보이므로 변경 없음)
            return;
        }

        // 내 좌석이 이미 있고, 변경 모드가 아니라면 선택 불가
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

    // ───────────────── 선택 완료 (CHECKIN / 좌석 변경) ─────────────────

    private void onConfirm() {
        if (selectedSeat == null) return;

        int newSeatNo = selectedSeat.getSeatNumber();

        // ───────── 좌석 변경 모드일 때 ─────────
        if (changeMode && mySeat != null) {
            // 1) 서버에 기존 좌석 CHECKOUT 전송
            SocketMessage outMsg = new SocketMessage();
            outMsg.setType("CHECKOUT");
            outMsg.setFloor(floor);
            outMsg.setRoom(room);
            outMsg.setSeatNo(mySeat.getSeatNumber());
            outMsg.setUserId(userId);
            socketClient.send(outMsg);

            // 2) 서버에 새 좌석 CHECKIN 전송
            SocketMessage inMsg = new SocketMessage();
            inMsg.setType("CHECKIN");
            inMsg.setFloor(floor);
            inMsg.setRoom(room);
            inMsg.setSeatNo(newSeatNo);
            inMsg.setUserId(userId);
            socketClient.send(inMsg);

            // 3) UI 는 “바로 변경된 것처럼” 처리
            int remaining = mySeat.getRemainingSeconds();
            int outRem    = mySeat.getOutRemainingSeconds();
            boolean wasOut = mySeat.isOutActive();

            mySeat.resetSeat();

            mySeat = selectedSeat;
            if (remaining > 0) mySeat.setMineWithRemaining(remaining);
            else mySeat.setMineDefault();

            if (wasOut && outRem > 0) {
                mySeat.startOutTimer(outRem);
            }

            changeMode   = false;
            selectedSeat = null;

            confirmButton.setVisible(false);
            confirmButton.setEnabled(false);
            confirmButton.setText("선택완료");

            outButton.setVisible(true);
            homeButton.setVisible(true);
            returnButton.setVisible(true);
            changeButton.setVisible(true);

            infoLabel.setText("이용중 좌석: " + mySeat.getSeatNumber());
        }

        // ───────── 처음 체크인 할 때 ─────────
        else {
            SocketMessage msg = new SocketMessage();
            msg.setType("CHECKIN");
            msg.setFloor(floor);
            msg.setRoom(room);
            msg.setSeatNo(newSeatNo);
            msg.setUserId(userId);
            socketClient.send(msg);

            // ★ 여기서 바로 내 자리로 확정
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


    // ───────────────── 외출 시작 / 복귀 ─────────────────

    private void onAwayStart() {
        if (mySeat == null) return;

        SocketMessage msg = new SocketMessage();
        msg.setType("AWAY_START");
        msg.setFloor(floor);
        msg.setRoom(room);
        msg.setSeatNo(mySeat.getSeatNumber());
        msg.setUserId(userId);
        socketClient.send(msg);

        mySeat.startOutTimer(60 * 60);
    }

    private void onAwayBack() {
        if (mySeat == null) return;

        SocketMessage msg = new SocketMessage();
        msg.setType("AWAY_BACK");
        msg.setFloor(floor);
        msg.setRoom(room);
        msg.setSeatNo(mySeat.getSeatNumber());
        msg.setUserId(userId);
        socketClient.send(msg);
        mySeat.stopOutTimer();
    }

    // ───────────────── 좌석 반납 (CHECKOUT) ─────────────────

    private void onReturn() {
        if (mySeat == null) return;

        SocketMessage msg = new SocketMessage();
        msg.setType("CHECKOUT");
        msg.setFloor(floor);
        msg.setRoom(room);
        msg.setSeatNo(mySeat.getSeatNumber());
        msg.setUserId(userId);
        socketClient.send(msg);

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

    // ───────────────── 좌석 변경 모드 ─────────────────

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

    // ───────────────── 라벨 업데이트 ─────────────────

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

    public void handleCheckinError() {
        // 방금 처음 체크인 시도한 케이스만 고려 (changeMode=false)
        if (!changeMode && mySeat != null && mySeat.isMine()) {
            mySeat.resetSeat();
            mySeat = null;
            selectedSeat = null;

            confirmButton.setVisible(true);
            confirmButton.setEnabled(false);
            confirmButton.setText("선택완료");

            outButton.setVisible(false);
            homeButton.setVisible(false);
            returnButton.setVisible(false);
            changeButton.setVisible(false);

            infoLabel.setText("선택 좌석: -");
        }
    }
}
