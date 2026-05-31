package com.example.chatbot.bot.handler;

import com.example.chatbot.bot.BotContext;

import java.util.regex.Matcher;

/**
 * CalculatorHandler — обработчик АРИФМЕТИЧЕСКИХ команд С ПАРАМЕТРАМИ.
 * <p>
 * Это пример «ответа на команды с параметрами» из требований: «умножь 12 на 157».
 * Параметры (числа и операцию) мы извлекаем из текста с помощью ГРУПП ЗАХВАТА
 * в регулярном выражении — то, что заключено в круглые скобки {@code (...)}.
 * <p>
 * Поддерживаются две формы записи:
 * <ol>
 *     <li><b>Словесная:</b> «умножь 12 на 157», «сложи 2 и 3», «раздели 10 на 4»,
 *         «вычти 5 из 9»;</li>
 *     <li><b>Символьная:</b> «12 * 157», «2 + 3», «10 / 4», «9 - 5».</li>
 * </ol>
 */
public class CalculatorHandler extends MessageHandler {

    /*
     * Разбор регулярного выражения (две альтернативы через символ «|»):
     *
     * ВЕТКА 1 — словесная форма:
     *   (умнож\w*|раздел\w*|подел\w*|слож\w*|вычт\w*|приба\w*|отним\w*)  -> группа 1: глагол
     *   \s+(-?\d+(?:[.,]\d+)?)                                          -> группа 2: первое число
     *   \s+(?:на|и|от|к|с|из)\s+                                        -> связка-предлог (без захвата)
     *   (-?\d+(?:[.,]\d+)?)                                             -> группа 3: второе число
     *
     * ВЕТКА 2 — символьная форма:
     *   (-?\d+(?:[.,]\d+)?)        -> группа 4: первое число
     *   \s( знак операции )\s      -> группа 5: один из символов  + - * × ÷  или дробная черта
     *   (-?\d+(?:[.,]\d+)?)        -> группа 6: второе число
     *
     * Пояснения к числу  -?\d+(?:[.,]\d+)? :
     *   -?        — необязательный минус (отрицательные числа);
     *   \d+       — одна или более цифр;
     *   (?:[.,]\d+)? — необязательная дробная часть с точкой или запятой.
     */
    private static final String REGEX =
            "(умнож\\w*|раздел\\w*|подел\\w*|слож\\w*|вычт\\w*|приба\\w*|отним\\w*)"
                    + "\\s+(-?\\d+(?:[.,]\\d+)?)\\s+(?:на|и|от|к|с|из)\\s+(-?\\d+(?:[.,]\\d+)?)"
                    + "|(-?\\d+(?:[.,]\\d+)?)\\s*([+\\-*/×÷])\\s*(-?\\d+(?:[.,]\\d+)?)";

    public CalculatorHandler() {
        super(REGEX);
    }

    @Override
    public String getName() {
        return "Калькулятор";
    }

    @Override
    public String getDescription() {
        return "Посчитать: «умножь 12 на 157», «сложи 2 и 3», «12 * 157»";
    }

    @Override
    public String respond(String input, BotContext context) {
        Matcher m = matcher(input);
        if (!m.find()) {
            // Теоретически сюда не попадём (canHandle уже проверил совпадение),
            // но на всякий случай возвращаем понятное сообщение.
            return "Не удалось разобрать выражение. Пример: «умножь 12 на 157».";
        }

        double a;
        double b;
        char operation;

        // group(1) != null означает, что сработала ПЕРВАЯ (словесная) ветка.
        if (m.group(1) != null) {
            String verb = m.group(1).toLowerCase();
            a = parseNumber(m.group(2));
            b = parseNumber(m.group(3));
            operation = verbToOperation(verb);
        } else {
            // Иначе сработала ВТОРАЯ (символьная) ветка — группы 4,5,6.
            a = parseNumber(m.group(4));
            operation = normalizeSign(m.group(5));
            b = parseNumber(m.group(6));
        }

        // Выполняем арифметику.
        Double result = calculate(a, operation, b);
        if (result == null) {
            return "На ноль делить нельзя.";
        }
        return formatNumber(a) + " " + operation + " " + formatNumber(b)
                + " = " + formatNumber(result);
    }

    /**
     * Преобразует строку с числом в double. Запятую заменяем на точку,
     * так как Double.parseDouble понимает только точку.
     */
    private double parseNumber(String raw) {
        return Double.parseDouble(raw.replace(',', '.'));
    }

    /**
     * Сопоставляет русский глагол знаку операции.
     * Используем startsWith по корню слова, чтобы ловить разные окончания
     * («умножь», «умножить», «умножай»).
     */
    private char verbToOperation(String verb) {
        if (verb.startsWith("умнож")) {
            return '*';
        }
        if (verb.startsWith("раздел") || verb.startsWith("подел")) {
            return '/';
        }
        if (verb.startsWith("слож") || verb.startsWith("приба")) {
            return '+';
        }
        // «вычт», «отним»
        return '-';
    }

    /** Приводит символы × и ÷ к обычным * и /. */
    private char normalizeSign(String sign) {
        switch (sign) {
            case "×":
                return '*';
            case "÷":
                return '/';
            default:
                return sign.charAt(0);
        }
    }

    /**
     * Собственно вычисление.
     *
     * @return результат, либо null при делении на ноль
     */
    private Double calculate(double a, char op, double b) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) {
                    return null; // деление на ноль недопустимо
                }
                return a / b;
            default:
                return null;
        }
    }

    /**
     * Аккуратно форматирует число: если оно целое (12.0) — показываем «12»,
     * иначе оставляем дробную часть.
     */
    private String formatNumber(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}
