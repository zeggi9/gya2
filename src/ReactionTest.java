import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;


public class ReactionTest {
    private Timer roundTimer;

    // Spelarens info
    private String playerName;
    private String playerClass;
    private String playerAge;
    private String playerGender;
    private String playerHobby;
//hksdfj
    // Highscore
    private long highscore = Long.MAX_VALUE;
    private String highscoreName = "---";
    private String highscoreClass = "---";

    // Level 1
    private boolean isGreen = false;
    private long greenTime;
    private JPanel panel;
    private JFrame gameFrame; // Spara referens till spelfönstret

    public ReactionTest() {
        loadHighscore();
        showStartWindow();
    }

    // Startfönster
    private void showStartWindow() {
        JFrame frame = new JFrame("Spelstart");
        frame.setSize(500, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        JLabel title = new JLabel("Fyll i dina uppgifter:");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setBounds(120, 20, 400, 40);
        frame.add(title);

        // Fält
        JTextField nameField = makeField(frame, "Namn:", 80);
        JTextField classField = makeField(frame, "Klass:", 150);
        JTextField ageField = makeField(frame, "Ålder:", 220);

        // Kön
        JLabel genderLabel = new JLabel("Kön:");
        genderLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        genderLabel.setBounds(50, 290, 200, 30);
        frame.add(genderLabel);

        String[] genders = {"Man", "Kvinna", "Annat"};
        JComboBox<String> genderBox = new JComboBox<>(genders);
        genderBox.setBounds(180, 290, 200, 30);
        frame.add(genderBox);

        // Hobbys
        JLabel hobbyLabel = new JLabel("Hobby:");
        hobbyLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        hobbyLabel.setBounds(50, 360, 200, 30);
        frame.add(hobbyLabel);

        String[] hobbies = {"Inga", "Gamer", "Sport", "Musik", "Konst", "Annat"};
        JComboBox<String> hobbyBox = new JComboBox<>(hobbies);
        hobbyBox.setBounds(180, 360, 200, 30);
        frame.add(hobbyBox);

        JTextField sportField = new JTextField();
        sportField.setBounds(180, 400, 200, 30);
        sportField.setVisible(false);
        frame.add(sportField);

        hobbyBox.addActionListener(e -> {
            sportField.setVisible(hobbyBox.getSelectedItem().equals("Sport"));
        });

        // Rund "Redo"-knapp
        JButton ready = new JButton("Redo") {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Color.GREEN);
                g.fillOval(0, 0, getWidth(), getHeight());
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 20));
                FontMetrics fm = g.getFontMetrics();
                int x = (getWidth() - fm.stringWidth("Redo")) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 4;
                g.drawString("Redo", x, y);
            }
        };

        ready.setBounds(175, 460, 120, 120);
        ready.setFocusPainted(false);
        ready.setContentAreaFilled(false);
        ready.setBorderPainted(false);

        frame.add(ready);

        ready.addActionListener(e -> {
            playerName = nameField.getText();
            playerClass = classField.getText();
            playerAge = ageField.getText();
            playerGender = genderBox.getSelectedItem().toString();
            playerHobby = hobbyBox.getSelectedItem().equals("Sport") && !sportField.getText().isBlank()
                    ? "Sport (" + sportField.getText() + ")"
                    : hobbyBox.getSelectedItem().toString();

            frame.dispose();
            showLevelSplash();
        });

        frame.setVisible(true);
    }

    private JTextField makeField(JFrame frame, String text, int y) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 20));
        label.setBounds(50, y, 200, 30);
        frame.add(label);

        JTextField field = new JTextField();
        field.setBounds(180, y, 200, 30);
        frame.add(field);
        return field;
    }


    // SPLASH SCREEN – LEVEL 1 (visas i 2 sek)
    private void showLevelSplash() {
        JFrame frame = new JFrame();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true);

        JPanel splash = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 150));
                g.drawString("LEVEL 1", getWidth() / 2 - 300, getHeight() / 2);
            }
        };

        frame.add(splash);
        frame.setVisible(true);

        Timer splashTimer = new Timer(2000, e -> {
            frame.dispose();
            startLevel1();
        });
        splashTimer.setRepeats(false);
        splashTimer.start();
    }

    // LEVEL 1 reaktionsspe
    private void startLevel1() {
        gameFrame = new JFrame("Reaktionstest – Level 1");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        gameFrame.setUndecorated(true);

        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                g.setColor(isGreen ? Color.GREEN : Color.RED);
                g.fillRect(0, 0, getWidth(), getHeight());

                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 40));
                g.drawString("Highscore: " +
                                (highscore == Long.MAX_VALUE ? "---" : highscore + " ms (" + highscoreName + " – " + highscoreClass + ")"),
                        20, 60);
            }
        };

        // Klick + SPACE
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { handleClick(); }
        });

        panel.setFocusable(true);
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) handleClick();
                if (e.getKeyCode() == KeyEvent.VK_R) resetHighscore();
            }
        });

        gameFrame.add(panel);
        gameFrame.setVisible(true);

        startNewRound();
    }

    private void handleClick() {
        if (!isGreen) {
            JOptionPane.showMessageDialog(gameFrame, "För tidigt!");
            startNewRound();
            return;
        }

        long reaction = System.currentTimeMillis() - greenTime;

        if (reaction < highscore) {
            highscore = reaction;
            highscoreName = playerName;
            highscoreClass = playerClass;
            saveHighscore();
        }

        JOptionPane.showMessageDialog(gameFrame,
                "Din tid: " + reaction + " ms\n" +
                        "Highscore: " + highscore + " ms\n" +
                        highscoreName + " – " + highscoreClass);

        startNewRound();
        panel.repaint();
    }

    private void resetHighscore() {
        highscore = Long.MAX_VALUE;
        highscoreName = "---";
        highscoreClass = "---";
        saveHighscore();
        panel.repaint();
    }

    private void startNewRound() {
        isGreen = false;
        panel.repaint();

        // Stoppa gammal timer
        if (roundTimer != null) {
            roundTimer.stop();
        }

        roundTimer = new Timer((int) (Math.random() * 9000) + 1000, e -> {
            isGreen = true;
            greenTime = System.currentTimeMillis();
            panel.repaint();
        });

        roundTimer.setRepeats(false);
        roundTimer.start();
    }

    // LOAD / SAVE HIGHSCORE
    private void loadHighscore() {
        try {
            File file = new File("highscore1.txt");
            if (!file.exists()) return;

            Scanner sc = new Scanner(file);
            String[] parts = sc.nextLine().split(";");
            highscore = Long.parseLong(parts[0]);
            highscoreName = parts[1];
            highscoreClass = parts[2];
            sc.close();
        } catch (Exception ignored) {}
    }

    private void saveHighscore() {
        try {
            FileWriter writer = new FileWriter("highscore1.txt");
            writer.write(highscore + ";" + highscoreName + ";" + highscoreClass);
            writer.close();
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        new ReactionTest();
    }
}