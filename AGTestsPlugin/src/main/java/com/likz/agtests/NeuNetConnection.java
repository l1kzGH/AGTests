package com.likz.agtests;

import com.likz.agtests.config.ConfigAG;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NeuNetConnection implements INeuroApi {

    private TerminalAG terminal;

    public NeuNetConnection() {
    }

    public NeuNetConnection(TerminalAG terminal) {
        this.terminal = terminal;
        terminal.addToRightPanel("Sending request to server");
    }

    @Override
    public CompletableFuture<String> generateTestMethod(String methodData, @Nullable String additionalData) {
        JSONObject reqJson = new JSONObject();
        reqJson.put("method", methodData);
        switch(additionalData) {
            case "pattern=1":
                reqJson.put("additionalData", "Arrange-Act-Assert");
                break;
            case "pattern=2":
                reqJson.put("additionalData", "Given-When-Then");
                break;
            default:
        }

        System.out.println(reqJson);
        CompletableFuture<String> resultStr = requestJson(reqJson);
        return resultStr.thenApply(result -> {
            JSONObject methodJson = new JSONObject(result);
            if (terminal != null)
                terminal.finishComponentRightPanel("Sending request to server");
            return methodJson.getString("method");
        }).exceptionally(error -> {
            System.out.println("Error occurred at NeuNetConnection: " + error.getMessage());
            if (terminal != null)
                terminal.errorComponentRightPanel("Sending request to server");
            return null;
        });
    }

    @Override
    public CompletableFuture<List<String>> generateTestMethods(List<String> methodDataArr, @Nullable String additionalData) {
        JSONArray reqJson = new JSONArray();
        for (String methodData : methodDataArr) {
            JSONObject jsonItem = new JSONObject();
            jsonItem.put("method", methodData);
            switch(additionalData) {
                case "pattern=1":
                    jsonItem.put("additionalData", "Arrange-Act-Assert");
                    break;
                case "pattern=2":
                    jsonItem.put("additionalData", "Given-When-Then");
                    break;
                default:
            }
            reqJson.put(jsonItem);
        }

        System.out.println(reqJson);
        CompletableFuture<String> resultJson = requestJsonArray(reqJson);
        return resultJson.thenApply(result -> {
            JSONArray methodArr = new JSONArray(result);
            if (terminal != null)
                terminal.finishComponentRightPanel("Sending request to server");
            List<String> list = new ArrayList<>();
            for(int i = 0; i < methodArr.length(); i++) {
                list.add(methodArr.getJSONObject(i).getString("method"));
            }
            return list;
        }).exceptionally(error -> {
            System.out.println("Error occurred at NeuNetConnection: " + error.getMessage());
            if (terminal != null)
                terminal.errorComponentRightPanel("Sending request to server");
            return null;
        });
    }

    private CompletableFuture<String> requestJson(JSONObject object) {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ConfigAG.URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(object.toString(), StandardCharsets.UTF_8))  // Отправляем JSON данные
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return response.body();
                    } else {
                        System.out.println("--- Some connection error ---");
                        System.out.println("Status = " + response.statusCode());
                        System.out.println(response.body());
                        return null;
                    }
                }).exceptionally(e -> {
                    System.out.println("Exception found - " + e);
                    return null;
                });
    }

    private CompletableFuture<String> requestJsonArray(JSONArray array) {
        HttpClient client = HttpClient.newHttpClient();
        // POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ConfigAG.URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(array.toString(), StandardCharsets.UTF_8))  // Отправляем JSON данные
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return response.body();
                    } else {
                        System.out.println("--- Some connection error ---");
                        System.out.println("Status = " + response.statusCode());
                        System.out.println(response.body());
                        return null;
                    }
                }).exceptionally(e -> {
                    System.out.println("Exception found - " + e);
                    return null;
                });
    }
}
