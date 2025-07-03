# Banking System Application

## Описание

Это приложение представляет собой банковскую систему, позволяющее пользователям управлять счетами, транзакциями и лимитами. Оно использует Spring Boot и Maven для разработки и управления зависимостями.

## Структура проекта

```
t1sheryo-bankingsystemapplication/
├── README.md
├── app/
│   ├── HELP.md
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   └── .mvn/
└── .gitignore
```

## Установка

### Предварительные требования

1. Убедитесь, что у вас установлены следующие инструменты:
   - [Docker](https://www.docker.com/get-started)
   - [MySQL](https://www.mysql.com/)

### Настройка базы данных

1. Проверьте установку Docker и MySQL:
   ```bash
   docker --version
   mysql --version
   ```

2. Убедитесь, что порт 3306 свободен:
   ```bash
   netstat -aon | findstr :3306
   ```

3. Создайте и запустите контейнер MySQL:
   ```bash
   docker run -d --name banking-mysql \
   -p 3306:3306 \
   -e MYSQL_ROOT_PASSWORD=root_password \
   -e MYSQL_DATABASE=banking \
   -e MYSQL_USER=user \
   -e MYSQL_PASSWORD=user_password \
   -v mysql-data:/var/lib/mysql \
   mysql:8.0
   ```

4. Проверьте состояние контейнера:
   ```bash
   docker ps
   ```

5. Подключитесь к серверу базы данных из командной строки:
   ```bash
   mysql -u user -p banking
   ```

6. Команды в командной строке базы данных:
   ```sql
   SHOW TABLES;
   EXIT;
   ```

## Запуск приложения

1. Перейдите в директорию `app` и запустите приложение:
   ```bash
   ./mvnw spring-boot:run
   ```

## Использование

Приложение предоставляет следующие функции:

- Получение и обновление курсов валют.
- Управление лимитами для аккаунтов.
- Проведение и отслеживание транзакций.

### Примеры API

#### Получение курса валют

```http
GET /bank/exchange-rates?from=USD&to=EUR
```

#### Обновление курса валют

```http
POST /bank/exchange-rates/update?from=USD&to=EUR
```

#### Создание лимита

```http
POST /bank/limits
Content-Type: application/json

{
  "accountId": 1,
  "limit": 1000.00,
  "category": "SERVICE",
  "limitCurrency": "USD"
}
```

## Тестирование

Для запуска тестов используйте команду:

```bash
./mvnw test
```

## Связь с разработчиком

Если у вас есть вопросы или предложения, вы можете связаться с разработчиком по электронной почте: [likholap.fedor@gmail.com](mailto:likholap.fedor@gmail.com).
```

