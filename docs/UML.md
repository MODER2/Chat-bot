# UML — диаграмма классов чат-бота

UML (Unified Modeling Language) — стандартный язык графических схем для описания
структуры программы. Диаграмма классов показывает **из каких классов** состоит
программа и **как они связаны** между собой.

> Диаграмма ниже написана на **Mermaid**. GitHub, IntelliJ IDEA (с плагином Mermaid)
> и многие редакторы Markdown отрисуют её автоматически. Если у вас не отображается
> картинка — текст диаграммы всё равно читается как структурированное описание.

## Обозначения связей

| Стрелка | Значение | Пример в проекте |
|---------|----------|------------------|
| `..|>`  | реализует интерфейс (realization) | `SimpleBot` реализует `IBot` |
| `--|>`  | наследует класс (inheritance) | `GreetingHandler` наследует `MessageHandler` |
| `o--`   | агрегация (хранит ссылку/список) | `ChatHistory` хранит список `Message` |
| `-->`   | зависимость/использование | `ChatWindow` использует `IBot` |

## Диаграмма классов

```mermaid
classDiagram
    direction LR

    %% ===== Слой МОДЕЛИ (Model) =====
    class Author {
        <<enumeration>>
        USER
        BOT
        +getDisplayName() String
    }

    class Message {
        -Author author
        -String text
        -LocalDateTime timestamp
        +getDisplayTime() String
        +toStorageLine() String
        +fromStorageLine(String)$ Message
    }

    class ChatHistory {
        -List~Message~ messages
        +add(Message) void
        +getMessages() List~Message~
        +getUserMessageCount() long
        +getBotMessageCount() long
    }

    %% ===== Слой ЛОГИКИ БОТА (Bot) =====
    class IBot {
        <<interface>>
        +respond(String) String
        +registerHandler(MessageHandler) void
        +getHandlers() List~MessageHandler~
        +getBotName() String
    }

    class AbstractBot {
        <<abstract>>
        -List~MessageHandler~ handlers
        #BotContext context
        +respond(String) String
        #registerDefaultHandlers()* void
        #defaultResponse(String) String
    }

    class SimpleBot {
        +registerDefaultHandlers() void
    }

    class BotContext {
        -ChatHistory history
        -String userName
        +getHistory() ChatHistory
        +getUserName() String
    }

    class MessageHandler {
        <<abstract>>
        -Pattern pattern
        +canHandle(String) boolean
        #matcher(String) Matcher
        +getName()* String
        +getDescription()* String
        +respond(String, BotContext)* String
    }

    class GreetingHandler
    class TimeHandler
    class DateHandler
    class CalculatorHandler
    class StatsHandler
    class NameHandler
    class HelpHandler
    class WebFactHandler
    class LlmHandler

    %% ===== Слой ХРАНЕНИЯ (Persistence) =====
    class HistoryStorage {
        <<interface>>
        +save(List~Message~) void
        +load() List~Message~
    }
    class FileHistoryStorage {
        -Path file
        +save(List~Message~) void
        +load() List~Message~
    }

    %% ===== Слой ИНТЕРФЕЙСА (UI) =====
    class ChatApplication {
        +start(Stage) void
        +stop() void
        +main(String[])$ void
    }
    class LoginWindow {
        +showAndGetName(Image, String) Optional~String~
    }
    class ChatWindow {
        -IBot bot
        -ChatHistory history
        -sendMessage() void
    }

    %% ===== Связи =====
    AbstractBot ..|> IBot
    SimpleBot --|> AbstractBot
    AbstractBot o-- MessageHandler
    AbstractBot --> BotContext
    BotContext --> ChatHistory

    GreetingHandler --|> MessageHandler
    TimeHandler --|> MessageHandler
    DateHandler --|> MessageHandler
    CalculatorHandler --|> MessageHandler
    StatsHandler --|> MessageHandler
    NameHandler --|> MessageHandler
    HelpHandler --|> MessageHandler
    WebFactHandler --|> MessageHandler
    LlmHandler --|> MessageHandler

    ChatHistory o-- Message
    Message --> Author

    FileHistoryStorage ..|> HistoryStorage

    ChatApplication --> LoginWindow
    ChatApplication --> ChatWindow
    ChatApplication --> HistoryStorage
    ChatApplication --> SimpleBot
    ChatWindow --> IBot
    ChatWindow --> ChatHistory
```

## Как читать диаграмму

1. **Три слоя.** Модель (`Message`, `ChatHistory`, `Author`) — данные; логика бота
   (`IBot`, `AbstractBot`, `SimpleBot`, обработчики) — «ум»; интерфейс
   (`ChatApplication`, `LoginWindow`, `ChatWindow`) — окна.
2. **Бот не зависит от интерфейса.** Стрелки идут от `ChatWindow` к `IBot`, но не
   наоборот. Поэтому того же бота можно подключить к онлайн-версии.
3. **Расширяемость.** Любой новый обработчик — это ещё одна стрелка `--|>` к
   `MessageHandler`. Остальные классы при этом не меняются.
