import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Main{
    private static String path;
    private static String nameCopy;
    private static String backup;
    private static int maxID = -1;
    private static int tableLength = 0;
    public static boolean isModify = false;

    public static String createTable(String name, String directory){
        File dir = new File(directory);
        if (!(dir.exists() && dir.isDirectory())){
            return "Указанной папки не существует, таблица не была создана";
        }
        if (directory.charAt(directory.length() - 1) != '\\'){
            directory = directory + '\\';
        }
        File table;
        if (isTxt(name)){
            table = new File(directory + name);
        }
        else{
            table = new File(directory + name + ".txt");
        }

        try {
            if (table.createNewFile()){
                path = table.getAbsolutePath();
            }
            else {
                return "Файл с таким именем в указанной папке уже существует.\n" +
                        "Если это таблица - вы можете открыть её, нажав на кнопку \"Открыть\".";
            }
        } catch (IOException e) {
            return "Ошибка при создании таблицы";
        }

        try(FileWriter fw = new FileWriter(table);
            BufferedWriter bw = new BufferedWriter(fw)){
            fw.write("ID;Название предмета;Срок сдачи;Номера групп;Описание");
        } catch (IOException e){
            path = null;
            return "Ошибка при создании таблицы";
        }

        try{
            nameCopy = createTableCopy();
            return "Таблица успешно создана";
        } catch (IOException e) {
            path = null;
            nameCopy = null;
            return "Ошибка при создании таблицы";
        }
    }

    public static String deleteTable(){
        if (path == null){
            return "Таблицы не может быть удалена, так как никакая таблица не открыта.\n" +
                    "Пожалуйста, создайте новую таблицу или откройте существующую";
        }
        File table = new File(path);
        File tableCopy = new File(nameCopy);
        String copyPath = path;
        if (table.delete()){
            path = null;
        }
        else {
            return "Ошибка при попытке удаления таблицы. Возможно, файл с таблицей открыт в другой программе";
        }
        if (tableCopy.delete()){
            nameCopy = null;
            maxID = -1;
            tableLength = 0;
            isModify = false;
            return "Таблица успешно удалена";
        }
        else {
            path = copyPath;
            return "Ошибка при попытке удаления таблицы. Повторите попытку позже";
        }
    }

    public static String openTable(String inputPath){
        File table = new File(inputPath);
        File tableCopy;
        int statusCode = checkTable(inputPath);
        if (statusCode == -5){
            return "Возникла непредвиденная ошибка при попытке проверить файл на корректность.\n" +
                   "Пожалуйста, повторите попытку позже";
        }
        if (statusCode == -4){
            return "Путь введён некорректно. Таблица не была открыта";
        }
        if (statusCode == -3){
            return "Файл с таблицей должен быть с расширением .txt";
        }
        if (statusCode == -2){
            return "Файл с таблицей не может быть прочитан. Пожалуйста, проверьте настройки доступа к файлу";
        }
        if (statusCode == -1){
            return "Таблица не может быть использована для построения базы данных\n" +
                   "Ошибка в формате: неверные заголовки столбцов";
        }
        if (statusCode > 0){
            return "Таблица не может быть использована для построения базы данных\n" +
                   "Ошибка в формате: некорректная " + statusCode + "строка";
        }

        try{
            nameCopy = createTableCopy();
            tableCopy = new File(nameCopy);
        } catch (IOException e) {
            return "Ошибка при попытке открыть таблицу";
        }

        try(FileReader fr = new FileReader(table);
            BufferedReader br = new BufferedReader(fr);
            FileWriter frc = new FileWriter(tableCopy);
            BufferedWriter brc = new BufferedWriter(frc)){
            String theLine;
            while ((theLine = br.readLine()) != null){
                brc.write(theLine);
                brc.write('\n');
                tableLength += 1;
            }
        }
        catch (IOException e){
            nameCopy = null;
            return "Ошибка при попытке открыть таблицу";
        }
        path = table.getPath();
        return "Таблица успешно открыта";
    }

    public static String cleanTable(){
        if (path == null){
            return "Таблица не может быть очищена, так как никакая таблица не открыта.\n" +
                    "Пожалуйста, создайте новую таблицу или откройте существующую";
        }
        try(FileWriter fw = new FileWriter(nameCopy)){
            tableLength = 0;
            isModify = true;
            return "Таблицы успешно очищена";
        } catch (FileNotFoundException e){
            return "Ошибка при попытке очистить таблицу. Файл с текущей таблицей не был найден";
        } catch (IOException e){
            return "Непредвиденный сбой при очистке таблицы";
        }
    }

    public static String saveTable(){
        if (path == null){
            return "Таблицы не может быть сохранена, так как никакая таблица не открыта.\n" +
                    "Пожалуйста, создайте новую таблицу или откройте существующую.";
        }
        try(FileReader frc = new FileReader(new File(nameCopy));
            BufferedReader brc = new BufferedReader(frc);
            FileWriter fr = new FileWriter(new File(path));
            BufferedWriter br = new BufferedWriter(fr)){
            String theLine;
            while ((theLine = brc.readLine()) != null){
                br.write(theLine);
                br.write('\n');
            }
            isModify = false;
        } catch (IOException e) {
            return "Ошибка при попытке сохранить таблицу. Повторите попытку ещё раз";
        }
        return "Таблица успешно сохранена";
    }

    public static String createBackup(){
        if (path == null){
            return "Вы не можете создать backup-файл, так как никакая таблица не открыта.\n" +
                    "Пожалуйста, создайте новую таблицу или откройте существующую";
        }
        File backupFile;
        try {
            int count = 0;
            while (true) {
                if (count == 0) {
                    backupFile = new File("backup.txt");
                } else {
                    String attempt = "backup (" + count + ").txt";
                    backupFile = new File(attempt);
                }
                if (backupFile.createNewFile()) {
                    break;
                }
                count += 1;
            }
        } catch (IOException e){
            return "Не удалось создать backup-файл";
        }
        try(FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            FileWriter frc = new FileWriter(backupFile);
            BufferedWriter brc = new BufferedWriter(frc)) {
            String theLine;
            while ((theLine = br.readLine()) != null) {
                brc.write(theLine);
                brc.write('\n');
            }
        } catch (IOException e){
            return "Не удалось сохранить текущее\nсостояние таблицы в backup-файл";
        }
        backup = backupFile.getAbsolutePath();
        return backup;
    }

    public static String backTo(){
        if (backup == null){
            return "Вы не можете вернуться к прежнему состоянию таблицы, так как\n" +
                    "backup-файл не создан. Пожалуйста, вначале создайте backup-файл";
        }
        try(FileReader fr = new FileReader(backup);
            BufferedReader br = new BufferedReader(fr);
            FileWriter frc = new FileWriter(nameCopy);
            BufferedWriter brc = new BufferedWriter(frc)) {
            String theLine;
            while ((theLine = br.readLine()) != null) {
                brc.write(theLine);
                brc.write('\n');
            }
        } catch (IOException e){
            return "Не удалось вернуться к сохранённому состоянию";
        }
        return "Таблица успешно возвращена в сохранённое состояние";
    }

    public static String insert(String row){
        try(FileWriter fw = new FileWriter(nameCopy, true);
            BufferedWriter bfw = new BufferedWriter(fw)){
            bfw.write(row);
        } catch (IOException e){
            return "Не удалось добавить новое д/з";
        }
        isModify = true;
        return "Новое д/з успешно добавлено";
    }

    public static int searchByKey(int ID){
        try(FileReader fr = new FileReader(nameCopy);
            BufferedReader br = new BufferedReader(fr)){
            br.readLine();
            String theLine;
            int numberOfLine = 0;
            while ((theLine = br.readLine()) != null){
                if (IDFromLine(theLine) == ID){
                    return numberOfLine;
                }
                numberOfLine += 1;
            }
            return -1;
        } catch (IOException e){
            return -1;
        }
    }

    public static List<Integer> searchByNotKey(int field, String search){
        List<Integer> rowsNumbers = new ArrayList<>();
        try(FileReader fr = new FileReader(nameCopy);
            BufferedReader br = new BufferedReader(fr)){
            br.readLine();
            String theLine;
            int rowNumber = 0;
            while ((theLine = br.readLine()) != null){
                if (valueFromLine(field, theLine).equals(search)){
                    rowsNumbers.add(rowNumber);
                }
                rowNumber += 1;
            }
        } catch (IOException e){
            List<Integer> errorList = new ArrayList<>();
            errorList.add(-1);
            return errorList;
        }
        return rowsNumbers;
    }

    public static void deleteRow(int index){
        index += 1; // потому что в Table без заголовка
        System.out.println(index);
        File newFile = new File("copy_2.txt");
        int currentIndex = 0;
        try {
            newFile.createNewFile();
        } catch (IOException e) {
            return;
        }
        try(FileReader fr = new FileReader(nameCopy);
            BufferedReader br = new BufferedReader(fr);
            FileWriter fw = new FileWriter(newFile);
            BufferedWriter bw = new BufferedWriter(fw)){
            String theLine;
            while((theLine = br.readLine()) != null) {
                if (currentIndex != index) {
                    bw.write(theLine);
                    bw.write('\n');
                }
                currentIndex += 1;
            }
        } catch (IOException e){
            return;
        }
        if ((new File(nameCopy)).delete()){
            System.out.println("Table is deleted");
        }
        if (newFile.renameTo(new File(nameCopy))){
            System.out.println("Renamed");
        }
    }

    public static void closeTable(boolean saveCurrentTable){
        if (saveCurrentTable){
            if (saveTable().equals("Таблица успешно сохранена")){
                (new File(nameCopy)).delete();
                if(backup != null){
                    (new File(backup)).delete();
                }
                path = null;
                nameCopy = null;
                backup = null;
                maxID = -1;
                tableLength = 0;
                isModify = false;
            }
        }
        else {
            (new File(nameCopy)).delete();
            if(backup != null){
                (new File(backup)).delete();
            }
            nameCopy = null;
            path = null;
            backup = null;
            maxID = -1;
            tableLength = 0;
            isModify = false;
        }
    }

    public static boolean isActive(){
        return path != null;
    }

    public static String getNameCopy(){
        return nameCopy;
    }

    public static long getTableLength(){
        return tableLength;
    }

    public static int getMaxID(){
        return maxID;
    }

    public static boolean isCorrectLine(String theLine){
        if (theLine.isEmpty()) return false;
        if (theLine.charAt(0) == ';') return false;
        if (theLine.charAt(theLine.length() - 1) == ';') return false;
        StringTokenizer st = new StringTokenizer(theLine, ";");
        if (st.countTokens() != 5) return false;
        String token1 = st.nextToken().trim(); // ID домашнего задания - int
        String token2 = st.nextToken().trim(); // Название предмета - String
        String token3 = st.nextToken().trim(); // Срок сдачи - LocalDate
        String token4 = st.nextToken().trim(); // Номера групп, например: ["23КНТ-4", "23МББЭ-3"] - String []
        String token5 = st.nextToken().trim(); // Описание домашнего задания (можно прикрепить ссылку на ресурс с д/з)

        // Проверка 4-го столбцов
        if (token4.charAt(0) != '[') return false;
        if (token4.charAt(token4.length() - 1) != ']') return false;
        StringBuilder token4Builder = new StringBuilder(token4);
        token4Builder.deleteCharAt(token4.length() - 1);
        token4Builder.deleteCharAt(0);
        StringTokenizer groupNumbers = new StringTokenizer(token4Builder.toString(), ",");
        if (token4.length() - token4.replace(",", "").length() != groupNumbers.countTokens() - 1) return false;
        while (groupNumbers.hasMoreTokens()){
            String group = groupNumbers.nextToken().trim();
            if (!group.matches("^\"\\d{2}[a-яА-Я]*-\\d*\"$")) return false;
        }

        // Проверка 1-го и 3-го столбцов
        StringTokenizer date = new StringTokenizer(token3, ".");
        if (date.countTokens() != 3) return false;
        String dateToken1 = date.nextToken();
        String dateToken2 = date.nextToken();
        String dateToken3 = date.nextToken();
        try{
            int ID = Integer.parseInt(token1);
            int day = Integer.parseInt(dateToken1);
            int month = Integer.parseInt(dateToken2);
            int year = Integer.parseInt(dateToken3);
            LocalDate localDate = LocalDate.of(year, month, day);
        } catch (Exception e){
            return false;
        }
        return true;
    }

    private static String createTableCopy() throws IOException{
        File tableCopy;
        int count = 0;
        while (true) {
            if (count == 0) {
                tableCopy = new File("Current_Table.txt");
            }
            else {
                String attempt = "Current_Table (" + count + ").txt";
                tableCopy = new File(attempt);
            }
            if (tableCopy.createNewFile()) {
                break;
            }
            count += 1;
        }
        return tableCopy.getName();
    }

    private static boolean isTxt(String path){
        int length = path.length();
        if (path.charAt(length - 4) != '.') return false;
        if (path.charAt(length - 3) != 't') return false;
        if (path.charAt(length - 2) != 'x') return false;
        return path.charAt(length - 1) == 't';
    }

    // Коды состояний:
    // -5 - неизвестная ошибка
    // -4 - ресурса с таким именем не существует или это не файл
    // -3 - расширение файла не txt
    // -2 - файл не доступен на чтение
    // -1 - ошибка в заголовках столбцов
    // 0 - файл корректен
    // i - ошибка в строке i, где i из N и i > 0
    private static int checkTable(String path){
        path = path.trim();
        File table = new File(path);
        if (!table.exists() || !table.isFile()){
            return -4;
        }
        if (!isTxt(path)){
            return -3;
        }
        if (!table.canRead()) {
            return -2;
        }
        try(FileReader fr = new FileReader(table);
            BufferedReader br = new BufferedReader(fr)){
            String theLine;
            if ((theLine = br.readLine()) != null){
                if (!theLine.equals("ID;Название предмета;Срок сдачи;Номера групп;Описание")){
                    return -1;
                }
            }
            int count = 1;
            while ((theLine = br.readLine()) != null){
                count += 1;
                if(!isCorrectLine(theLine)){
                    return count;
                }
                int currentID = IDFromLine(theLine);
                if (currentID <= maxID) {
                    return count;
                }
                maxID = currentID;
            }
        } catch (IOException e){
            return -5;
        }
        return 0;
    }

    private static int IDFromLine(String theLine){
        StringBuilder currentIDString = new StringBuilder();
        int index = 0;
        char c;
        while ((c = theLine.charAt(index)) != ';'){
            currentIDString.append(c);
            index += 1;
        }
        return Integer.parseInt(currentIDString.toString());
    }

    private static String valueFromLine(int field, String theLine){
        StringTokenizer st = new StringTokenizer(theLine, ";");
        for (int i = 0; i < field; i++){
            st.nextToken();
        }
        if (field == 3){
            return st.nextToken().replace("\"", "").replace(" ", "");
        }
        return st.nextToken();
    }

    public static void main(String [] args){
        MainWindow mainWindow = new MainWindow();
    }
}