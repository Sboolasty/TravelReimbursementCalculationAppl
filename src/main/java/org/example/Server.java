package org.example;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

public class Server {

    private static final Admin admin = new Admin();
    private static final Settings settings = new Settings();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/user", new UserHandler());
        server.createContext("/admin", new AdminHandler());
        server.setExecutor(null);
        server.start();
    }

    static class UserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                handlePostRequest(exchange);
            } else {
                serveFile(exchange, "user.html");
            }
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException {
            String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                    .lines().collect(Collectors.joining("\n"));

            Map<String, String> params = parsePostParameters(requestBody);
            String response = "";

            try {
                if (params.containsKey("receiptType") && params.containsKey("receiptValue")) {
                    String type = params.get("receiptType");
                    double value = Double.parseDouble(params.get("receiptValue"));
                    settings.addReceipt(type, value);
                } else if (params.containsKey("calculateTotal") && params.containsKey("days") && params.containsKey("distance")) {
                    int days = Integer.parseInt(params.get("days"));
                    double distance = Double.parseDouble(params.get("distance"));
                    double total = settings.calculateTotalReimbursement(days, distance);
                    response = "Total Reimbursement: $" + total;
                }
            } catch (NumberFormatException e) {
                response = "Error: Invalid number format.";
            }

            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }

        private Map<String, String> parsePostParameters(String requestBody) {
            return Arrays.stream(requestBody.split("&"))
                    .map(param -> param.split("="))
                    .collect(Collectors.toMap(p -> p[0], p -> p[1]));
        }
    }

    static class AdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    handlePostRequest(exchange);
                } else {
                    serveAdminPage(exchange);
                }
            } catch (Exception e) {
                e.printStackTrace(); // Drukuj wyjątek na konsolę
                String response = "Internal Server Error";
                exchange.sendResponseHeaders(500, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException {
            String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                    .lines().collect(Collectors.joining("\n"));

            Map<String, String> params = parsePostParameters(requestBody);

            if (params.containsKey("addReceiptType")) {
                admin.addReceiptType(params.get("addReceiptType"));
            } else if (params.containsKey("removeReceiptType")) {
                admin.removeReceiptType(params.get("removeReceiptType"));
            } else if (params.containsKey("dailyAllowanceRate")) {
                double rate = Double.parseDouble(params.get("dailyAllowanceRate"));
                settings.setDailyAllowanceRate(rate);
            } else if (params.containsKey("mileageRate")) {
                double rate = Double.parseDouble(params.get("mileageRate"));
                settings.setMileageRate(rate);
            }

            serveAdminPage(exchange);
        }

        private Map<String, String> parsePostParameters(String requestBody) {
            return Arrays.stream(requestBody.split("&"))
                    .map(param -> param.split("="))
                    .collect(Collectors.toMap(p -> p[0], p -> p[1]));
        }

        private void serveAdminPage(HttpExchange exchange) throws IOException {
            String htmlContent = new String(Files.readAllBytes(Paths.get("src/main/resources/admin.html")));

            List<String> receiptTypes = admin.getReceiptTypes();
            String receiptList = receiptTypes.stream()
                    .map(type -> "<li>" + type + "</li>")
                    .collect(Collectors.joining("\n"));

            htmlContent = htmlContent.replace("<!--RECEIPT_TYPES_PLACEHOLDER-->", receiptList);

            byte[] responseBytes = htmlContent.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }

    private static void serveFile(HttpExchange exchange, String filePathString) throws IOException {
        Path filePath = Paths.get("src/main/resources", filePathString);
        try {
            byte[] fileBytes = Files.readAllBytes(filePath);
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, fileBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
        } catch (NoSuchFileException e) {
            String response = "404 File not found!";
            exchange.sendResponseHeaders(404, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}