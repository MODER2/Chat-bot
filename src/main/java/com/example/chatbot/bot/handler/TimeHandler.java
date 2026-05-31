package com.example.chatbot.bot.handler;

import com.example.chatbot.bot.BotContext;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * TimeHandler — обработчик вопроса о ТЕКУЩЕМ ВРЕМЕНИ.
 * <p>
 * Пример «ответа на простую команду (вопрос)» из требований: «Который час?».
 * Берёт системное время компьютера и форматирует его в «ЧЧ:ММ:СС».
 */
public class TimeHandler extends MessageHandler {

    /** Формат вывода времени: часы:минуты:секунды. */
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Распознаём разные формулировки одного и того же вопроса:
     * «который час», «сколько времени», «текущее время», «время?».
     */
    public TimeHandler() {
        super("(который\\s+час|сколько\\s+(сейчас\\s+)?времени|текущее\\s+время|\\bвремя\\b)");
    }

    @Override
    public String getName() {
        return "Текущее время";
    }

    @Override
    public String getDescription() {
        return "Узнать время: «Который час?», «Сколько времени?»";
    }

    @Override
    public String respond(String input, BotContext context) {
        // LocalTime.now() — это время по часам компьютера в текущий момент.
        String now = LocalTime.now().format(TIME_FORMAT);
        return "Сейчас " + now + ".";
    }
}
