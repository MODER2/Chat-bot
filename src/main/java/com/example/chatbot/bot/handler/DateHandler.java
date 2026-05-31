package com.example.chatbot.bot.handler;

import com.example.chatbot.bot.BotContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * DateHandler — обработчик вопроса о ТЕКУЩЕЙ ДАТЕ.
 * <p>
 * Отвечает на «Какое сегодня число?», «Какая дата?», «Какой сегодня день недели?».
 * Демонстрирует работу с датами и локализацией (русские названия месяцев/дней).
 */
public class DateHandler extends MessageHandler {

    /** Русская локаль, чтобы месяцы и дни недели были на русском языке. */
    private static final Locale RU = Locale.forLanguageTag("ru");

    /** Формат «31 мая 2026 г.». */
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMMM yyyy 'г.'", RU);

    public DateHandler() {
        super("(какое\\s+(сегодня\\s+)?число|какая\\s+(сегодня\\s+)?дата|какой\\s+(сегодня\\s+)?день|\\bсегодня\\b)");
    }

    @Override
    public String getName() {
        return "Текущая дата";
    }

    @Override
    public String getDescription() {
        return "Узнать дату: «Какое сегодня число?», «Какой сегодня день?»";
    }

    @Override
    public String respond(String input, BotContext context) {
        LocalDate today = LocalDate.now();
        // Полное название дня недели на русском, например «воскресенье».
        String weekDay = today.getDayOfWeek().getDisplayName(TextStyle.FULL, RU);
        return "Сегодня " + today.format(DATE_FORMAT) + ", " + weekDay + ".";
    }
}
