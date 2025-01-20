package filius.gui.anwendungssicht;

import filius.software.clientserver.NotepadPlusPlus;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;

import filius.software.system.*;
// import filius.Main;
// import filius.gui.CloseableBrowserTabbedPaneUI;
// import filius.software.system.Betriebssystem;

public class GUIApplicationNotepadPlusPlusWindow extends GUIApplicationWindow {
    private JTextPane textPane;
    private JScrollPane scrollPane;
    private JPanel noWrapPanel;
    private JTextField fileNameField;

    JMenuItem noneOption;
    JMenuItem javaOption;
    JMenuItem htmlOption;
    JMenuItem cssOption;

    private JButton openButton;
    private JButton saveButton;
    private JFileChooser fileChooser;
    private JPanel panel;
    private boolean isSyntaxHighlightingEnabled = false;
    private boolean isWordWrapEnabled = false;

    private final SimpleAttributeSet keywordStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet dataTypeStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet normalStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet modifierStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet stringStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet commentStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet doctypeStyle = new SimpleAttributeSet();


    // Declare the line numbers area here
    private JTextArea lineNumbersArea;

    private String currentFilePath = null;
    private String currentLanguage = "None"; // Default 

    public GUIApplicationNotepadPlusPlusWindow(GUIDesktopPanel desktop, String appName) {
        super(desktop, appName);
        setTitle("Notepad++ Filius");
        initializeStyles();
        initializeUI();
    }

    private void initializeStyles() {
        StyleConstants.setForeground(keywordStyle, java.awt.Color.BLUE);
        StyleConstants.setBold(keywordStyle, true);

        StyleConstants.setForeground(dataTypeStyle, new java.awt.Color(70, 160, 170));
        StyleConstants.setBold(dataTypeStyle, true);

        StyleConstants.setForeground(normalStyle, java.awt.Color.BLACK);

        StyleConstants.setForeground(modifierStyle, java.awt.Color.MAGENTA);
        StyleConstants.setBold(modifierStyle, true);

        StyleConstants.setForeground(stringStyle, new java.awt.Color(160, 120, 70));
        StyleConstants.setItalic(stringStyle, true);

        StyleConstants.setForeground(commentStyle, new java.awt.Color(50, 150, 50));
        StyleConstants.setItalic(commentStyle, true);

        StyleConstants.setForeground(doctypeStyle, new java.awt.Color(120, 120, 120));
        StyleConstants.setBold(doctypeStyle, true);
    }

    private void initializeUI() {
        StyledDocument doc = new DefaultStyledDocument() {
            @Override
            public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
                super.insertString(offset, str, a);
                applySyntaxHighlighting();
            }

            @Override
            public void remove(int offs, int len) throws BadLocationException {
                super.remove(offs, len);
                applySyntaxHighlighting();
            }
        };

        textPane = new JTextPane(doc);

        //default no word wrap
        JPanel noWrapPanel = new JPanel( new BorderLayout() );
        noWrapPanel.add( textPane );
        

        

        // Initialize line numbers area here
        lineNumbersArea = new JTextArea();
        lineNumbersArea.setEditable(false);
        lineNumbersArea.setBackground(new java.awt.Color(235, 235, 235));
        lineNumbersArea.setFont(textPane.getFont());


        scrollPane = new JScrollPane(noWrapPanel);
        scrollPane.setPreferredSize(new Dimension(355, 335));

        // Scroll Pane for the Line Numbers
        JScrollPane lineNumbersScrollPane = new JScrollPane(lineNumbersArea);
        lineNumbersScrollPane.setPreferredSize(new Dimension(25, 325));

        // Panel for Buttons
        openButton = new JButton("Open");
        saveButton = new JButton("Save as");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(openButton);
        buttonPanel.add(saveButton);

        // ActionListeners for Buttons
        openButton.addActionListener(this::openFile);
        saveButton.addActionListener(this::saveFile);

        // Add Menu Bar
        JMenuBar menuBar = new JMenuBar();

        JMenu optionsMenu = new JMenu("Options");
            JMenu languageMenu = new JMenu("Language");
                noneOption = new JMenuItem("\u2713  None");
                javaOption = new JMenuItem("   Java");
                htmlOption = new JMenuItem("   HTML");
                cssOption = new JMenuItem("   CSS");
            
            JMenuItem wordWrapMenu = new JMenuItem("   Word wrap");


        JMenu fileMenu = new JMenu("File");
            JMenuItem save = new JMenuItem("Save");
            JMenuItem savePC = new JMenuItem("Save to PC");
            JMenuItem open = new JMenuItem("Open");
            JMenuItem openPC = new JMenuItem("Open from PC");
            JMenuItem newFile = new JMenuItem("New...");

        JMenu editMenu = new JMenu("Edit"); // Add Edit menu
            JMenuItem findReplace = new JMenuItem("Find and Replace"); // Add Find and Replace option


        save.addActionListener(this::saveFile);
        savePC.addActionListener(this::saveFileToPC);
        open.addActionListener(this::openFile);
        openPC.addActionListener(this::openFileFromPC);
        
        newFile.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to discard unsaved changes?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                textPane.setText("");  // Clear text
                setTitle("Notepad++ Filius");  // Reset title
                currentFilePath = null;  // Reset current file path
            }
        });

        findReplace.addActionListener(e -> showFindAndReplaceDialog());
        

        noneOption.addActionListener(e -> switchLanguage("None"));
        javaOption.addActionListener(e -> switchLanguage("Java"));
        htmlOption.addActionListener(e -> switchLanguage("HTML"));
        cssOption.addActionListener(e -> switchLanguage("CSS"));

        wordWrapMenu.addActionListener(e -> toggleWordWrap(wordWrapMenu));

        fileMenu.add(save);
        fileMenu.add(savePC);
        fileMenu.add(open);
        fileMenu.add(openPC);
        fileMenu.add(newFile);

        editMenu.add(findReplace); // Add Find and Replace to Edit menu

        languageMenu.add(noneOption);
        languageMenu.add(javaOption);
        languageMenu.add(htmlOption);
        languageMenu.add(cssOption);
        optionsMenu.add(languageMenu);
        optionsMenu.add(wordWrapMenu);

        menuBar.add(fileMenu);      // Add File menu to the menu bar
        menuBar.add(editMenu);      // Add Edit menu to the menu bar
        menuBar.add(optionsMenu);   // Add Options menu to the menu bar

        JPanel menuBarPanel = new JPanel(new BorderLayout());
        menuBarPanel.add(menuBar);

        JPanel topPanel = new JPanel(new GridLayout(2, 0));
        topPanel.add(menuBarPanel);
        topPanel.add(buttonPanel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(lineNumbersScrollPane, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.SOUTH);
    }

    
    //////////////////////////////////
    /// Syntax highlighting options //
    //////////////////////////////////
    private void switchLanguage(String language) {
        currentLanguage = language;
        updateLanguageMenuSelection(); // update menu entrys
        if ("None".equals(language)) {
            clearHighlighting(); // Deaktiviert Syntax-Hervorhebung
        } else {
            applySyntaxHighlighting(); // Aktiviert Syntax-Hervorhebung
        }
    }

    private void applySyntaxHighlighting() {
        if ("None".equals(currentLanguage)) {
            clearHighlighting();
            return;
        }

        String text = textPane.getText().replace("\r\n", "\n");;
        StyledDocument doc = textPane.getStyledDocument();
        doc.setCharacterAttributes(0, text.length(), normalStyle, true);

        switch (currentLanguage) {
            case "Java":
                highlightJava(doc, text);
                break;
            case "HTML":
                highlightHTML(doc, text);
                break;
            case "CSS":
                highlightCSS(doc, text);
                break;
        }
    }

    private void highlightJava(StyledDocument doc, String text) {
        // Map<String, SimpleAttributeSet> patterns = Map.of(
        //     "\b(if|else|for|while|return|class|static|void)\b", keywordStyle,
        //     "\b(int|double|float|string|char|bool)\b", dataTypeStyle,
        //     "\b(public|private|protected)\b", modifierStyle,
        //     "\"(.*?)\"", stringStyle
        // );
    
        // patterns.forEach((pattern, style) -> highlightPattern(doc, text, pattern, style));

        String[] keywords = {"if", "else", "for", "while", "return", "class", "static", "void"};
        for (String keyword : keywords) {
            highlightPattern(doc, text, "\\b" + keyword + "\\b", keywordStyle);
        }

        String[] dataTypeKeywords = {"int", "double", "float", "string", "char", "bool"};
        for (String keyword : keywords) {
            highlightPattern(doc, text, "\\b" + keyword + "\\b", dataTypeStyle);
        }
        
        String[] modifiers = {"public", "private", "protected"};
        for (String modifier : modifiers) {
            highlightPattern(doc, text, "\\b" + modifier + "\\b", modifierStyle);
        }

        highlightPattern(doc, text, "\"(.*?)\"", stringStyle);

        // Highlight comments
        highlightPattern(doc, text, "//.*", commentStyle); // Single-line comments
        highlightPattern(doc, text, "/\\*.*?\\*/", commentStyle); // Multi-line comments
    }
    

    private void highlightHTML(StyledDocument doc, String text) {
        doc.setCharacterAttributes(0, text.length(), normalStyle, true);
        highlightPattern(doc, text, "<[^>]+>", keywordStyle);
        highlightPattern(doc, text, "\"(.*?)\"", stringStyle);
        highlightPattern(doc, text, "<!--.*?-->", commentStyle); // Comments
        highlightPattern(doc, text, "<![Dd][Oo][Cc][Tt][Yy][Pp][Ee][^>]*>", doctypeStyle); // Doctype override style
    }

    private void highlightCSS(StyledDocument doc, String text) {
        highlightPattern(doc, text, "[a-zA-Z-]+(?=:)", keywordStyle);
        highlightPattern(doc, text, "\"(.*?)\"", stringStyle);
        highlightPattern(doc, text, "/\\*.*?\\*/", commentStyle); // Comments
    }

    private void highlightPattern(StyledDocument doc, String text, String pattern, AttributeSet style) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        while (m.find()) {
            doc.setCharacterAttributes(m.start(), m.end() - m.start(), style, true);
        }
    }

    private void clearHighlighting() {
        StyledDocument doc = textPane.getStyledDocument();
        doc.setCharacterAttributes(0, doc.getLength(), normalStyle, true);
    }

    private void updateLanguageMenuSelection() {
        noneOption.setText("   None");
        javaOption.setText("   Java");
        htmlOption.setText("   HTML");
        cssOption.setText("   CSS");
    
        switch (currentLanguage) {
            case "None":
                noneOption.setText("\u2713  None"); // Unicode-tick
                break;
            case "Java":
                javaOption.setText("\u2713  Java");
                break;
            case "HTML":
                htmlOption.setText("\u2713  HTML");
                break;
            case "CSS":
                cssOption.setText("\u2713  CSS");
                break;
        }
    }
    

    private void toggleWordWrap(JMenuItem item) {
        isWordWrapEnabled = !isWordWrapEnabled; // Zustand umschalten
    
        if (isWordWrapEnabled) {
            item.setText("\u2713   Word wrap");
            // TextPane direkt in ScrollPane verwenden (Word Wrap aktiviert)
            scrollPane.setViewportView(textPane);
        } else {
            item.setText("   Word wrap");
            // Panel ohne Word Wrap verwenden
            if (noWrapPanel == null) {
                noWrapPanel = new JPanel(new BorderLayout());
                noWrapPanel.add(textPane);
            }
            scrollPane.setViewportView(noWrapPanel);
        }
    
        // update UI
        scrollPane.revalidate();
        scrollPane.repaint();
    }

    private void showFindAndReplaceDialog() {
        JDialog findReplaceDialog = new JDialog((Frame) null, "Find and Replace", true);
        findReplaceDialog.setLayout(new BorderLayout());

        JCheckBox enableReplaceCheckBox = new JCheckBox("Enable Replace");
        JTextField findField = new JTextField();
        JTextField replaceField = new JTextField();
        replaceField.setEnabled(false); // Initially disabled

        JLabel occurrencesLabel = new JLabel("Occurrences: 0");

        enableReplaceCheckBox.addActionListener(ev -> {
            replaceField.setEnabled(enableReplaceCheckBox.isSelected());
        });

        JButton searchForwardButton = new JButton("\u2192"); // Unicode for right arrow
        JButton searchBackwardButton = new JButton("\u2190"); // Unicode for left arrow
        JButton replaceCurrentButton = new JButton("Replace");

        searchForwardButton.addActionListener(ev -> {
            String findText = findField.getText();
            if (!findText.isEmpty()) {
                int startIndex = textPane.getCaretPosition();
                String content = textPane.getText();
                int index = content.indexOf(findText, startIndex);

                if (index != -1) {
                    textPane.setCaretPosition(index + findText.length());
                    textPane.select(index, index + findText.length());
                } else {
                    JOptionPane.showMessageDialog(findReplaceDialog, "No more occurrences found.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        searchBackwardButton.addActionListener(ev -> {
            String findText = findField.getText();
            if (!findText.isEmpty()) {
                int startIndex = textPane.getCaretPosition() - 1;
                String content = textPane.getText();
                int index = content.lastIndexOf(findText, startIndex);

                if (index != -1) {
                    textPane.setCaretPosition(index);
                    textPane.select(index, index + findText.length());
                } else {
                    JOptionPane.showMessageDialog(findReplaceDialog, "No more occurrences found.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        replaceCurrentButton.addActionListener(ev -> {
            String findText = findField.getText();
            String replaceText = replaceField.getText();
            if (enableReplaceCheckBox.isSelected() && textPane.getSelectedText() != null && textPane.getSelectedText().equals(findText)) {
                textPane.replaceSelection(replaceText);
            }
        });

        findField.addCaretListener(ev -> {
            String findText = findField.getText();
            if (!findText.isEmpty()) {
                String content = textPane.getText();
                int occurrences = countOccurrences(content, findText);
                occurrencesLabel.setText("Occurrences: " + occurrences);
            } else {
                occurrencesLabel.setText("Occurrences: 0");
            }
        });

        JPanel fieldsPanel = new JPanel(new GridLayout(2, 2));
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JPanel bottomPanel = new JPanel(new BorderLayout());

        fieldsPanel.add(new JLabel("Find:"));
        fieldsPanel.add(findField);
        fieldsPanel.add(enableReplaceCheckBox);
        fieldsPanel.add(replaceField);

        buttonsPanel.add(searchBackwardButton);
        buttonsPanel.add(searchForwardButton);
        buttonsPanel.add(replaceCurrentButton);

        bottomPanel.add(occurrencesLabel, BorderLayout.SOUTH);

        findReplaceDialog.add(fieldsPanel, BorderLayout.NORTH);
        findReplaceDialog.add(buttonsPanel, BorderLayout.CENTER);
        findReplaceDialog.add(bottomPanel, BorderLayout.SOUTH);

        findReplaceDialog.setSize(400, 200);
        findReplaceDialog.setLocationRelativeTo(this);
        findReplaceDialog.setVisible(true);
    }

    
    private int countOccurrences(String content, String findText) {
        int count = 0;
        int index = 0;
        while ((index = content.indexOf(findText, index)) != -1) {
            count++;
            index += findText.length();
        }
        return count;
    }

    private void openFileFromPC(ActionEvent e) {
        fileChooser = new JFileChooser();
        java.io.File workingDirectory = new java.io.File(System.getProperty("user.dir"));
        fileChooser.setCurrentDirectory(workingDirectory);

        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
                textPane.read(reader, null);
                reader.close();

                currentFilePath = file.getAbsolutePath();
                setTitle("Notepad++: " + file.getName());
                setLanguageBasedOnExtension(file.getName());
                // if (isSyntaxHighlightingEnabled) {
                //     applySyntaxHighlighting();
                // }
            } catch (java.io.IOException ioException) {
                JOptionPane.showMessageDialog(this, "Failed to open file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveFileToPC(ActionEvent e) {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }

        if (currentFilePath != null) {
            fileChooser.setSelectedFile(new java.io.File(currentFilePath));
        }

        int returnValue = fileChooser.showSaveDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(file));
                textPane.write(writer);
                writer.close();

                currentFilePath = file.getAbsolutePath();
                setTitle("Notepad++: " + file.getName());
                setLanguageBasedOnExtension(file.getName());
            } catch (java.io.IOException ioException) {
                JOptionPane.showMessageDialog(this, "Failed to save file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openFile(ActionEvent e) {
                Datei currentFile = null;
                DMTNFileChooser fc = new DMTNFileChooser( (Betriebssystem) holeAnwendung().getSystemSoftware() );
                int rueckgabe = fc.openDialog();

                if (rueckgabe == DMTNFileChooser.OK) {
                    currentFile = holeAnwendung().getSystemSoftware().getDateisystem().holeDatei(fc.getAktuellerOrdner(), fc.getAktuellerDateiname());

                    setTitle(currentFile.getName());
                    textPane.setText(currentFile.getDateiInhalt());
                    setLanguageBasedOnExtension(currentFile.getName());
                    if (isSyntaxHighlightingEnabled) {
                        applySyntaxHighlighting();
                    }
                } else {
                    // JOptionPane.showMessageDialog(this, "ERROR (" + this.hashCode() + "): Fehler beim oeffnen einer Datei", "Error", JOptionPane.ERROR_MESSAGE);
                }
    }

    private void saveFile(ActionEvent e) {
        Datei currentFile = null;
        DMTNFileChooser fc = new DMTNFileChooser( (Betriebssystem) holeAnwendung().getSystemSoftware() );

        int rueckgabe = fc.saveDialog();

		if (rueckgabe == DMTNFileChooser.OK) {
			String fileName = fc.getAktuellerDateiname();
			currentFile = new Datei(fileName, messages.getString("texteditor_msg8"), textPane.getText());
			this.holeAnwendung().getSystemSoftware().getDateisystem().speicherDatei(fc.getAktuellerOrdner(), currentFile);
			setTitle(currentFile.getName());
            setLanguageBasedOnExtension(currentFile.getName());
		}
    }


    private void setLanguageBasedOnExtension(String fileName) {
        if (fileName.endsWith(".java")) {
            switchLanguage("Java");
        } else if (fileName.endsWith(".html")) {
            switchLanguage("HTML");
        } else if (fileName.endsWith(".css")) {
            switchLanguage("CSS");
        } else {
            switchLanguage("None"); // Standard: Keine Sprache
        }
    }





    @Override
    public void update(Observable o, Object arg) {
        return;
    }
}


