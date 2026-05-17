package com.codtech.task2;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Scanner;

/**
 * Simple REST API client that fetches weather data from the OpenWeatherMap API
 * and displays it in a structured format.
 *
 * API docs: https://openweathermap.org/current
 */
public class WeatherClient {

    // Predefined cities with their latitude/longitude
    private static final Map<Integer, City> CITIES = Map.of(
            1, new City("Bengaluru", 12.9716, 77.5946),
            2, new City("New Delhi", 28.6139, 77.2090),
            3, new City("Mumbai", 19.0760, 72.8777),
            4, new City("Hubli-Dharwad", 15.3647, 75.1240)
    );

    // TODO: replace this with your own OpenWeatherMap API key
    private static final String API_KEY = "77010ee5e6ebd8747a35f70d9d825e16";

    private static final String BASE_URL =
            "https://api.openweathermap.org/data/2.5/weather";

    private final HttpClient httpClient;
    private final Gson gson;

    public WeatherClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public static void main(String[] args) {
        WeatherClient client = new WeatherClient();
        client.runInteractive();
    }

    /**
     * Simple console UI.
     */
    private void runInteractive() {
        System.out.println("=== CODTECH Internship Task-2 ===");
        System.out.println("REST API Client - Current Weather");
        System.out.println();

        System.out.println("Select a city to view its current weather:");
        CITIES.forEach((id, city) ->
                System.out.printf("%d. %s%n", id, city.getName()));

        System.out.print("Enter choice (1-4): ");
        Scanner scanner = new Scanner(System.in);
        int choice;

        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException ex) {
            System.out.println("Invalid input. Please run the program again.");
            return;
        }

        City city = CITIES.get(choice);
        if (city == null) {
            System.out.println("Invalid choice. Please run the program again.");
            return;
        }

        System.out.printf("%nFetching weather data for %s (OpenWeatherMap)...%n%n", city.getName());

        try {
            WeatherApiResponse response = fetchCurrentWeather(city.getLatitude(), city.getLongitude());
            if (response == null || response.main == null) {
                System.out.println("No weather data available.");
                return;
            }
            displayWeather(city.getName(), response);
        } catch (IOException | InterruptedException e) {
            System.out.println("Error while calling the weather API: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            System.out.println("Failed to parse JSON response: " + e.getMessage());
        }
    }

    /**
     * Calls the OpenWeatherMap API and parses the JSON.
     */
    private WeatherApiResponse fetchCurrentWeather(double latitude, double longitude)
            throws IOException, InterruptedException {

        String url = BASE_URL
                + "?lat=" + latitude
                + "&lon=" + longitude
                + "&units=metric"
                + "&appid=" + API_KEY;

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " from API");
        }

        String json = response.body();
        return gson.fromJson(json, WeatherApiResponse.class);
    }

    /**
     * Displays weather data in a structured, readable format.
     */
    private void displayWeather(String cityName, WeatherApiResponse response) {
        CurrentWeather current = response.toCurrentWeather();
        System.out.println("----------- WEATHER REPORT -----------");
        System.out.println("City           : " + cityName);
        System.out.println("Temperature    : " + current.temperature + " °C");
        System.out.println("Feels Like     : " + current.feelsLike + " °C");
        System.out.println("Humidity       : " + current.humidity + " %");
        System.out.println("Wind Speed     : " + current.windspeed + " m/s");
        System.out.println("Wind Direction : " + current.winddirection + "°");
        System.out.println("Reported At    : " + current.time);
        System.out.println("--------------------------------------");
    }

    /**
     * Simple value object for city coordinates.
     */
    private static class City {
        private final String name;
        private final double latitude;
        private final double longitude;

        public City(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getName() {
            return name;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }

    /**
     * POJO representing the JSON structure from OpenWeatherMap
     * (only the fields we need).
     */
    private static class WeatherApiResponse {
        Main main;
        Wind wind;
        long dt;      // Unix timestamp
        String name;  // city name (from API, may differ slightly)

        CurrentWeather toCurrentWeather() {
            CurrentWeather cw = new CurrentWeather();
            if (main != null) {
                cw.temperature = main.temp;
                cw.feelsLike = main.feels_like;
                cw.humidity = main.humidity;
            }
            if (wind != null) {
                cw.windspeed = wind.speed;
                cw.winddirection = wind.deg;
            }
            cw.time = String.valueOf(dt);
            return cw;
        }
    }

    private static class CurrentWeather {
        double temperature;
        double feelsLike;
        int humidity;
        double windspeed;
        double winddirection;
        String time;
    }

    private static class Main {
        double temp;
        double feels_like;
        int humidity;
    }

    private static class Wind {
        double speed;
        double deg;
    }
}
