import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MainWindow extends JFrame {
    private JTable table;

    public MainWindow() {
        super("HomeWorks");
        String iconPath = "icon.png";
        ImageIcon iconFile = new ImageIcon(MainWindow.class.getResource(iconPath));
        setIconImage(iconFile.getImage());
        setSize(1000, 600);
        setMinimumSize(new Dimension(800, 600));
        setResizable(false);
        setLocationRelativeTo(null);

        // Русский язык
        UIManager.put("OptionPane.yesButtonText", "Да");
        UIManager.put("OptionPane.noButtonText", "Нет");
        UIManager.put("OptionPane.cancelButtonText", "Отмена");

        // Создание кнопок на верхней панели
        JButton create = new JButton("Создать");
        JButton delete = new JButton("Удалить");
        JButton open = new JButton("Открыть");
        JButton clean = new JButton("Очистить");
        JButton save = new JButton("Сохранить");

        // Создание кнопок нижней панели
        JButton createBackup = new JButton("Создать backup-файл");
        JButton backTo = new JButton("Вернуться к backup-файлу");
        JButton search = new JButton("Поиск д/з");
        JButton insert = new JButton("Добавить д/з");
        JButton remove = new JButton("Удалить д/з");

        // Добавление слушателя ко всем кнопкам и команд
        ActionListener myButtonsListener = new ButtonsListener();
        create.setActionCommand("Создать");
        delete.setActionCommand("Удалить");
        open.setActionCommand("Открыть");
        clean.setActionCommand("Очистить");
        save.setActionCommand("Сохранить");
        createBackup.setActionCommand("Создать backup-файл");
        backTo.setActionCommand("Вернуться к backup-файлу");
        search.setActionCommand("Поиск д/з");
        insert.setActionCommand("Добавить д/з");
        remove.setActionCommand("Удалить д/з");
        create.addActionListener(myButtonsListener);
        delete.addActionListener(myButtonsListener);
        open.addActionListener(myButtonsListener);
        clean.addActionListener(myButtonsListener);
        save.addActionListener(myButtonsListener);
        createBackup.addActionListener(myButtonsListener);
        backTo.addActionListener(myButtonsListener);
        search.addActionListener(myButtonsListener);
        insert.addActionListener(myButtonsListener);
        remove.addActionListener(myButtonsListener);

        // Верхняя панель с кнопками
        JPanel buttonsTopPanel = new JPanel();
        buttonsTopPanel.add(create);
        buttonsTopPanel.add(delete);
        buttonsTopPanel.add(open);
        buttonsTopPanel.add(clean);
        buttonsTopPanel.add(save);
        getContentPane().add(BorderLayout.NORTH, buttonsTopPanel);

        // Нижняя панель с кнопками
        JPanel buttonsDownPanel = new JPanel();
        buttonsDownPanel.add(createBackup);
        buttonsDownPanel.add(backTo);
        buttonsDownPanel.add(search);
        buttonsDownPanel.add(insert);
        buttonsDownPanel.add(remove);
        this.getContentPane().add(BorderLayout.SOUTH, buttonsDownPanel);

        // Панель с таблицей и сама таблица
        JPanel tablePanel = new JPanel();
        table = new JTable();
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Название предмета");
        model.addColumn("Срок сдачи");
        model.addColumn("Номера групп");
        model.addColumn("Описание");
        table.setModel(model);
        JScrollPane scrollPane = new JScrollPane(table);
        tablePanel.add(scrollPane);
        getContentPane().add(BorderLayout.CENTER, tablePanel);

        // Шрифт для кнопок
        Font buttonFont = new Font("Arial", Font.BOLD, 18);
        create.setFont(buttonFont);
        delete.setFont(buttonFont);
        open.setFont(buttonFont);
        clean.setFont(buttonFont);
        save.setFont(buttonFont);
        createBackup.setFont(buttonFont);
        backTo.setFont(buttonFont);
        search.setFont(buttonFont);
        insert.setFont(buttonFont);
        remove.setFont(buttonFont);

        // Шрифт для таблицы
        table.getTableHeader().setFont(new Font("Arial", Font.ITALIC, 16));
        table.setFont(new Font("Arial", Font.PLAIN, 14));

        // Нажатие на крестик
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (Main.isActive() && Main.isModify) {
                    int answer = JOptionPane.showConfirmDialog(MainWindow.this,
                            "Вы хотите сохранить текущую таблицу?",
                            "Закрытие таблицы",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (answer == JOptionPane.YES_OPTION) {
                        Main.closeTable(true);
                        System.exit(0);
                    }
                    if (answer == JOptionPane.NO_OPTION) {
                        Main.closeTable(false);
                        System.exit(0);
                    }
                    if (answer == JOptionPane.CANCEL_OPTION) {
                        return;
                    }
                }
                if (Main.isActive() && !Main.isModify){
                    Main.closeTable(false);
                }
                System.exit(0);
            }
        });

        setVisible(true);
    }

    private void addRow(Object[] row) {
        DefaultTableModel model = (DefaultTableModel)table.getModel();
        model.addRow(row);
    }

    private void cleanAllCells() {
        DefaultTableModel model = (DefaultTableModel)table.getModel();
        model.setRowCount(0);
    }

    private void fullUpdate() {
        cleanAllCells();

        try (FileReader fr = new FileReader(Main.getNameCopy());
             BufferedReader br = new BufferedReader(fr)){
            br.readLine();
            String theLine;
            while((theLine = br.readLine()) != null) {
                addRow(objectsFromUserString(theLine));
            }
        } catch (IOException e) {
            errorMessage(MainWindow.this, "Не удалось отобразить таблицу");
            Main.closeTable(false);
        }
    }

    private void cleanRow(int row) {
        DefaultTableModel model = (DefaultTableModel)table.getModel();
        model.removeRow(row);
    }

    private Object[] objectsFromUserString(String theLine) {
        StringTokenizer st = new StringTokenizer(theLine, ";");
        String token1 = st.nextToken().trim();
        String token2 = st.nextToken().trim();
        String token3 = st.nextToken().trim();
        String token4 = st.nextToken().trim();
        String token5 = st.nextToken().trim();
        StringTokenizer date = new StringTokenizer(token3, ".");
        int day = Integer.parseInt(date.nextToken());
        int month = Integer.parseInt(date.nextToken());
        int year = Integer.parseInt(date.nextToken());
        LocalDate dueDay = LocalDate.of(year, month, day);
        StringBuilder token4Builder = new StringBuilder(token4);
        token4Builder.deleteCharAt(token4.length() - 1);
        token4Builder.deleteCharAt(0);
        StringTokenizer groupNumbers = new StringTokenizer(token4Builder.toString(), ",");
        String[] groups = new String[groupNumbers.countTokens()];

        for(int index = 0; groupNumbers.hasMoreTokens(); ++index) {
            StringBuilder group = new StringBuilder(groupNumbers.nextToken().trim());
            group.deleteCharAt(group.length() - 1);
            group.deleteCharAt(0);
            groups[index] = group.toString();
        }

        return new Object[]{Integer.parseInt(token1), token2, dueDay, Arrays.toString(groups), token5};
    }

    private Object[] objectsFromTableString(String row){
        StringTokenizer st = new StringTokenizer(row, ";");
        String token1 = st.nextToken().trim();
        String token2 = st.nextToken().trim();
        String token3 = st.nextToken().trim();
        String token4 = st.nextToken().trim();
        String token5 = st.nextToken().trim();
        StringTokenizer date = new StringTokenizer(token3, "-");
        int year = Integer.parseInt(date.nextToken());
        int month = Integer.parseInt(date.nextToken());
        int day = Integer.parseInt(date.nextToken());
        LocalDate dueDay = LocalDate.of(year, month, day);
        StringBuilder token4Builder = new StringBuilder(token4);
        token4Builder.deleteCharAt(token4.length() - 1);
        token4Builder.deleteCharAt(0);
        StringTokenizer groupNumbers = new StringTokenizer(token4Builder.toString(), ",");
        String[] groups = new String[groupNumbers.countTokens()];

        for(int index = 0; groupNumbers.hasMoreTokens(); ++index) {
            String group = groupNumbers.nextToken().trim();
            groups[index] = group;
        }

        return new Object[]{Integer.parseInt(token1), token2, dueDay, Arrays.toString(groups), token5};
    }

    private void errorMessage(Frame owner, String message) {
        JOptionPane.showMessageDialog(owner, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private void successMessage(Frame owner, String message) {
        JOptionPane.showMessageDialog(owner, message, "Успешное выполнение", JOptionPane.PLAIN_MESSAGE);
    }

    private JTable getPartOfTable(List<Integer> indexes){
        JTable partOfTable = new JTable();
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Название предмета");
        model.addColumn("Срок сдачи");
        model.addColumn("Номера групп");
        model.addColumn("Описание");
        partOfTable.setModel(model);
        for (Integer index : indexes){
            StringBuilder sb = new StringBuilder();
            for (int colomn = 0; colomn < 5; colomn++){
                sb.append(table.getModel().getValueAt(index, colomn));
                if (colomn != 4) sb.append(';');
            }
            model.addRow(objectsFromTableString(sb.toString()));
        }
        return partOfTable;
    }

    private class ButtonsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("Создать")) {
                if (Main.isActive() && Main.isModify) {
                    int answer = JOptionPane.showConfirmDialog(MainWindow.this,
                            "Вы хотите сохранить текущую таблицу?",
                            "Закрытие таблицы",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (answer == JOptionPane.YES_OPTION) {
                        Main.closeTable(true);
                    }

                    if (answer == JOptionPane.NO_OPTION) {
                        Main.closeTable(false);
                    }

                    if (answer == JOptionPane.CANCEL_OPTION) {
                        return;
                    }
                }

                // Окошко сохранения
                JDialog dialog = new JDialog(MainWindow.this, "Создание таблицы", true);
                dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                dialog.setSize(400, 200);
                dialog.setLocationRelativeTo(null);
                dialog.setResizable(false);
                dialog.getContentPane().setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));

                // Панель с именем
                JPanel namePanel = new JPanel();
                JTextField name = new JTextField(20);
                JLabel nameLabel = new JLabel("Имя файла: ");
                namePanel.add(nameLabel);
                namePanel.add(name);

                // Панель с путем
                JPanel pathPanel = new JPanel();
                JTextField path = new JTextField(20);
                JLabel pathLabel = new JLabel("Путь до папки: ");
                pathPanel.add(pathLabel);
                pathPanel.add(path);

                // Панель с вводом
                JPanel inputPanel = new JPanel();
                BoxLayout boxlayout = new BoxLayout(inputPanel, BoxLayout.Y_AXIS);
                inputPanel.setLayout(boxlayout);
                inputPanel.add(namePanel);
                inputPanel.add(pathPanel);
                dialog.getContentPane().add(BorderLayout.NORTH, inputPanel);

                /*
                dialog.getContentPane().add(namePanel);
                dialog.getContentPane().add(pathPanel);
                dialog.getContentPane().add(Box.createVerticalGlue());
                */

                /*
                JPanel inputPanel = new JPanel();
                BoxLayout boxlayout = new BoxLayout(inputPanel, BoxLayout.Y_AXIS);
                inputPanel.setLayout(boxlayout);
                inputPanel.add(nameLabel);
                inputPanel.add(subject);
                inputPanel.add(Box.createVerticalGlue());
                inputPanel.add(pathLabel);
                inputPanel.add(dateDay);
                dialog.getContentPane().add("North", inputPanel);
                 */

                // Панель с кнопками
                JButton create = new JButton("Создать");
                JButton cancel = new JButton("Отмена");
                JPanel buttonsPanel = new JPanel();
                buttonsPanel.add(create);
                buttonsPanel.add(cancel);
                dialog.getContentPane().add(BorderLayout.SOUTH, buttonsPanel);

                // Слушатели кнопок
                create.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String inputName = name.getText();
                        String inputPath = path.getText();
                        String response = Main.createTable(inputName, inputPath);
                        if (response.equals("Таблица успешно создана")) {
                            cleanAllCells();
                            successMessage(MainWindow.this, response);
                            dialog.dispose();
                        } else {
                            errorMessage(MainWindow.this, response);
                        }

                    }
                });
                cancel.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dialog.dispose();
                    }
                });

                dialog.setVisible(true);
            }
            if (command.equals("Удалить")) {
                String response = Main.deleteTable();
                if (response.equals("Таблица успешно удалена")) {
                    MainWindow.this.cleanAllCells();
                    MainWindow.this.successMessage(MainWindow.this, response);
                } else {
                    MainWindow.this.errorMessage(MainWindow.this, response);
                }
            }
            if (command.equals("Открыть")) {
                if (Main.isActive() && Main.isModify) {
                    int answer = JOptionPane.showConfirmDialog(MainWindow.this,
                            "Вы хотите сохранить текущую таблицу?",
                            "Закрытие таблицы",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (answer == JOptionPane.YES_OPTION) {
                        Main.closeTable(true);
                    }

                    if (answer == JOptionPane.NO_OPTION) {
                        Main.closeTable(false);
                    }

                    if (answer == JOptionPane.CANCEL_OPTION) {
                        return;
                    }
                }

                String inputPath = JOptionPane.showInputDialog(MainWindow.this,
                        "Введите полный путь до файла");
                if (inputPath == null) {
                    return;
                }
                String response = Main.openTable(inputPath);
                if (response.equals("Таблица успешно открыта")) {
                    cleanAllCells();
                    try (FileReader fr = new FileReader(Main.getNameCopy());
                        BufferedReader br = new BufferedReader(fr)){
                        br.readLine();
                        String theLine;
                        while((theLine = br.readLine()) != null) {
                            addRow(objectsFromUserString(theLine));
                        }
                    } catch (IOException obj) {
                        errorMessage(MainWindow.this, "Не удалось отобразить таблицу");
                        Main.closeTable(false);
                        return;
                    }

                    successMessage(MainWindow.this, response);
                }
                else {
                    errorMessage(MainWindow.this, response);
                }
            }
            if (command.equals("Очистить")) {
                String response = Main.cleanTable();
                if (response.equals("Таблицы успешно очищена")) {
                    cleanAllCells();
                    successMessage(MainWindow.this, response);
                } else {
                    errorMessage(MainWindow.this, response);
                }
            }
            if (command.equals("Сохранить")) {
                String response = Main.saveTable();
                if (response.equals("Таблица успешно сохранена")) {
                    MainWindow.this.successMessage(MainWindow.this, response);
                } else {
                    MainWindow.this.errorMessage(MainWindow.this, response);
                }
            }
            if (command.equals("Создать backup-файл")) {
                String response = Main.createBackup();
                if (response.equals("Вы не можете создать backup-файл, так как никакая таблица не открыта.\nПожалуйста, создайте новую таблицу или откройте существующую")){
                    errorMessage(MainWindow.this, response);
                    return;
                }
                if (response.equals("Не удалось создать backup-файл")) {
                    errorMessage(MainWindow.this, response);
                } else {
                    successMessage(MainWindow.this, "Backup-файл успешно создан");
                }
            }
            if (command.equals("Вернуться к backup-файлу")) {
                String response = Main.backTo();
                if (response.equals("Таблица успешно возвращена в сохранённое состояние")) {
                    fullUpdate();
                    successMessage(MainWindow.this, response);
                } else {
                    errorMessage(MainWindow.this, response);
                }
            }
            if (command.equals("Поиск д/з")) {
                if (!Main.isActive()) {
                    String message = "Невозможно найти д/з, так как никакая таблица не открыта.\nПожалуйста, создайте новую таблицу или откройте существующую";
                    errorMessage(MainWindow.this, message);
                    return;
                }

                JDialog dialog = new JDialog(MainWindow.this, "Поиск д/з", true);
                dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                dialog.setSize(300, 250);
                dialog.setLocationRelativeTo(null);
                dialog.setResizable(false);

                // Поля, по которым можно делать поиск
                String [] fields = {"ID", "Название", "Срок сдачи", "Номера групп", "Описание"};

                // Поля для ввода данных
                JComboBox<String> field = new JComboBox<>(fields);
                JTextArea value = new JTextArea(7, 21);
                value.setLineWrap(true);
                value.setWrapStyleWord(true);

                // Подписи к полям для ввода данных
                JLabel fieldLabel = new JLabel("Столбец: ");
                JLabel valueLabel = new JLabel("Значение: ");

                // Панель ввода поля, по значению которого происходит поиск
                JPanel fieldPanel = new JPanel();
                fieldPanel.add(fieldLabel);
                fieldPanel.add(field);

                // Панель ввода удаляемого значения
                JPanel valueLabelPanel = new JPanel();
                JPanel valueInputPanel = new JPanel();
                valueLabelPanel.add(valueLabel);
                valueInputPanel.add(value);

                // Общая панель ввода
                JPanel inputPanel = new JPanel();
                inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
                inputPanel.add(fieldPanel);
                inputPanel.add(valueLabelPanel);
                inputPanel.add(valueInputPanel);

                // Панель с кнопками
                JPanel buttonPanel = new JPanel();
                JButton toSearch = new JButton("Искать");
                JButton cancel = new JButton("Отмена");
                buttonPanel.add(toSearch);
                buttonPanel.add(cancel);

                // Размещение на диалоговом окне
                dialog.add(BorderLayout.NORTH, inputPanel);
                dialog.add(BorderLayout.SOUTH, buttonPanel);

                // Слушатели кнопок
                toSearch.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String searchField = (String)field.getSelectedItem();
                        String searchValue = value.getText();
                        if (searchField == null){
                            return;
                        }
                        if (searchValue.isEmpty()){
                            errorMessage(MainWindow.this, "Введите какое-нибудь значение");
                            return;
                        }
                        if (searchField.equals("ID")){
                            int searchID;
                            try{
                                searchID = Integer.parseInt(searchValue);
                            } catch (NumberFormatException ex) {
                                successMessage(MainWindow.this, "Не было найдено ни одной строки в таблице");
                                return;
                            }
                            Integer response = Main.searchByKey(searchID);
                            List<Integer> indexes = new ArrayList<>();
                            indexes.add(response);
                            JTable searchResultTable = getPartOfTable(indexes);

                            JDialog searchResult = new JDialog(MainWindow.this, "Результат поиска д/з", false);
                            searchResult.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                            searchResult.setSize(600, 400);
                            searchResult.setLocationRelativeTo(null);
                            searchResult.setResizable(true);

                            searchResult.add(new JScrollPane(searchResultTable));
                            searchResult.setVisible(true);
                            dialog.dispose();
                        }
                        if (searchField.equals("Название")){
                            List<Integer> indexes = Main.searchByNotKey(1, searchValue);
                            if (indexes.isEmpty()){
                                successMessage(MainWindow.this, "Не было найдено ни одной строки в таблице");
                                dialog.dispose();
                                return;
                            }
                            JTable searchResultTable = getPartOfTable(indexes);

                            JDialog searchResult = new JDialog(MainWindow.this, "Результат поиска д/з", false);
                            searchResult.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                            searchResult.setSize(600, 400);
                            searchResult.setLocationRelativeTo(null);
                            searchResult.setResizable(true);

                            searchResult.add(new JScrollPane(searchResultTable));
                            searchResult.setVisible(true);
                            dialog.dispose();
                        }
                        if (searchField.equals("Срок сдачи")){
                            List<Integer> indexes = Main.searchByNotKey(2, searchValue);
                            if (indexes.isEmpty()){
                                successMessage(MainWindow.this, "Не было найдено ни одной строки в таблице");
                                dialog.dispose();
                                return;
                            }
                            JTable searchResultTable = getPartOfTable(indexes);

                            JDialog searchResult = new JDialog(MainWindow.this, "Результат поиска д/з", false);
                            searchResult.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                            searchResult.setSize(600, 400);
                            searchResult.setLocationRelativeTo(null);
                            searchResult.setResizable(true);

                            searchResult.add(new JScrollPane(searchResultTable));
                            searchResult.setVisible(true);
                            dialog.dispose();
                        }
                        if (searchField.equals("Номера групп")){
                            List<Integer> indexes = Main.searchByNotKey(3, searchValue.replace(" ", ""));
                            if (indexes.isEmpty()){
                                successMessage(MainWindow.this, "Не было найдено ни одной строки в таблице");
                                dialog.dispose();
                                return;
                            }
                            JTable searchResultTable = getPartOfTable(indexes);

                            JDialog searchResult = new JDialog(MainWindow.this, "Результат поиска д/з", false);
                            searchResult.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                            searchResult.setSize(600, 400);
                            searchResult.setLocationRelativeTo(null);
                            searchResult.setResizable(true);

                            searchResult.add(new JScrollPane(searchResultTable));
                            searchResult.setVisible(true);
                            dialog.dispose();
                        }
                        if (searchField.equals("Описание")){
                            List<Integer> indexes = Main.searchByNotKey(4, searchValue);
                            if (indexes.isEmpty()){
                                successMessage(MainWindow.this, "Не было найдено ни одной строки в таблице");
                                dialog.dispose();
                                return;
                            }
                            JTable searchResultTable = getPartOfTable(indexes);

                            JDialog searchResult = new JDialog(MainWindow.this, "Результат поиска д/з", false);
                            searchResult.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                            searchResult.setSize(600, 400);
                            searchResult.setLocationRelativeTo(null);
                            searchResult.setResizable(true);

                            searchResult.add(new JScrollPane(searchResultTable));
                            searchResult.setVisible(true);
                            dialog.dispose();
                        }
                    }
                });
                cancel.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dialog.dispose();
                    }
                });

                dialog.setVisible(true);
            }
            if (command.equals("Добавить д/з")) {
                if (!Main.isActive()) {
                    String message = "Невозможно добавить новое д/з, так как никакая таблица не открыта.\nПожалуйста, создайте новую таблицу или откройте существующую";
                    errorMessage(MainWindow.this, message);
                    return;
                }

                JDialog dialog = new JDialog(MainWindow.this, "Добавление д/з", true);
                dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                dialog.setSize(500, 400);
                dialog.setLocationRelativeTo(null);
                dialog.setResizable(false);

                JPanel inputPanel = new JPanel();
                inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

                // Поля для ввода данных
                JTextField subject = new JTextField(10);
                JTextField dateDay = new JTextField(5);
                JTextField dateMonth = new JTextField(5);
                JTextField dateYear = new JTextField(5);
                JTextField groups = new JTextField(15);
                JTextArea description = new JTextArea(10, 40);
                description.setText("Описание");
                description.setLineWrap(true);
                description.setWrapStyleWord(true);

                // Подписи к полям для ввода данных
                JLabel subjectLabel = new JLabel("Название предмета: ");
                JLabel dateDayLabel = new JLabel("День: ");
                JLabel dateMonthLabel = new JLabel("Месяц: ");
                JLabel dateYearLabel = new JLabel("Год: ");
                JLabel numbersGroupsLabel = new JLabel("Номера групп: ");

                // Панель с вводом названия предмета
                JPanel subjectPanel = new JPanel();
                subjectPanel.add(subjectLabel);
                subjectPanel.add(subject);

                // Панель с вводом дня сдачи
                JPanel day = new JPanel();
                day.add(dateDayLabel);
                day.add(dateDay);

                // Панель с вводом месяца сдачи
                JPanel month = new JPanel();
                month.add(dateMonthLabel);
                month.add(dateMonth);

                // Панель с вводом года сдачи
                JPanel year = new JPanel();
                year.add(dateYearLabel);
                year.add(dateYear);

                // Панель с полным вводом срока сдачи
                JPanel datePanel = new JPanel();
                datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.Y_AXIS));
                datePanel.add(day);
                datePanel.add(month);
                datePanel.add(year);

                // Панель с вводом номеров групп
                JPanel numbersGroupsPanel = new JPanel();
                numbersGroupsPanel.add(numbersGroupsLabel);
                numbersGroupsPanel.add(groups);

                // Панель с описанием д/з
                JPanel descriptionPanel = new JPanel();
                JScrollPane scrollPane = new JScrollPane(description);
                descriptionPanel.add(scrollPane);

                // Панель с кнопками
                JPanel buttonPanel = new JPanel();
                JButton toAdd = new JButton("Добавить");
                JButton cancel = new JButton("Отмена");
                buttonPanel.add(toAdd);
                buttonPanel.add(cancel);

                // Добавление панелей в общую панель ввода
                inputPanel.add(subjectPanel);
                inputPanel.add(datePanel);
                inputPanel.add(numbersGroupsPanel);
                inputPanel.add(descriptionPanel);

                // Размещение на диалоговом окне
                dialog.add(BorderLayout.NORTH, inputPanel);
                dialog.add(BorderLayout.SOUTH, buttonPanel);

                // Слушатели кнопок
                toAdd.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (subject.getText().isEmpty()) {
                            errorMessage(MainWindow.this, "Введите название предмета");
                            return;
                        }
                        if (!dateDay.getText().isEmpty() && !dateMonth.getText().isEmpty() && !dateYear.getText().isEmpty()) {
                            try {
                                int day = Integer.parseInt(dateDay.getText());
                                int month = Integer.parseInt(dateMonth.getText());
                                int var4 = Integer.parseInt(dateYear.getText());
                            } catch (NumberFormatException obj) {
                                errorMessage(MainWindow.this, "Введите дату в виде цифр");
                                return;
                            }
                        } else {
                            MainWindow.this.errorMessage(MainWindow.this, "Введите срок сдачи полностью");
                            return;
                        }
                        if (groups.getText().isEmpty()) {
                            errorMessage(MainWindow.this, "Введите группы, которым выдано д/з");
                            return;
                        }
                        if (description.getText().isEmpty()) {
                            errorMessage(MainWindow.this, "Добавьте описание к заданию");
                            return;
                        }

                        String token2 = subject.getText();
                        String token3 = dateDay.getText() + "." + dateMonth.getText() + "." + dateYear.getText();
                        StringBuilder token4 = new StringBuilder();
                        StringTokenizer groupsTokenizer = new StringTokenizer(groups.getText(), ",");
                        if (groups.getText().length() - groups.getText().replace(",", "").length() != groupsTokenizer.countTokens() - 1) {
                            MainWindow.this.errorMessage(MainWindow.this, "Удалите лишние запятые");
                            return;
                        }
                        token4.append("[");

                        String row;
                        while(groupsTokenizer.hasMoreTokens()) {
                            row = groupsTokenizer.nextToken().trim();
                            if (!row.matches("^\\d{2}[a-яА-Я]*-\\d*$")) {
                                MainWindow.this.errorMessage(MainWindow.this, "Группы введены в неверном формате");
                                return;
                            }

                            token4.append("\"");
                            token4.append(row);
                            token4.append("\"");
                            if (groupsTokenizer.hasMoreTokens()) {
                                token4.append(", ");
                            }
                        }

                        token4.append("]");
                        row = Main.getMaxID() + 1 + ";" + token2 + ";" + token3 + ";" + token4 + ";" + description.getText();
                        if (!Main.isCorrectLine(row)) {
                            errorMessage(MainWindow.this, "Некорректный формат. Д/з не было добавлено");
                            return;
                        }

                        String response = Main.insert(row);
                        if (response.equals("Новое д/з успешно добавлено")) {
                            addRow(objectsFromUserString(row));
                            successMessage(MainWindow.this, response);
                            dialog.dispose();
                        } else {
                            errorMessage(MainWindow.this, response);
                        }
                    }
                });
                cancel.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dialog.dispose();
                    }
                });

                dialog.setVisible(true);
            }
            if (command.equals("Удалить д/з")) {
                if (!Main.isActive()) {
                    String message = "Невозможно удалить д/з, так как никакая таблица не открыта.\nПожалуйста, создайте новую таблицу или откройте существующую";
                    errorMessage(MainWindow.this, message);
                    return;
                }

                JDialog dialog = new JDialog(MainWindow.this, "Удаление д/з", true);
                dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                dialog.setSize(300, 250);
                dialog.setLocationRelativeTo(null);
                dialog.setResizable(false);

                // Поля, по которым можно делать удаление
                String [] fields = {"ID", "Название", "Срок сдачи", "Номера групп", "Описание"};

                // Поля для ввода данных
                JComboBox<String> field = new JComboBox<>(fields);
                JTextArea value = new JTextArea(7, 21);
                value.setLineWrap(true);
                value.setWrapStyleWord(true);

                // Подписи к полям для ввода данных
                JLabel fieldLabel = new JLabel("Столбец: ");
                JLabel valueLabel = new JLabel("Значение: ");

                // Панель ввода поля, по значению которого происходит удаление
                JPanel fieldPanel = new JPanel();
                fieldPanel.add(fieldLabel);
                fieldPanel.add(field);

                // Панель ввода удаляемого значения
                JPanel valueLabelPanel = new JPanel();
                JPanel valueInputPanel = new JPanel();
                valueLabelPanel.add(valueLabel);
                valueInputPanel.add(value);

                // Общая панель ввода
                JPanel inputPanel = new JPanel();
                inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
                inputPanel.add(fieldPanel);
                inputPanel.add(valueLabelPanel);
                inputPanel.add(valueInputPanel);

                // Панель с кнопками
                JPanel buttonPanel = new JPanel();
                JButton toDelete = new JButton("Удалить");
                JButton cancel = new JButton("Отмена");
                buttonPanel.add(toDelete);
                buttonPanel.add(cancel);

                // Размещение на диалоговом окне
                dialog.add(BorderLayout.NORTH, inputPanel);
                dialog.add(BorderLayout.SOUTH, buttonPanel);

                // Слушатели кнопок
                toDelete.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String deleteField = (String)field.getSelectedItem();
                        String deleteValue = value.getText();
                        if (deleteField == null){
                            return;
                        }
                        if (deleteValue.isEmpty()){
                            errorMessage(MainWindow.this, "Введите какое-нибудь значение");
                            return;
                        }
                        if (deleteField.equals("ID")){
                            int searchID;
                            try{
                                searchID = Integer.parseInt(deleteValue);
                            } catch (NumberFormatException ex) {
                                successMessage(MainWindow.this, "Не было удалено ни одной строки в таблице");
                                dialog.dispose();
                                return;
                            }
                            int response = Main.searchByKey(searchID);
                            if (response == -1){
                                successMessage(MainWindow.this, "Не было удалено ни одной строки в таблице");
                                dialog.dispose();
                                return;
                            }
                            Main.deleteRow(response);
                            cleanRow(response);
                            successMessage(MainWindow.this, "Была удалена одна строка");
                            dialog.dispose();
                            return;
                        }
                        if (deleteField.equals("Название")){
                            List<Integer> indexes = Main.searchByNotKey(1, deleteValue);
                            if (indexes.isEmpty()){
                                successMessage(MainWindow.this, "Не было удалено ни одной строки в таблице");
                                dialog.dispose();
                                return;
                            }
                            int count = 0;
                            for (Integer index : indexes){
                                Main.deleteRow(index - count);
                                cleanRow(index - count);
                                count += 1;
                            }
                            successMessage(MainWindow.this, "Успешно удалено");
                            return;
                        }
                        if (deleteField.equals("Срок сдачи")){
                            List<Integer> indexes = Main.searchByNotKey(2, deleteValue);
                            if (indexes.isEmpty()){
                                successMessage(MainWindow.this, "Не было удалено ни одной строки в таблице");
                                dialog.dispose();
                                return;
                            }
                            int count = 0;
                            for (Integer index : indexes){
                                Main.deleteRow(index - count);
                                cleanRow(index - count);
                                count += 1;
                            }
                            successMessage(MainWindow.this, "Успешно удалено");
                            return;
                        }
                        if (deleteField.equals("Номера групп")){
                            List<Integer> indexes = Main.searchByNotKey(3, deleteValue.replace(" ", ""));
                            if (indexes.isEmpty()){
                                successMessage(MainWindow.this, "Не было удалено ни одной строки в таблице");
                                dialog.dispose();
                                return;
                            }
                            int count = 0;
                            for (Integer index : indexes){
                                Main.deleteRow(index - count);
                                cleanRow(index - count);
                                count += 1;
                            }
                            successMessage(MainWindow.this, "Успешно удалено");
                            return;
                        }
                        if (deleteField.equals("Описание")){
                            List<Integer> indexes = Main.searchByNotKey(4, deleteValue);
                            if (indexes.isEmpty()){
                                successMessage(MainWindow.this, "Не было удалено ни одной строки в таблице");
                                dialog.dispose();
                                return;
                            }
                            int count = 0;
                            for (Integer index : indexes){
                                Main.deleteRow(index - count);
                                cleanRow(index - count);
                                count += 1;
                            }
                            successMessage(MainWindow.this, "Успешно удалено");
                            return;
                        }
                    }
                });
                cancel.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dialog.dispose();
                    }
                });

                dialog.setVisible(true);
            }
        }
    }
}
