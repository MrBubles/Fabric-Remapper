package dev.hunter.deobf;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Gui {

    private static final int gap = 3;
    public JTextField outputField;
    public JFileChooser chooser;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    private Path input;
    private Path output;
    private String minecraftVersion;
    private boolean decompile;

    public static void displayMessage(String message, String title, int status) {
        JPanel panel = new JPanel(new BorderLayout(gap, gap));
        panel.setBorder(BorderFactory.createEmptyBorder(gap, gap, gap, gap));

        JLabel messageLabel = new JLabel(message);
        panel.add(messageLabel, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(null, panel, title, status);
    }

    private static Path getCurrentPath() {
        return Paths.get("").toAbsolutePath();
    }

    public Gui start() {
        System.out.println("Starting gui...");

        chooser = new JFileChooser(getCurrentPath().toFile());
        JFrame frame = new JFrame("multi-decompiler-gui");
        frame.setSize(400, 250);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = screenSize.height;
        int width = screenSize.width;
        frame.setLocation(width / 2 - frame.getWidth() / 2, height / 2 - frame.getHeight() / 2);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        startButtons(frame);
        return this;
    }

    private synchronized void startButtons(JFrame frame) {
        executor.execute(() -> {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(gap, gap, gap, gap);

            JLabel inputLabel = new JLabel("Input: ");
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.LINE_END;
            panel.add(inputLabel, c);

            JTextField inputField = new JTextField("", gap);
            c.gridx = 1;
            c.gridy = 0;
            c.fill = GridBagConstraints.HORIZONTAL;
            inputField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    input = Paths.get(inputField.getText());
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    input = Paths.get(inputField.getText());
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    input = Paths.get(inputField.getText());
                }
            });
            panel.add(inputField, c);

            JButton inputChooser = new JButton("Change");
            c.gridx = 2;
            c.gridy = 0;
            c.fill = GridBagConstraints.NONE;
            inputChooser.addActionListener((event) -> input = handleInputChooserAction(inputField, input));
            panel.add(inputChooser, c);

            JLabel outputLabel = new JLabel("Output: ");
            c.gridx = 0;
            c.gridy = 1;
            c.anchor = GridBagConstraints.LINE_END;
            panel.add(outputLabel, c);

            outputField = new JTextField("", gap);
            c.gridx = 1;
            c.gridy = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            outputField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    output = Paths.get(outputField.getText());
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    output = Paths.get(outputField.getText());
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    output = Paths.get(outputField.getText());
                }
            });
            panel.add(outputField, c);

            JButton outputChooser = new JButton("Change");
            c.gridx = 2;
            c.gridy = 1;
            c.fill = GridBagConstraints.NONE;
            outputChooser.addActionListener((event) -> output = handleInputChooserAction(outputField, output));
            panel.add(outputChooser, c);

            JCheckBox decompileCheckBox = new JCheckBox("Decompile", false);
            c.gridx = 0;
            c.gridy = 2;
            c.anchor = GridBagConstraints.LINE_END;
            decompileCheckBox.addActionListener((event) -> decompile = decompileCheckBox.isSelected());
            panel.add(decompileCheckBox, c);

            JButton deobfuscateButton = new JButton("Deobfuscate");
            c.gridx = 1;
            c.gridy = 3;
            c.fill = GridBagConstraints.CENTER;
            deobfuscateButton.addActionListener((event) -> {
                try {
                    Main.remap(input, output, minecraftVersion, this.decompile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            panel.add(deobfuscateButton, c);

            frame.add(panel);
            frame.setVisible(true);
        });
    }

    private Path handleInputChooserAction(JTextField field, Path originalField) {
        chooser.setVisible(true);
        int option = chooser.showSaveDialog(null);
        if (option == JFileChooser.APPROVE_OPTION) {
            String selectedFilePath = chooser.getSelectedFile().getAbsolutePath();
            field.setText(selectedFilePath);
            chooser.setVisible(false);
            return Paths.get(selectedFilePath);
        }
        return originalField;
    }
}