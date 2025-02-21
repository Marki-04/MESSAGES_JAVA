import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

public class Serveur {

    private static final int PORT = 8080;
    private static final String FILE = "messages.json";
    private static ArrayList<Message> messages = new ArrayList<>();

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            // Boucle pour garder le serveur ouvert afin de pouvoir gérer plusieurs requêtes
            while (true) {
                // Le client
                Socket socket = serverSocket.accept();
                // Lecteur et printer
                BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);

                // Le buffer et la ligne lue
                StringBuilder message = new StringBuilder();
                String ligne;
                String auteur;

                // Prendre les messages enregistrés du fichier json
                String deserialisationReussi = deserialiserMessage();

                // FAIRE EN SORTE DE GÉRÉR LE MESSAGE D'ERREUR SI LE FICHIER MESSAGE.JSON EXISTE PAS

                socketOut.println("*** "+deserialisationReussi+" ***");
                int nbMessagesMemoire = messages.size();
                socketOut.println("Number of messages sent : "+nbMessagesMemoire);

                // Demmander le nom d'utilisateur
                socketOut.println("Veuillez saisir votre nom d'utilisateur : ('LOG' afin d'afficher l'historique)");
                auteur = socketIn.readLine();

                if (auteur.equals("LOG")) {
                    for (Message m : messages) {
                        socketOut.println("Auteur: " + m.getAuteur() + ", Message: " + m.getContenu());
                    }

                } else{

                    socketOut.println("Bienvenue " + auteur + " Veuillez entrer votre message : ");

                    // Boucle pour lire toutes les lignes que l'utilisateur entre
                    while ((ligne = socketIn.readLine()) != null && !ligne.isEmpty()) {
                        // Si la communication n'est pas terminée
                        message.append(ligne).append("\n");
                    }

                    // Afficher le message
                    System.out.println("--- MESSAGE ---\n");
                    System.out.println(message);
                    System.out.println("---  FIN  ---");
                    // Enregistrer le message
                    messages.add(new Message(message.toString(), auteur));
                    String fin = serialiserMessage();
                    socketOut.println(fin);

                    // Fermer la connexion
                    socketOut.println("Disconnected from server");
                    socket.close();

                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Fonction qui permettra d'enregistrer le message entré par l'utilisateur
    private static String serialiserMessage() {
        // Créer la variable Gson
        Gson gson = new Gson();

        // Ajouter le message dans un String Json
        String json = gson.toJson(Serveur.messages);

        // Sérialiser le message
        try {
            FileWriter fw = new FileWriter(FILE);
            fw.write(json);
            fw.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "Message has been saved";
    }

    // Fonction qui permet de désérialiser nos messages en mémoire
    private static String deserialiserMessage(){
        // Try catch pour la désérialisation
        try {
            // Créer la variable Gson
            Gson gson = new Gson();
            BufferedReader br = new BufferedReader(new FileReader(FILE));
            // Chercher le type spécifique du arraylist
            Type type = new TypeToken<ArrayList<Message>>() {}.getType();
            // Ajouter les elements enregistrés dans la liste
            messages = gson.fromJson(br, type);
            return "Saved messages in memory FOUND";

        }catch (IOException e){
            throw new RuntimeException(e);
        }
   }

}
