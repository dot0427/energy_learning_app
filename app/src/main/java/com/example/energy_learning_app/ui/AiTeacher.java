package com.example.energy_learning_app.ui;

import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import okhttp3.*;

import com.example.energy_learning_app.ui.PromptBuilder;

public class AiTeacher {

    public interface TutorCallback {
        void onSuccess(TutorPack pack);

        void onError(Exception e);
    }

    public static class TutorPack {
        public String explanation;
        public List<QuizItem> quiz;
    }

    public static class QuizItem {
        public String question;
        public Map<String, String> options; // A,B,C,D
        public String answer;               // "A"/"B"/"C"/"D"
        public String rationale;
    }

    // === 只解析到 content ===
    static class ChatCompletionsResponse {
        List<Choice> choices;

        static class Choice {
            Message message;
        }

        static class Message {
            String content;
        }
    }

    private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";
    private static final okhttp3.MediaType JSON
            = okhttp3.MediaType.get("application/json; charset=utf-8");

    private final okhttp3.OkHttpClient client;
    private final com.google.gson.Gson gson = new com.google.gson.Gson();
    private final String apiKey;


    public AiTeacher(String apiKey) {
        this.apiKey = apiKey;

        this.client = new okhttp3.OkHttpClient.Builder()
                .protocols(java.util.Arrays.asList(okhttp3.Protocol.HTTP_1_1))
                .connectTimeout(java.time.Duration.ofSeconds(30))
                .writeTimeout(java.time.Duration.ofSeconds(30))
                .readTimeout(java.time.Duration.ofSeconds(180))
                .callTimeout(java.time.Duration.ofSeconds(180))
                .retryOnConnectionFailure(true)
                .build();
    }

    public Call fetchTutorPack(String prompt, TutorCallback callback) {

        java.util.List<Message> msgs = new java.util.ArrayList<>();
        msgs.add(new Message("system", "You are a helpful physics tutor."));
        msgs.add(new Message("user", prompt));

        ChatRequest reqObj = new ChatRequest("gpt-4o-mini", msgs);
        String requestJson = gson.toJson(reqObj);

        RequestBody body = RequestBody.create(requestJson, JSON);

        Request request = new Request.Builder()
                .url(ENDPOINT)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        android.util.Log.d("AiTeacher", "requestJson=" + requestJson);

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, java.io.IOException e) {
                android.util.Log.e("AiTeacher", "API call failed", e);
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (Response r = response) {
                    if (!r.isSuccessful()) {
                        String err = (r.body() != null) ? r.body().string() : "";
                        callback.onError(new RuntimeException("HTTP " + r.code() + " " + err));
                        return;
                    }

                    String raw = (r.body() != null) ? r.body().string() : "";
                    ChatCompletionsResponse apiResp = gson.fromJson(raw, ChatCompletionsResponse.class);
                    String content = (apiResp != null && apiResp.choices != null && !apiResp.choices.isEmpty()
                            && apiResp.choices.get(0).message != null)
                            ? apiResp.choices.get(0).message.content
                            : null;

                    if (content == null || content.trim().isEmpty()) {
                        callback.onError(new RuntimeException("No content from model"));
                        return;
                    }

                    TutorPack pack = gson.fromJson(content, TutorPack.class);

                    android.util.Log.d("QUIZ_DEBUG", "options = " + pack.quiz.get(0).options);

                    if (pack == null || pack.quiz == null || pack.quiz.size() < 4) {
                        callback.onError(new RuntimeException("Quiz < 4 (got " +
                                (pack == null || pack.quiz == null ? 0 : pack.quiz.size()) + ")"));
                        return;
                    }

                    callback.onSuccess(pack);

                } catch (Exception e) {
                    callback.onError(e);
                }
            }
        });
        return call;
    }

    static class ChatRequest {
        String model;
        java.util.List<Message> messages;

        ChatRequest(String model, java.util.List<Message> messages) {
            this.model = model;
            this.messages = messages;
        }
    }

    static class Message {
        String role;
        String content;

        Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}