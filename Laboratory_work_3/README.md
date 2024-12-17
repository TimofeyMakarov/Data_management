# Лабораторная работа №3
Вариант 14. Все задания лабораторной работы выполненны в PostgreSQL
## Описание схемы базы данных
Состоит из 4-х таблиц:
- Покупатель (customer)
  | id | surname | city | discount |
  |----|---------|------|----------|
- Агент (agent)
  | id | surname | city | commission_fee |
  |----|---------|------|----------------|
- Товар (product)
  | id | product_name | price | warehouse_city | maximum_number |
  |----|--------------|-------|----------------|----------------|
- Покупка\презентация (purchase_presentation)
  | number | date | customer_id | agent_id | product_id | quantity | price |
  |--------|------|-------------|----------|------------|----------|-------|
Для каждой таблицы первый столбец является *первичным ключом*. При этом таблица "Покупка" (purchase) содержит три *внешних ключа*: customer_id, agent_id, product_id.
## Задание №1
**Формулировка**: *Дана схема базы данных в виде следующих отношений. С помощью операторов SQL создать логическую структуру соответствующих таблиц для хранения в СУБД, используя известные средства поддержания целостности (NOT NULL, UNIQUE, и т.д.). Обосновать выбор типов данных и используемые средства поддержания целостности. При выборе подходящих типов данных использовать информацию о конкретных значениях полей БД (см. прил.1)*

**Решение на SQL**:
```SQL
CREATE TABLE customer
(
	id INTEGER PRIMARY KEY CHECK (id > 0),
	surname TEXT NOT NULL,
	city TEXT NOT NULL,
	discount INTEGER NOT NULL CHECK (discount BETWEEN 0 AND 100)
);

CREATE TABLE agent
(
	id INTEGER PRIMARY KEY CHECK (id > 0),
	surname TEXT NOT NULL,
	city TEXT NOT NULL,
	commission_fee INTEGER NOT NULL CHECK (commission_fee BETWEEN 0 AND 100)
);

CREATE TABLE product
(
	id INTEGER PRIMARY KEY CHECK (id > 0),
	product_name TEXT NOT NULL,
	price INTEGER NOT NULL CHECK (price > 0),
	warehouse_city TEXT NOT NULL,
	maximum_number INTEGER NOT NULL CHECK (maximum_number > 0)
);

CREATE TYPE Months AS ENUM 
('Январь', 
'Февраль',
'Март',
'Апрель',
'Май',
'Июнь',
'Июль',
'Август',
'Сентябрь',
'Октябрь',
'Ноябрь',
'Декабрь');

CREATE TABLE purchase_presentation
(
	number INTEGER PRIMARY KEY CHECK (number > 0),
	date Months NOT NULL,
	customer_id INTEGER NOT NULL,
	agent_id INTEGER NOT NULL,
	product_id INTEGER NOT NULL,
	quantity INTEGER NOT NULL CHECK (quantity > 0),
	price INTEGER NOT NULL CHECK (price > 0),
	FOREIGN KEY (customer_id) REFERENCES customer (id)
		ON UPDATE CASCADE
		ON DELETE CASCADE,
	FOREIGN KEY (agent_id) REFERENCES agent (id)
		ON UPDATE CASCADE
		ON DELETE CASCADE,
	FOREIGN KEY (product_id) REFERENCES product (id)
		ON UPDATE CASCADE
		ON DELETE CASCADE
);
```

**Результат работы в СУБД**: ![image](https://github.com/user-attachments/assets/9c36bb72-4334-4f36-8b6b-1a4bf1156b1a)

**Пояснения**:
1) Тип для полей id и number был выбран INTEGER с условием на положительность, так как это наиболее интуитивно подходящий тип для данного поля. Конечно, можно делать это поле текстового формата с тем расчётом, чтобы избежать проблемы переполнения, но строковые представления чисел весят иногда больше (например, число 2 млрд будет весить 4 байта в формате Integer, а строка "2.000.000.000" будет весить 10 байт) и придётся делать какую-то проверку на то, что данная строка именно число, а не что-то другое. Также для поля date в таблице purchase_presentation был создан тип Month.
2) Для всех полей установил ограничение NOT NULL (кроме первичных - там это требование заключено в ограничении PRIMARY KEY), потому что отстутствие той или иной информации может оказаться существенным для реальной модели (например, отстутвие указанного города для склада с товаром может повлечь за собой проблемы с доставкой).
3) Установил минимальные требования корректности данных при помощи ограничения CHECK. Например, скидка не может быть больше 100% (просто в целях предотвращения каких-то очень грубых случайных ошибок при заполнении базы данных).
4) Связал поля customer_id, agent_id, product_id с соответсвующими полями из таблиц customer, agent, product, а в качестве поведения изменений для каждого внешнего ключа было выбрано CASCADE для того, чтобы в случае изменения записей в основной таблице эти изменения отразились в точности и на записях связанных таблиц, точно также и удаление.

## Задание №2
**Формулировка**: *Ввести в ранее созданные таблицы конкретные данные (см. прил. 1). Использовать скрипт-файл из операторов INSERT или вспомогательную утилиту.*

**Решение на SQL**:
```SQL
INSERT INTO customer VALUES
	(001, 'Зуденкова', 'Москва', 0),
	(002, 'Россиев', 'С.-Петербург', 5),
	(003, 'Пушкина', 'Вологда', 0),
	(004, 'Роговцев', 'Иваново', 3),
	(005, 'Камышлейцева', 'Москва', 3);

INSERT INTO agent VALUES
	(001, 'Купцова', 'Москва', 4),
 	(002, 'Шадрин', 'Н.Новгрод', 4),
 	(003, 'Пузанкова', 'Иваново', 3),
 	(004, 'Тукмакова', 'С.-Петербург', 4),
 	(005, 'Коротин', 'Н.Новгрод', 4),
 	(006, 'Лаптев', 'Москва', 4);

INSERT INTO product VALUES
	(001, 'Кастрюля 1л.', 10000, 'Н.Новгрод', 10),
 	(002, 'Блюдо', 5100, 'Москва', 17),
 	(003, 'Нож', 2200, 'Н.Новгрод', 22),
 	(004, 'Кастрюля 2л.', 15000, 'С.-Петербург', 8),
 	(005, 'Вилка', 2000, 'Иваново', 14),
 	(006, 'Сковорода', 9800, 'С.-Петербург', 12),
 	(007, 'Тарелка', 4000, 'Москва', 18);

INSERT INTO purchase_presentation VALUES
	(00010, 'Январь', 001, 005, 007, 3, 12000),
	(00011, 'Январь', 002, 002, 006, 2, 19600),
	(00012, 'Январь', 002, 004, 004, 1, 15000),
	(00013, 'Январь', 005, 006, 003, 3, 6600),
	(00014, 'Февраль',003, 003, 004, 1, 15000),
	(00015, 'Апрель', 005, 005, 005, 12, 60000),
	(00016, 'Май', 002, 005, 001, 2, 20000),
	(00017, 'Май', 004, 003, 004, 1, 15000),
	(00018, 'Июнь', 002, 003, 001, 2, 20000),
	(00019, 'Июнь', 005, 001, 004, 1, 15000),
	(00020, 'Июнь', 005, 006, 003, 2, 4400),
	(00021, 'Июль', 002, 004, 004, 1, 15000),
	(00022, 'Июль', 003, 002, 005, 5, 10000),
	(00023, 'Июль', 003, 002, 003, 4, 8800),
	(00024, 'Июль', 003, 004, 006, 2, 19600),
	(00025, 'Июль', 004, 006, 006, 2, 19600),
	(00026, 'Июль', 003, 002, 002, 5, 25500);
```
**Результат работы в СУБД**: ![image](https://github.com/user-attachments/assets/775232e7-fa36-4e6a-8ba4-f4bed9c991e8)

## Задание №3
**Формулировка**: *Используя оператор SELECT создать запрос для вывода всех строк каждой таблицы. Проверить правильность ввода. При необходимости произвести коррекцию значений операторами INSERT, UPDATE, DELETE*

**Решение на SQL**:
```SQL
SELECT * FROM customer;
SELECT * FROM agent;
SELECT * FROM product;
SELECT * FROM purchase_presentation;
```
**Результат работы в СУБД**: ![image](https://github.com/user-attachments/assets/5e5ccc89-f0c9-4696-9fc6-98ec1ae6607d)

## Задание №4
**Формулировка**: *Создать запросы для вывода:<br>
а) всех различных фамилий покупателей и размеров их скидок;<br>
б) всех различных мест проживания агентов;<br>
в) всех названий товаров и мест их складирования.*

**Решение на SQL**:
```SQL
SELECT surname, discount FROM customer;
SELECT city FROM agent;
SELECT product_name, warehouse_city FROM product;
```
**Результат работы в СУБД**: ![image](https://github.com/user-attachments/assets/9fb226f2-9fbf-4e04-b4f4-982ebb739998)

## Задание №5
**Формулировка**: *Создав запрос получить следующую информацию:<br>
а) фамилии и размер скидки покупателей, проживающих в Москве и С.-Петербурге или тех, чьи фамилии оканчиваются на “ев”;<br>
б) номер, дату презентации, количество товара и стоимость покупки для тех записей, где стоимость составила менее 10000 руб. Отсортировать по возрастанию стоимости;<br>
в) названия товара и адрес складирования, для товаров, оставшихся в количестве не менее 10.*

**Решение на SQL**:
```SQL
SELECT surname, discount FROM customer
WHERE ((city IN ('Москва', 'Санкт-Петербург')) OR (surname LIKE '%ев'));

SELECT number, date, quantity, price FROM purchase_presentation
WHERE price < 10000
ORDER BY price;

SELECT product_name, warehouse_city FROM product
WHERE maximum_number >= 10;
```
**Результат работы в СУБД**: ![image](https://github.com/user-attachments/assets/5a36632f-9b6e-4577-a0de-138c060f7132)

## Задание №6
**Формулировка**: *На основании данных о презентациях вывести все данные в таком формате:<br>
а) номер, дата, фамилия покупателя, фамилия агента, дата. Отсортировать по первым двум полям;<br>
б) фамилия покупателя, название товара, количество.*

**Решение на SQL**:
```SQL
SELECT number, 
	date, 
	customer.surname AS customer_surname, 
	agent.surname AS agent_surname
FROM purchase_presentation 
	JOIN customer 
		ON (purchase_presentation.customer_id = customer.id)
	JOIN agent 
		ON (purchase_presentation.agent_id = agent.id)
ORDER BY number, date;

SELECT surname AS customer_surname,
	product_name,
	quantity
FROM purchase_presentation
	JOIN customer
		ON purchase_presentation.customer_id = customer.id
	JOIN product
		ON purchase_presentation.product_id = product.id;
```
**Результат работы в СУБД**: ![image](https://github.com/user-attachments/assets/4d34f447-e90a-468f-8571-8d4957355f43)

## Задание №7
**Формулировка**: *Вывести:<br>
а) фамилии агентов, которые продавали вилки или у которых что-либо покупали покупатели своего города;<br>
б) имена и адреса покупателей, покупавших предметы с ценой более 8000 руб не ранее февраля месяца. Вывести вместе с фамилиями агентов, которые продали предмет, произведя по ним сортировку;<br>
в) название и стоимость предметов, купленых Тукмаковой (Россиевым) у живущих в других городах агентов;<br>
г) название и максимальное количество предметов, которые продавались более чем одним агентом.*

**Решение на SQL**:
```SQL
SELECT surname
FROM agent
WHERE (
	EXISTS (SELECT * FROM purchase_presentation
		JOIN product 
		ON purchase_presentation.product_id = product.id
		WHERE (purchase_presentation.agent_id = agent.id AND product_name = 'Вилка'))
	OR EXISTS (SELECT * FROM purchase_presentation
		JOIN customer
		ON purchase_presentation.customer_id = customer.id
		WHERE (purchase_presentation.agent_id = agent.id AND customer.city = agent.city))
      );

SELECT number,
	customer.surname AS customer_surname,
	customer.city AS customer_city,
	agent.surname AS agent_surname
FROM purchase_presentation
	JOIN customer
		ON purchase_presentation.customer_id = customer.id
	JOIN agent
		ON purchase_presentation.agent_id = agent.id
WHERE ((price > 8000) AND (date >= 'Февраль'))
ORDER BY agent_surname;

SELECT product_name, product.price AS price
FROM purchase_presentation
	JOIN customer
		ON purchase_presentation.customer_id = customer.id
	JOIN agent
		ON purchase_presentation.agent_id = agent.id
	JOIN product
		ON purchase_presentation.product_id = product.id
WHERE ((customer.surname = 'Россиев') AND (customer.city != agent.city));

SELECT product_name FROM product
WHERE id IN 
	(
	SELECT product_id
	FROM purchase_presentation
		JOIN product
			ON purchase_presentation.product_id = product.id
	GROUP BY product_id
	HAVING COUNT(DISTINCT agent_id) > 1
	);
```
**Результат работы в СУБД**: ![image](https://github.com/user-attachments/assets/3a6fc6d9-6dcb-4aab-b434-42197d87d01b)

## Задание №8
**Формулировка**: *Создать запрос для модификации всех значений столбца со стоимостью покупки таблицы покупка/презентация, чтобы он содержал истинную сумму, оплачиваемую покупателем ( с учетом скидки). Вывести новые значения.*

**Решение на SQL**: 
```SQL
UPDATE purchase_presentation
SET price = price * (100 + (SELECT discount FROM customer WHERE customer.id = customer_id)) / 100;
```
**Результат работы в СУБД**: ![image](https://github.com/user-attachments/assets/2a0e96ef-21c4-4b3e-81c0-b9ee062a336f)

## Задание №9
**Формулировка**: 
**Решение на SQL**:
**Результат работы в СУБД**:

## Задание №10
**Формулировка**: 
**Решение на SQL**:
**Результат работы в СУБД**:

## Задание №11
**Формулировка**: 
**Решение на SQL**:
**Результат работы в СУБД**:

## Задание №12
**Формулировка**: 
**Решение на SQL**:
**Результат работы в СУБД**:

## Задание №13
**Формулировка**: 
**Решение на SQL**:
**Результат работы в СУБД**:

## Задание №14
**Формулировка**: 
**Решение на SQL**:
**Результат работы в СУБД**:

## Задание №15
**Формулировка**: 
**Решение на SQL**:
**Результат работы в СУБД**:
