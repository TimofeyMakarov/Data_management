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
```SQL
CREATE OR REPLACE FUNCTION onInsertWithoutPriceToPurchasePresentation()
RETURNS TRIGGER
AS $$
DECLARE proguct_price INT;
BEGIN
    IF NEW.price IS NULL THEN
        SELECT price INTO proguct_price FROM product WHERE id = NEW.product_id;
        IF proguct_price IS NULL THEN
            RAISE EXCEPTION 'Товара с указанным id не существует';
        END IF;
        NEW.price = (NEW.quantity * proguct_price) * 
                    (100 - (SELECT discount FROM customer WHERE customer.id = NEW.customer_id)) / 100;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER onInsertWithoutPriceToPurchasePresentationTrigger
BEFORE INSERT ON purchase_presentation
FOR EACH ROW EXECUTE FUNCTION onInsertWithoutPriceToPurchasePresentation();
```

**Результат работы в СУБД**:
![image](https://github.com/user-attachments/assets/f8656f25-27aa-4f8e-a7ac-1faed247c4c7)

## Задание 4
**Формулировка**: *Создать представление (view), содержащее поля: № и дата презентации, фамилии агента и покупателя, скидка и стоимость покупки. Обеспечить возможность изменения предоставленной скидки. При этом должна быть пересчитана стоимость.*

**Решение на SQL**:
```SQL
CREATE OR REPLACE VIEW purchasePresentationInformation 
(number, date, agent_surname, customer_surname, discount, price) AS (
    SELECT number, date, agent.surname, customer.surname, discount, purchase_presentation.price
    FROM purchase_presentation 
        JOIN agent ON (purchase_presentation.agent_id = agent.id)
        JOIN customer ON (purchase_presentation.customer_id = customer.id)
    );

CREATE OR REPLACE FUNCTION onUpdateToPurchasePresentationInformation()
RETURNS TRIGGER
AS $$
DECLARE id_of_customer INT;
BEGIN
    IF NEW.discount IS DISTINCT FROM OLD.discount THEN
        SELECT customer_id INTO id_of_customer FROM purchase_presentation WHERE number = NEW.number;
        UPDATE customer SET discount = NEW.discount WHERE id = id_of_customer;
        UPDATE purchase_presentation SET price = price / (100 - OLD.discount) * (100 - NEW.discount)
               WHERE customer_id = id_of_customer;
    ELSE RAISE EXCEPTION 'Можно обновить только значение поля discount';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER onUpdateToPurchasePresentationInformationTrigger
INSTEAD OF UPDATE ON purchasePresentationInformation
FOR EACH ROW EXECUTE FUNCTION onUpdateToPurchasePresentationInformation();
```

**Результат работы в СУБД**:
![image](https://github.com/user-attachments/assets/9257e159-2306-421e-b9d3-8cbfb94cde95)
