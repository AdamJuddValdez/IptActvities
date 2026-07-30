import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class WeatherApp {

    private static final String API_KEY = "4996b8bad1330f246d2b3582ae216517";

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Latitude: ");
        double latitude = scanner.nextDouble();

        System.out.print("Enter Longitude: ");
        double longitude = scanner.nextDouble();

        try {

            String apiURL =
                    "https://api.openweathermap.org/data/2.5/weather"
                    + "?lat=" + latitude
                    + "&lon=" + longitude
                    + "&appid=" + API_KEY
                    + "&units=metric";

            URL url = new URL(apiURL);

            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    connection.getInputStream()));

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            JSONObject json = new JSONObject(response.toString());

            String location = json.getString("name");

            double temperature =
                    json.getJSONObject("main")
                        .getDouble("temp");

            String weather =
                    json.getJSONArray("weather")
                        .getJSONObject(0)
                        .getString("description");

            System.out.println("\n===== CURRENT WEATHER =====");
            System.out.println("Location: " + location);
            System.out.println("Temperature: " + temperature + " °C");
            System.out.println("Weather: " + weather);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        scanner.close();
    }
}