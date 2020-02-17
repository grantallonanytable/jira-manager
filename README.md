<h2>Менеджер для работы с Jira.</h2>
```
  Данная ветка реализует сервис с помощью Spring Boot, не мержить в основную ветку!
```

<h3>Swagger</h3>
* GET /_jira-manager/openapi

<h3>Запуск</h3>

Пока что запускается такими способами:
* из Idea метод Main.main;
* из идеи через плагин `spring-boot:repackage` -> `spring-boot:run`.

<h4>TODO:</h4>
* gitlab-ci;
* запуск локально через `java.exe -jar ****`.

<h3> Настройки `applicaion.conf`.</h3>

<h4>Календарь праздничных и рабочих дней</h4>

```
calendar {
  notWorkDaysList = [
    "01-01-2018",
    "02-01-2018"
  ]
  workHolidaysList = [
    "28-04-2018"
  ]
}
```
Логин и пароль для подключения к жире (переопределить параметром при запуске или в configMap)

```
engine {
     userName = "<LOGIN>"
     password = "<PASSWORD>"
   }
```


<strong>Заменить настройки репо, прокси, название проекта (группы), хост куба и стенда, хост жиры, начинающиеся с TODO</strong>
