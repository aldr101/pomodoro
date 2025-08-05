import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javazoom.jl.player.Player;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class PomodoroTimer {
    private static Thread audioThread;
    private static Player mp3Player;
    private static final AtomicBoolean audioShouldPlay = new AtomicBoolean(false);
    private static final int WORK_DURATION = 25 * 60;
    private static final int BREAK_DURATION = 5 * 60;
    private static JFrame frame;
    private static JLabel statusLabel;
    private static JLabel timerLabel;
    private static final java.util.List<String> tasks = new java.util.ArrayList<>();
    private static int currentTaskIndex = 0;
    private static JLabel taskListLabel;
    private static javax.swing.JButton startButton;
    private static javax.swing.JButton pauseButton;
    private static javax.swing.Timer activeTimer;
    private static boolean isPaused = false;
    private static int pausedTimeLeft = 0;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Pomodoro Timer ✨");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(640, 400);
            frame.setLayout(new BorderLayout(0, 20));
            frame.getContentPane().setBackground(new java.awt.Color(255, 230, 242));

            javax.swing.JPanel heartsPanel = new javax.swing.JPanel();
            heartsPanel.setOpaque(false);
            heartsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 10));
            int numHearts = 7;
            javax.swing.ImageIcon heartIconImg = new javax.swing.ImageIcon("resources/images/heart8bit.png");

            for (int i = 0; i < numHearts; i++) {
                JLabel heartIcon;
                if (heartIconImg.getImageLoadStatus() == java.awt.MediaTracker.COMPLETE) {
                    heartIcon = new JLabel(heartIconImg);
                } else {
                    heartIcon = new JLabel("♥", SwingConstants.CENTER);
                    heartIcon.setFont(new Font("Monospaced", Font.BOLD, 36));
                }
                heartIcon.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
                heartsPanel.add(heartIcon);
            }

            Font pixelFont;
            try {
                pixelFont = Font.createFont(Font.TRUETYPE_FONT, PomodoroTimer.class.getResourceAsStream("/fonts/PressStart2P-Regular.ttf")).deriveFont(Font.PLAIN, 22f);
            } catch (Exception e) {
                pixelFont = new Font("Monospaced", Font.BOLD, 22);
            }
            Font pixelFontLarge = pixelFont.deriveFont(Font.BOLD, 48f);

            statusLabel = new JLabel("Welcome!", SwingConstants.CENTER);
            statusLabel.setFont(pixelFont);
            statusLabel.setForeground(new java.awt.Color(255, 105, 180));
            statusLabel.setOpaque(true);
            statusLabel.setBackground(new java.awt.Color(255, 240, 250));

            timerLabel = new JLabel("", SwingConstants.CENTER);
            timerLabel.setFont(pixelFontLarge);
            timerLabel.setForeground(new java.awt.Color(255, 20, 147));
            timerLabel.setOpaque(true);
            timerLabel.setBackground(new java.awt.Color(255, 240, 250));

            taskListLabel = new JLabel();
            taskListLabel.setFont(new Font("Monospaced", Font.PLAIN, 16));
            taskListLabel.setForeground(new java.awt.Color(80, 80, 80));
            taskListLabel.setBackground(new java.awt.Color(255, 240, 250));
            taskListLabel.setOpaque(true);

            startButton = new javax.swing.JButton("Start Timer");
            startButton.setFont(new Font("Monospaced", Font.BOLD, 18));
            startButton.setBackground(new java.awt.Color(255, 192, 203));
            startButton.setForeground(new java.awt.Color(120, 0, 60));

            pauseButton = new javax.swing.JButton("Pause");
            pauseButton.setFont(new Font("Monospaced", Font.BOLD, 18));
            pauseButton.setBackground(new java.awt.Color(255, 240, 250));
            pauseButton.setForeground(new java.awt.Color(120, 0, 60));
            pauseButton.setEnabled(false);

            javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
            buttonPanel.setOpaque(false);
            buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 0));
            buttonPanel.add(startButton);
            buttonPanel.add(pauseButton);

            javax.swing.JPanel eastPanel = new javax.swing.JPanel();
            eastPanel.setLayout(new java.awt.BorderLayout(0, 10));
            eastPanel.setOpaque(false);
            eastPanel.add(taskListLabel, BorderLayout.CENTER);
            eastPanel.add(buttonPanel, BorderLayout.SOUTH);

            frame.add(heartsPanel, BorderLayout.SOUTH);
            frame.add(statusLabel, BorderLayout.NORTH);
            frame.add(timerLabel, BorderLayout.CENTER);
            frame.add(eastPanel, BorderLayout.EAST);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            scaleFontsToWindowSize();
            frame.addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    scaleFontsToWindowSize();
                }
            });

            promptForTasks();
        });
    }

    private static void scaleFontsToWindowSize() {
        int frameWidth = frame.getWidth();
        int frameHeight = frame.getHeight();
        float baseSize = Math.min(frameWidth, frameHeight) / 30f;
        float titleSize = Math.max(16f, Math.min(baseSize, 36f));
        float timerSize = Math.max(28f, Math.min(baseSize * 2, 64f));
        float taskSize = Math.max(12f, Math.min(baseSize * 0.8f, 24f));
        statusLabel.setFont(statusLabel.getFont().deriveFont(titleSize));
        timerLabel.setFont(timerLabel.getFont().deriveFont(timerSize));
        taskListLabel.setFont(taskListLabel.getFont().deriveFont(taskSize));
    }

    private static void promptForTasks() {
        SwingUtilities.invokeLater(() -> {
            tasks.clear();
            currentTaskIndex = 0;
            for (int i = 0; i < 5; i++) {
                String task = JOptionPane.showInputDialog(frame, "Enter task " + (i + 1) + " (or leave blank to finish):", "Pomodoro Task List", JOptionPane.QUESTION_MESSAGE);
                if (task == null || task.trim().isEmpty()) break;
                tasks.add(task.trim());
            }
            if (tasks.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No tasks entered. Exiting.", "Pomodoro Timer", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }
            updateTaskListLabel();
            startButton.setEnabled(true);
            pauseButton.setEnabled(false);
            for (java.awt.event.ActionListener al : startButton.getActionListeners()) startButton.removeActionListener(al);
            for (java.awt.event.ActionListener al : pauseButton.getActionListeners()) pauseButton.removeActionListener(al);
            startButton.addActionListener(e -> {
                startButton.setEnabled(false);
                pauseButton.setEnabled(true);
                startNextTask();
            });
            pauseButton.addActionListener(e -> {
                if (!isPaused) {
                    isPaused = true;
                    pauseButton.setText("Resume");
                    if (activeTimer != null) activeTimer.stop();
                    pauseAudio();
                } else {
                    isPaused = false;
                    pauseButton.setText("Pause");
                    if (activeTimer != null && pausedTimeLeft > 0) {
                        resumeCountdown(pausedTimeLeft);
                        playAudio();
                    }
                }
            });
        });
    }

    private static void startNextTask() {
        if (currentTaskIndex >= tasks.size()) {
            statusLabel.setText("All tasks completed! Great job!");
            timerLabel.setText("");
            pauseButton.setEnabled(false);
            return;
        }
        String task = tasks.get(currentTaskIndex);
        statusLabel.setText("Working on: " + task);
        updateTaskListLabel();
        runCountdown("Work", WORK_DURATION, () -> {
            statusLabel.setText("Break time!");
            runCountdown("Break", BREAK_DURATION, () -> {
                currentTaskIndex++;
                updateTaskListLabel();
                new javax.swing.Timer(1000, e -> startNextTask()).start();
            });
        });
    }

    private static void runCountdown(String label, int seconds, Runnable onFinish) {
        final int[] timeLeft = {seconds};
        if (activeTimer != null) activeTimer.stop();
        isPaused = false;
        pauseButton.setText("Pause");
        activeTimer = new javax.swing.Timer(1000, null);
        activeTimer.addActionListener(e -> {
            if (isPaused) {
                pausedTimeLeft = timeLeft[0];
                pauseAudio();
                return;
            }
            if (timeLeft[0] == seconds) playAudio();
            if (timeLeft[0] >= 0) {
                int mins = timeLeft[0] / 60;
                int secs = timeLeft[0] % 60;
                timerLabel.setText(label + " time left: %02d:%02d".formatted(mins, secs));
                timeLeft[0]--;
                pausedTimeLeft = timeLeft[0];
            } else {
                activeTimer.stop();
                timerLabel.setText("");
                stopAudio();
                if (onFinish != null) onFinish.run();
            }
        });
        activeTimer.setInitialDelay(0);
        activeTimer.start();
    }

    private static void playAudio() {
        stopAudio();
        audioShouldPlay.set(true);
        audioThread = new Thread(() -> {
            try {
                InputStream is = PomodoroTimer.class.getResourceAsStream("/sounds/lofi_timer.mp3");
                if (is == null) {
                    System.out.println("⚠️ Could not find audio file. Check path or if it's in the build.");
                    return;
                }
                mp3Player = new Player(new BufferedInputStream(is));
                while (audioShouldPlay.get()) {
                    if (!mp3Player.play(1)) break;
                }
            } catch (Exception ex) {
                System.out.println("Error playing audio: " + ex.getMessage());
            }
        });
        audioThread.setDaemon(true);
        audioThread.start();
    }

    private static void pauseAudio() {
        audioShouldPlay.set(false);
        if (mp3Player != null) mp3Player.close();
        mp3Player = null;
    }

    private static void stopAudio() {
        audioShouldPlay.set(false);
        if (mp3Player != null) mp3Player.close();
        mp3Player = null;
    }

    private static void resumeCountdown(int secondsLeft) {
        if (activeTimer != null) activeTimer.stop();
        isPaused = false;
        pauseButton.setText("Pause");
        runCountdown(timerLabel.getText().contains("Break") ? "Break" : "Work", secondsLeft, () -> {
            if (timerLabel.getText().contains("Break")) {
                currentTaskIndex++;
                updateTaskListLabel();
                new javax.swing.Timer(1000, e -> startNextTask()).start();
            }
        });
    }

    private static void updateTaskListLabel() {
        StringBuilder html = new StringBuilder("<html><b>Task List:</b><br>");
        for (int i = 0; i < tasks.size(); i++) {
            if (i < currentTaskIndex) {
                html.append("<span style='color:gray;text-decoration:line-through'>• ").append(tasks.get(i)).append("</span><br>");
            } else if (i == currentTaskIndex) {
                html.append("<span style='color:#ff1493;font-weight:bold'>→ ").append(tasks.get(i)).append("</span><br>");
            } else {
                html.append("• ").append(tasks.get(i)).append("<br>");
            }
        }
        html.append("</html>");
        taskListLabel.setText(html.toString());
    }
}