package com.imageng.gui;

import com.imageng.model.ArtStyle;
import com.imageng.model.AspectRatio;
import com.imageng.model.GenerateRequest;
import com.imageng.model.GenerateResponse;
import com.imageng.service.impl.AiImageGenerationServiceImpl;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.List;

public class ImageGeneratorSwingApp extends JFrame {

    private final AiImageGenerationServiceImpl service = new AiImageGenerationServiceImpl();

    private JTextArea promptArea;
    private JComboBox<String> styleComboBox;
    private JComboBox<String> ratioComboBox;
    private JButton generateButton;
    private JButton downloadButton;
    private JButton copyUrlButton;
    private JLabel statusLabel;
    private JLabel imagePreviewLabel;
    private JProgressBar progressBar;

    private String currentImageUrl = null;
    private byte[] currentImageBytes = null;

    private static final String[] EXAMPLE_PROMPTS = {
            "A majestic phoenix rising from cosmic flames in space",
            "Cyberpunk cityscape at night with neon reflections",
            "Ancient magical library with floating books and crystals",
            "Underwater kingdom with bioluminescent creatures",
            "Steampunk airship floating above Victorian clouds"
    };

    public ImageGeneratorSwingApp() {
        setTitle("AI Text-to-Image Generator - Java Desktop");
        setSize(1000, 780);
        setMinimumSize(new Dimension(850, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initDarkTheme();
        initUI();
    }

    private void initDarkTheme() {
        UIManager.put("Panel.background", new Color(18, 18, 28));
        UIManager.put("Label.foreground", new Color(240, 240, 255));
        UIManager.put("Button.background", new Color(45, 35, 75));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("TextArea.background", new Color(25, 25, 40));
        UIManager.put("TextArea.foreground", Color.WHITE);
        UIManager.put("TextArea.caretColor", Color.WHITE);
        UIManager.put("ComboBox.background", new Color(30, 30, 48));
        UIManager.put("ComboBox.foreground", Color.WHITE);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(15, 15, 26));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(15, 15, 26));
        JLabel titleLabel = new JLabel("✨ AI Text-to-Image Generator (Java)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(168, 85, 247));
        JLabel subtitleLabel = new JLabel("Transform your imagination into stunning visuals with Java");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(160, 160, 180));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Center split: Left Controls, Right Preview
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.45);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        // Controls Panel
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setBackground(new Color(24, 24, 38));
        controlsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 50, 95), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Prompt section
        JLabel promptTitle = new JLabel("Describe Your Vision:");
        promptTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        promptTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        promptArea = new JTextArea(4, 25);
        promptArea.setLineWrap(true);
        promptArea.setWrapStyleWord(true);
        promptArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        promptArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane promptScroll = new JScrollPane(promptArea);
        promptScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        promptScroll.setBorder(BorderFactory.createLineBorder(new Color(80, 70, 120)));

        // Examples
        JPanel examplePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        examplePanel.setOpaque(false);
        examplePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel egLabel = new JLabel("Try: ");
        egLabel.setForeground(new Color(180, 180, 210));
        examplePanel.add(egLabel);
        for (String eg : EXAMPLE_PROMPTS) {
            String shortText = eg.length() > 22 ? eg.substring(0, 22) + "..." : eg;
            JButton egBtn = new JButton(shortText);
            egBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            egBtn.setBackground(new Color(36, 32, 58));
            egBtn.addActionListener(e -> promptArea.setText(eg));
            examplePanel.add(egBtn);
        }

        // Style selector
        JLabel styleLabel = new JLabel("Art Style:");
        styleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        styleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] styleNames = ArtStyle.getAll().stream().map(ArtStyle::getDisplayName).toArray(String[]::new);
        styleComboBox = new JComboBox<>(styleNames);
        styleComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        styleComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        // Aspect ratio selector
        JLabel ratioLabel = new JLabel("Aspect Ratio:");
        ratioLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ratioLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] ratioNames = AspectRatio.getAll().stream().map(r -> r.getName() + " (" + r.getRatio() + ")").toArray(String[]::new);
        ratioComboBox = new JComboBox<>(ratioNames);
        ratioComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        ratioComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        // Generate button
        generateButton = new JButton("✨ Generate Image");
        generateButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        generateButton.setBackground(new Color(139, 92, 246));
        generateButton.setForeground(Color.WHITE);
        generateButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        generateButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        generateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        generateButton.addActionListener(e -> generateImage());

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(180, 180, 200));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Web launch button
        JButton webBtn = new JButton("🌐 Open Web App (http://localhost:8080)");
        webBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        webBtn.setBackground(new Color(30, 41, 59));
        webBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        webBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        webBtn.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI("http://localhost:8080"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not open browser: " + ex.getMessage());
            }
        });

        // Assemble Controls
        controlsPanel.add(promptTitle);
        controlsPanel.add(Box.createVerticalStrut(8));
        controlsPanel.add(promptScroll);
        controlsPanel.add(Box.createVerticalStrut(6));
        controlsPanel.add(examplePanel);
        controlsPanel.add(Box.createVerticalStrut(12));
        controlsPanel.add(styleLabel);
        controlsPanel.add(Box.createVerticalStrut(6));
        controlsPanel.add(styleComboBox);
        controlsPanel.add(Box.createVerticalStrut(12));
        controlsPanel.add(ratioLabel);
        controlsPanel.add(Box.createVerticalStrut(6));
        controlsPanel.add(ratioComboBox);
        controlsPanel.add(Box.createVerticalStrut(18));
        controlsPanel.add(generateButton);
        controlsPanel.add(Box.createVerticalStrut(8));
        controlsPanel.add(progressBar);
        controlsPanel.add(Box.createVerticalStrut(6));
        controlsPanel.add(statusLabel);
        controlsPanel.add(Box.createVerticalGlue());
        controlsPanel.add(webBtn);

        // Preview Panel
        JPanel previewContainer = new JPanel(new BorderLayout(10, 10));
        previewContainer.setBackground(new Color(24, 24, 38));
        previewContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 50, 95), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        imagePreviewLabel = new JLabel("Your generated artwork will appear here", SwingConstants.CENTER);
        imagePreviewLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        imagePreviewLabel.setForeground(new Color(130, 130, 160));
        imagePreviewLabel.setOpaque(true);
        imagePreviewLabel.setBackground(new Color(15, 15, 24));
        imagePreviewLabel.setBorder(BorderFactory.createDashedBorder(new Color(80, 70, 120), 2, 5, 2, false));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        downloadButton = new JButton("💾 Download Image");
        downloadButton.setEnabled(false);
        downloadButton.setBackground(new Color(16, 185, 129));
        downloadButton.addActionListener(e -> saveImageToFile());

        copyUrlButton = new JButton("📋 Copy URL");
        copyUrlButton.setEnabled(false);
        copyUrlButton.setBackground(new Color(79, 70, 229));
        copyUrlButton.addActionListener(e -> copyImageUrlToClipboard());

        actionPanel.add(copyUrlButton);
        actionPanel.add(downloadButton);

        previewContainer.add(imagePreviewLabel, BorderLayout.CENTER);
        previewContainer.add(actionPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(controlsPanel);
        splitPane.setRightComponent(previewContainer);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void generateImage() {
        String prompt = promptArea.getText().trim();
        if (prompt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a prompt first!", "Prompt Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        generateButton.setEnabled(false);
        progressBar.setVisible(true);
        statusLabel.setText("Generating magic with AI...");
        imagePreviewLabel.setIcon(null);
        imagePreviewLabel.setText("Creating your masterpiece...");

        String selectedStyle = (String) styleComboBox.getSelectedItem();
        String selectedRatioFull = (String) ratioComboBox.getSelectedItem();
        String ratioValue = selectedRatioFull != null && selectedRatioFull.contains("(")
                ? selectedRatioFull.substring(selectedRatioFull.indexOf("(") + 1, selectedRatioFull.indexOf(")"))
                : "1:1";

        GenerateRequest req = new GenerateRequest(prompt, selectedStyle, ratioValue);

        SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                GenerateResponse response = service.generate(req);
                if (!response.isSuccess()) {
                    throw new RuntimeException(response.getError());
                }
                currentImageUrl = response.getImageUrl();
                currentImageBytes = service.downloadImageBytes(currentImageUrl);
                if (currentImageBytes != null && currentImageBytes.length > 0) {
                    return ImageIO.read(new java.io.ByteArrayInputStream(currentImageBytes));
                }
                throw new RuntimeException("Could not fetch generated image");
            }

            @Override
            protected void done() {
                generateButton.setEnabled(true);
                progressBar.setVisible(false);
                try {
                    BufferedImage img = get();
                    if (img != null) {
                        displayImage(img);
                        statusLabel.setText("Done! Generated successfully.");
                        downloadButton.setEnabled(true);
                        copyUrlButton.setEnabled(true);
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Failed: " + ex.getMessage());
                    imagePreviewLabel.setText("Generation error. Please try again.");
                    JOptionPane.showMessageDialog(ImageGeneratorSwingApp.this, "Error generating image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void displayImage(BufferedImage img) {
        int targetWidth = imagePreviewLabel.getWidth() - 20;
        int targetHeight = imagePreviewLabel.getHeight() - 20;
        if (targetWidth <= 0) targetWidth = 512;
        if (targetHeight <= 0) targetHeight = 512;

        double scale = Math.min((double) targetWidth / img.getWidth(), (double) targetHeight / img.getHeight());
        int w = (int) (img.getWidth() * scale);
        int h = (int) (img.getHeight() * scale);

        Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        imagePreviewLabel.setText("");
        imagePreviewLabel.setIcon(new ImageIcon(scaled));
    }

    private void saveImageToFile() {
        if (currentImageBytes == null) return;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("ai-generated-" + System.currentTimeMillis() + ".png"));
        int ret = fileChooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            try (FileOutputStream fos = new FileOutputStream(fileChooser.getSelectedFile())) {
                fos.write(currentImageBytes);
                JOptionPane.showMessageDialog(this, "Image saved successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not save file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void copyImageUrlToClipboard() {
        if (currentImageUrl == null) return;
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(currentImageUrl), null);
        JOptionPane.showMessageDialog(this, "Image URL copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ImageGeneratorSwingApp app = new ImageGeneratorSwingApp();
            app.setVisible(true);
        });
    }
}
