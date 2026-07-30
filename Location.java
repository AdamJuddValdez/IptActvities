import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Location {

    public static void main(String[] args) {

        String apiKey = "4996b8bad1330f246d2b3582ae216517";
        double latitude;
        double longitude;

        Scanner dataIn = new Scanner(System.in);

        System.out.print("Enter Latitude: ");
        latitude = dataIn.nextDouble();

        System.out.print("Enter Longitude: ");
        longitude = dataIn.nextDouble();

        try {

            String location =
                    getLocation(apiKey, latitude, longitude);

            displayLocation(location, latitude, longitude);

        } catch (IOException | JSONException e) {

            e.printStackTrace();
        }
    }

    private static String getLocation(
            String apiKey,
            double latitude,
            double longitude) throws IOException {

        String apiUrl =
                "https://api.openweathermap.org/data/2.5/weather?lat="
                        + latitude
                        + "&lon="
                        + longitude
                        + "&appid="
                        + apiKey;

        URL url = new URL(apiUrl);

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        try (
                InputStream inputStream = connection.getInputStream();
                Scanner scanner = new Scanner(inputStream)
        ) {

            scanner.useDelimiter("\\A");

            String response =
                    scanner.hasNext()
                            ? scanner.next()
                            : "";

            try {

                JSONObject json =
                        new JSONObject(response);

                String city =
                        json.optString("name");

                return city.isEmpty()
                        ? "Unknown Location"
                        : city;

            } catch (JSONException e) {

                return "Unknown Location";
            }

        } finally {

            connection.disconnect();
        }
    }

    private static void displayLocation(
            String location,
            double latitude,
            double longitude) throws JSONException {

        System.out.println(
                "Location is "
                        + location
                        + " (Latitude "
                        + latitude
                        + " and Longitude "
                        + longitude
                        + ")."
        );
    }
}