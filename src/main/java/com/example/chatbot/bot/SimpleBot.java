package com.example.chatbot.bot;

import com.example.chatbot.bot.handler.CalculatorHandler;
import com.example.chatbot.bot.handler.DateHandler;
import com.example.chatbot.bot.handler.GreetingHandler;
import com.example.chatbot.bot.handler.HelpHandler;
import com.example.chatbot.bot.handler.LlmHandler;
import com.example.chatbot.bot.handler.NameHandler;
import com.example.chatbot.bot.handler.StatsHandler;
import com.example.chatbot.bot.handler.TimeHandler;
import com.example.chatbot.bot.handler.WebFactHandler;

/**
 * SimpleBot — КОНКРЕТНАЯ реализация бота на основе правил (обработчиков).
 * <p>
 * Наследуется от {@link AbstractBot} и реализует единственный «недостающий» метод
 * {@link #registerDefaultHandlers()}, в котором подключает нужный набор
 * обработчиков. Вся остальная логика (перебор обработчиков, выбор подходящего)
 * уже написана в родительском классе.
 * <p>
 * <b>Порядок регистрации = порядок проверки.</b> Сначала идут более «специфичные»
 * обработчики, затем более «широкие». Так мы избегаем ситуаций, когда общий
 * обработчик перехватывает сообщение раньше специального.
 */
public class SimpleBot extends AbstractBot {

    /**
     * @param context контекст с историей и именем пользователя
     */
    public SimpleBot(BotContext context) {
        super("Бот", context);
    }

    @Override
    protected void registerDefaultHandlers() {
        // 1. Справка — должна распознаваться раньше «болтовни».
        registerHandler(new HelpHandler(this));
        // 2. Приветствия.
        registerHandler(new GreetingHandler());
        // 3. Имена («как меня/тебя зовут»).
        registerHandler(new NameHandler());
        // 4. Время.
        registerHandler(new TimeHandler());
        // 5. Дата.
        registerHandler(new DateHandler());
        // 6. Статистика переписки.
        registerHandler(new StatsHandler());
        // 7. Калькулятор (команды с параметрами).
        registerHandler(new CalculatorHandler());
        // 8. Факт из интернета (демонстрация веб-запроса).
        registerHandler(new WebFactHandler());
        // 9. Вопрос к нейросети (демонстрация работы с LLM-сервером).
        registerHandler(new LlmHandler());

        // Чтобы научить бота новой команде, достаточно написать новый класс-наследник
        // MessageHandler и добавить здесь одну строчку registerHandler(...).
    }
}
