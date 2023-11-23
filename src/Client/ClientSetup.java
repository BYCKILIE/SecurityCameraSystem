package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientSetup implements ActionListener {
    JFrame frame = new JFrame("Client Setup");
    JButton start = new JButton("Connect");
    JTextField entry = new JTextField();
    JComboBox<String> devices;

    public ClientSetup(int connectedCameras) {
        frame.setResizable(false);
        frame.setIconImage(new ImageIcon("images/setup.png").getImage());
        frame.setLayout(new FlowLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


        JPanel[] panels = new JPanel[2];
        for (int i = 0; i < 2; i++) {
            panels[i] = new JPanel();
            panels[i].setPreferredSize(new Dimension(170, 125));
        }

        panels[0].setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        panels[1].setLayout(new FlowLayout(FlowLayout.LEFT, 0, 10));

        String[] texts = new String[]{"IP Address:", "Camera:"};
        JPanel[] helpers = new JPanel[texts.length];
        JLabel[] labels = new JLabel[texts.length];

        for (int i = 0; i < texts.length; i++) {
            helpers[i] = new JPanel();
            helpers[i].setPreferredSize(new Dimension(150, 30));
            helpers[i].setLayout(new FlowLayout(FlowLayout.RIGHT));
            labels[i] = new JLabel(texts[i]);
            labels[i].setFont(new Font("arial", Font.BOLD, 15));
            helpers[i].add(labels[i]);
            panels[0].add(helpers[i]);
        }

        entry.setPreferredSize(new Dimension(150, 30));
        panels[1].add(entry);

        String[] cameras = new String[connectedCameras];
        cameras[0] = "Default Camera";
        for (int i = 1; i < connectedCameras; i++) {
            cameras[i] = "Camera " + i;
        }

        devices = new JComboBox<>(cameras);
        devices.setPreferredSize(new Dimension(150, 30));
        panels[1].add(devices);

        start.setPreferredSize(new Dimension(150, 30));
        start.addActionListener(this);
        panels[1].add(start);

        frame.add(panels[0]);
        frame.add(panels[1]);

        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
        frame.pack();
    }

    private String verify() {
        return entry.getText().isEmpty() ? "localHost" : entry.getText();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == start) {
            frame.dispose();
            Thread thread = new Thread(() -> new Client(verify(), devices.getSelectedIndex()));
            thread.setPriority(7);
            thread.start();
        }
    }
}
