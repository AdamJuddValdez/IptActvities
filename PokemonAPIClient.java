// File: PokemonAPIClient.java
// Purpose: Connect to the public PokeAPI (https://pokeapi.co), send a GET request,
//          read the raw JSON response, and manually extract a few fields
//          WITHOUT using any external JSON library (Gson/Jackson/etc).

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class PokemonAPIClient {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to Pokedex (using PokeAPI)");
        System.out.print("Enter a Pokemon name (ex. pichu, pikachu, raichu): ");
        String userInput = scanner.nextLine().trim().toLowerCase();

        // --- Basic input validation ---
        if (userInput.isEmpty()) {
            System.out.println("Error: You must enter a Pokemon name.");
            scanner.close();
            return;
        }

        // The API endpoint takes the Pokemon name (or id number) directly in the URL path.
        // Typing "pikachu" turns this into: https://pokeapi.co/api/v2/pokemon/pikachu
        String apiUrl = "https://pokeapi.co/api/v2/pokemon/" + userInput;

        try {
            String jsonResponse = sendGetRequest(apiUrl);

            System.out.println("\n--- RAW JSON RESPONSE (first 600 characters) ---");
            int previewLength = Math.min(600, jsonResponse.length());
            System.out.println(jsonResponse.substring(0, previewLength) + " ...(truncated)...");

            // Extract useful fields manually from the JSON text
            String id = extractNumberValue(jsonResponse, "id");
            String height = extractNumberValue(jsonResponse, "height");
            String weight = extractNumberValue(jsonResponse, "weight");
            String baseExperience = extractNumberValue(jsonResponse, "base_experience");

            System.out.println("\n--- EXTRACTED INFORMATION ---");
            System.out.println("Name: " + capitalize(userInput));
            System.out.println("Pokedex ID: " + id);
            System.out.println("Height: " + height + " decimetres (1 dm = 10 cm)");
            System.out.println("Weight: " + weight + " hectograms (1 hg = 100 g)");
            System.out.println("Base Experience: " + baseExperience);

        } catch (IOException e) {
            // Covers: unknown host, timeout, non-200 response code, etc.
            System.out.println("\nError: Could not retrieve data for \"" + userInput + "\".");
            System.out.println("Reason: " + e.getMessage());
            System.out.println("Tip: check the spelling of the Pokemon name and try again.");
        } finally {
            scanner.close();
        }
    }

    // Opens an HTTP GET connection to the given URL and returns the full
    // response body as a single String.
    private static String sendGetRequest(String urlString) throws IOException {
        // Go to the URL and open a connection (like walking up and knocking on the door)
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        // Calling getResponseCode() actually sends the request and gets the server's answer
        int responseCode = connection.getResponseCode();

        // Checks if the Pokemon name is valid (in the "database")
        if (responseCode != HttpURLConnection.HTTP_OK) {
            connection.disconnect();
            // If not found (HTTP 404), stop here and throw an error instead of continuing
            throw new IOException("Server responded with HTTP code " + responseCode
                    + " (Pokemon probably doesn't exist)");
        }

        // Read the response body line by line (copying everything down, not printing yet)
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        connection.disconnect();

        // Hand back everything that was read, as one single String (the raw JSON)
        return response.toString();
    }

    /**
     * Manually extracts the value of a NUMERIC JSON field, e.g. turns
     * ..."height":4,"id":25... into "4" when key = "height".
     *
     * How it works:
     *  1. Build the exact text pattern to search for: "height":
     *  2. Find where that pattern starts in the raw JSON string.
     *  3. Walk forward character by character while we still see digits
     *     (or a leading minus sign), collecting them.
     *  4. Return everything collected as the extracted number (as text).
     *
     * NOTE: This simple approach works because "id", "height", "weight",
     * and "base_experience" each appear only ONCE in this particular API
     * response, at the top level of the JSON. If a field name also shows
     * up nested elsewhere (like "name", which appears inside "abilities",
     * "species", "types", etc.), this simple search would grab the WRONG
     * occurrence -- that's why this program deliberately picks fields that
     * are unique in the response.
     */
    private static String extractNumberValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) {
            return "N/A";
        }
        // Move past the label itself, so we're pointing right at the number
        startIndex += searchKey.length();

        // Keep moving forward while the character is still a digit (or a minus sign)
        int endIndex = startIndex;
        while (endIndex < json.length()
                && (Character.isDigit(json.charAt(endIndex)) || json.charAt(endIndex) == '-')) {
            endIndex++;
        }

        // Cut out just the number we walked across
        return json.substring(startIndex, endIndex);
    }

    /** Capitalizes the first letter of a string, e.g. "pikachu" -> "Pikachu". */
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}