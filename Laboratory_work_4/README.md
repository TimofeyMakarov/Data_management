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
    ELSE information := 'Данный агент ничего не продал';
    END IF;
    CLOSE curs;
    RETURN information;
END;
$$ LANGUAGE plpgsql;
```

**Результат работы в СУБД**:
![image](https://github.com/user-attachments/assets/f8b53ba7-2bca-4d81-88f8-1d9ef6fe1f99)

## Задание 2
**Формулировка**: *Добавить таблицу, содержащую списки товаров у каждого агента. При вводе покупки проверять наличие товара у данного агента.*

**Решение на SQL**:

**Результат работы в СУБД**:

## Задание 3
**Формулировка**: *Реализовать триггер такой, что при вводе строки в таблице покупок, если стоимость не указана, то она вычисляется*

**Решение на SQL**:

**Результат работы в СУБД**:

## Задание 4
**Формулировка**: *Создать представление (view), содержащее поля: № и дата презентации, фамилии агента и покупателя, скидка и стоимость покупки. Обеспечить возможность изменения предоставленной скидки. При этом должна быть пересчитана стоимость.*

**Решение на SQL**:

**Результат работы в СУБД**:
