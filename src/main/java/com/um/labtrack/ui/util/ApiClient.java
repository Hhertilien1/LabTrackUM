package com.um.labtrack.ui.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Utility class for making HTTP requests to the Spring Boot backend API.
 * Provides methods for GET, POST, PUT, and DELETE operations.
 */
public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;
    private static String currentUsername = null;
    
    /**
     * Sets the current authenticated username.
     * This should be called after successful login.
     *
     * @param username The username of the logged-in user
     */
    public static void setCurrentUsername(String username) {
        currentUsername = username;
    }
    
    /**
     * Clears the current authenticated username.
     * This should be called on logout.
     */
    public static void clearCurrentUsername() {
        currentUsername = null;
    }
    
    /**
     * Gets the current authenticated username.
     *
     * @return The current username, or null if not set
     */
    public static String getCurrentUsername() {
        return currentUsername;
    }

    /**
     * Makes an HTTP GET request.
     *
     * @param endpoint The API endpoint (e.g., "/users")
     * @return The response body as a string
     * @throws Exception If the request fails
     */
    public static String get(String endpoint) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        // Add authentication header if username is set
        if (currentUsername != null) {
            connection.setRequestProperty("X-Username", currentUsername);
        }
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);

        int responseCode = connection.getResponseCode();
        
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return readResponse(connection);
        } else {
            throw new Exception("HTTP Error Code: " + responseCode);
        }
    }

    /**
     * Makes an HTTP POST request.
     *
     * @param endpoint The API endpoint
     * @param jsonBody  The JSON body to send
     * @return The response body as a string
     * @throws Exception If the request fails
     */
    public static String post(String endpoint, String jsonBody) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        // Add authentication header if username is set
        if (currentUsername != null) {
            connection.setRequestProperty("X-Username", currentUsername);
        }
        connection.setDoOutput(true);
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);

        if (jsonBody != null && !jsonBody.isEmpty()) {
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
        }

        int responseCode = connection.getResponseCode();
        
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            return readResponse(connection);
        } else {
            // Try to read error response body
            String errorBody = readResponse(connection);
            if (errorBody != null && !errorBody.isEmpty()) {
                throw new Exception("HTTP Error Code: " + responseCode + " - " + errorBody);
            }
            throw new Exception("HTTP Error Code: " + responseCode);
        }
    }

    /**
     * Makes an HTTP PUT request.
     *
     * @param endpoint The API endpoint
     * @param jsonBody  The JSON body to send
     * @return The response body as a string
     * @throws Exception If the request fails
     */
    public static String put(String endpoint, String jsonBody) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        // Add authentication header if username is set
        if (currentUsername != null) {
            connection.setRequestProperty("X-Username", currentUsername);
        }
        connection.setDoOutput(true);
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);

        if (jsonBody != null && !jsonBody.isEmpty()) {
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
        }

        int responseCode = connection.getResponseCode();
        
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
            if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                return "";
            }
            return readResponse(connection);
        } else {
            throw new Exception("HTTP Error Code: " + responseCode);
        }
    }

    /**
     * Makes an HTTP DELETE request.
     *
     * @param endpoint The API endpoint
     * @throws Exception If the request fails
     */
    public static void delete(String endpoint) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("DELETE");
        // Add authentication header if username is set
        if (currentUsername != null) {
            connection.setRequestProperty("X-Username", currentUsername);
        }
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);

        int responseCode = connection.getResponseCode();
        
        if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
            throw new Exception("HTTP Error Code: " + responseCode);
        }
    }

    /**
     * Reads the response from an HTTP connection.
     *
     * @param connection The HTTP connection
     * @return The response body as a string
     * @throws Exception If reading fails
     */
    private static String readResponse(HttpURLConnection connection) throws Exception {
        InputStream inputStream = null;
        BufferedReader reader = null;
        
        try {
            // Check if there's an error stream first
            int responseCode = connection.getResponseCode();
            if (responseCode >= 400) {
                inputStream = connection.getErrorStream();
            } else {
                inputStream = connection.getInputStream();
            }
            
            if (inputStream == null) {
                return "";
            }
            
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (response.length() > 0) {
                    response.append("\n");
                }
                response.append(line);
            }
            
            return response.toString();
        } catch (Exception e) {
            // If we get an EOF or other read error, return empty string for successful status codes
            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                return ""; // Empty response is OK for successful requests
            }
            throw e; // Re-throw for error status codes
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    // Ignore close errors
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    // Ignore close errors
                }
            }
        }
    }
}
