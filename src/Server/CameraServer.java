package Server;

import org.opencv.core.Core;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CameraServer {

    public CameraServer(int connectedDevices) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        HashMap<Integer, Camera> cameras = new HashMap<>();
        HashMap<String, ArrayList<Boolean>> controlVariables = new HashMap<>();
        ServerGUI serverGUI = new ServerGUI(controlVariables);
        AtomicInteger clientIDs = new AtomicInteger(1);

        try (ServerSocket serverSocket = new ServerSocket(2505)) {
            System.out.println("Server is starting, please wait...");

            System.out.println("Console log >>\n");
            System.out.println("> Server is running. Waiting for a client to connect...");

            for (int i = 0; i < connectedDevices; i++) {
                Camera camera = new Camera(i);
                cameras.put(i, camera);
                new Thread(new Security(i, camera)).start();
            }

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("> Client connected: " + clientSocket.getInetAddress());

                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                int device = dis.readInt();

                String currID = "Client " + clientIDs.getAndIncrement();

                controlVariables.put(currID, createControlVariable());
                serverGUI.createNewClient(currID, clientSocket.toString());
                new Thread(new ClientHandler(currID, cameras.get(device), clientSocket,
                        controlVariables.get(currID), serverGUI)).start();
            }
        } catch (IOException e) {
            System.err.println("> Server closed");
        }
    }

    private ArrayList<Boolean> createControlVariable() {
        ArrayList<Boolean> controlInit = new ArrayList<>();
        controlInit.add(true);
        controlInit.add(false);
        controlInit.add(true);
        return controlInit;
    }

}
