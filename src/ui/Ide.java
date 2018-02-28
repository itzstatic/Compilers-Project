package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

public class Ide {
	
	private JButton compile = new  JButton("Compile");
	private JTextArea console = new JTextArea();
	private JTextField feedback = new JTextField();
	private DefaultHighlightPainter painter = new DefaultHighlightPainter(Color.red);
	
	public Ide() {
		JFrame frame = new JFrame("C-");
		frame.setSize(400, 200);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JScrollPane scroll = new JScrollPane(console);
		
		Font font = new Font("Arial", Font.PLAIN, 30);
		compile.setFont(font);
		console.setFont(font);
		
		feedback.setEditable(false);
		feedback.setFont(font);
		feedback.setForeground(Color.red);

		console.setTabSize(4);
		
		frame.setLayout(new BorderLayout());
		frame.add(scroll, BorderLayout.CENTER);
		frame.add(feedback, BorderLayout.SOUTH);
		frame.add(compile, BorderLayout.EAST);
		
		frame.setVisible(true);
	}
	
	public void addButtonListener(ActionListener listener) {
		compile.addActionListener(listener);
	}
	
	public void highlight(int from, int to) {
		try {
			console.getHighlighter().addHighlight(from, to, painter);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	public void showFeedback(String message, boolean accepted) {
		feedback.setForeground(accepted ? Color.green : Color.red);
		feedback.setText(message);
		if (accepted) {
		}
	}

	public String getSourceCode() {
		return console.getText();
	}
}
