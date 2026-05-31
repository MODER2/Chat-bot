package com.example.chatbot.bot;

import com.example.chatbot.model.ChatHistory;

/**
 * BotContext — «контекст бота»: набор данных, к которым обработчики сообщений
 * могут обращаться, чтобы построить ответ.
 * <p>
 * Зачем он нужен? Некоторым обработчикам мало одного лишь текста запроса:
 * <ul>
 *     <li>обработчик статистики должен заглянуть в {@link ChatHistory},
 *         чтобы посчитать количество сообщений;</li>
 *     <li>обработчик «как меня зовут?» должен знать имя пользователя.</li>
 * </ul>
 * Чтобы не передавать в каждый метод множество отдельных параметров, мы
 * упаковываем их в один объект-контекст и отдаём его обработчику целиком.
 * <p>
 * Класс относится к логике бота и НЕ зависит от JavaFX.
 */
public class BotContext {

    /** Ссылка на историю переписки (общая с приложением). */
    private final ChatHistory history;

    /** Имя пользователя, введённое в стартовом окне. */
    private String userName;

    /**
     * @param history история сообщений, которую обработчики смогут читать
     * @param userName имя пользователя (можно изменить позже через сеттер)
     */
    public BotContext(ChatHistory history, String userName) {
        this.history = history;
        this.userName = userName;
    }

    public ChatHistory getHistory() {
        return history;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
