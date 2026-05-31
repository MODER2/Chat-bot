package com.example.chatbot.bot.handler;

import com.example.chatbot.bot.BotContext;
import com.example.chatbot.bot.IBot;

/**
 * HelpHandler — обработчик команды «ПОМОЩЬ».
 * <p>
 * Показывает список всего, что умеет бот. Список строится ДИНАМИЧЕСКИ: обработчик
 * спрашивает у бота его обработчики ({@link IBot#getHandlers()}) и печатает их
 * имена и описания. Благодаря этому, когда вы добавите новый обработчик, он
 * автоматически появится в справке — отдельно править текст помощи не нужно.
 */
public class HelpHandler extends MessageHandler {

    /** Ссылка на бота нужна, чтобы перечислить его обработчики. */
    private final IBot bot;

    /**
     * @param bot бот, чьи возможности будем перечислять
     */
    public HelpHandler(IBot bot) {
        super("(помощь|помоги|что\\s+ты\\s+умеешь|команды|справка|хелп|help)");
        this.bot = bot;
    }

    @Override
    public String getName() {
        return "Помощь";
    }

    @Override
    public String getDescription() {
        return "Список команд: «помощь», «что ты умеешь?»";
    }

    @Override
    public String respond(String input, BotContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Вот что я умею:\n");
        // Перебираем все обработчики бота и печатаем их описания.
        for (MessageHandler handler : bot.getHandlers()) {
            // Саму «помощь» в список не включаем, чтобы не было рекурсии в тексте.
            if (handler == this) {
                continue;
            }
            sb.append("• ").append(handler.getDescription()).append("\n");
        }
        sb.append("А ещё я просто поболтаю, если напишете что-то своё.");
        return sb.toString();
    }
}
