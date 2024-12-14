# Лабораторная работа №2

## Предметная область

Мною была разработана база данных для
контроля домашних заданий в рамках университета ВШЭ.
Целевая аудитория: преподаватели и студенты.

## Реализованные функции
- создание, открытие, удаление, очистка, сохранение БД
- добавление новой записи в БД (с проверкой уникальности по ключевым полям)
- удаление записи из БД по значению некоторого поля (ключевого и не ключевого (в последнем случае удаляются все записи, совпадающие по значению))
- поиск по БД по значению некоторого поля (ключевого и не ключевого (в последнем случае найти нужно все записи, совпадающие по значению)) с выводом на экран результатов поиска
- создание backup-файла БД
- восстановление БД из backup-файла

## Сложности:
- Добавление: O(1)
- Поиск: O(n)
- Удаление: O(n)