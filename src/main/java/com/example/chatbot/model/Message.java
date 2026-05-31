package com.example.chatbot.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Message — МОДЕЛЬ одного сообщения в чате.
 * <p>
 * Это «единица данных»: ровно одна строчка переписки. Класс относится к слою
 * <b>Model</b> (модель данных) архитектуры приложения и НЕ зависит ни от JavaFX,
 * ни от способа хранения. Благодаря этому те же объекты {@code Message} можно
 * использовать и в десктоп-приложении, и в онлайн-боте.
 * <p>
 * Каждое сообщение хранит:
 * <ul>
 *     <li>{@link #author}    — кто отправил (USER или BOT);</li>
 *     <li>{@link #text}      — сам текст сообщения;</li>
 *     <li>{@link #timestamp} — момент отправки (дата и время).</li>
 * </ul>
 * <p>
 * Класс сделан <b>неизменяемым</b> (immutable): все поля {@code final}, а методов,
 * меняющих состояние, нет. Неизменяемые объекты безопаснее — их нельзя случайно
 * «испортить» из другого места программы.
 */
public final class Message {

    /** Формат даты-времени для записи в файл: "2026-05-31 14:03:17". */
    private static final DateTimeFormatter STORAGE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Формат времени для показа в окне чата: "14:03". */
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    /** Кто отправил сообщение. */
    private final Author author;

    /** Текст сообщения. */
    private final String text;

    /** Дата и время отправки. */
    private final LocalDateTime timestamp;

    /**
     * Полный конструктор. Используется, когда время уже известно
     * (например, при загрузке истории из файла).
     *
     * @param author    автор сообщения
     * @param text      текст сообщения
     * @param timestamp момент отправки
     */
    public Message(Author author, String text, LocalDateTime timestamp) {
        this.author = author;
        this.text = text;
        this.timestamp = timestamp;
    }

    /**
     * Удобный конструктор: время проставляется автоматически «сейчас».
     * Именно его мы вызываем, когда пользователь или бот отправляет новое сообщение.
     *
     * @param author автор сообщения
     * @param text   текст сообщения
     */
    public Message(Author author, String text) {
        this(author, text, LocalDateTime.now());
    }

    public Author getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * @return время в коротком виде "HH:mm" для подписи под сообщением в окне
     */
    public String getDisplayTime() {
        return timestamp.format(DISPLAY_FORMAT);
    }

    /**
     * Превращает сообщение в одну строку для записи в файл (сериализация).
     * <p>
     * Формат: {@code АВТОР|ВРЕМЯ|ТЕКСТ}. Разделитель — символ '|'.
     * В тексте перевод строки заменяем на маркер \n, чтобы одно сообщение
     * занимало ровно одну строку в файле.
     * <p>
     * Пример: {@code USER|2026-05-31 14:03:17|Привет, Бот!}
     *
     * @return строковое представление для сохранения в файл
     */
    public String toStorageLine() {
        String safeText = text.replace("\n", "\\n");
        return author.name() + "|" + timestamp.format(STORAGE_FORMAT) + "|" + safeText;
    }

    /**
     * Обратная операция к {@link #toStorageLine()}: разбирает строку из файла
     * и создаёт объект {@code Message} (десериализация).
     *
     * @param line строка из файла истории
     * @return восстановленное сообщение, либо {@code null}, если строка «битая»
     */
    public static Message fromStorageLine(String line) {
        // Делим строку максимум на 3 части: автор, время, текст.
        // limit=3 нужен, чтобы символы '|' внутри текста не ломали разбор.
        String[] parts = line.split("\\|", 3);
        if (parts.length < 3) {
            return null; // строка повреждена — пропускаем её
        }
        try {
            Author author = Author.valueOf(parts[0]);
            LocalDateTime time = LocalDateTime.parse(parts[1], STORAGE_FORMAT);
            String text = parts[2].replace("\\n", "\n");
            return new Message(author, text, time);
        } catch (Exception e) {
            // Если автор или дата не распознались — считаем строку битой.
            return null;
        }
    }

    /**
     * Текстовое представление объекта (используется при отладке).
     */
    @Override
    public String toString() {
        return "[" + getDisplayTime() + "] " + author.getDisplayName() + ": " + text;
    }
}
