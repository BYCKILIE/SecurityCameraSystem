package Server;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerGUI {
    private final HashMap<String, ArrayList<Boolean>> controlVariables;
    private final HashMap<String, JButton> identifier = new HashMap<>();
    private final JPanel controlPanel = new JPanel();

    public ServerGUI(HashMap<String, ArrayList<Boolean>> controlVariables) {
        this.controlVariables = controlVariables;

        JFrame frame = new JFrame("SecurityCameras Server");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(640, 800));
        frame.setIconImage(new ImageIcon("images/server.jpg").getImage());

        JPanel parentPanel = new JPanel();
        parentPanel.setLayout(new BoxLayout(parentPanel, BoxLayout.Y_AXIS));

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBackground(new Color(0xD2282828));
        textArea.setFont(new Font("arial", Font.BOLD, 15));
        textArea.setForeground(Color.white);

        JScrollPane textScrollPane = new JScrollPane(textArea);
        textScrollPane.setPreferredSize(new Dimension(0, 400));

        PrintStream printStream = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                textArea.append(String.valueOf((char) b));
                textArea.setCaretPosition(textArea.getDocument().getLength());
            }
        });

        System.setOut(printStream);
        System.setErr(printStream);

        controlPanel.setBackground(new Color(0xD2282828));
        controlPanel.setBorder(BorderFactory.createLineBorder(Color.white, 1));
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));

        JScrollPane panelScrollPane = new JScrollPane(controlPanel);
        panelScrollPane.setPreferredSize(new Dimension(0, 400));

        parentPanel.add(textScrollPane);
        parentPanel.add(panelScrollPane);

        frame.add(parentPanel);
        frame.setVisible(true);
    }

    public void createNewClient(String id, String signature) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(120, 30));
        button.setText(id);
        button.setToolTipText(signature);
        button.setFont(new Font("arial", Font.PLAIN, 14));

        JPopupMenu settingsMenu = new JPopupMenu();
        JMenuItem remove = new JMenuItem("Remove Client");
        JMenuItem stream = new JMenuItem("Streaming (ON)");
        JMenuItem alarm = new JMenuItem("Alarm (ON)");
        settingsMenu.add(remove);
        settingsMenu.add(stream);
        settingsMenu.add(alarm);

        remove.addActionListener(e -> removeClient(id));

        stream.addActionListener(e -> {
            if (controlVariables.get(id).get(1)) {
                controlVariables.get(id).set(1, false);
                stream.setText("Streaming (ON)");
            } else {
                controlVariables.get(id).set(1, true);
                stream.setText("Streaming(OFF)");
            }
        });

        alarm.addActionListener(e -> {
            if (controlVariables.get(id).get(2)) {
                controlVariables.get(id).set(2, false);
                alarm.setText("Alarm (OFF)");
            } else {
                controlVariables.get(id).set(2, true);
                alarm.setText("Alarm (ON)");
            }
        });

        button.addActionListener(e -> settingsMenu.show(button, 0, button.getHeight()));

        identifier.put(id, button);

        controlPanel.add(button);
        controlPanel.revalidate();
        controlPanel.repaint();
    }

    void removeClient(String id) {
        controlVariables.get(id).set(0, false);
        controlPanel.remove(identifier.get(id));
        controlPanel.revalidate();
        controlPanel.repaint();
    }


}
