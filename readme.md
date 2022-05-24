# Система версионирования KV

## Описание
Система хранения информации различных пользователей в формате key-value с сохранением истории изменений value. 

У каждого пользователя есть имя, телефонный номер, email и ассоциированный с пользователем набор key-value в формате String-String.

Пример применения - сервис для хранения заметок пользователей (`key` - заголовок, `value` - текст заметки) с хранением информации об изменениях текста.

При работе использует сторонний сервис для валидации данных (телефонного номера).

Телефонные номера пользователей уникальны и однозначно определяют конкретного пользователя.

Существует возможность ограничить глубину хранения истории изменений (хранить последние *N* значений для каждого ключа).

## Технологии
- Spring Boot
- Postgres
- Liquibase
- SwaggerUI
- Kotest
- Testcontainers

## API
API приложения описано в [api.md](api.md)

## Запуск
Проект развёрнут на heroku: [https://tinkoff-course-kv-storage.herokuapp.com/swagger-ui/index.html#/](https://tinkoff-course-kv-storage.herokuapp.com/swagger-ui/index.html#/)
