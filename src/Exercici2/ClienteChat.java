package Exercici2;

import java.io.*;
import java.net.*;

public class ClienteChat {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.print("Tu número: ");
            String sender = console.readLine();
            System.out.print("Número del receptor: ");
            String receiver = console.readLine();

            while (true) {
                System.out.print("Escriu el missatge a enviar: ");
                String message = console.readLine();
                if ("adeu".equalsIgnoreCase(message)) break;

                out.println(sender + ";" + receiver + ";" + message);

                // Leer la respuesta del servidor
                String readBytes = in.readLine();
                String conversationHeader = in.readLine();
                StringBuilder conversation = new StringBuilder();
                String line;


                while ((line = in.readLine()) != null) {
                    if (line.startsWith("---------------------------------")) {
                        break;
                    }
                    conversation.append(line).append("\n");
                }

                // Mostrar la respuesta y la conversación completa
                System.out.println(readBytes);
                System.out.println(conversationHeader);
                System.out.println(conversation.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
