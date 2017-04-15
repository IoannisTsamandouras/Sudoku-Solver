package com.sudokusolver;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class SudokuSolver extends JFrame implements ActionListener {

	// Sudoku cells  
	private Cell[][] cells;
	// Values in Sudoku cells
	private int[][] values;

	// Set up puzzle components 
	public SudokuSolver() {
		super("Sudoku Solver");		
		prepareUI();

		setLayout(new FlowLayout(FlowLayout.CENTER));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((d.width / 2 - 175), (d.height / 2 - 275));
		setResizable(false);
		setVisible(true);
	}
	
	// Set up UI for puzzle
	private void prepareUI() {
		// Align title, grid, and button panels vertically
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		// Sudoku grid panel
		JPanel title = new JPanel();
		JPanel sudokuPanel = new JPanel();
		sudokuPanel.setLayout(new GridLayout(3, 3, 1, 1));

		// Set up box panels
		JPanel[] boxes = new JPanel[9];
		boxes = prepareBox(sudokuPanel, boxes);
		
		// Set up cells
		values = new int[9][9];
		cells = new Cell[9][9];
		prepareCells(boxes);
		
		// First row of buttons
		JPanel buttonsPanel = new JPanel();
		// Submit to validate
		JButton submitButton = new JButton("Submit");
		// Solve puzzle
		JButton solveButton = new JButton("Solve"); 
		// Clear not fixed cells
		JButton eraseButton = new JButton("Erase"); 
		// Clear all cells including fixed
		JButton eraseAllButton = new JButton("Erase All"); 
		
		buttonsPanel.add(submitButton);
		buttonsPanel.add(solveButton);
		buttonsPanel.add(eraseButton);
		buttonsPanel.add(eraseAllButton);
		
		// Second row of buttons
		JPanel buttonsPanel2 = new JPanel();
		// Set filled cells as preset
		JButton presetButton = new JButton("Mark As Preset"); 
		
		buttonsPanel2.add(presetButton);
		
		submitButton.addActionListener(this);
		solveButton.addActionListener(this);
		presetButton.addActionListener(this);
		eraseButton.addActionListener(this);
		eraseAllButton.addActionListener(this);
		
		panel.add(title);
		panel.add(sudokuPanel);
		panel.add(buttonsPanel);
		panel.add(buttonsPanel2);
		add(panel);
	}
	
	// Set up Sudoku panel
	private JPanel[] prepareBox(JPanel sudokuPanel, JPanel[] boxes) {
		for (int i = 0; i < 9; i++) {
			boxes[i] = new JPanel();
			boxes[i].setLayout(new GridLayout(3, 3, 0, 0));
			boxes[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
			sudokuPanel.add(boxes[i]);
		}		
		return boxes;
	}
	
	// Set up cells for input and add them to panels
	private void prepareCells(JPanel[] boxes) {
		int index = 0;
		// Adjust current row
		for (int i = 0; i < 9; i++) {
			if (i <= 2) {
				index = 0;
				}
			else if (i <= 5) {
				index = 3;
				}
			else {
				index = 6;
			}			
			for (int j = 0; j < 9; j++) {
				cells[i][j] = new Cell(i, j);
				boxes[index + (j / 3)].add(cells[i][j]);
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		JButton button = (JButton) event.getSource();
		String buttonType = button.getText();
		
		if (buttonType.equals("Submit")) {
			submitSudoku();
			}
		else if (buttonType.equals("Solve")) {
			startSolving();
			}
		else if (buttonType.equals("Erase")) {
			erase();
			}
		else if (buttonType.equals("Erase All")) {
			eraseAllIncludingPreset();
			}
		else {
			checkPreset();
			}
		}
	
	// Submit Sudoku to validate
	private void submitSudoku() {
		if (isSolved()) {
			JOptionPane.showMessageDialog(getRootPane(), 
					"<html><center>Sudoku has been solved!</center></html>", 
					"Sudoku Validation", JOptionPane.INFORMATION_MESSAGE);
			}
		else {
			JOptionPane.showMessageDialog(getRootPane(), 
					"<html><center>Sudoku is not complete!</center></html>", 
					"Sudoku Validation", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	
	// Start working if Sudoku is ready
	private void startSolving() {
		// If Sudoku is full, don't do anything
		if (isSudokuFull()) {
			JOptionPane.showMessageDialog(getRootPane(), 
					"<html><center>There are no open cells</center></html>", 
					"Solving Sudoku", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// Validate before starting
		if (isValidToStart()) {
			markAsPreset();
			if (!solve(0, 0)) {
				JOptionPane.showMessageDialog(getRootPane(), 
						"<html><center>Unable to solve</center></html>", 
						"Solving Sudoku", JOptionPane.ERROR_MESSAGE);
				}
			}
		// If Sudoku not valid at the start, don't do anything
		else {
			JOptionPane.showMessageDialog(getRootPane(), 
					"<html><center>This is not a valid Sudoku</center></html>", 
					"Solving Sudoku", JOptionPane.ERROR_MESSAGE);
			}
		}
	
	// Erase all values but fixed values
	private void erase() {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if (values[i][j] != 0) {
					if (cells[i][j].editable) {
						cells[i][j].setText("");
						values[i][j] = 0;
					}
				}
			}
		}
	}
	
	// Erase all values including those with fixed values
	private void eraseAllIncludingPreset() {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if (values[i][j] != 0) {
					if (!cells[i][j].editable) {
						cells[i][j].editable = true;
						cells[i][j].setEditable(true);
						cells[i][j].setForeground(Color.BLACK);
					}
					cells[i][j].setText("");
					values[i][j] = 0;
				}
			}
		}
	}
	
	// Evaluate cells before making prefilled cells fixed
	private void checkPreset() {
		// Validate current state and make filled cells fixed
		if (!isValidToStart()) {
			JOptionPane.showMessageDialog(getRootPane(), 
					"<html><center>This is not a valid Sudoku to start.</center></html>", 
					"Sudoku Solver", JOptionPane.ERROR_MESSAGE);
			}
		else {
			markAsPreset();
			}
		}
	
	// Check if Sudoku is solved correctly
	private boolean isSolved() {
		for (int i = 0; i < 9; i++) {
			int[] aRow = new int[9];
			int[] aCol = new int[9];
			
			for (int j = 0; j < 9; j++) {
				// If cell is empty, quit 
				if (values[i][j] == 0) {
					return false;
					}
				
				aRow[j] = values[i][j];
				aCol[j] = values[j][i];
				
				// Check if the value in this cell is duplicated in 3x3 box
				if (isContainedInBox(i, j, values[i][j])) {
					return false;
					}
				}
			
			// Check rows and columns
			if (!isCorrect(aRow, aCol)) {
				return false;
				}
			}
		return true;
	}
	
	// Check if specified row and column are correct
	private boolean isCorrect(int[] aRow, int[] aCol) {
		Arrays.sort(aRow);
		Arrays.sort(aCol);
		
		for (int i = 0; i < 9; i++) {
			if (aRow[i] != i + 1 && aCol[i] != i + 1) {
				return false;
				}
			}
		return true;
	}
	
	// Check if Sudoku is in valid condition to start
	private boolean isValidToStart() {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if (values[i][j] != 0) {
					if (isContainedInBox(i, j, values[i][j]) ||
							isContainedInRowColumn(i, j, values[i][j])) {
						return false;
						}
					}
				}
			}
		return true;
	}
	
	// Make filled cells fixed
	private void markAsPreset() {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if (values[i][j] != 0) {
					if (!isContainedInBox(i, j, values[i][j]) &&
							!isContainedInRowColumn(i, j, values[i][j])) {
						cells[i][j].editable = false;
						cells[i][j].setEditable(false);
						cells[i][j].setForeground(new Color(150, 150, 150));
						}
					}
				}
			}
		}
	
	// Check if a value is incorrect or duplicated in the box
	private boolean isContainedInBox(int row, int col, int value) {
		// Find the top left of 3x3 box to start validating from
		int startRow = row/3 * 3;
		int startCol = col/3 * 3;
		
		// Check within the box except its cell
		for (int i = startRow; i < startRow + 3; i++) {
			for (int j = startCol; j < startCol + 3; j++) {
				if (!(i == row && j == col)) {
					if (values[i][j] == value) {
						return true;
						}
					}
				}
			}
		return false;
	}
	
	// Check if a value is duplicated in its row and column
	private boolean isContainedInRowColumn(int row, int col, int value) {
		for (int i = 0; i < 9; i++) {			
			if (i != col) {
				if (values[row][i] == value) { 
					return true;
					}
				}
			if (i != row) {
				if (values[i][col] == value){
					return true;
					}
				}
			}
		return false;
	}
	
	// Check if all cells are filled up
	private boolean isSudokuFull() {
		for (int i = 0; i < 9; i++){
			for (int j = 0; j < 9; j++) {
				if (values[i][j] == 0) {
					return false;
					}
				}
			}
		return true;		
	}
	
	// Recursive solution 
	private boolean solve(int row, int col) {
		if (row == 9){
			return true;
			}
		// If cell already set or fixed skip to next cell
		if (values[row][col] != 0) {
			if (solve(col == 8 ?(row + 1) :row, (col + 1) % 9)) {
				return true;
				}
			}
		else {
			// Random numbers from 1 to 9
			Integer[] randoms = generateRandomNumbers();
			for (int i = 0; i < 9; i++) {
				
				// If no duplicates assign the value and go to next cell
				if (!isContainedInRowColumn(row, col, randoms[i]) &&
						!isContainedInBox(row, col, randoms[i])) {
					values[row][col] = randoms[i];
					cells[row][col].setText(String.valueOf(randoms[i]));
					
					// Move to next cell left-to-right and top-to-bottom cell
					if (solve(col == 8? (row + 1) : row, (col + 1) % 9))
						return true;
					else { 
						// Backtracking
						values[row][col] = 0;
						cells[row][col].setText("");
					}
				}
			}
		}
		return false;
	}
	
	// Generate random unique numbers
	private Integer[] generateRandomNumbers() {
		ArrayList<Integer> randoms = new ArrayList<Integer>();
		for (int i = 0; i < 9; i++) {
			randoms.add(i + 1);
			}
		Collections.shuffle(randoms);
		
		return randoms.toArray(new Integer[9]);
	}
	
	// Represents Sudoku cell 	
	private class Cell extends JTextField {
		
		// flag for cell if can accept input
		private boolean editable;

		public Cell(final int row, final int col) {
			super(1);			
			editable = true;
			
			setBackground(Color.WHITE);
			setBorder(BorderFactory.createLineBorder(Color.BLACK));
			setHorizontalAlignment(CENTER);
			setPreferredSize(new Dimension(35, 35));
			setFont(new Font("Lucida Console", Font.BOLD, 28));
			
			addFocusListener(new FocusListener(){

				@Override
				public void focusGained(FocusEvent arg0) {
					// Change colors of fields
					int startRow = row/3 * 3;
					int startCol = col/3 * 3;

					for (int i = 0; i < 9; i++) {
						// Horizontal
						cells[i][col].setBackground(new Color(185, 220, 255));
						// Vertical
						cells[row][i].setBackground(new Color(185, 220, 255));
					}

					// 3x3 Box
					for (int i = startRow; i < startRow + 3; i++) {
						for (int j = startCol; j < startCol + 3; j++) {
							cells[i][j].setBackground(new Color(185, 220, 255));
							}
						}
					}
				
				@Override
				public void focusLost(FocusEvent arg0) {
					// Reset previous color of fields to white
					int startRow = row / 3 * 3;
					int startCol = col / 3 * 3;
					
					// Reset background color to white)
					for (int i = 0; i < 9; i++) {
						// Horizontal
						cells[i][col].setBackground(Color.WHITE);
						// Vertical
						cells[row][i].setBackground(Color.WHITE);
					}
					
					for (int i = startRow; i < startRow + 3; i++){
						for (int j = startCol; j < startCol + 3; j++){
							cells[i][j].setBackground(Color.WHITE);
							}
						}
					}
				});
			
			addKeyListener(new KeyAdapter() {			
				@Override
				public void keyPressed(KeyEvent e) {
					// Only numeric input
					if (editable)
						if (e.getKeyChar() >= '1' && e.getKeyChar() <= '9') {
							setEditable(true);
							setText("");
							values[row][col] = e.getKeyChar() - 48;
						} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
							setEditable(true);
							// Avoid beep sound
							setText("0"); 
							values[row][col] = 0;
						} else
							setEditable(false);
					
					// Navigation by arrow keys
					switch (e.getKeyCode()) {
					case KeyEvent.VK_DOWN:
						cells[(row + 1) % 9][col].requestFocusInWindow();
						break;
					case KeyEvent.VK_RIGHT:
						cells[row][(col + 1) % 9].requestFocusInWindow();
						break;
					case KeyEvent.VK_UP:
						cells[(row == 0)? 8 : (row - 1)][col].requestFocusInWindow();
						break;
					case KeyEvent.VK_LEFT:
						cells[row][(col == 0)? 8 : (col - 1)].requestFocusInWindow();
						break;
					}
				}
			});
		}
	}
	
	public static void main(String[] args) {
		new SudokuSolver();
	}
}
