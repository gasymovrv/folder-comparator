:: файл обязательно должен быть в кодировке "Cyrillic (Windows 1251)"
:: иначе параметры передаваемые программе будут нечитаемы 
:: В консоле тип шрифта должен быть "Lucida Console"
chcp 1251

java -jar target/folder-comparator-1.0-SNAPSHOT-jar-with-dependencies.jar "E:\Photo&Video" "F:\Data - Ruslan\Photo&Video"

pause
