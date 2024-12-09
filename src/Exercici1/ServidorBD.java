package Exercici1;

import java.io.*;
import java.net.*;

public class ServidorBD {
    private static final String BBDD_FILE = "bbdd.txt";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            ensureDatabaseFile(); // Crear el archivo si no existe
            System.out.println("Servidor escuchando en el puerto 12345...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Crear el archivo si no existe
    private static void ensureDatabaseFile() {
        File file = new File(BBDD_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
                System.out.println("Archivo de base de datos creado: " + BBDD_FILE);
            } catch (IOException e) {
                System.err.println("Error al crear el archivo de base de datos.");
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String command;
                while ((command = in.readLine()) != null) {
                    if ("exit".equalsIgnoreCase(command)) {
                        out.println("Cerrando conexión...");
                        break; // Finalizar la conexión con este cliente
                    }

                    switch (command.toLowerCase()) {
                        case "insert":
                            String id = in.readLine();
                            String name = in.readLine();
                            String surname = in.readLine();
                            if (insertRecord(id, name, surname)) {
                                out.println("Inserted data on BBDD");
                            } else {
                                out.println("Error: ID already exists");
                            }
                            break;
                        case "select":
                            id = in.readLine();
                            out.println(selectRecord(id));
                            break;
                        case "delete":
                            id = in.readLine();
                            if (deleteRecord(id)) {
                                out.println("Deleted data on BBDD");
                            } else {
                                out.println("Error: Element not found");
                            }
                            break;
                        default:
                            out.println("Comando no válido");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private synchronized boolean insertRecord(String id, String name, String surname) {
            try (BufferedReader br = new BufferedReader(new FileReader(BBDD_FILE));
                 BufferedWriter bw = new BufferedWriter(new FileWriter(BBDD_FILE, true))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(id + ";")) {
                        return false; // ID ya existe
                    }
                }
                bw.write(id + ";" + name + ";" + surname);
                bw.newLine();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        private synchronized String selectRecord(String id) {
            try (BufferedReader br = new BufferedReader(new FileReader(BBDD_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(id + ";")) {
                        return line.replace(";", " ");
                    }
                }
                return "Element not found";
            } catch (IOException e) {
                e.printStackTrace();
                return "Error reading file";
            }
        }

        private synchronized boolean deleteRecord(String id) {
            File tempFile = new File("temp_bbdd.txt");
            File originalFile = new File(BBDD_FILE);
            boolean found = false;

            try (BufferedReader br = new BufferedReader(new FileReader(originalFile));
                 BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(";");
                    if (parts[0].equals(id)) {
                        found = true; // ID encontrado, no escribir esta línea en el archivo temporal
                    } else {
                        bw.write(line);
                        bw.newLine();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            // Reemplazar el archivo original con el temporal solo si se encontró el ID
            if (found) {
                if (!originalFile.delete()) {
                    System.err.println("Error: No se pudo eliminar el archivo originaldde.");
                    return false;
                }
                if (!tempFile.renameTo(originalFile)) {
                    System.err.println("Error: No se pudo renombrar el archivo temporal.");
                    return false;
                }
            } else {
                tempFile.delete(); // Eliminar el archivo temporal si el ID no fue encontrado
            }

            return found;
        }
    }
}
