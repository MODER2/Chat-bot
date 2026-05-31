package com.example.chatbot.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * LoginWindow — СТАРТОВОЕ ОКНО для ввода имени пользователя.
 * <p>
 * Реализует требование: «При запуске появляется окно для задания имени
 * пользователя, потом основное окно». Окно модальное — пока не введём имя,
 * к основному окну перейти нельзя.
 * <p>
 * Класс относится к слою UI (интерфейс). Он НЕ содержит логики бота — только
 * собирает имя и отдаёт его наружу.
 */
public class LoginWindow {

    /** Сюда сохраняем введённое имя; остаётся null, если окно просто закрыли. */
    private String enteredName = null;

    /**
     * Показывает окно и ЖДЁТ, пока пользователь введёт имя или закроет окно.
     *
     * @param icon       иконка приложения (может быть null)
     * @param stylesheet ссылка на CSS-стиль (может быть null)
     * @return Optional с именем, либо пустой Optional, если окно закрыли без ввода
     */
    public Optional<String> showAndGetName(Image icon, String stylesheet) {
        // Создаём отдельное окно (Stage) и делаем его модальным.
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Вход в чат");
        if (icon != null) {
            stage.getIcons().add(icon);
        }

        // --- Элементы интерфейса ---
        Label title = new Label("Добро пожаловать!\nКак вас зовут?");
        title.getStyleClass().add("header-label");
        title.setWrapText(true);
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);

        TextField nameField = new TextField();
        nameField.setPromptText("Введите имя…"); // подсказка внутри поля

        Button okButton = new Button("Войти");
        okButton.getStyleClass().add("send-button");
        okButton.setDefaultButton(true); // срабатывает по Enter

        // --- Логика кнопки «Войти» ---
        Runnable submit = () -> {
            String text = nameField.getText().trim();
            // Если поле пустое — используем имя «Гость».
            enteredName = text.isEmpty() ? "Гость" : text;
            stage.close();
        };
        okButton.setOnAction(e -> submit.run());
        // Enter в поле ввода тоже выполняет вход.
        nameField.setOnAction(e -> submit.run());

        // --- Компоновка ---
        VBox layout = new VBox(14, title, nameField, okButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(24));

        Scene scene = new Scene(layout, 360, 240);
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet);
        }
        stage.setScene(scene);

        // Курсор сразу в поле ввода.
        nameField.requestFocus();

        // showAndWait блокирует выполнение, пока окно не закроется.
        stage.showAndWait();

        return Optional.ofNullable(enteredName);
    }
}
