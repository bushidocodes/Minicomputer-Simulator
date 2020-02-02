package pers.shane.demo;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Test {
    private JTextField Input;
    private JPanel rootPanel;
    private JLabel inputLabel;
    private JLabel addressLabel;
    private JLabel memoryLabel;
    private JButton haltButton;
    private JButton startButton;
    private JButton endButton;
    private JButton SSButton;
    private JButton LRButton;
    private JLabel outputNameLabel;
    private JTextField addressTextField;
    private JTextField memoryTextFiled;
    private JLabel R0Label;
    private JTextField R0TextField;
    private JTextField R1TextField;
    private JTextField R2TextField;
    private JLabel R1Label;
    private JLabel R2Label;
    private JLabel R3Label;
    private JTextField R3TextField;
    private JTextField IRTextField;
    private JLabel IRLabel;
    private JLabel X1Label;
    private JTextField X1TextField;
    private JLabel X2Label;
    private JTextField X2TestField;
    private JLabel X3Label;
    private JTextField X3TestField;
    private JLabel MARLabel;
    private JTextField MARTextField;
    private JTextField MBRTextField;
    private JLabel MBRLabel;
    private JLabel PCLabel;
    private JTextField PCTextField;
    private JButton runButton;
    private JTextField outputTextField;
    private JButton writeButton;
    private JButton readButton;

    public Test() {
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Write the codes here
                System.out.println("TestRun");     //A Test output for the listener method

            }
        });
        haltButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Write the codes here
                System.out.println("TestHalt");     //A Test output for the listener method

            }
        });
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Write the codes here
                System.out.println("TestStart");     //A Test output for the listener method

            }
        });
        endButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Write the codes here
                System.out.println("TestEnd");     //A Test output for the listener method

            }
        });
        SSButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Write the codes here
                System.out.println("TestSingle Step");     //A Test output for the listener method

            }
        });
        LRButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Write the codes here
                System.out.println("TestLoad Register");     //A Test output for the listener method

            }
        });
        readButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Write the codes here
                System.out.println("TestRead");     //A Test output for the listener method

            }
        });
        writeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Write the codes here
                System.out.println("TestWrite");     //A Test output for the listener method

            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Test");
        frame.setContentPane(new Test().rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
