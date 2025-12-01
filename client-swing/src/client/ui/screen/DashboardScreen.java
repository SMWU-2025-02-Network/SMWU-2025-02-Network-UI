package client.ui.screen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class DashboardScreen extends JPanel {

    private final JPanel graphArea;
    private final JLabel topLabel;
    private final JLabel bottomLabel;
    private JLabel tempValueLabel;
    private JLabel lightValueLabel;
    private JLabel co2ValueLabel;

    private Font ttfFont;

    //  센서 히스토리 (최근 N개)
    private final List<Integer> tempHistory = new ArrayList<>();
    private final List<Integer> lightHistory = new ArrayList<>();
    private final List<Integer> co2History = new ArrayList<>();
    private static final int MAX_POINTS = 30;

    // 현재 그래프에 어떤 종류를 보여줄지
    private String currentType = "온도"; // "온도" / "조도" / "CO2"

    public DashboardScreen() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 폰트
        try {
            ttfFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/resources/omyupretty.ttf"));
        } catch (Exception e) {
            e.printStackTrace();
            ttfFont = new Font("SansSerif", Font.PLAIN, 18);
        }

        // ───────── 상단 박스들 ─────────
        JPanel topBoxes = new JPanel(new GridLayout(1, 3, 10, 0));
        topBoxes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topBoxes.setBackground(Color.WHITE);

        JPanel tempBox = createTopBox("온도");
        JPanel lightBox = createTopBox("조도");
        JPanel co2Box = createTopBox("CO2");

        topBoxes.add(tempBox);
        topBoxes.add(lightBox);
        topBoxes.add(co2Box);

        add(topBoxes, BorderLayout.NORTH);

        // ───────── 그래프 영역 ─────────
        JPanel graphPanel = new JPanel(new BorderLayout());
        graphPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        graphPanel.setBackground(Color.WHITE);
        graphPanel.setPreferredSize(new Dimension(0, 420));

        topLabel = new JLabel("온도 변화 그래프", SwingConstants.CENTER);
        topLabel.setFont(ttfFont.deriveFont(Font.BOLD, 28f));
        topLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        graphPanel.add(topLabel, BorderLayout.NORTH);

        JPanel padded = new JPanel();
        padded.setLayout(new BoxLayout(padded, BoxLayout.Y_AXIS));
        padded.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        padded.setBackground(Color.WHITE);

        graphArea = new JPanel(new BorderLayout());
        graphArea.setBackground(Color.WHITE);
        graphArea.setPreferredSize(new Dimension(760, 320));
        graphArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        padded.add(graphArea);
        graphPanel.add(padded, BorderLayout.CENTER);

        bottomLabel = new JLabel("평균: -", SwingConstants.CENTER);
        bottomLabel.setFont(ttfFont.deriveFont(Font.PLAIN, 25f));
        bottomLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 20, 5));
        graphPanel.add(bottomLabel, BorderLayout.SOUTH);

        add(graphPanel, BorderLayout.CENTER);

        // ───────── 상단 박스 클릭 → 그래프 타입 변경 ─────────
        tempBox.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                currentType = "온도";
                updateGraphFromHistory();
            }
        });
        lightBox.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                currentType = "조도";
                updateGraphFromHistory();
            }
        });
        co2Box.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                currentType = "CO2";
                updateGraphFromHistory();
            }
        });

        // 초기 값 (데이터 없을 때는 0 표시만, 그래프는 비어있음)
        updateValues(0, 0, 0);
        updateGraphFromHistory();
    }

    // 상단 박스 하나 (온도/조도/CO2)
    private JPanel createTopBox(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.decode("#DBDBDB"));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(200, 120));

        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(ttfFont.deriveFont(Font.BOLD, 18f));
        panel.add(label, BorderLayout.NORTH);

        JLabel valueLabel = new JLabel("0 " + getUnitForType(title), SwingConstants.CENTER);
        valueLabel.setFont(ttfFont.deriveFont(Font.PLAIN, 25f));
        panel.add(valueLabel, BorderLayout.CENTER);

        switch (title) {
            case "온도" -> tempValueLabel = valueLabel;
            case "조도" -> lightValueLabel = valueLabel;
            case "CO2" -> co2ValueLabel = valueLabel;
        }

        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return panel;
    }

    // 서버에서 DASHBOARD_UPDATE 받을 때 호출할 메서드
    public void updateSensorData(double temp, double light, double co2) {
        SwingUtilities.invokeLater(() -> {
            // 1) 상단 값 업데이트
            updateValues(temp, light, co2);

            // 2) 히스토리에 누적
            addToHistory(tempHistory, (int) Math.round(temp));
            addToHistory(lightHistory, (int) Math.round(light));
            addToHistory(co2History, (int) Math.round(co2));

            // 3) 현재 선택된 타입 기준으로 그래프 갱신
            updateGraphFromHistory();
        });
    }

    // 상단 표시 값 업데이트 (실제 센서 값)
    public void updateValues(double temp, double light, double co2) {
        tempValueLabel.setText(String.format("%.1f %s", temp, getUnitForType("온도")));
        lightValueLabel.setText(String.format("%.0f %s", light, getUnitForType("조도")));
        co2ValueLabel.setText(String.format("%.0f %s", co2, getUnitForType("CO2")));
    }

    // history 리스트에 값 추가 (최대 MAX_POINTS만 유지)
    private void addToHistory(java.util.List<Integer> list, int value) {
        list.add(value);
        if (list.size() > MAX_POINTS) {
            list.remove(0);
        }
    }

    // 현재 currentType 기준으로 히스토리에서 데이터 꺼내서 그래프 갱신
    private void updateGraphFromHistory() {
        java.util.List<Integer> src;
        switch (currentType) {
            case "조도" -> src = lightHistory;
            case "CO2" -> src = co2History;
            default -> src = tempHistory;
        }

        if (src.isEmpty()) {
            graphArea.removeAll();
            graphArea.revalidate();
            graphArea.repaint();
            bottomLabel.setText("평균 : -");
            topLabel.setText(currentType + " 변화 그래프 (" + getUnitForType(currentType) + ")");
            return;
        }

        int[] data = src.stream().mapToInt(Integer::intValue).toArray();
        String unit = getUnitForType(currentType);

        topLabel.setText(currentType + " 변화 그래프 (" + unit + ")");
        double avg = IntStream.of(data).average().orElse(0.0);
        bottomLabel.setText(String.format("평균 : %.1f %s", avg, unit));

        graphArea.removeAll();
        graphArea.add(new GraphPanel(data, currentType, unit, ttfFont), BorderLayout.CENTER);
        graphArea.revalidate();
        graphArea.repaint();
    }

    private static String getUnitForType(String type) {
        return switch (type) {
            case "온도" -> "'C";
            case "조도" -> "lux";
            case "CO2" -> "ppm";
            default -> "";
        };
    }

    // ───────── 그래프 그리는 내부 클래스 (기존 그대로) ─────────
    private static class GraphPanel extends JPanel {
        private final int[] data;
        private final String type;
        private final String unit;
        private final Font font;
        private final int padding = 40;
        private final int labelPadding = 30;
        private final Stroke graphStroke = new BasicStroke(2f);
        private final Color gridColor = new Color(220, 220, 220);
        private final Color lineColor = new Color(100, 149, 237);
        private final Color pointColor = new Color(65, 105, 225);

        public GraphPanel(int[] data, String type, String unit, Font font) {
            this.data = data;
            this.type = type;
            this.unit = unit;
            this.font = font;
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(760, 320));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.length == 0) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double minValue = Double.MAX_VALUE, maxValue = Double.MIN_VALUE;
            for (int val : data) { minValue = Math.min(minValue, val); maxValue = Math.max(maxValue, val); }
            if (Math.abs(maxValue - minValue) < 1) { maxValue += 1; minValue -= 1; }

            int width = getWidth(), height = getHeight();
            int graphWidth = width - (2 * padding) - labelPadding;
            int graphHeight = height - 2 * padding;

            int len = data.length;
            int denom = Math.max(1, len - 1);

            int yGridCount = 6;
            g2.setColor(gridColor);
            for (int i = 0; i <= yGridCount; i++) {
                int y = padding + (i * graphHeight) / yGridCount;
                g2.drawLine(padding + labelPadding, y, padding + labelPadding + graphWidth, y);
            }

            g2.setColor(Color.BLACK);
            g2.drawRect(padding + labelPadding, padding, graphWidth, graphHeight);

            // Y 라벨
            g2.setFont(font.deriveFont(Font.PLAIN, 11f));
            for (int i = 0; i <= yGridCount; i++) {
                double yValue = maxValue - i * (maxValue - minValue) / yGridCount;
                String yLabel = String.format("%.0f%s", yValue, unit);
                int y = padding + (i * graphHeight) / yGridCount;
                int labelWidth = g2.getFontMetrics().stringWidth(yLabel);
                g2.setColor(Color.BLACK);
                g2.drawString(yLabel, padding + labelPadding - labelWidth - 6, y + 4);
            }

            // X 라벨
            int xLabelCount = Math.min(data.length, 6);
            g2.setColor(Color.BLACK);
            for (int i = 0; i < xLabelCount; i++) {
                int idx = (xLabelCount == 1) ? 0 : i * (data.length - 1) / (xLabelCount - 1);
                String xLabel = String.valueOf(idx);
                int x = padding + labelPadding + (idx * graphWidth) / denom;
                int labelWidth = g2.getFontMetrics().stringWidth(xLabel);
                g2.drawString(xLabel, x - labelWidth / 2, padding + graphHeight + 16);
            }

            double xScale = (double) graphWidth / denom;
            double yScale = (double) graphHeight / (maxValue - minValue);

            java.util.List<Point> graphPoints = new java.util.ArrayList<>();
            for (int i = 0; i < data.length; i++) {
                int x = (int) (padding + labelPadding + (i * xScale));
                int y = (int) (padding + (maxValue - data[i]) * yScale);
                graphPoints.add(new Point(x, y));
            }

            g2.setColor(lineColor);
            g2.setStroke(graphStroke);
            for (int i = 0; i < graphPoints.size() - 1; i++) {
                int x1 = graphPoints.get(i).x, y1 = graphPoints.get(i).y;
                int x2 = graphPoints.get(i + 1).x, y2 = graphPoints.get(i + 1).y;
                g2.drawLine(x1, y1, x2, y2);
            }

            g2.setColor(pointColor);
            for (Point p : graphPoints) {
                g2.fillOval(p.x - 3, p.y - 3, 6, 6);
            }

            int lastVal = data[data.length - 1];
            double avg = IntStream.of(data).average().orElse(0.0);
            g2.setFont(font.deriveFont(Font.BOLD, 12f));
            g2.setColor(Color.DARK_GRAY);
            g2.drawString("Latest: " + lastVal + unit, padding + labelPadding + 6, padding + 14);
            g2.drawString(String.format("Avg: %.1f%s", avg, unit), width - padding - 120, padding + 14);

            g2.setFont(font.deriveFont(Font.BOLD, 14f));
            g2.drawString(type + " (" + unit + ")", padding + 6, padding - 6);
        }
    }
}
