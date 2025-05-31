package com.events.parser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Pattern;

public class DateParser {

    private static final DateTimeFormatter FORMAT_WITH_YEAR = DateTimeFormatter.ofPattern("d MMMM yyyy 'в' HH:mm", new Locale("ru"));
    private static final DateTimeFormatter FORMAT_NO_YEAR = DateTimeFormatter.ofPattern("d MMMM 'в' HH:mm yyyy", new Locale("ru"));

    public static LocalDateTime parseDate(String dateStr) {
        try {
            // Проверка: есть ли в строке год?
            if (Pattern.compile("\\d{4}").matcher(dateStr).find()) {
                // Год есть → парсим как "31 мая 2025 в 13:00"
                return LocalDateTime.parse(dateStr, FORMAT_WITH_YEAR);
            } else {
                // Года нет → добавим 2025
                String dateWithYear = dateStr + " 2025";
                return LocalDateTime.parse(dateWithYear, FORMAT_NO_YEAR);
            }
        } catch (DateTimeParseException e) {
            // Логирование или обработка ошибки
            throw new RuntimeException("Ошибка парсинга даты: " + dateStr, e);
        }
    }
}
