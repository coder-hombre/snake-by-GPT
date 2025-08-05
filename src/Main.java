import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.io.*;

public class Main {
    public static void main(String[] args) {
        // Get screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.width * 0.75);
        int height = (int) (screenSize.height * 0.75);
        JFrame frame = new JFrame("Snake Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        GamePanel panel = new GamePanel(width, height);
        frame.add(panel);
        frame.pack();
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class GamePanel extends JPanel implements ActionListener {
    int SCREEN_WIDTH;
    int SCREEN_HEIGHT;
    static final int UNIT_SIZE = 25;
    int GAME_UNITS;
    static final int DELAY = 75;
    final int x[];
    final int y[];
    int bodyParts = 6;
    int applesEaten;
    int appleX;
    int appleY;
    char direction = 'R';
    boolean running = false;
    Timer timer;
    Random random;

    enum GameState { MENU, RUNNING, GAME_OVER }
    GameState gameState = GameState.MENU;
    JButton startButton;

    int highScore = 0;
    final String HIGH_SCORE_FILE = "highscore.dat";

    GamePanel(int width, int height) {
        SCREEN_WIDTH = width;
        SCREEN_HEIGHT = height;
        GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
        x = new int[GAME_UNITS];
        y = new int[GAME_UNITS];
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        setLayout(null);
        setupMenu();
        loadHighScore();
    }

    private void setupMenu() {
        startButton = new JButton("Start Game");
        startButton.setFont(new Font("Ink Free", Font.BOLD, 30));
        startButton.setBounds(SCREEN_WIDTH/2 - 120, SCREEN_HEIGHT/2 - 40, 240, 80);
        startButton.addActionListener(e -> {
            remove(startButton);
            requestFocusInWindow();
            startGame();
        });
        add(startButton);
        repaint();
    }

    private void loadHighScore() {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(HIGH_SCORE_FILE))) {
            highScore = dis.readInt();
        } catch (IOException e) {
            highScore = 0;
        }
    }

    private void saveHighScore() {
        if (applesEaten > highScore) {
            highScore = applesEaten;
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(HIGH_SCORE_FILE))) {
                dos.writeInt(highScore);
            } catch (IOException e) {JOptionPane.showMessageDialog(this, "Unable to save high score!\n" + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void startGame() {
        newApple();
        bodyParts = 6;
        applesEaten = 0;
        direction = 'R';
        for (int i = 0; i < x.length; i++) { x[i] = 0; y[i] = 0; }
        gameState = GameState.RUNNING;
        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameState == GameState.MENU) {
            drawMenu(g);
        } else if (gameState == GameState.RUNNING) {
            draw(g);
        } else if (gameState == GameState.GAME_OVER) {
            gameOver(g);
        }
    }

    private void drawMenu(Graphics g) {
        int buffer = 35;
        // Draw high score at the top with buffer
        g.setColor(Color.yellow);
        g.setFont(new Font("Ink Free", Font.BOLD, 30));
        String highScoreText = "High Score: " + highScore;
        g.drawString(highScoreText, (SCREEN_WIDTH - g.getFontMetrics().stringWidth(highScoreText)) / 2, buffer);
        // Draw game title below high score
        g.setColor(Color.green);
        g.setFont(new Font("Ink Free", Font.BOLD, 60));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("Snake Game", (SCREEN_WIDTH - metrics.stringWidth("Snake Game")) / 2, buffer + 80);
    }

    public void draw(Graphics g) {
        if (running) {
            g.setColor(Color.red);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g.setColor(Color.green);
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                } else {
                    g.setColor(new Color(45, 180, 0));
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                }
            }
            // Draw high score at the top with buffer
            g.setColor(Color.yellow);
            g.setFont(new Font("Ink Free", Font.BOLD, 30));
            int buffer = 35;
            String highScoreText = "High Score: " + highScore;
            g.drawString(highScoreText, (SCREEN_WIDTH - g.getFontMetrics().stringWidth(highScoreText)) / 2, buffer);
            // Draw current score below high score
            g.setColor(Color.red);
            g.setFont(new Font("Ink Free", Font.BOLD, 40));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics.stringWidth("Score: " + applesEaten)) / 2, buffer + 40);
        } else {
            gameOver(g);
        }
    }

    public void newApple() {
        appleX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        appleY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U':
                y[0] = y[0] - UNIT_SIZE;
                break;
            case 'D':
                y[0] = y[0] + UNIT_SIZE;
                break;
            case 'L':
                x[0] = x[0] - UNIT_SIZE;
                break;
            case 'R':
                x[0] = x[0] + UNIT_SIZE;
                break;
        }
    }

    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
        }
    }

    public void checkCollisions() {
        // checks if head collides with body
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
            }
        }
        // check if head touches left border
        if (x[0] < 0) {
            running = false;
        }
        // check if head touches right border
        if (x[0] >= SCREEN_WIDTH) {
            running = false;
        }
        // check if head touches top border
        if (y[0] < 0) {
            running = false;
        }
        // check if head touches bottom border
        if (y[0] >= SCREEN_HEIGHT) {
            running = false;
        }
        if (!running) {
            timer.stop();
            gameState = GameState.GAME_OVER;
            repaint();
        }
    }

    public void gameOver(Graphics g) {
        saveHighScore();
        // Score
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics1.stringWidth("Score: " + applesEaten)) / 2, g.getFont().getSize());
        // High Score
        g.setColor(Color.yellow);
        g.setFont(new Font("Ink Free", Font.BOLD, 30));
        g.drawString("High Score: " + highScore, (SCREEN_WIDTH - g.getFontMetrics(g.getFont()).stringWidth("High Score: " + highScore)) / 2, g.getFont().getSize() + 40);
        // Game Over text
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics2.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);
        // Restart instruction
        g.setFont(new Font("Ink Free", Font.BOLD, 30));
        g.setColor(Color.white);
        g.drawString("Press ENTER to return to menu", (SCREEN_WIDTH - g.getFontMetrics(g.getFont()).stringWidth("Press ENTER to return to menu")) / 2, SCREEN_HEIGHT / 2 + 60);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState == GameState.RUNNING && running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (gameState == GameState.RUNNING) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        if (direction != 'R') {
                            direction = 'L';
                        }
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (direction != 'L') {
                            direction = 'R';
                        }
                        break;
                    case KeyEvent.VK_UP:
                        if (direction != 'D') {
                            direction = 'U';
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                        if (direction != 'U') {
                            direction = 'D';
                        }
                        break;
                }
            } else if (gameState == GameState.GAME_OVER) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    gameState = GameState.MENU;
                    setupMenu();
                    repaint();
                }
            }
        }
    }
}