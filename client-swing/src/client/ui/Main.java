package client.ui;

import client.ui.screen.StartScreen;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Look & Feel을 Nimbus로 통일 (윈도우/맥 UI 차이 거의 없어짐)
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            System.out.println("Nimbus L&F 설정 실패 → 기본 L&F 사용");
        }

        // 반드시 EDT(Event Dispatch Thread)에서 실행
        SwingUtilities.invokeLater(() -> {
            new StartScreen();
        });
    }
}
