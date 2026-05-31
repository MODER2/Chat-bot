package com.example.chatbot.bot.handler;

import com.example.chatbot.bot.BotContext;

/**
 * GreetingHandler — обработчик ПРИВЕТСТВИЙ.
 * <p>
 * Реагирует на реплики заданного шаблона: «Привет», «Привет, Бот!»,
 * «Здравствуй», «Хай», «Доброе утро/день/вечер» и т.п.
 * Это пример ответа «на несколько реплик заданного шаблона» из требований.
 */
public class GreetingHandler extends MessageHandler {

    /**
     * Регулярное выражение разбираем по частям:
     * <ul>
     *   <li>{@code \b} — граница слова, чтобы «привет» не срабатывал внутри
     *       другого слова;</li>
     *   <li>{@code (привет|здравствуй|...)} — список синонимов приветствия;</li>
     *   <li>{@code \w*} — допускаем окончания: «приветИК», «здравствуйТЕ»;</li>
     *   <li>{@code доброе\s+(утро|день|вечер)} — отдельная ветка для пожеланий.</li>
     * </ul>
     */
    public GreetingHandler() {
        super("\\b(привет\\w*|здравствуй\\w*|хай|хеллоу|здаров\\w*|доброе\\s+(утро|день|вечер))\\b");
    }

    @Override
    public String getName() {
        return "Приветствие";
    }

    @Override
    public String getDescription() {
        return "Поздороваться: «Привет, Бот!», «Здравствуй», «Доброе утро»";
    }

    @Override
    public String respond(String input, BotContext context) {
        // Обращаемся к пользователю по имени, которое он ввёл при запуске.
        String name = context.getUserName();
        return "Привет, " + name + "! Рад вас видеть. Чем могу помочь? "
                + "Напишите «помощь», чтобы узнать мои команды.";
    }
}
