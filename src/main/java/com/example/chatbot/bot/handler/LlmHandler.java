package com.example.chatbot.bot.handler;

import com.example.chatbot.bot.BotContext;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LlmHandler — обработчик, который перенаправляет вопрос НЕЙРОСЕТИ (LLM-серверу).
 * <p>
 * Демонстрирует пункт цели работы «взаимодействие с сервером LLM». Срабатывает на
 * команды вида «ии: <вопрос>», «спроси у нейросети ...», «gpt ...». Обработчик
 * формирует HTTP-запрос к серверу, совместимому с OpenAI Chat Completions API,
 * и возвращает ответ модели.
 * <p>
 * <b>Важно про безопасность:</b> секретный ключ доступа НЕ хранится в коде.
 * Он берётся из переменной окружения {@code OPENAI_API_KEY}. Адрес сервера можно
 * переопределить через {@code OPENAI_BASE_URL} (по умолчанию — официальный OpenAI),
 * а модель — через {@code OPENAI_MODEL}. Если ключ не задан, обработчик объясняет,
 * как включить эту функцию, и НЕ роняет программу.
 */
public class LlmHandler extends MessageHandler {

    /** Где взять ключ и настройки — из переменных окружения. */
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String BASE_URL = envOrDefault("OPENAI_BASE_URL",
            "https://api.openai.com/v1/chat/completions");
    private static final String MODEL = envOrDefault("OPENAI_MODEL", "gpt-4o-mini");

    /** Шаблон для извлечения поля "content" из JSON-ответа модели. */
    private static final Pattern CONTENT_FIELD =
            Pattern.compile("\"content\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public LlmHandler() {
        super("^\\s*(ии|gpt|нейросеть|нейронк\\w*)\\s*[:,]?\\s+|спроси\\s+у\\s+(ии|нейросети|gpt)");
    }

    @Override
    public String getName() {
        return "Вопрос нейросети (LLM)";
    }

    @Override
    public String getDescription() {
        return "Спросить нейросеть: «ии: расскажи о Java» (нужен ключ OPENAI_API_KEY)";
    }

    @Override
    public String respond(String input, BotContext context) {
        // Если ключ не задан — объясняем, как включить функцию.
        if (API_KEY == null || API_KEY.isBlank()) {
            return "Функция нейросети выключена. Чтобы включить, задайте переменную "
                    + "окружения OPENAI_API_KEY со своим ключом и перезапустите программу.";
        }

        // Убираем из запроса служебное слово-триггер, оставляя сам вопрос.
        String question = input
                .replaceFirst("(?i)^\\s*(ии|gpt|нейросеть|нейронк\\w*)\\s*[:,]?\\s+", "")
                .replaceFirst("(?i)спроси\\s+у\\s+(ии|нейросети|gpt)\\s*", "")
                .trim();
        if (question.isEmpty()) {
            return "Напишите вопрос после слова «ии:», например: «ии: что такое JavaFX?».";
        }

        try {
            // Формируем тело запроса в формате OpenAI Chat Completions.
            // Пользовательский текст экранируем, чтобы не сломать JSON.
            String body = "{"
                    + "\"model\":\"" + MODEL + "\","
                    + "\"messages\":[{\"role\":\"user\",\"content\":\"" + escapeJson(question) + "\"}]"
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY) // ключ передаётся в заголовке
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return "Сервер нейросети ответил кодом " + response.statusCode() + ".";
            }

            // Достаём текст ответа модели и приводим экранированные символы к обычным.
            Matcher m = CONTENT_FIELD.matcher(response.body());
            if (m.find()) {
                return unescapeJson(m.group(1));
            }
            return "Не удалось разобрать ответ нейросети.";

        } catch (Exception e) {
            return "Ошибка обращения к нейросети: " + e.getClass().getSimpleName() + ".";
        }
    }

    /** Возвращает значение переменной окружения или значение по умолчанию. */
    private static String envOrDefault(String name, String def) {
        String v = System.getenv(name);
        return (v == null || v.isBlank()) ? def : v;
    }

    /** Экранирует спецсимволы для безопасной вставки строки в JSON. */
    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /** Обратная операция: превращает \n, \" и т.п. обратно в обычные символы. */
    private static String unescapeJson(String s) {
        return s.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}
