package ru.shadewallcorp.jiraTask.jiraManager.date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static ru.shadewallcorp.jiraTask.jiraManager.date.DateStatus.*;

/**
 * Класс для работы с датами
 *

 */
@Component
public class DateUtil {
    // TODO: congigMap, application.yml
    private static final String PATH_TO_NOT_WORK_DAYS = "src/main/java/ru/shadewallcorp/tds/jiraManager/date/data/notWorkDays.txt";
    private static final String PATH_TO_WORK_HOLIDAYS = "src/main/java/ru/shadewallcorp/tds/jiraManager/date/data/workHolidays.txt";

    @Value("#{'${calendar.notWorkDaysList}'.split(',')}")
    private static List<String> notWorkDaysListDef;
    @Value("#{'${calendar.workHolidaysList}'.split(',')}")
    private static List<String> workHolidaysListDef;
    // TODO не читается из конфига
    // TODO: от перименованного application.yml зависит чарт и другие *.conf
    // TODO try    @Value(“#{new java.text.SimpleDateFormat(‘${aDateFormat}’).parse(‘${aDateStr}’)}”)
    private static List<Date> notWorkDaysList;
    private static List<Date> workHolidaysList;

    static {
        DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
        DATE_TIME_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        notWorkDaysList = ofNullable(notWorkDaysListDef).map(List::stream)
                .map(days -> days
                        .map(s -> { try { return DateUtil.DATE_FORMAT.parse(s); } catch (ParseException e) {return null;}})
                        .collect(Collectors.toList()))
        .orElse(Collections.emptyList());
        workHolidaysList = ofNullable(workHolidaysListDef).map(List::stream)
                .map(days -> days
                        .map(s -> { try { return DateUtil.DATE_FORMAT.parse(s); } catch (ParseException e) {return null;}})
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
        if (notWorkDaysList.isEmpty()) {
            notWorkDaysList = getDateListByFile(PATH_TO_NOT_WORK_DAYS);
        }
        if (workHolidaysList.isEmpty()) {
            workHolidaysList = getDateListByFile(PATH_TO_WORK_HOLIDAYS);
        }
    }

    public static final DateFormat DATE_FORMAT;
    public static final DateFormat DATE_TIME_FORMAT;

    /**
     * Добавляет часы и минуты к дате
     *
     * @param date    дата
     * @param hours   количество часов, которые нужно прибавить
     * @param minutes количество минут, которые нужно прибавить
     * @return дату с прибавленным количеством часов и минут
     */
    public static Date addHoursAndMinutes(Date date, long hours, long minutes) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, (int) hours);
        calendar.add(Calendar.MINUTE, (int) minutes);
        return calendar.getTime();
    }

    /**
     * Добавляет рабочие часы к дате
     * <p>
     * Время считается с 09:00 до 18:00 (без выходных или с выходными в зависимости от значения excludeHolidays)
     *
     * @param date            дата
     * @param excludeHolidays индикатор неучета выходных и праздничных дней при расчете (если true, то не учитываем их)
     * @param hours           количество часов, которые нужно прибавить
     * @return дату с прибавленным количеством часов
     */
    public static Date addWorkHours(Date date, long hours, boolean excludeHolidays) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        date = getWorkTime(date, excludeHolidays);
        // прибавляем по 1 часу, если при прибавлении вышли за пределы 18:00, то прибавляем 1 день
        // могут быть две ситуации после прибавления: время = 18:XX, время = 19:00
        // переносим часы и минуты на следующий рабочий день и устанавливаем
        // время = XX:YY, где XX = (9 + значение часов после прибавления часа - 18) YY - минуты после прибавления
        for (int i = 0; i < hours; i++) {
            calendar.setTime(date);
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            date = calendar.getTime();
            if (!isWorkTime(date)) {
                date = getDateWithChangedTime(date, 9 + (calendar.get(Calendar.HOUR_OF_DAY)) - 18, calendar.get(Calendar.MINUTE));
                date = addWorkDays(date, 1, excludeHolidays);
            }
        }
        return date;
    }

    /**
     * Добавляет дни к дате
     *
     * @param date дата
     * @param day  дни, которое нужно нужно прибавить
     * @return дату с прибавленным указанного количества дней
     */
    public static Date addDays(Date date, long day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, (int) day);
        return calendar.getTime();
    }

    /**
     * Добавляет только рабочие дни к дате
     * <p>
     * Время считается с 09:00 до 18:00 (без выходных или с выходными в зависимости от значения excludeHolidays)
     *
     * @param date            дата
     * @param days            количество дней, которые нужно прибавить
     * @param excludeHolidays индикатор неучета выходных и праздничных дней при расчете (если true, то не учитываем их)
     * @return дату с прибавленным количеством дней
     */
    public static Date addWorkDays(Date date, long days, boolean excludeHolidays) throws ParseException {
        date = getWorkTime(date, excludeHolidays);
        if (excludeHolidays) {
            // прибавляем рабочие дни
            int i = 0;
            while (i < days) {
                date = addDays(date, 1);
                if (!isHoliday(date)) i++;
            }
            // если в результате получился нерабочий день, то прибавляем по 1 дню, пока не попадем на рабочий день
            while (isHoliday(date)) {
                date = addDays(date, 1);
            }
        } else return addDays(date, days);
        return date;
    }

    /**
     * Вычисляет разницу между двумя датами
     *
     * @param startDate дата начала
     * @param endDate   дата окончания
     * @return разницу между двумя датами
     */
    public static Time diffDate(Date startDate, Date endDate) {
        long timeDiff = Math.abs(startDate.getTime() - endDate.getTime());
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff);
        long hours = minutes / 60;
        minutes = minutes % 60;
        long days = hours / 24;
        hours = hours % 24;
        return new Time(days, hours, minutes);
    }

    /**
     * Вычисляет разницу между двумя датами в рабочие дни
     * <p>
     * Время считается с 09:00 до 18:00 (без выходных или с выходными в зависимости от значения excludeHolidays)
     *
     * @param startDate       дата начала
     * @param endDate         дата окончания
     * @param excludeHolidays индикатор неучета выходных и праздничных дней при расчете (если true, то пропускаем их при расчете)
     * @return разницу между двумя датами
     */
    public static Time diffWorkDate(Date startDate, Date endDate, boolean excludeHolidays) throws ParseException {
        // приводим даты к рабочим
        startDate = getWorkTime(startDate, excludeHolidays);
        endDate = getWorkTime(endDate, excludeHolidays);

        Calendar calendarStart = getCalendar(startDate, new Time(0, 18, 0), false, true, true);
        Calendar calendarEnd = getCalendar(endDate, new Time(0, 9, 0), false, true, true);

        // если разница между двумя датами меньше 1 дня, то возвращаем разницу сразу
        if (calendarStart.get(Calendar.DAY_OF_MONTH) == calendarEnd.get(Calendar.DAY_OF_MONTH))
            return diffDate(startDate, endDate);

        Date endWorkDateForStartDate = calendarStart.getTime();
        Date startWorkDateForEndDate = calendarEnd.getTime();
        // вычисляем разницу между концом рабочего дня (18:00) и startDate
        Time startDiffTime = diffDate(endWorkDateForStartDate, startDate);
        // вычисляем разницу между endDate и началом рабочего дня (09:00)
        Time endDiffTime = diffDate(startWorkDateForEndDate, endDate);

        calendarStart.setTime(startDate);
        calendarEnd.setTime(endDate);

        int dayCount = 0;
        boolean stopFlag = false;
        // прибавляем по 1 дню к startDate до тех пор, пока числа (day) не будут равны и считаем их количество
        while (!stopFlag) {
            startDate = addWorkDays(startDate, 1, excludeHolidays);
            dayCount++;
            calendarStart.setTime(startDate);
            stopFlag = calendarStart.get(Calendar.YEAR) == calendarEnd.get(Calendar.YEAR)
                    && calendarStart.get(Calendar.MONTH) == calendarEnd.get(Calendar.MONTH)
                    && calendarStart.get(Calendar.DAY_OF_MONTH) == calendarEnd.get(Calendar.DAY_OF_MONTH);
        }
        Time resTimeDiff = sumTime(sumTime(startDiffTime, endDiffTime, true), new Time(dayCount, 0, 0), true);
        resTimeDiff.setDays(resTimeDiff.getDays() - 1);
        return resTimeDiff;
    }

    /**
     * Вычисляет сумму двух отрезков рабочего времени
     *
     * @param firstTime  первое слагаемое
     * @param secondTime второе слагаемое
     * @param isWorkTime индиктор рабочего времени в дне (если true, то день = 9 часов, если false, то 24)
     * @return сумму двух дат
     */
    public static Time sumTime(Time firstTime, Time secondTime, boolean isWorkTime) {
        long workHours = isWorkTime ? 9 : 24;
        long firstMinutes = firstTime.getDays() * workHours * 60 + firstTime.getHours() * 60 + firstTime.getMinutes();
        long secondMinutes = secondTime.getDays() * workHours * 60 + secondTime.getHours() * 60 + secondTime.getMinutes();
        long minutes = firstMinutes + secondMinutes;
        long hours = minutes / 60;
        minutes %= 60;
        long days = hours / workHours;
        hours %= workHours;
        return new Time(days, hours, minutes);
    }

    /**
     * Вычисляет разницу между двумя отрезками времени
     *
     * @param firstTime  уменьшаемое
     * @param secondTime вычитаемое
     * @param isWorkTime индиктор рабочего времени в дне (если true, то день = 9 часов, если false, то 24)
     * @return разницу между двумя отрезками времени
     */
    public static Time diffTime(Time firstTime, Time secondTime, boolean isWorkTime) {
        long workHours = isWorkTime ? 9 : 24;
        long firstMinutes = firstTime.getDays() * workHours * 60 + firstTime.getHours() * 60 + firstTime.getMinutes();
        long secondMinutes = secondTime.getDays() * workHours * 60 + secondTime.getHours() * 60 + secondTime.getMinutes();
        long minutes = firstMinutes - secondMinutes;
        long hours = minutes / 60;
        minutes %= 60;
        long days = hours / workHours;
        hours %= workHours;
        return new Time(days, hours, minutes);
    }

    /**
     * Меняет время на рабочее
     * <p>
     * Время считается с 09:00 до 18:00 (без выходных или с выходными в зависимости от значения excludeHolidays)
     *
     * @param date            дата
     * @param excludeHolidays индикатор неучета выходных и праздничных дней при расчете (если true, то не учитываем их)
     * @return рабочее время
     */
    private static Date getWorkTime(Date date, boolean excludeHolidays) {
        // получаем рабочее время без выходных
        if (!isWorkTime(date)) {
            if (getDateStatus(date) == HOLIDAY_AFTER_WORK_TIME || getDateStatus(date) == WEEKDAY_AFTER_WORK_TIME)
                date = addDays(date, 1);
            date = getDateWithChangedTime(date, 9, 0);
        }
        // если нужно не учитывать выходные и праздничные дни
        if (excludeHolidays) {
            // прибавляем по 1 дню, пока не попадем на рабочий день
            if (isHoliday(date))
                date = getDateWithChangedTime(date, 9, 0);
            while (isHoliday(date)) {
                date = addDays(date, 1);
            }
        }
        return date;
    }

    /**
     * Вычисляет выходной день или нет
     *
     * @param date дата
     * @return индикатор выходного дня
     */
    private static boolean isHoliday(Date date) {
        DateStatus dateStatus = getDateStatus(date);
        return dateStatus == HOLIDAY_WORK_TIME
                || dateStatus == HOLIDAY_AFTER_WORK_TIME
                || dateStatus == HOLIDAY_BEFORE_WORK_TIME;
    }

    /**
     * Вычисляет рабочее время или нет
     *
     * @param date дата
     * @return индикатор рабочего времени
     */
    private static boolean isWorkTime(Date date) {
        DateStatus dateStatus = getDateStatus(date);
        return dateStatus == WEEKDAY_WORK_TIME || dateStatus == HOLIDAY_WORK_TIME;
    }

    /**
     * Меняет время в дате
     *
     * @param date   дата
     * @param hour   часы
     * @param minute минуты
     * @return дату с измененным временем
     */
    private static Date getDateWithChangedTime(Date date, int hour, int minute) {
        return getCalendar(date, new Time(0, hour, minute), false, true, true).getTime();
    }

    /**
     * Метод, определяющий статус времени
     *
     * @param date дата
     * @return статус времени
     */
    private static DateStatus getDateStatus(Date date) {
        Calendar calendar = Calendar.getInstance();
        Date dateWithoutTime = getDateWithChangedTime(date, 0, 0);
        calendar.setTime(dateWithoutTime);
        boolean isWeekday = !((calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                || notWorkDaysList.contains(dateWithoutTime)) && !workHolidaysList.contains(dateWithoutTime));
        calendar.setTime(date);
        boolean isBeforeWorkTime = calendar.get(Calendar.HOUR_OF_DAY) < 9;
        boolean isAfterWorkTime = calendar.get(Calendar.HOUR_OF_DAY) > 18 || (calendar.get(Calendar.HOUR_OF_DAY) == 18 && calendar.get(Calendar.MINUTE) > 0);
        boolean isWorkTime = !isBeforeWorkTime && !isAfterWorkTime;
        return DateStatus.getDateStatus(isWeekday, isWorkTime, isBeforeWorkTime, isAfterWorkTime);
    }

    /**
     * Создает объект Calendar с установленным временем
     *
     * @param date          дата
     * @param time          устанавливаемое время
     * @param changeDays    флаг изменения дней
     * @param changeHours   флаг изменения часов
     * @param changeMinutes флаг изменения минут
     * @return объект Calendar с установленным временем
     */
    private static Calendar getCalendar(Date date, Time time, boolean changeDays, boolean changeHours, boolean changeMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (changeDays) calendar.set(Calendar.DAY_OF_MONTH, (int) time.getDays());
        if (changeHours) calendar.set(Calendar.HOUR_OF_DAY, (int) time.getHours());
        if (changeMinutes) calendar.set(Calendar.MINUTE, (int) time.getMinutes());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**
     * Преобразовывает строку в дату
     *
     * @param date дата в строковом виде
     * @return дату по строке
     * @throws ParseException
     */
    public static Date date(String date) throws ParseException {
        return DATE_TIME_FORMAT.parse(date);
    }

    /**
     * Преобразовывает набор long в объект Time
     *
     * @param days количество дней
     * @param hours количество часов
     * @param minutes количество минут
     * @return объект Time
     * @throws ParseException
     */
    public static Time time(long days, long hours, long minutes) throws ParseException {
        return new Time (days, hours, minutes);
    }

    /**
     * Преобразовывает дату в строку
     *
     * @param date объект даты
     * @return дату в строковом виде
     */
    public static String date(Date date) {
        String dateStr;
        try {
            dateStr = DATE_TIME_FORMAT.format(date);
        } catch (Exception e) {
            dateStr = "";
        }
        return dateStr;
    }

    /**
     * Преобразовывает дату в строку
     *
     * @param date объект даты
     * @param pattern шаблон даты
     * @return дату в строковом виде
     */
    public static String date(Date date, String pattern) {
        String dateStr;
        try {
            DateFormat dateFormat = new SimpleDateFormat(pattern);
            dateStr = dateFormat.format(date);
        } catch (Exception e) {
            dateStr = "";
        }
        return dateStr;
    }

    /**
     * Возвращает список дат из файла
     *
     * @param pathToFile путь к файлу
     * @return список дат из файла
     */
    private static List<Date> getDateListByFile(String pathToFile) {
        List<Date> dateList = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(new File(pathToFile).getCanonicalPath()))) {
            stream.forEach(row -> {
                try {
                    dateList.add(DATE_FORMAT.parse(row));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dateList;
    }
}
