package org.example;

import org.junit.Before;
import org.junit.Test;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import static org.mockito.Mockito.*;

public class MainTest {

    private HttpURLConnection mockConnection;

    @Before
    public void setUp() throws Exception {
        // Initialize Firebase if it hasn't been initialized already
        if (FirebaseApp.getApps().isEmpty()) {
            Main.initializeFirebase();
        }

        // Mock HttpURLConnection
        mockConnection = mock(HttpURLConnection.class);

        // Inject the mock connection into the method we are testing
        Main.connectionSupplier = () -> mockConnection;
    }

    @Test
    public void testAuthenticateUserSuccess() throws Exception {
        // Mock a successful authentication response
        String successResponse = "{\"idToken\":\"fake_id_token\",\"email\":\"user@example.com\"}";
        InputStream successStream = new ByteArrayInputStream(successResponse.getBytes());
        when(mockConnection.getInputStream()).thenReturn(successStream);
        when(mockConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);

        // Mock the OutputStream
        OutputStream mockOutputStream = new ByteArrayOutputStream();
        when(mockConnection.getOutputStream()).thenReturn(mockOutputStream);

        // Call the method
        Main.authenticateUser("camelfx2000@gmail.com", "testtest");

        // Verify that the connection was used and output stream was written to
        verify(mockConnection).getOutputStream();
        verify(mockConnection).getInputStream();
    }

    @Test
    public void testAuthenticateUserFailure() throws Exception {
        // Mock an authentication failure response
        String failureResponse = "{\"error\":{\"message\":\"INVALID_PASSWORD\"}}";
        InputStream errorStream = new ByteArrayInputStream(failureResponse.getBytes());
        when(mockConnection.getErrorStream()).thenReturn(errorStream);
        when(mockConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_UNAUTHORIZED);

        // Mock the OutputStream
        OutputStream mockOutputStream = new ByteArrayOutputStream();
        when(mockConnection.getOutputStream()).thenReturn(mockOutputStream);

        // Call the method
        Main.authenticateUser("camelfx2000@gmail.com", "testtest1A");
        // Verify that the connection was used and error stream was read
        verify(mockConnection).getOutputStream();
        verify(mockConnection).getErrorStream();
    }
}
