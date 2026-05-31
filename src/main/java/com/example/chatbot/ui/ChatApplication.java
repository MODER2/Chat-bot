package com.example.chatbot.ui;

import com.example.chatbot.bot.BotContext;
import com.example.chatbot.bot.IBot;
import com.example.chatbot.bot.SimpleBot;
import com.example.chatbot.model.ChatHistory;
import com.example.chatbot.persistence.FileHistoryStorage;
import com.example.chatbot.persistence.HistoryStorage;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

/**
 * ChatApplication — ТОЧКА ВХОДА приложения и «дирижёр», связывающий все слои.
 * <p>
 * Это единственный класс, который «знает» обо всём: о модели, о боте, о хранилище
 * и об окнах. Он не содержит бизнес-логики — только порядок запуска:
 * <ol>
 *     <li>загрузить историю из файла (если она есть);</li>
 *     <li>показать окно ввода имени;</li>
 *     <li>создать бота;</li>
 *     <li>открыть основное окно чата;</li>
 *     <li>при закрытии — сохранить историю в файл.</li>
 * </ol>
 * JavaFX устроен так: метод {@link #main(String[])} вызывает {@link #launch},
 * платформа сама инициализируется и вызывает {@link #start(Stage)} в потоке
 * интерфейса (JavaFX Application Thread).
 */
public class ChatApplication extends Application {

    /** Имя файла, в котором хранится история переписки. */
    private static final String HISTORY_FILE = "chat_history.txt";

    /** Пути к ресурсам внутри проекта (папка resources). */
    private static final String ICON_PATH = "/com/example/chatbot/ui/icon.png";
    private static final String CSS_PATH = "/com/example/chatbot/ui/styles.css";

    /** Хранилище истории. Создаётся при старте, используется при выходе. */
    private final HistoryStorage storage = new FileHistoryStorage(HISTORY_FILE);

    /** История сообщений в оперативной памяти. */
    private final ChatHistory history = new ChatHistory();

    /**
     * Главный метод JavaFX-приложения. Вызывается автоматически после
     * инициализации платформы.
     *
     * @param primaryStage главное окно, которое создаёт платформа
     */
    @Override
    public void start(Stage primaryStage) {
        // 1) Загружаем историю из файла в оперативную память.
        history.addAll(storage.load());

        // 2) Готовим общие ресурсы — иконку и стиль.
        Image icon = loadIcon();
        String stylesheet = loadStylesheet();

        // 3) Показываем окно ввода имени и ЖДЁМ результат.
        LoginWindow login = new LoginWindow();
        Optional<String> nameOpt = login.showAndGetName(icon, stylesheet);
        if (nameOpt.isEmpty()) {
            // Пользователь закрыл окно входа — завершаем приложение.
            Platform.exit();
            return;
        }
        String userName = nameOpt.get();

        // 4) Создаём контекст и бота. Бот зависит от истории и имени пользователя.
        BotContext context = new BotContext(history, userName);
        IBot bot = new SimpleBot(context);

        // 5) Строим основное окно чата.
        new ChatWindow(primaryStage, bot, history, icon, stylesheet);

        // 6) Перед закрытием окна сохраняем историю в файл.
        primaryStage.setOnCloseRequest(event -> storage.save(history.getMessages()));

        primaryStage.show();
    }

    /**
     * Дополнительно сохраняем историю в методе stop(), который JavaFX вызывает
     * при штатном завершении приложения (подстраховка к setOnCloseRequest).
     */
    @Override
    public void stop() {
        storage.save(history.getMessages());
    }

    /**
     * Загружает иконку приложения из ресурсов.
     *
     * @return объект Image или null, если файл не найден
     */
    private Image loadIcon() {
        try (InputStream is = getClass().getResourceAsStream(ICON_PATH)) {
            return (is == null) ? null : new Image(is);
        } catch (Exception e) {
            return null; // без иконки приложение всё равно запустится
        }
    }

    /**
     * Возвращает ссылку на CSS-файл стилей в виде строки для JavaFX.
     *
     * @return URL стиля или null, если файл не найден
     */
    private String loadStylesheet() {
        URL url = getClass().getResource(CSS_PATH);
        return (url == null) ? null : url.toExternalForm();
    }

    /**
     * Стандартная точка входа Java-программы.
     * {@link Application#launch} запускает механизм JavaFX.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
