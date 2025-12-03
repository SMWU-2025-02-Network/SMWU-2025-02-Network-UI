package client.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class SeatPanel extends JPanel {
    private final int seatNumber;
    private final JLabel numberLabel;
    private final JLabel timerLabel;      // 잔여시간 또는 외출시간
    private Timer seatTimer;
    private Timer outTimer;
    private int remainingSeconds = 0;
    private int outRemainingSeconds = 0;

    public enum State { EMPTY, SELECTED, OCCUPIED, OUT, MINE }
    private State state = State.EMPTY;
    private State prevState = State.EMPTY;
    private boolean seatTimerWasRunning = false;

    private static Font ttfFont; // 폰트

    private boolean mine = false;   // 이 좌석이 내 좌석인지 여부

    public boolean isMine() {
        return mine;
    }

    public void setMine(boolean value) {
        this.mine = value;
    }

    static {
        try {
            ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
        } catch (Exception e) {
            e.printStackTrace();
            ttfFont = new Font("SansSerif", Font.PLAIN, 16);
        }
    }

    public SeatPanel(int number) {
        this.seatNumber = number;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setPreferredSize(new Dimension(80, 80));

        numberLabel = new JLabel(String.valueOf(number), SwingConstants.CENTER);
        numberLabel.setFont(ttfFont.deriveFont(Font.BOLD, 25f)); // 글씨체 변경
        add(numberLabel, BorderLayout.CENTER);

        timerLabel = new JLabel("", SwingConstants.CENTER);
        timerLabel.setFont(ttfFont.deriveFont(Font.PLAIN, 15f)); // 글씨체 변경
        add(timerLabel, BorderLayout.NORTH);

        setState(State.EMPTY);
    }

    public int getSeatNumber() { return seatNumber; }
    public State getState() { return state; }
    public int getRemainingSeconds() { return remainingSeconds; }
    public int getOutRemainingSeconds() { return outRemainingSeconds; }
    public boolean isOutActive() { return outTimer != null && outTimer.isRunning(); }

    public void setState(State s) {
        this.state = s;
        switch (s) {
            case EMPTY -> setBackground(Color.decode("#d6d6d6"));
            case SELECTED -> setBackground(Color.decode("#fe6b6b"));
            case OCCUPIED -> setBackground(Color.decode("#66b7fe"));
            case OUT -> setBackground(Color.decode("#fdfd9a"));
            case MINE -> setBackground(Color.decode("#aefe75"));
        }
        repaint();
    }

    public void setSelected() { setState(State.SELECTED); }

    public void setMineWithRemaining(int seconds) {
        this.mine = true;
        stopSeatTimer();
        remainingSeconds = seconds;
        setState(State.MINE);
        startSeatTimer(remainingSeconds);
    }

    public void setMineDefault() {
        this.mine = true;
        setMineWithRemaining(2 * 60 * 60); }

    private void startSeatTimer(int seconds) {
        remainingSeconds = seconds;
        if (seatTimer != null) seatTimer.stop();

        seatTimer = new Timer(1000, e -> {
            if (remainingSeconds <= 0) {
                seatTimer.stop();
                seatTimer = null;
                resetSeat();
            } else {
                timerLabel.setText(formatTime(remainingSeconds));
                remainingSeconds--;
            }
        });
        seatTimer.setInitialDelay(0);
        seatTimer.start();
    }

    private void stopSeatTimer() {
        if (seatTimer != null) {
            seatTimer.stop();
            seatTimer = null;
        }
    }

    public void startOutTimer(int seconds) {
        outRemainingSeconds = seconds;
        prevState = this.state;
        setState(State.OUT);
        seatTimerWasRunning = (seatTimer != null);
        if (seatTimer != null) { seatTimer.stop(); seatTimer = null; }

        if (outTimer != null) outTimer.stop();

        outTimer = new Timer(1000, e -> {
            if (outRemainingSeconds <= 0) {
                outTimer.stop();
                outTimer = null;
                outRemainingSeconds = 0;
                SwingUtilities.invokeLater(() -> {
                    timerLabel.setText("");
                    if (prevState == State.MINE) {
                        setState(State.MINE);
                        if (seatTimerWasRunning && remainingSeconds > 0) startSeatTimer(remainingSeconds);
                    } else if (prevState == State.OCCUPIED) setState(State.OCCUPIED);
                    else if (prevState == State.SELECTED) setState(State.SELECTED);
                    else setState(State.EMPTY);
                    seatTimerWasRunning = false;
                    prevState = State.EMPTY;
                });
            } else {
                timerLabel.setText("외출: " + formatTime(outRemainingSeconds));
                outRemainingSeconds--;
            }
        });
        outTimer.setInitialDelay(0);
        outTimer.start();
    }


    public void stopOutTimer() {
        if (outTimer != null) { outTimer.stop(); outTimer = null; }
        outRemainingSeconds = 0;
        timerLabel.setText("");

        if (prevState == State.MINE) {
            setState(State.MINE);
            if (seatTimerWasRunning && remainingSeconds > 0) startSeatTimer(remainingSeconds);
        } else if (prevState == State.OCCUPIED) setState(State.OCCUPIED);
        else if (prevState == State.SELECTED) setState(State.SELECTED);
        else setState(State.EMPTY);

        seatTimerWasRunning = false;
        prevState = State.EMPTY;
    }

    public void resetSeat() {
        this.mine = false;
        stopSeatTimer();
        if (outTimer != null) { outTimer.stop(); outTimer = null; }
        remainingSeconds = 0;
        outRemainingSeconds = 0;
        timerLabel.setText("");
        setState(State.EMPTY);
        seatTimerWasRunning = false;
        prevState = State.EMPTY;
    }

    private static String formatTime(int secs) {
        int hrs = secs / 3600;
        int mins = (secs % 3600) / 60;
        int s = secs % 60;
        return String.format("%02d:%02d:%02d", hrs, mins, s);
    }

    public void startSharedTimer(int seconds) {
        // mine 플래그는 건드리지 않고, 단순히 잔여시간만 보여주기
        remainingSeconds = seconds;
        if (seatTimer != null) seatTimer.stop();

        seatTimer = new Timer(1000, e -> {
            if (remainingSeconds <= 0) {
                seatTimer.stop();
                seatTimer = null;
                // 여기서는 resetSeat() 까지 하지는 말고,
                // 서버에서 SEAT_UPDATE가 오면 그때 상태를 바꿔주는 식으로 써도 됨.
                timerLabel.setText("");
            } else {
                timerLabel.setText(formatTime(remainingSeconds));
                remainingSeconds--;
            }
        });
        seatTimer.setInitialDelay(0);
        seatTimer.start();
    }

}



