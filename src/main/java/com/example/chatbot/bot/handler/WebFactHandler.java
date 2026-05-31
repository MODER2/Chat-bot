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
 * WebFactHandler — обработчик, который делает РЕАЛЬНЫЙ ВЕБ-ЗАПРОС.
 * <p>
 * Демонстрирует пункт цели работы «взаимодействие с веб-запросами». По команде
 * «расскажи факт» обработчик обращается к публичному API в интернете
 * (https://catfact.ninja/fact), получает JSON-ответ и достаёт из него текст факта.
 * <p>
 * Используется {@link HttpClient} — встроенный в Java (с версии 11) HTTP-клиент,
 * поэтому подключать сторонние библиотеки не нужно. Если интернета нет или сервер
 * недоступен, обработчик не «падает», а вежливо сообщает об ошибке.
 */
public class WebFactHandler extends MessageHandler {

    /** Адрес публичного API, который возвращает случайный факт о котах. */
    private static final String API_URL = "https://catfact.ninja/fact";

    /**
     * Регулярное выражение для извлечения значения поля "fact" из JSON.
     * В учебных целях мы парсим JSON простым regex; в «боевых» проектах для этого
     * берут полноценную библиотеку (Jackson, Gson). Шаблон ищет:
     *   "fact" : "....." и захватывает то, что внутри кавычек (группа 1).
     */
    private static final Pattern FACT_FIELD =
            Pattern.compile("\"fact\"\\s*:\\s*\"([^\"]+)\"");

    /** HTTP-клиент создаём один раз и переиспользуем. */
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)) // тайм-аут на подключение
            .build();

    public WebFactHandler() {
        super("(расскажи\\s+факт|интересный\\s+факт|\\bфакт\\b|факт\\s+о\\s+кот\\w*)");
    }

    @Override
    public String getName() {
        return "Факт из интернета";
    }

    @Override
    public String getDescription() {
        return "Факт из сети (веб-запрос): «расскажи факт»";
    }

    @Override
    public String respond(String input, BotContext context) {
        try {
            // 1) Готовим GET-запрос с тайм-аутом на ожидание ответа.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(7))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            // 2) Отправляем запрос и ждём ответ как строку.
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            // 3) Проверяем код ответа (200 = успех).
            if (response.statusCode() != 200) {
                return "Сервер фактов ответил кодом " + response.statusCode()
                        + ". Попробуйте позже.";
            }

            // 4) Достаём текст факта из JSON.
            Matcher m = FACT_FIELD.matcher(response.body());
            if (m.find()) {
                return "Интересный факт (с catfact.ninja):\n" + m.group(1);
            }
            return "Не удалось разобрать ответ сервера.";

        } catch (Exception e) {
            // Сюда попадём, если нет интернета или превышен тайм-аут.
            return "Не получилось сходить в интернет за фактом (" + e.getClass().getSimpleName()
                    + "). Проверьте подключение к сети.";
        }
    }
}
