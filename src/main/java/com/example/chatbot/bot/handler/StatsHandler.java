package com.example.chatbot.bot.handler;

import com.example.chatbot.bot.BotContext;
import com.example.chatbot.model.ChatHistory;

/**
 * StatsHandler — обработчик вопросов о СТАТИСТИКЕ ПЕРЕПИСКИ.
 * <p>
 * Пример «вопроса о статистике по обмену сообщениями» из требований.
 * Этот обработчик показывает, зачем нужен {@link BotContext}: чтобы построить
 * ответ, ему нужны данные истории ({@link ChatHistory}), а не только текст вопроса.
 */
public class StatsHandler extends MessageHandler {

    public StatsHandler() {
        super("(статистик\\w*|сколько\\s+сообщений|сколько\\s+я\\s+написал|сколько\\s+мы\\s+общались)");
    }

    @Override
    public String getName() {
        return "Статистика";
    }

    @Override
    public String getDescription() {
        return "Статистика переписки: «статистика», «сколько сообщений?»";
    }

    @Override
    public String respond(String input, BotContext context) {
        ChatHistory history = context.getHistory();

        // Текущее сообщение-вопрос ещё не добавлено в историю в момент ответа,
        // поэтому числа отражают переписку ДО этого вопроса — это нормально.
        long total = history.getTotalCount();
        long fromUser = history.getUserMessageCount();
        long fromBot = history.getBotMessageCount();

        return "Статистика нашей переписки:\n"
                + "• всего сообщений: " + total + "\n"
                + "• от вас (" + context.getUserName() + "): " + fromUser + "\n"
                + "• от меня (бота): " + fromBot;
    }
}
