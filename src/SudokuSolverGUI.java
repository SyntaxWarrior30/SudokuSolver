import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;


public class SudokuSolverGUI extends JFrame {

    public final IntegerInputField[][] textField; // TextFields to hold user input of the Sudoku puzzle
    public GridPanel sudokuGrid; // Custom grid panel for Sudoku puzzle
    public boolean isDone; // Boolean value to store if done or not
    public boolean terminate; // Boolean value to store if user wants to terminate solve
    public Thread solveThread;

    public SudokuSolverGUI() {
        isDone = true;

        textField = new IntegerInputField[9][9];
        sudokuGrid = new GridPanel(new GridLayout(9, 9, 0, 0));

        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                textField[x][y] = new IntegerInputField();
                textField[x][y].setPreferredSize(new Dimension(40, 40));
                this.setFont(new Font("Arial", Font.PLAIN, 100));
                textField[x][y].setHorizontalAlignment(JTextField.CENTER);
                sudokuGrid.add(textField[x][y]);
            }
        }

        this.add(sudokuGrid);
    }

    public void clearGrid() {
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                textField[x][y].setText("");
                textField[x][y].setToolTipText("");
                textField[x][y].setForeground(Color.black);
                textField[x][y].setEditable(true);
            }
        }
    }

    public void solve() {
        terminate = false;
        solveThread = new Thread(this::solveHelper);
        solveThread.start();
    }

    public void terminateSolve() {
        terminate = true;
        if (solveThread != null) {
            solveThread.interrupt();
            SudokuSolverTester.jSlider.setEnabled(true);
            SudokuSolverTester.solveButton.setEnabled(true);
            SudokuSolverTester.genRandPuzzle.setEnabled(true);
        }
    }

    private boolean solveHelper() {
        int row = -1;
        int col = -1;
        boolean isFilled = true;

        // Find the next empty cell
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (textField[r][c].getText().isEmpty()) {
                    row = r;
                    col = c;
                    isFilled = false;
                    break;
                }
            }
            if (!isFilled) {
                break;
            }
        }

        // If all cells are filled, the Sudoku puzzle is solved
        if (isFilled) {
            SudokuSolverTester.clearStopButton.setText("<html><center>Clear</html></center>");
            SudokuSolverTester.jSlider.setEnabled(true);
            SudokuSolverTester.solveButton.setEnabled(true);
            SudokuSolverTester.genRandPuzzle.setEnabled(true);
            return true;
        }

        for (int num = 1; num <= 9; num++) {
            if (terminate) {
                // Thread was interrupted, terminate solving
                return false;
            } else {
                sleep();
            }

            if (isValid(row, col, num)) {
                textField[row][col].setForeground(Color.GREEN);
                textField[row][col].setText(String.valueOf(num));

                if (solveHelper()) {
                    return true;
                }

                // Backtrack if the solution is not found
                textField[row][col].setText("");
            }
        }
        return false;
    }


    private boolean isValid(int row, int col, int num) {
        // Check row constraints
        for (int i = 0; i < textField.length; i++) {
            if (i != col && textField[row][i].getText().equals(String.valueOf(num))) {
                return false;
            }
        }

        // Check column constraints
        for (int i = 0; i < textField.length; i++) {
            if (i != row && textField[i][col].getText().equals(String.valueOf(num))) {
                return false;
            }
        }

        // Check 3x3 box constraint
        int boxRow = row - row % 3;
        int boxCol = col - col % 3;
        for (int i = boxRow; i < boxRow + 3; i++) {
            for (int j = boxCol; j < boxCol + 3; j++) {
                if (textField[i][j].getText().equals(String.valueOf(num))) {
                    return false;
                }
            }
        }

        return true;
    }

    private void sleep() {
        try {
            long sleepTime = SudokuSolverTester.jSlider.getMaximum() - ((long) SudokuSolverTester.jSlider.getValue() * (SudokuSolverTester.jSlider.getMaximum() - SudokuSolverTester.jSlider.getMinimum()) / 100);
            while (sleepTime > 0) {
                long startTime = System.currentTimeMillis();
                TimeUnit.MILLISECONDS.sleep(sleepTime);
                long elapsedTime = System.currentTimeMillis() - startTime;
                sleepTime -= elapsedTime;
            }
        } catch (InterruptedException e) {
            // Restore the interrupted status
            Thread.currentThread().interrupt();
        }
    }

    public void disableBoard() {
        for (JTextField[] jTextFields : textField) {
            for (JTextField jTextField : jTextFields) {
                jTextField.setEditable(false);
                jTextField.setToolTipText("Cannot Edit During Solve");
            }
        }
    }

    public void generateBoard() {
        this.clearGrid();
        SudokuGenerator randomBoard = new SudokuGenerator(9, (int)(Math.random() * 76) + 5);
        randomBoard.generate();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if(randomBoard.grid[r][c] != 0) {
                    textField[r][c].setText(String.valueOf(randomBoard.grid[r][c]));
                } else {
                    textField[r][c].setText("");
                }
            }
        }
    }
}
