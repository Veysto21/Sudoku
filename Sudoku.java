import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Sudoku {
    private static final int SIZE = 9;
    private static final int SUBGRID = 3;
    private static JTextField[][] cells = new JTextField[SIZE][SIZE];
    private static int[][] solution;
    private static int[][] puzzle;
    private static JLabel timerLabel = new JLabel("Time: 0s");
    private static int secondsElapsed = 0;
    private static Timer gameTimer;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> showMenu());
    }

    private static void showMenu() {
        String[] options = {"Easy", "Medium", "Hard"};
        int choice = JOptionPane.showOptionDialog(null, "Select Difficulty",
                "Sudoku", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);

        if (choice == -1) System.exit(0);

        int clues;
        switch (choice) {
            case 1: clues = 36; break;  // Medium
            case 2: clues = 26; break;  // Hard
            default: clues = 46; break; // Easy
        }

        solution = generateFullBoard();
        puzzle = createPuzzle(solution, clues);
        buildGUI();
    }

    private static void buildGUI() {
        JFrame frame = new JFrame("Sudoku - Java GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(650, 750);
        frame.setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel(new GridLayout(SIZE, SIZE));

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                JTextField cell = new JTextField();
                cell.setHorizontalAlignment(JTextField.CENTER);
                cell.setFont(new Font("SansSerif", Font.BOLD, 20));

                if (puzzle[r][c] != 0) {
                    cell.setText(String.valueOf(puzzle[r][c]));
                    cell.setEditable(false);
                    cell.setBackground(Color.LIGHT_GRAY);
                } else {
                    cell.setText("");
                    final int row = r, col = c;
                    cell.setBackground(Color.WHITE);
                    cell.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyReleased(KeyEvent e) {
                            String val = cell.getText();
                            if (!val.matches("[1-9]?")) cell.setText("");
                        }
                    });
                }

                cell.setBorder(BorderFactory.createMatteBorder(
                        r % SUBGRID == 0 ? 3 : 1,
                        c % SUBGRID == 0 ? 3 : 1,
                        r == SIZE - 1 ? 3 : 1,
                        c == SIZE - 1 ? 3 : 1,
                        Color.BLACK));

                cells[r][c] = cell;
                gridPanel.add(cell);
            }
        }

        // Top Panel with Timer and Buttons
        JPanel topPanel = new JPanel(new BorderLayout());
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(timerLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel();
        JButton checkBtn = new JButton("Check");
        JButton resetBtn = new JButton("Reset");
        JButton newGameBtn = new JButton("New Game");

        buttonPanel.add(checkBtn);
        buttonPanel.add(resetBtn);
        buttonPanel.add(newGameBtn);

        checkBtn.addActionListener(e -> checkSolution());
        resetBtn.addActionListener(e -> resetPuzzle());
        newGameBtn.addActionListener(e -> {
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(buttonPanel);
            topFrame.dispose(); // Close current game window
            newGame();          // Start new game
        });

        topPanel.add(buttonPanel, BorderLayout.EAST);
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(gridPanel, BorderLayout.CENTER);
        frame.setVisible(true);

        startTimer();
    }

    private static void checkSolution() {
        boolean correct = true;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                String val = cells[r][c].getText();
                if (!val.matches("[1-9]")) {
                    correct = false;
                } else if (Integer.parseInt(val) != solution[r][c]) {
                    correct = false;
                }
            }
        }

        if (correct) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "You solved it!");
        } else {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "Not correct. Try again!");
        }
    }

    private static void resetPuzzle() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (puzzle[r][c] == 0) {
                    cells[r][c].setText("");
                }
            }
        }
        secondsElapsed = 0;
        timerLabel.setText("Time: 0s");
    }

    private static void newGame() {
        gameTimer.cancel();
        showMenu(); // Restart difficulty selection and GUI
    }

    private static void startTimer() {
        gameTimer = new Timer();
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                secondsElapsed++;
                SwingUtilities.invokeLater(() -> timerLabel.setText("Time: " + secondsElapsed + "s"));
            }
        }, 1000, 1000);
    }

    // Sudoku generator
    private static int[][] generateFullBoard() {
        int[][] board = new int[SIZE][SIZE];
        fillBoard(board, 0, 0);
        return board;
    }

    private static boolean fillBoard(int[][] board, int row, int col) {
        if (row == SIZE) return true;
        int nextRow = (col == SIZE - 1) ? row + 1 : row;
        int nextCol = (col + 1) % SIZE;

        java.util.List<Integer> numbers = new java.util.ArrayList<>();
        for (int i = 1; i <= SIZE; i++) numbers.add(i);
        Collections.shuffle(numbers);

        for (int num : numbers) {
            if (isSafe(board, row, col, num)) {
                board[row][col] = num;
                if (fillBoard(board, nextRow, nextCol)) return true;
                board[row][col] = 0;
            }
        }

        return false;
    }

    private static boolean isSafe(int[][] board, int row, int col, int num) {
        for (int i = 0; i < SIZE; i++) {
            if (board[row][i] == num || board[i][col] == num) return false;
        }

        int rStart = (row / SUBGRID) * SUBGRID;
        int cStart = (col / SUBGRID) * SUBGRID;
        for (int r = rStart; r < rStart + SUBGRID; r++) {
            for (int c = cStart; c < cStart + SUBGRID; c++) {
                if (board[r][c] == num) return false;
            }
        }

        return true;
    }

    private static int[][] createPuzzle(int[][] board, int filledCells) {
        int[][] puzzle = deepCopy(board);
        int cellsToRemove = SIZE * SIZE - filledCells;

        Random rand = new Random();
        while (cellsToRemove > 0) {
            int r = rand.nextInt(SIZE);
            int c = rand.nextInt(SIZE);
            if (puzzle[r][c] != 0) {
                puzzle[r][c] = 0;
                cellsToRemove--;
            }
        }

        return puzzle;
    }

    private static int[][] deepCopy(int[][] input) {
        int[][] copy = new int[input.length][];
        for (int i = 0; i < input.length; i++)
            copy[i] = input[i].clone();
        return copy;
    }
}
