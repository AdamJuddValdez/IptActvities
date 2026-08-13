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

        if (userInput.isEmpty()) {
            System.out.println("Error: You must enter a Pokemon name.");
            scanner.close();
            return;
        }
         
        // The API endpoint takes the Pokemon name (or id number) directly in the URL path.
        String apiUrl = "https://pokeapi.co/api/v2/pokemon/" + userInput;

        try {
            String jsonResponse = sendGetRequest(apiUrl);

            System.out.println("\n--- RAW JSON RESPONSE (first 600 characters) ---");
            int previewLength = Math.min(600, jsonResponse.length());
            System.out.println(jsonResponse.substring(0, previewLength) + " ...(truncated)...");
            
             //Extract useful fields manually from the JSON text 
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
            System.out.println("\nError: Could not retrieve data for \"" + userInput + "\".");
            System.out.println("Reason: " + e.getMessage());
            System.out.println("Tip: check the spelling of the Pokemon name and try again.");
        } finally {
            scanner.close();
        }
    }
   
   //Opens an HTTP GET connection to the given URL and returns the full
   //response body as a single String.
    private static String sendGetRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int responseCode = connection.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK) {
            connection.disconnect();
            throw new IOException("Server responded with HTTP code " + responseCode
                    + " (Pokemon probably doesn't exist)");
        }
         
         // Read the response body line by line
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        connection.disconnect();

        return response.toString();
    }

    /**
     * Manually extracts the value of a NUMERIC JSON field, e.g. turns
     * "height":4,"id":25... into "4" when key = "height".
     * How it works:
     *  1. Build the exact text pattern to search for: "height":
     *  2. Find where that pattern starts in the raw JSON string.
     *  3. Walk forward character by character while we still see digits
     *     (or a leading minus sign), collecting them.
     *  4. Return everything collected as the extracted number (as text).
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
        startIndex += searchKey.length();

        int endIndex = startIndex;
        while (endIndex < json.length()
                && (Character.isDigit(json.charAt(endIndex)) || json.charAt(endIndex) == '-')) {
            endIndex++;
        }
        return json.substring(startIndex, endIndex);
    }

   //capitalize the first letter of the word
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}