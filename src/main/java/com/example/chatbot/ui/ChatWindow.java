package com.example.chatbot.ui;

import com.example.chatbot.bot.IBot;
import com.example.chatbot.model.Author;
import com.example.chatbot.model.ChatHistory;
import com.example.chatbot.model.Message;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * ChatWindow — ОСНОВНОЕ ОКНО приложения (чат).
 * <p>
 * Отвечает за визуальную часть: список сообщений в виде «пузырей», поле ввода,
 * кнопку «Отправить» и горячие клавиши. Логику ответов окно НЕ содержит — оно
 * лишь обращается к {@link IBot}. Это и есть разделение «интерфейс ↔ логика»:
 * тот же бот мог бы работать и без этого окна.
 */
public class ChatWindow {

    /** Бот, который формирует ответы. Зависимость от интерфейса, а не от реализации. */
    private final IBot bot;

    /** История сообщений (модель). Сюда же сохраняем переписку перед выходом. */
    private final ChatHistory history;

    /**
     * Список сообщений для отображения. ObservableList — «наблюдаемый» список:
     * при добавлении элемента ListView сам перерисовывается.
     */
    private final ObservableList<Message> items = FXCollections.observableArrayList();

    /** Виджет списка сообщений. */
    private final ListView<Message> listView = new ListView<>(items);

    /** Поле ввода текста. TextArea — многострочное (можно перенос по Shift+Enter). */
    private final TextArea inputArea = new TextArea();

    private final Scene scene;

    /**
     * @param stage      окно, в котором показываем чат
     * @param bot        бот для генерации ответов
     * @param history    история сообщений
     * @param icon       иконка приложения (может быть null)
     * @param stylesheet ссылка на CSS (может быть null)
     */
    public ChatWindow(Stage stage, IBot bot, ChatHistory history, Image icon, String stylesheet) {
        this.bot = bot;
        this.history = history;

        // ----- Заголовок -----
        Label header = new Label("Чат с ботом");
        header.getStyleClass().add("header-label");
        header.setMaxWidth(Double.MAX_VALUE);

        // ----- Список сообщений с «пузырями» -----
        configureMessageCells();
        VBox.setVgrow(listView, Priority.ALWAYS);

        // ----- Поле ввода + кнопка -----
        inputArea.setPromptText("Введите сообщение…  (Enter — отправить, Shift+Enter — новая строка)");
        inputArea.setWrapText(true);
        inputArea.setPrefRowCount(2);
        HBox.setHgrow(inputArea, Priority.ALWAYS);

        Button sendButton = new Button("Отправить");
        sendButton.getStyleClass().add("send-button");
        sendButton.setOnAction(e -> sendMessage());

        HBox inputBar = new HBox(10, inputArea, sendButton);
        inputBar.setPadding(new Insets(10));
        inputBar.setAlignment(Pos.CENTER);

        // ----- Горячие клавиши -----
        setupHotkeys();

        // ----- Компоновка окна -----
        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(listView);
        root.setBottom(inputBar);

        scene = new Scene(root, 560, 640);
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet);
        }

        stage.setScene(scene);
        stage.setTitle("JavaFX Чат-бот");
        if (icon != null) {
            stage.getIcons().add(icon);
        }

        // Показываем уже загруженную из файла историю.
        renderExistingHistory();
    }

    /**
     * Настраивает, КАК рисуется каждая строка списка: в виде «пузыря» сообщения
     * с подписью (автор + время), выровненного слева (бот) или справа (пользователь).
     */
    private void configureMessageCells() {
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                // Сам текст сообщения внутри «пузыря».
                Label bubble = new Label(msg.getText());
                bubble.setWrapText(true);
                bubble.setMaxWidth(380);

                // Подпись под пузырём: «Автор · ЧЧ:ММ».
                Label meta = new Label(msg.getAuthor().getDisplayName() + " · " + msg.getDisplayTime());
                meta.getStyleClass().add("meta-label");

                boolean fromUser = msg.getAuthor() == Author.USER;
                // Разные стили пузыря для пользователя и бота.
                bubble.getStyleClass().add(fromUser ? "bubble-user" : "bubble-bot");

                VBox box = new VBox(3, bubble, meta);
                box.setAlignment(fromUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

                // Контейнер на всю ширину, чтобы выровнять пузырь влево/вправо.
                HBox row = new HBox(box);
                row.setAlignment(fromUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                setGraphic(row);
                setText(null);
            }
        });
    }

    /**
     * Назначает горячие клавиши на поле ввода:
     * <ul>
     *     <li><b>Enter</b> — отправить сообщение;</li>
     *     <li><b>Ctrl+Enter</b> — тоже отправить (как указано в задании);</li>
     *     <li><b>Shift+Enter</b> — перенос строки (не отправлять).</li>
     * </ul>
     * Используем EventFilter, чтобы перехватить нажатие ДО того, как TextArea
     * вставит перевод строки.
     */
    private void setupHotkeys() {
        inputArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                if (e.isShiftDown()) {
                    // Shift+Enter — оставляем стандартное поведение (новая строка).
                    return;
                }
                // Enter или Ctrl+Enter — отправляем и «гасим» событие,
                // чтобы перевод строки не попал в поле.
                e.consume();
                sendMessage();
            }
        });
    }

    /**
     * Отправка сообщения: добавляем реплику пользователя, получаем ответ бота,
     * добавляем его и прокручиваем список вниз.
     */
    private void sendMessage() {
        String text = inputArea.getText().trim();
        if (text.isEmpty()) {
            return; // пустые сообщения не отправляем
        }

        // 1) Сообщение пользователя.
        addMessage(new Message(Author.USER, text));

        // 2) Ответ бота (вся «умная» часть — внутри bot.respond()).
        String answer = bot.respond(text);
        addMessage(new Message(Author.BOT, answer));

        // 3) Очищаем поле ввода и возвращаем в него фокус.
        inputArea.clear();
        inputArea.requestFocus();
    }

    /**
     * Добавляет сообщение и в модель (history), и в отображаемый список (items),
     * после чего прокручивает список к последнему сообщению.
     */
    private void addMessage(Message message) {
        history.add(message);
        items.add(message);
        listView.scrollTo(items.size() - 1);
    }

    /**
     * Показывает историю, загруженную из файла при старте. Если истории нет —
     * бот здоровается первым.
     */
    private void renderExistingHistory() {
        if (history.isEmpty()) {
            // Первое приветствие бота (добавляем и в историю, и на экран).
            addMessage(new Message(Author.BOT,
                    "Здравствуйте! Я чат-бот. Напишите «помощь», чтобы узнать мои возможности."));
        } else {
            // Просто показываем ранее сохранённые сообщения (в историю не дублируем).
            items.addAll(history.getMessages());
            listView.scrollTo(items.size() - 1);
        }
    }

    /** @return сцена основного окна (нужна приложению при необходимости) */
    public Scene getScene() {
        return scene;
    }
}
