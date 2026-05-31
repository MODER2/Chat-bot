package com.example.chatbot.bot;

import com.example.chatbot.bot.handler.MessageHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AbstractBot — АБСТРАКТНЫЙ КЛАСС-«заготовка» бота.
 * <p>
 * Реализует общую для всех ботов механику работы, описанную интерфейсом
 * {@link IBot}, но оставляет наследникам решение о том, КАКИЕ именно обработчики
 * подключить. Это реализуется через абстрактный метод
 * {@link #registerDefaultHandlers()} (шаблонный метод — Template Method).
 * <p>
 * Алгоритм ответа ({@link #respond(String)}):
 * <ol>
 *     <li>по очереди перебираем зарегистрированные обработчики;</li>
 *     <li>спрашиваем у каждого {@code canHandle(input)} — «ты можешь ответить?»;</li>
 *     <li>первый согласившийся обработчик строит ответ — на этом останавливаемся;</li>
 *     <li>если ни один не подошёл — возвращаем «ответ по умолчанию».</li>
 * </ol>
 * Порядок регистрации важен: более «узкие» (специфичные) обработчики стоит
 * добавлять раньше «широких».
 */
public abstract class AbstractBot implements IBot {

    /** Список обработчиков — «мозг» бота. Перебирается при каждом ответе. */
    private final List<MessageHandler> handlers = new ArrayList<>();

    /** Контекст: история и имя пользователя, доступные обработчикам. */
    protected final BotContext context;

    /** Отображаемое имя бота. */
    private final String botName;

    /**
     * @param botName имя бота (например, «Бот»)
     * @param context контекст с историей и именем пользователя
     */
    protected AbstractBot(String botName, BotContext context) {
        this.botName = botName;
        this.context = context;
        // Просим конкретного наследника подключить его набор обработчиков.
        registerDefaultHandlers();
    }

    /**
     * Наследник обязан реализовать этот метод и зарегистрировать в нём свои
     * обработчики через {@link #registerHandler(MessageHandler)}.
     * Вызывается автоматически из конструктора.
     */
    protected abstract void registerDefaultHandlers();

    @Override
    public void registerHandler(MessageHandler handler) {
        if (handler != null) {
            handlers.add(handler);
        }
    }

    @Override
    public List<MessageHandler> getHandlers() {
        return Collections.unmodifiableList(handlers);
    }

    @Override
    public String getBotName() {
        return botName;
    }

    /**
     * Перебирает обработчики и отдаёт ответ первого подходящего.
     * Если подходящего нет — вызывает {@link #defaultResponse(String)}.
     */
    @Override
    public String respond(String userInput) {
        if (userInput == null || userInput.isBlank()) {
            return "Вы ничего не написали. Напишите «помощь», чтобы узнать мои возможности.";
        }
        for (MessageHandler handler : handlers) {
            if (handler.canHandle(userInput)) {
                return handler.respond(userInput, context);
            }
        }
        return defaultResponse(userInput);
    }

    /**
     * Ответ по умолчанию, когда ни один обработчик не распознал команду.
     * Наследник может переопределить (например, перенаправить вопрос нейросети).
     *
     * @param userInput исходное сообщение пользователя
     * @return вежливый ответ-«заглушка»
     */
    protected String defaultResponse(String userInput) {
        return "Извините, я не понял запрос. Напишите «помощь», и я расскажу, что умею.";
    }
}
