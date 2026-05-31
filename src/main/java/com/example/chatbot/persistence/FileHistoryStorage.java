package com.example.chatbot.persistence;

import com.example.chatbot.model.Message;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * FileHistoryStorage — реализация {@link HistoryStorage}, хранящая историю
 * в ТЕКСТОВОМ ФАЙЛЕ.
 * <p>
 * Выполняет требование задания: «записывать историю в файл при завершении
 * программы; загружать из файла при запуске». Каждое сообщение — одна строка
 * файла (формат описан в {@link Message#toStorageLine()}).
 * <p>
 * Используется кодировка UTF-8, чтобы корректно сохранять русский текст.
 */
public class FileHistoryStorage implements HistoryStorage {

    /** Путь к файлу истории. По умолчанию — «chat_history.txt» рядом с программой. */
    private final Path file;

    /**
     * @param fileName имя (или путь) файла, в котором хранится история
     */
    public FileHistoryStorage(String fileName) {
        this.file = Path.of(fileName);
    }

    @Override
    public void save(List<Message> messages) {
        // Превращаем каждое сообщение в строку и собираем список строк.
        List<String> lines = new ArrayList<>();
        for (Message m : messages) {
            lines.add(m.toStorageLine());
        }
        try {
            // Files.write перезаписывает файл целиком списком строк в UTF-8.
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Не роняем программу из-за проблемы с диском — просто сообщаем в консоль.
            System.err.println("Не удалось сохранить историю: " + e.getMessage());
        }
    }

    @Override
    public List<Message> load() {
        List<Message> result = new ArrayList<>();
        // Если файла ещё нет (первый запуск) — возвращаем пустую историю.
        if (!Files.exists(file)) {
            return result;
        }
        try {
            // Читаем все строки файла.
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.isBlank()) {
                    continue; // пропускаем пустые строки
                }
                Message m = Message.fromStorageLine(line);
                if (m != null) { // null = строка повреждена, пропускаем
                    result.add(m);
                }
            }
        } catch (IOException e) {
            System.err.println("Не удалось загрузить историю: " + e.getMessage());
        }
        return result;
    }
}
