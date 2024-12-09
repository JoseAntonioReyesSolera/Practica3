package Exercici2;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorChat {
    private static final Map<String, File> conversations = new HashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Servidor de Chat escuchando en el puerto 12345...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ChatHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ChatHandler implements Runnable {
        private Socket clientSocket;

        public ChatHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String input;
                while ((input = in.readLine()) != null) { // Leer mensajes continuamente
                    String[] parts = input.split(";", 3); // Formato: sender;receiver;message
                    if (parts.length < 3) {
                        out.println("Error: Mensaje mal formado");
                        continue;
                    }

                    String sender = parts[0];
                    String receiver = parts[1];
                    String message = parts[2];

                    // Clave para identificar la conversación
                    String fileKey = sender.compareTo(receiver) < 0
                            ? sender + "_" + receiver
                            : receiver + "_" + sender;

                    File conversationFile = conversations.computeIfAbsent(fileKey, k -> new File(k + ".txt"));

                    // Guardar el mensaje en el archivo
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(conversationFile, true))) {
                        bw.write(sender + ": " + message);
                        bw.newLine();
                    }

                    // Leer el contenido completo de la conversación
                    StringBuilder conversation = new StringBuilder();
                    conversation.append("---------------------------------\n");
                    try (BufferedReader br = new BufferedReader(new FileReader(conversationFile))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            conversation.append(line).append("\n");
                        }
                    }
                    System.out.println(conversation.toString());
                    // Enviar la conversación completa al cliente
                    out.println("read " + message.length() + " bytes.");
                    out.println("Contingut actual de la conversa:");
                    out.println(conversation.toString());

                    // Finalizar conversación si el mensaje es "adeu"
                    if ("adeu".equalsIgnoreCase(message)) {
                        System.out.println("Cerrando conversación entre " + sender + " y " + receiver);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
