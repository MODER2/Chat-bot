package com.example.chatbot.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ChatHistory — ИСТОРИЯ переписки, которая хранится в ОПЕРАТИВНОЙ ПАМЯТИ (RAM).
 * <p>
 * Это контейнер для всех {@link Message} текущего сеанса. Класс относится к слою
 * <b>Model</b> и не знает ничего про интерфейс. Он умеет:
 * <ul>
 *     <li>добавлять сообщения ({@link #add(Message)});</li>
 *     <li>отдавать весь список ({@link #getMessages()});</li>
 *     <li>считать простую статистику (сколько сообщений всего, от пользователя,
 *         от бота) — это нужно для команд вида «сколько сообщений?».</li>
 * </ul>
 * <p>
 * «Хранение в оперативной памяти» означает, что список живёт, пока запущена
 * программа. Чтобы данные не пропали после закрытия, отдельный класс
 * {@code FileHistoryStorage} сохраняет историю в файл и загружает обратно.
 */
public class ChatHistory {

    /**
     * Внутренний список сообщений. Это и есть «оперативная память» истории.
     * ArrayList хранит элементы в порядке добавления — как в реальной переписке.
     */
    private final List<Message> messages = new ArrayList<>();

    /**
     * Добавляет одно сообщение в конец истории.
     *
     * @param message сообщение для добавления (не должно быть null)
     */
    public void add(Message message) {
        if (message != null) {
            messages.add(message);
        }
    }

    /**
     * Добавляет сразу несколько сообщений (например, при загрузке из файла).
     *
     * @param loaded список загруженных сообщений
     */
    public void addAll(List<Message> loaded) {
        if (loaded != null) {
            messages.addAll(loaded);
        }
    }

    /**
     * Возвращает историю «только для чтения».
     * <p>
     * {@link Collections#unmodifiableList} оборачивает список так, что вызывающий
     * код может его читать и перебирать, но НЕ может изменить (добавить/удалить).
     * Так мы защищаем внутреннее состояние истории от изменений «снаружи».
     *
     * @return неизменяемый список всех сообщений
     */
    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    /** @return общее количество сообщений в истории */
    public int getTotalCount() {
        return messages.size();
    }

    /**
     * Считает количество сообщений конкретного автора.
     *
     * @param author автор, чьи сообщения считаем
     * @return количество сообщений этого автора
     */
    public long countByAuthor(Author author) {
        // Перебираем все сообщения и считаем те, у кого автор совпадает.
        return messages.stream()
                .filter(m -> m.getAuthor() == author)
                .count();
    }

    /** @return количество сообщений, отправленных пользователем */
    public long getUserMessageCount() {
        return countByAuthor(Author.USER);
    }

    /** @return количество сообщений, отправленных ботом */
    public long getBotMessageCount() {
        return countByAuthor(Author.BOT);
    }

    /** @return true, если в истории ещё нет ни одного сообщения */
    public boolean isEmpty() {
        return messages.isEmpty();
    }
}
