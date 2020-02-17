import org.junit.Test;
import ru.shadewallcorp.jiraTasks.jiraManager.date.Time;
import ru.shadewallcorp.jiraTasks.jiraManager.util.ReportGenerator;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static ru.shadewallcorp.jiraTasks.jiraManager.date.DateUtil.addDays;
import static ru.shadewallcorp.jiraTasks.jiraManager.date.DateUtil.addHoursAndMinutes;
import static ru.shadewallcorp.jiraTasks.jiraManager.date.DateUtil.addWorkDays;
import static ru.shadewallcorp.jiraTasks.jiraManager.date.DateUtil.addWorkHours;
import static ru.shadewallcorp.jiraTasks.jiraManager.date.DateUtil.date;
import static ru.shadewallcorp.jiraTasks.jiraManager.date.DateUtil.diffDate;
import static ru.shadewallcorp.jiraTasks.jiraManager.date.DateUtil.diffTime;
import static ru.shadewallcorp.jiraTasks.jiraManager.date.DateUtil.diffWorkDate;
import static ru.shadewallcorp.jiraTasks.jiraManager.date.DateUtil.sumTime;
import static ru.shadewallcorp.jiraTasks.jiraManager.date.DateUtil.time;

/**
 * Класс для тестирования методов класса DateUtil
 *
 */
public class DateUtilTest {
    @Test
    public void testAddDate() throws ParseException {
        /*
        Добавление дней
         */

        // круглосуточно, начальная дата - выходной
        assertEquals(date("11-01-2018 02:05:34"), addDays(date("01-01-2018 02:05:34"), 10));
        // круглосуточно, начальная дата - будний день
        assertEquals(date("10-01-2018 02:05:34"), addDays(date("09-01-2018 02:05:34"), 1));

        /*
        Добавление рабочих дней (учитывая праздничные дни и время рабочего дня с 09:00 до 18:00)
         */

        // рабочее время, начальная дата - будний день, в отрезок времени не входят выходные
        assertEquals(date("01-03-2018 10:00:34"), addWorkDays(date("28-02-2018 10:00:34"), 1, true));
        // рабочее время, начальная дата - выходной день, в отрезок времени входят выходные
        assertEquals(date("23-01-2018 09:00:00"), addWorkDays(date("01-01-2018 02:05:34"), 10, true));
        // рабочее время, начальная дата - будний день, раньше начала рабочего дня, в отрезок времени входят выходные
        assertEquals(date("15-01-2018 09:00:00"), addWorkDays(date("09-01-2018 02:05:34"), 4, true));
        // рабочее время, начальная дата - будний день, самое начало рабочего дня (09:00), в отрезок времени входят выходные
        assertEquals(date("15-01-2018 09:00:00"), addWorkDays(date("09-01-2018 09:00:00"), 4, true));
        // рабочее время, начальная дата - будний день, конец рабочего дня (но еще рабочий), в отрезок времени входят выходные
        assertEquals(date("15-01-2018 18:00:00"), addWorkDays(date("09-01-2018 18:00:00"), 4, true));
        // рабочее время, начальная дата - будний день, после конца рабочего дня, в отрезок времени входят выходные
        assertEquals(date("16-01-2018 09:00:00"), addWorkDays(date("09-01-2018 18:01:00"), 4, true));
        assertEquals(date("11-01-2018 09:00:00"), addWorkDays(date("09-01-2018 18:01:00"), 1, true));
        // рабочее время, начальная дата - выходной день, раньше начала рабочего дня, в отрезок времени входят выходные
        assertEquals(date("19-01-2018 09:00:00"), addWorkDays(date("13-01-2018 02:05:34"), 4, true));
        // рабочее время, начальная дата - выходной день, самое начало рабочего дня (09:00), в отрезок времени входят выходные
        assertEquals(date("19-01-2018 09:00:00"), addWorkDays(date("13-01-2018 09:00:00"), 4, true));
        // рабочее время, начальная дата - выходной день, конец рабочего дня (но еще рабочий), в отрезок времени входят выходные
        assertEquals(date("19-01-2018 09:00:00"), addWorkDays(date("13-01-2018 18:00:00"), 4, true));
        // рабочее время, начальная дата - выходной день, после конца рабочего дня, в отрезок времени входят выходные
        assertEquals(date("19-01-2018 09:00:00"), addWorkDays(date("13-01-2018 18:01:00"), 4, true));
        assertEquals(date("16-01-2018 09:00:00"), addWorkDays(date("13-01-2018 18:01:00"), 1, true));
        assertEquals(date("02-02-2018 09:00:00"), addWorkDays(date("13-01-2018 18:01:00"), 14, true));
        // рабочее время, начальная дата - будний день, в отрезок времени входят выходные и рабочий выходной
        assertEquals(date("03-05-2018 15:01:00"), addWorkDays(date("27-04-2018 15:01:00"), 2, true));
        // рабочее время, начальная дата - будний день, в отрезок времени входит рабочий выходной
        assertEquals(date("28-04-2018 15:01:00"), addWorkDays(date("27-04-2018 15:01:00"), 1, true));
        // рабочее время, начальная дата - будний день, большой период
        assertEquals(date("17-12-2018 15:00:00"), addWorkDays(date("07-02-2018 15:00:00"), 215, true));

        /*
        Добавление рабочих дней (не учитывая праздничные дни, но учитывая время рабочего дня с 09:00 до 18:00)
         */

        // рабочее время, начальная дата - будний день, в отрезок времени не входят выходные
        assertEquals(date("01-03-2018 10:00:34"), addWorkDays(date("28-02-2018 10:00:34"), 1, false));
        // рабочее время, начальная дата - выходной день, в отрезок времени входят выходные
        assertEquals(date("11-01-2018 09:00:00"), addWorkDays(date("01-01-2018 02:05:34"), 10, false));
        // рабочее время, начальная дата - будний день, раньше начала рабочего дня, в отрезок времени входят выходные
        assertEquals(date("13-01-2018 09:00:00"), addWorkDays(date("09-01-2018 02:05:34"), 4, false));
        // рабочее время, начальная дата - будний день, самое начало рабочего дня (09:00), в отрезок времени входят выходные
        assertEquals(date("13-01-2018 09:00:00"), addWorkDays(date("09-01-2018 09:00:00"), 4, false));
        // рабочее время, начальная дата - будний день, конец рабочего дня (но еще рабочий), в отрезок времени входят выходные
        assertEquals(date("13-01-2018 18:00:00"), addWorkDays(date("09-01-2018 18:00:00"), 4, false));
        // рабочее время, начальная дата - будний день, после конца рабочего дня, в отрезок времени входят выходные
        assertEquals(date("14-01-2018 09:00:00"), addWorkDays(date("09-01-2018 18:01:00"), 4, false));
        assertEquals(date("11-01-2018 09:00:00"), addWorkDays(date("09-01-2018 18:01:00"), 1, false));
        // рабочее время, начальная дата - выходной день, раньше начала рабочего дня, в отрезок времени входят выходные
        assertEquals(date("17-01-2018 09:00:00"), addWorkDays(date("13-01-2018 02:05:34"), 4, false));
        // рабочее время, начальная дата - выходной день, самое начало рабочего дня (09:00), в отрезок времени входят выходные
        assertEquals(date("17-01-2018 09:00:00"), addWorkDays(date("13-01-2018 09:00:00"), 4, false));
        // рабочее время, начальная дата - выходной день, конец рабочего дня (но еще рабочий), в отрезок времени входят выходные
        assertEquals(date("17-01-2018 18:00:00"), addWorkDays(date("13-01-2018 18:00:00"), 4, false));
        // выходной, начальная дата - выходной день, после конца рабочего дня, в отрезок времени входят выходные
        assertEquals(date("18-01-2018 09:00:00"), addWorkDays(date("13-01-2018 18:01:00"), 4, false));
        assertEquals(date("15-01-2018 09:00:00"), addWorkDays(date("13-01-2018 18:01:00"), 1, false));
        assertEquals(date("28-01-2018 09:00:00"), addWorkDays(date("13-01-2018 18:01:00"), 14, false));
        // рабочее время, начальная дата - будний день, в отрезок времени входят выходные и рабочий выходной
        assertEquals(date("01-05-2018 15:01:00"), addWorkDays(date("27-04-2018 15:01:00"), 4, false));

        /*
        Изменения времени
         */

        // нулевые изменения
        assertEquals(date("28-02-2018 23:01:00"), addHoursAndMinutes(date("28-02-2018 23:01:00"), 0, 0));
        // изменение минут, влекущее изменение часов
        assertEquals(date("27-04-2018 16:02:00"), addHoursAndMinutes(date("27-04-2018 15:01:00"), 0, 61));
        // изменение минут, влекущее изменение часов, дня и месяца
        assertEquals(date("01-03-2018 00:02:00"), addHoursAndMinutes(date("28-02-2018 23:01:00"), 0, 61));
        // изменение часов, влекущее изменение дня и месяца
        assertEquals(date("01-03-2018 01:01:00"), addHoursAndMinutes(date("28-02-2018 23:01:00"), 2, 0));
        // изменение часов и минут, влекущее изменение дня и месяца
        assertEquals(date("01-03-2018 02:02:00"), addHoursAndMinutes(date("28-02-2018 23:01:00"), 2, 61));

        /*
        Добавление рабочих дней (учитывая праздничные дни и время рабочего дня с 09:00 до 18:00)
         */

        // начальная дата до начала рабочего дня, рабочий день
        assertEquals(date("28-02-2018 11:00:00"), addWorkHours(date("28-02-2018 00:01:00"), 2, true));
        // начальная дата конец рабочего дня (но не превышает), рабочий день
        assertEquals(date("28-02-2018 11:00:00"), addWorkHours(date("27-02-2018 18:00:00"), 2, true));
        // начальная дата больше рабочего дня, рабочий день
        assertEquals(date("28-02-2018 11:00:00"), addWorkHours(date("27-02-2018 18:59:00"), 2, true));
        // начальная дата укладывается а рабочий день, рабочий день, с переходом на следующий день после прибавления
        assertEquals(date("28-02-2018 10:59:00"), addWorkHours(date("27-02-2018 17:59:00"), 2, true));
        // начальная дата до начала рабочего дня, выходной
        assertEquals(date("09-01-2018 11:00:00"), addWorkHours(date("01-01-2018 00:01:00"), 2, true));
        // начальная дата конец рабочего дня (но не превышает), выходной
        assertEquals(date("09-01-2018 11:00:00"), addWorkHours(date("01-01-2018 18:00:00"), 2, true));
        // начальная дата больше рабочего дня, выходной
        assertEquals(date("09-01-2018 11:00:00"), addWorkHours(date("01-01-2018 18:59:00"), 2, true));
        // начальная дата укладывется в рабочий день, выходной
        assertEquals(date("09-01-2018 11:00:00"), addWorkHours(date("01-01-2018 17:59:00"), 2, true));
        // начальная дата укладывется в рабочий день, рабочий выходной
        assertEquals(date("28-04-2018 10:59:00"), addWorkHours(date("27-04-2018 17:59:00"), 2, true));
        // начальная дата укладывется в рабочий день, рабочий выходной, в отрезке времени есть выходные, добавление часов влияет на добавление дней
        assertEquals(date("04-05-2018 10:59:00"), addWorkHours(date("28-04-2018 17:59:00"), 11, true));

        /*
        Добавление рабочих дней (не учитывая праздничные дни, но учитывая время рабочего дня с 09:00 до 18:00)
         */

        // начальная дата до начала рабочего дня, рабочий день
        assertEquals(date("28-02-2018 11:00:00"), addWorkHours(date("28-02-2018 00:01:00"), 2, false));
        // начальная дата конец рабочего дня (но не превышает), рабочий день
        assertEquals(date("28-02-2018 11:00:00"), addWorkHours(date("27-02-2018 18:00:00"), 2, false));
        // начальная дата больше рабочего дня, рабочий день
        assertEquals(date("28-02-2018 11:00:00"), addWorkHours(date("27-02-2018 18:59:00"), 2, false));
        // начальная дата укладывается а рабочий день, рабочий день, с переходом на следующий день после прибавления
        assertEquals(date("28-02-2018 10:59:00"), addWorkHours(date("27-02-2018 17:59:00"), 2, false));
        // начальная дата до начала рабочего дня, выходной
        assertEquals(date("01-01-2018 11:00:00"), addWorkHours(date("01-01-2018 00:01:00"), 2, false));
        // начальная дата конец рабочего дня (но не превышает), выходной
        assertEquals(date("02-01-2018 11:00:00"), addWorkHours(date("01-01-2018 18:00:00"), 2, false));
        // начальная дата больше рабочего дня, выходной
        assertEquals(date("02-01-2018 11:00:00"), addWorkHours(date("01-01-2018 18:59:00"), 2, false));
        // начальная дата укладывется в рабочий день, выходной
        assertEquals(date("02-01-2018 10:59:00"), addWorkHours(date("01-01-2018 17:59:00"), 2, false));
        // начальная дата укладывется в рабочий день, рабочий выходной
        assertEquals(date("28-04-2018 10:59:00"), addWorkHours(date("27-04-2018 17:59:00"), 2, false));
        // начальная дата укладывется в рабочий день, рабочий выходной, в отрезке времени есть выходные, добавление часов влияет на добавление дней
        assertEquals(date("30-04-2018 10:59:00"), addWorkHours(date("28-04-2018 17:59:00"), 11, false));
    }

    @Test
    public void testDiffDate() throws ParseException {
         /*
        Вычисление разницы между двумя датами
         */

        assertDiffDate(date("28-04-2018 17:59:00"), date("29-04-2018 17:59:00"), false, false, new Time(1, 0, 0));
        assertDiffDate(date("28-04-2018 17:59:00"), date("29-04-2018 16:30:00"), false, false, new Time(0, 22, 31));
        assertDiffDate(date("28-02-2018 17:59:00"), date("02-03-2018 16:30:00"), false, false, new Time(1, 22, 31));
        assertDiffDate(date("28-02-2018 17:59:00"), date("02-12-2018 16:30:00"), false, false, new Time(276, 22, 31));

        /*
        Вычисление разницы между двумя датами (учитывая праздничные дни и время рабочего дня с 09:00 до 18:00)
         */

        // начало в будний день, 0 дней разницы
        assertDiffDate(date("09-01-2018 17:59:00"), date("10-01-2018 15:45:00"), true, true, new Time(0, 6, 46));
        // начало в выходной, 0 дней разницы
        assertDiffDate(date("01-01-2018 17:59:00"), date("09-01-2018 15:45:00"), true, true, new Time(0, 6, 45));
        // начало в будний день
        assertDiffDate(date("09-01-2018 17:59:00"), date("10-01-2018 15:45:00"), true, true, new Time(0, 6, 46));
        // начало в будний день, в отрезке времени есть выходные
        assertDiffDate(date("12-01-2018 17:59:00"), date("15-01-2018 15:45:00"), true, true, new Time(0, 6, 46));
        // начало в будний день, в отрезке времени есть рабочий выходной
        assertDiffDate(date("27-04-2018 17:59:00"),date("28-04-2018 15:45:00"), true, true, new Time(0, 6, 46));
        // начало и конец в рабочий выходное, нулевая разница
        assertDiffDate(date("28-04-2018 15:45:00"), date("28-04-2018 15:45:00"), true, true, new Time(0, 0, 0));
        // начало в будний день, конец в выходной день
        assertDiffDate(date("28-04-2018 15:45:00"), date("02-05-2018 15:45:00"), true, true, new Time(0, 2, 15));
        // начало в будний день (конец рабочего дня), конец в выходной день
        assertDiffDate(date("28-04-2018 18:00:00"), date("02-05-2018 15:45:00"), true, true, new Time(0, 0, 0));
        // начало в будний день (конец рабочего дня), конец в будний день (начало рабочего дня)
        assertDiffDate(date("26-04-2018 18:00:00"), date("27-04-2018 09:00:00"), true, true, new Time(0, 0, 0));
        // начало в будний день (раньше конца рабочего дня), конец в выходной день (начало рабочего дня)
        assertDiffDate(date("26-04-2018 17:59:00"), date("27-04-2018 09:00:00"), true, true, new Time(0, 0, 1));
        // начало в будний день (позже конца рабочего дня), конец в будний день (начало рабочего дня)
        assertDiffDate(date("26-04-2018 18:01:00"), date("27-04-2018 09:00:00"), true, true, new Time(0, 0, 0));
        // начало в будний день (раньше конца рабочего дня), конец в будний день (раньше начала рабочего дня)
        assertDiffDate(date("26-04-2018 17:59:00"), date("27-04-2018 08:00:00"), true, true, new Time(0, 0, 1));
        // начало в будний день (раньше конца рабочего дня), большой период с изменением дней
        assertDiffDate(date("07-02-2018 15:00:00"), date("17-12-2018 15:00:00"), true, true, new Time(215, 0, 0));
        // начало в будний день (раньше конца рабочего дня), большой период с изменением дней, часов и минут
        assertDiffDate(date("07-02-2018 15:00:00"), date("17-12-2018 14:30:00"), true, true, new Time(214, 8, 30));

        /*
        Вычисление разницы между двумя датами (не учитывая праздничные дни, но учитывая время рабочего дня с 09:00 до 18:00)
         */

        // начало в будний день, 0 дней разницы
        assertDiffDate(date("09-01-2018 17:59:00"), date("10-01-2018 15:45:00"), true, false, new Time(0, 6, 46));
        // начало в выходной, 0 дней разницы
        assertDiffDate(date("01-01-2018 17:59:00"), date("09-01-2018 15:45:00"), true, false, new Time(7, 6, 46));
        // начало в будний день
        assertDiffDate(date("09-01-2018 17:59:00"), date("10-01-2018 15:45:00"), true, false, new Time(0, 6, 46));
        // начало в будний день, в отрезке времени есть выходные
        assertDiffDate(date("12-01-2018 17:59:00"), date("15-01-2018 15:45:00"), true, false, new Time(2, 6, 46));
        // начало в будний день, в отрезке времени есть рабочий выходной
        assertDiffDate(date("27-04-2018 17:59:00"), date("28-04-2018 15:45:00"), true, false, new Time(0, 6, 46));
        // начало и конец в рабочий выходное, нулевая разница
        assertDiffDate(date("28-04-2018 15:45:00"), date("28-04-2018 15:45:00"), true, false, new Time(0, 0, 0));
        // начало в будний день, конец в выходной день
        assertDiffDate(date("28-04-2018 15:45:00"), date("02-05-2018 15:45:00"), true, false, new Time(4, 0, 0));
        // начало в будний день (конец рабочего дня), конец в выходной день
        assertDiffDate(date("28-04-2018 18:00:00"), date("02-05-2018 15:45:00"), true, false, new Time(3, 6, 45));
        // начало в будний день (конец рабочего дня), конец в будний день (начало рабочего дня)
        assertDiffDate(date("26-04-2018 18:00:00"), date("27-04-2018 09:00:00"), true, false, new Time(0, 0, 0));
        // начало в будний день (раньше конца рабочего дня), конец в выходной день (начало рабочего дня)
        assertDiffDate(date("26-04-2018 17:59:00"), date("27-04-2018 09:00:00"), true, false, new Time(0, 0, 1));
        // начало в будний день (позже конца рабочего дня), конец в будний день (начало рабочего дня)
        assertDiffDate(date("26-04-2018 18:01:00"), date("27-04-2018 09:00:00"), true, false, new Time(0, 0, 0));
        // начало в будний день (раньше конца рабочего дня), конец в будний день (раньше начала рабочего дня)
        assertDiffDate(date("26-04-2018 17:59:00"), date("27-04-2018 08:00:00"), true, false, new Time(0, 0, 1));
        // начало в будний день (раньше конца рабочего дня), большой период с изменением дней
        assertDiffDate(date("07-02-2018 15:00:00"), date("17-12-2018 15:00:00"), true, false, new Time(313, 0, 0));
        // начало в будний день (раньше конца рабочего дня), большой период с изменением дней, часов и минут
        assertDiffDate(date("07-02-2018 15:00:00"), date("17-12-2018 14:30:00"), true, false, new Time(312, 8, 30));
    }

    @Test
    public void testSumTime() throws ParseException {
        /*
        Суммирование Time (учитывая время рабочего дня с 09:00 до 18:00)
         */

        // Суммирование каждой компоненты без влияния на остальные
        assertTime(time(210,3,50), time(15,4,9), true, true, time(225,7,59));
        // Нулевое суммирование
        assertTime(time(0,0,0), time(0,0,0), true, true, time(0,0,0));
        // Суммирование рабочих часов приводит к увеличению рабочих дней
        assertTime(time(1,7,50), time(1,4,9), true, true, time(3,2,59));
        // Суммирование рабочих минут приводит к увеличению рабочих часов и рабочих дней
        assertTime(time(1,7,50), time(1,1,10), true, true, time(3,0,0));
        // Суммирование рабочих минут приводит к увеличению рабочих часов и рабочих дней. Увеличение рабочих дней больше чем на 1
        assertTime(time(1,23,50), time(1,23,11), true, true, time(7,2,1));

        /*
        Суммирование Time (не учитывая время рабочего дня с 09:00 до 18:00)
         */

        // Суммирование каждой компоненты без влияния на остальные
        assertTime(time(210,3,50), time(15,4,9), false, true, time(225,7,59));
        // Нулевое суммирование
        assertTime(time(0,0,0), time(0,0,0), false, true, time(0,0,0));
        // Суммирование часов не приводит к увеличению рабочих дней
        assertTime(time(1,7,50), time(1,4,9), false, true, time(2,11,59));
        // Суммирование минут приводит к увеличению часов, но не приводит к увеличению дней
        assertTime(time(1,7,50), time(1,1,10), false, true, time(2,9,0));
        // Суммирование минут приводит к увеличению часов и рабочих дней
        assertTime(time(1,23,50), time(1,23,11), false, true, time(3,23,1));
    }

    @Test
    public void testTimeDiff() throws ParseException {
        /*
        Разница Time (учитывая время рабочего дня с 09:00 до 18:00). Тесты, обратные тестам из testSumTime
         */

        assertTime(time(225,7,59), time(15,4,9), true, false, time(210,3,50));
        assertTime(time(0,0,0), time(0,0,0), true, false, time(0,0,0));
        assertTime(time(3,2,59), time(1,4,9), true, false, time(1,7,50));
        assertTime(time(3,0,0), time(1,7,50), true, false, time(1,1,10));
        assertTime(time(7,2,1), time(1,23,50), true, false, time(3,5,11));

        /*
        Разница Time (не учитывая время рабочего дня с 09:00 до 18:00). Тесты, обратные тестам из testSumTime
         */

        assertTime(time(225,7,59), time(15,4,9), false, false, time(210,3,50));
        assertTime(time(0,0,0), time(0,0,0), false, false, time(0,0,0));
        assertTime(time(2,11,59), time(1,4,9), false, false, time(1,7,50));
        assertTime(time(2,9,0), time(1,1,10), false, false, time(1,7,50));
        assertTime(time(3,23,1), time(1,23,50), false, false, time(1,23,11));

    }

    public void testGenerateReport() throws Exception {
        List<String> firstRow = new ArrayList<>();
        List<String> secondRow = new ArrayList<>();

        firstRow.add("Значение 1 1");
        firstRow.add("Значение 1 2");
        firstRow.add("Значение 1 3");
        firstRow.add("Значение 1 4");

        secondRow.add("Значение 2 1");
        secondRow.add("Значение 2 2");
        secondRow.add("Значение 2 3");
        secondRow.add("Значение 2 4");
        Map<Integer, List<String>> rows = new HashMap<>();
        rows.put(0, firstRow);
        rows.put(1, secondRow);
        ReportGenerator.saveReportToLocalExcel2003("D://test.xls", rows);
    }

    private void assertDiffDate(Date startDate, Date endDate, boolean onlyWorkTime, boolean excludeHolidays, Time expectedTime) throws ParseException {
        Time actualTime = onlyWorkTime ? diffWorkDate(startDate, endDate, excludeHolidays) : diffDate(startDate, endDate);
        if(expectedTime.getDays() != actualTime.getDays() || expectedTime.getHours() != actualTime.getHours()
                || expectedTime.getMinutes() != actualTime.getMinutes()) {
            assertEquals(
                    "days: " + expectedTime.getDays() + ", hours: " + expectedTime.getHours() + ", minutes: "
                            + expectedTime.getMinutes(),
                    "days: " + actualTime.getDays() + ", hours: " + actualTime.getHours() + ", minutes: "
                            + actualTime.getMinutes()
            );
        }
    }

    private void assertTime(Time firstTime, Time secondTime, boolean onlyWorkTime, boolean isSum, Time expectedTime) throws ParseException {
        Time actualTime = isSum ? sumTime(firstTime, secondTime, onlyWorkTime) : diffTime(firstTime, secondTime, onlyWorkTime);
        if(expectedTime.getDays() != actualTime.getDays() || expectedTime.getHours() != actualTime.getHours()
                || expectedTime.getMinutes() != actualTime.getMinutes()) {
            assertEquals(
                    "days: " + expectedTime.getDays() + ", hours: " + expectedTime.getHours() + ", minutes: "
                            + expectedTime.getMinutes(),
                    "days: " + actualTime.getDays() + ", hours: " + actualTime.getHours() + ", minutes: "
                            + actualTime.getMinutes()
            );
        }
    }
}
