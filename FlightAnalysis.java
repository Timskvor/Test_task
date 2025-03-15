import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.*;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FlightAnalysis {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy H:mm");

    public static void main(String[] args) throws IOException, ParseException {
        JSONArray tickets = getJsonArray();

        Map<String, Integer> minFlightTimes = new HashMap<>();
        List<Integer> prices = new ArrayList<>();

        for (Object obj : tickets) {
            JSONObject ticket = (JSONObject) obj;
            if (!"VVO".equals(ticket.get("origin")) || !"TLV".equals(ticket.get("destination"))) {
                continue;
            }

            String carrier = (String) ticket.get("carrier");
            int duration = calculateFlightDuration(ticket);
            minFlightTimes.put(carrier, Math.min(minFlightTimes.getOrDefault(carrier, Integer.MAX_VALUE), duration));
            prices.add(((Long) ticket.get("price")).intValue());
        }

        System.out.println("Минимальное время полета для каждого авиаперевозчика:");
        minFlightTimes.forEach((carrier, duration) ->
                System.out.printf("%s: %d часов %d минут\n", carrier, duration / 60, duration % 60));

        double averagePrice = prices.stream().mapToInt(Integer::intValue).average().orElse(0);
        double medianPrice = calculateMedian(prices);
        System.out.printf("Разница между средней ценой и медианой: %.2f\n", Math.abs(averagePrice - medianPrice));
    }

    private static JSONArray getJsonArray() throws IOException, ParseException {
        JSONParser parser = new JSONParser();

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream("tickets.json"));
        bis.mark(3);
        byte[] bom = new byte[3];
        int bytesRead = bis.read(bom);
        if (bytesRead >= 3
                && (bom[0] & 0xFF) == 0xEF
                && (bom[1] & 0xFF) == 0xBB
                && (bom[2] & 0xFF) == 0xBF) {
        } else {
            bis.reset();
        }

        InputStreamReader reader = new InputStreamReader(bis, "UTF-8");
        JSONObject root = (JSONObject) parser.parse(reader);

        return (JSONArray) root.get("tickets");
    }

    private static int calculateFlightDuration(JSONObject ticket) {
        LocalDateTime departure = LocalDateTime.parse(ticket.get("departure_date") + " " + ticket.get("departure_time"), DATE_TIME_FORMATTER);
        LocalDateTime arrival = LocalDateTime.parse(ticket.get("arrival_date") + " " + ticket.get("arrival_time"), DATE_TIME_FORMATTER);
        return (int) Duration.between(departure, arrival).toMinutes();
    }

    private static double calculateMedian(List<Integer> prices) {
        Collections.sort(prices);
        int size = prices.size();
        if (size % 2 == 0) {
            return (prices.get(size / 2 - 1) + prices.get(size / 2)) / 2.0;
        } else {
            return prices.get(size / 2);
        }
    }
}
