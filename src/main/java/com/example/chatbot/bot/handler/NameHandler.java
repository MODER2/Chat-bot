package com.example.chatbot.bot.handler;

import com.example.chatbot.bot.BotContext;

/**
 * NameHandler — обработчик вопросов про ИМЕНА.
 * <p>
 * Отвечает на «Как меня зовут?» (берёт имя из контекста) и «Как тебя зовут?»
 * (рассказывает имя бота). Ещё один пример использования {@link BotContext}.
 */
public class NameHandler extends MessageHandler {

    public NameHandler() {
        super("(как\\s+(меня|тебя)\\s+зовут|(моё|твоё|мое|твое)\\s+имя|кто\\s+ты)");
    }

    @Override
    public String getName() {
        return "Имена";
    }

    @Override
    public String getDescription() {
        return "Вопросы об именах: «Как меня зовут?», «Кто ты?»";
    }

    @Override
    public String respond(String input, BotContext context) {
        String lower = input.toLowerCase();
        // Если в вопросе есть «меня» или «моё/мое имя» — речь о пользователе.
        if (lower.contains("меня") || lower.contains("моё имя") || lower.contains("мое имя")) {
            return "Вас зовут " + context.getUserName() + ".";
        }
        // Иначе вопрос про бота.
        return "Я — чат-бот. Меня зовут Бот. Я помогаю с простыми вопросами и вычислениями.";
    }
}
