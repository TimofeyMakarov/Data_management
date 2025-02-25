# Лабораторная работа №4
Вариант 14

## Задание 1
**Формулировка**: *Реализовать хранимую процедуру, возвращающую текстовую строку, содержащую информацию об агенте (идентификатор, фамилия, дата, покупатель и стоимость последней продажи). Обработать ситуацию, когда агент ничего не продал.*

**Решение на SQL**:
```SQL
CREATE OR REPLACE FUNCTION getInformationAboutAgentById (id INT)
RETURNS TEXT
AS $$
DECLARE curs CURSOR(input_id INT) FOR 
    SELECT agent.id, agent.surname, purchase_presentation.date, 
           purchase_presentation.customer_id, purchase_presentation.price 
    FROM agent JOIN purchase_presentation 
    ON (agent.id = purchase_presentation.agent_id) 
    WHERE agent.id = input_id
    ORDER BY purchase_presentation.number DESC;
    recval RECORD; 
    information TEXT = '';
BEGIN
    OPEN curs (id);
    FETCH NEXT FROM curs INTO recval;
    IF FOUND THEN
        information := 'id: '          || recval.id          || '; ' || 
                       'Фамилия: '     || recval.surname     || '; ' ||
                       'Дата: '        || recval.date        || '; ' ||
                       'Покупатель: '  || recval.customer_id || '; ' ||
                       'Цена: '        || recval.price;
    ELSE information := 'Записей о продажах агента с таким id не было найдено';
    END IF;
    CLOSE curs;
    RETURN information;
END;
$$ LANGUAGE plpgsql;
```

**Результат работы в СУБД**:
![image](https://github.com/user-attachments/assets/f48f3c24-649a-4c94-91bc-3aa3b20c1324)

## Задание 2
**Формулировка**: *Добавить таблицу, содержащую списки товаров у каждого агента. При вводе покупки проверять наличие товара у данного агента.*

**Решение на SQL**:
```SQL
-- Добавление таблицы
CREATE TABLE products_of_agent
(
	agent_id INTEGER PRIMARY KEY REFERENCES agent (id) ON UPDATE CASCADE ON DELETE CASCADE,
	list_of_products TEXT
);

INSERT INTO products_of_agent VALUES
	(1, 'Кастрюля 1л., Блюдо, Нож'),
	(2, 'Нож, Вилка, Тарелка'),
	(3, 'Кастрюля 2л.'),
	(4, 'Блюдо, Сковорода'),
	(5, 'Кастрюля 1л., Кастрюля 2л., Тарелка'),
	(6, 'Вилка, Сковорода');

-- Создание триггера для проверки наличия товаров у соответствующих агентов
CREATE OR REPLACE FUNCTION onInsertPurchasePresentation ()
RETURNS TRIGGER
AS $$
DECLARE product_list TEXT; name_of_product TEXT;
BEGIN
    PERFORM * FROM agent WHERE id = NEW.agent_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Агента с таким id не существует';
	END IF;
	SELECT product_name INTO name_of_product FROM product WHERE (id = NEW.product_id);
	IF name_of_product IS NULL THEN
        RAISE EXCEPTION 'Товара с таким id не существует';
	END IF;
    SELECT list_of_products INTO product_list
        FROM products_of_agent WHERE agent_id = NEW.agent_id;
    IF product_list NOT LIKE '%' || name_of_product || '%' THEN
        RAISE EXCEPTION 'В списке товаров агента с заданным id не указано такого товара';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER onInsertPurchasePresentationTrigger
BEFORE INSERT ON purchase_presentation
FOR EACH ROW EXECUTE FUNCTION onInsertPurchasePresentation ();
```

**Результат работы в СУБД**:
![image](https://github.com/user-attachments/assets/224febba-3645-46dd-b2a3-3c688742828e)

## Задание 3
**Формулировка**: *Реализовать триггер такой, что при вводе строки в таблице покупок, если стоимость не указана, то она вычисляется*

**Решение на SQL**:

**Результат работы в СУБД**:

## Задание 4
**Формулировка**: *Создать представление (view), содержащее поля: № и дата презентации, фамилии агента и покупателя, скидка и стоимость покупки. Обеспечить возможность изменения предоставленной скидки. При этом должна быть пересчитана стоимость.*

**Решение на SQL**:

**Результат работы в СУБД**:
