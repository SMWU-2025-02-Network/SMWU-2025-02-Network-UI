package client.ui.screen;
//관리자 전용 좌석 화면

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

public class ManagerSeatMapScreen extends JPanel {

    private final List<SeatPanel> seats = new ArrayList<>();
    private final JLabel infoLabel;
    private Font ttfFont;

    private final SocketClient socketClient;
    private final int floor;
    private final String room;

    public ManagerSeatMapScreen(SocketClient socketClient, int floor, String room) {
        this.socketClient = socketClient;
        this.floor = floor;
        this.room = room;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initFont();

        // ===== 좌석 그리드 =====
        JPanel seatGrid = initSeatGrid();
        add(seatGrid, BorderLayout.CENTER);

        // ===== 하단 정보 라벨 =====
        infoLabel = new JLabel("좌석을 클릭하면 상세 상태를 볼 수 있습니다.", SwingConstants.CENTER);
        infoLabel.setFont(ttfFont.deriveFont(Font.BOLD, 22f));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(infoLabel, BorderLayout.SOUTH);
    }

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

            // 클릭하면 상태/남은시간만 표시 (선택, 체크인 없음)
            p.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onSeatClicked(p);
                }
            });
        }

        return seatGrid;
    }

    // ───────────────── 좌석 클릭 시 상세 상태 표시 ─────────────────
    private void onSeatClicked(SeatPanel p) {
        StringBuilder sb = new StringBuilder();
        sb.append("좌석 ").append(p.getSeatNumber()).append(" : ");

        switch (p.getState()) {
            case EMPTY:
                sb.append("비어 있음");
                break;
            case OCCUPIED:
                sb.append("사용 중");
                break;
            case OUT:
                sb.append("외출 중");
                break;
            default:
                sb.append("알 수 없음");
        }

        int remainUse = p.getRemainingSeconds();       // 전체 사용 남은 시간
        int remainAway = p.getOutRemainingSeconds();   // 외출 남은 시간
        boolean awayActive = p.isOutActive();

        if (remainUse > 0) {
            sb.append(" / 남은 사용시간: ").append(formatSeconds(remainUse));
        }
        if (awayActive && remainAway > 0) {
            sb.append(" / 외출 남은시간: ").append(formatSeconds(remainAway));
        }

        infoLabel.setText(sb.toString());
    }

    private String formatSeconds(int sec) {
        int m = sec / 60;
        int s = sec % 60;
        return String.format("%d분 %02d초", m, s);
    }

    // ───────────────── 서버 → UI 좌석 업데이트 (SEAT_UPDATE) ─────────────────
    public void applySeatUpdate(List<SocketMessage.SeatInfo> seatInfos) {
        // 전체 초기화
        for (SeatPanel sp : seats) {
            sp.resetSeat();
        }

        int useCnt = 0;
        int awayCnt = 0;

        for (SocketMessage.SeatInfo info : seatInfos) {
            int seatNo = info.getSeatNo();
            if (seatNo < 1 || seatNo > seats.size()) continue;

            SeatPanel sp = seats.get(seatNo - 1);

            String state = info.getState();          // "EMPTY", "IN_USE", "AWAY"
            Integer remain = info.getRemainSeconds();

            if ("EMPTY".equals(state)) {
                continue;
            }

            if ("IN_USE".equals(state)) {
                useCnt++;
                sp.setState(SeatPanel.State.OCCUPIED);
                if (remain != null && remain > 0) {
                    sp.startSharedTimer(remain);   // 남은 사용시간 타이머
                }
            } else if ("AWAY".equals(state)) {
                awayCnt++;
                int awaySec = (remain != null ? remain : 60 * 60);
                sp.setState(SeatPanel.State.OCCUPIED); // SeatPanel이 OUT 상태 내부에서 관리
                sp.startOutTimer(awaySec);             // 외출 남은시간 타이머
            }
        }

        // 하단 라벨에 전체 요약도 같이 보여주기
        infoLabel.setText(
                String.format("전체 %d석 / 사용 중 %d석 / 외출 %d석",
                        seats.size(), useCnt, awayCnt)
        );
    }
}
