package org.example;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.util.Objects;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.function.Supplier;

public class Main {
    private static final String FIREBASE_WEB_API_KEY = "secret_Key";

    public static Supplier<URL> urlSupplier = () -> {
        try {
            return new URL("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + FIREBASE_WEB_API_KEY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    };

    public static Supplier<HttpURLConnection> connectionSupplier = () -> {
        try {
            URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + FIREBASE_WEB_API_KEY);
            return (HttpURLConnection) url.openConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    };


    public static void main(String[] args) throws FirebaseAuthException {
        // Initialize Firebase
        initializeFirebase();

        // Example usage of authenticateUser
        authenticateUser("camelfx2000@gmail.com", "testtest");
    }

    public static void initializeFirebase() {
        try {
            File file = new File(Objects.requireNonNull(Main.class.getClassLoader().getResource("javaauthenticate-firebase-adminsdk-vnfe5-9440f3451a.json")).getFile());

            FileInputStream serviceAccount = new FileInputStream(file);

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://javaauthenticate-default-rtdb.firebaseio.com")
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void authenticateUser(String email, String password) throws FirebaseAuthException {
        UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
        if (userRecord!=null) {
            try {

                HttpURLConnection conn = connectionSupplier.get();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");

                String payload = String.format(
                        "{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",
                        email, password);

                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                        String responseBody = scanner.useDelimiter("\\A").next();
                        System.out.println("User authenticated successfully: " + responseBody);
                    }
                } else {
                    try (Scanner scanner = new Scanner(conn.getErrorStream(), StandardCharsets.UTF_8)) {
                        String errorResponse = scanner.useDelimiter("\\A").next();
                        System.out.println("Authentication failed: " + errorResponse);
                    }
                }

            } catch (IOException e) {
                System.out.println("Error during authentication: " + e.getMessage());
            }
        } else {
            System.out.println("Authentication failed.");
        }

    }
}
