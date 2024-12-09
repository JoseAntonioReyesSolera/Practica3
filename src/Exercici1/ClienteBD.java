package Exercici1;

import java.io.*;
import java.net.*;

public class ClienteBD {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            while (true) {
                System.out.println("¿Qué operación deseas realizar? (insert, select, delete, exit):");
                String command = console.readLine().trim();

                if ("exit".equalsIgnoreCase(command)) {
                    System.out.println("Cerrando cliente...");
                    break;
                }

                out.println(command);

                switch (command.toLowerCase()) {
                    case "insert":
                        System.out.print("ID: ");
                        out.println(console.readLine());
                        System.out.print("Nombre: ");
                        out.println(console.readLine());
                        System.out.print("Apellido: ");
                        out.println(console.readLine());
                        break;
                    case "select", "delete":
                        System.out.print("ID: ");
                        out.println(console.readLine());
                        break;
                    default:
                        System.out.println("Comando no válido. Intenta nuevamente.");
                        continue;
                }

                // Leer y mostrar la respuesta del servidor
                System.out.println("Respuesta del servidor: " + in.readLine());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
