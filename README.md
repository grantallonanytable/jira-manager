<h2>Менеджер для работы с Jira.</h2>

<h3>Swagger</h3>

* GET `/_jira-manager/openapi`&ndash; документация по API;

<h3>REST-API</h3>

В теле сообщения (для POST) указывать два обязательных параметра:

```
{
    "userName": "<LOGIN>",
    "password": "<PASSWORD>"
}
```

* GET `/api/jira-manager/graphics?dateFrom&dateTo`  &ndash; данные для графиков;
* POST `/api/jira-manager/tasks` &ndash; список задач, тело запроса ниже. 
Логин-пароль опциональны, при отсутствии берутся из `configMap` (для k8s) или `application.conf` (k8s/standalone).

```
{
  "userName": "<LOGIN>",
  "password": "<PASSWORD>"
  "issueId": "ABC-12474",
  "dateFrom":"01-01-2001",
  "dateTo":"02-01-2001",
  "isFull":"True"
}
```

* GET `/api/jira-manager/report?issueId&isFull&dateFrom&dateTo` - сгенерировать отчет в формате Excel 2003, логин и пароль используются по умолчанию;
* POST `/api/jira-manager/updateLabels` &ndash; _(не тестировалось)_, *TO_DELETE*, в ингрессе отсутствует;
* POST `/api/jira-manager/updateDeadLine` &ndash; _(не тестировалось)_, *TO_DELETE*, в ингрессе отсутствует;
* POST `/api/jira-manager/setScrum &ndash;` &ndash; _(не тестировалось)_, *TO_DELETE*, в ингрессе отсутствует;

<h3>Запуск</h3>
<h4>Запуск локально. Standalone.</h4>
Параметры передаются при запуске.

```
java.exe -jar  /e/work/git/jira-manager/impl/target/jira-manager-impl-1.0.0-SNAPSHOT.jar GENERATE_REPORT_BY_ISSUE USER PASSWORD ABC-12474 "01-01-2010 00:00:00" "01-01-2099 00:00:00" /e/tmp/   
```                         
     
<h4>Запуск локально. Lagom.</h4>
Для удобства в конфигах указан специально порт (serviceHttpPort=2100). Остальные важные параметры также в конфиге.

```
maven|mvnw lagom:runAll
curl -X GET http://localhost:2100/api/jira-manager/report?issueId=TDS-12474 --output c:\temp\file
curl -X POST http://todo-kubernetes-test-stand-enviroment/api/jira-manager/report?issueId=ABC-12474 -d "{\"userName\":\"<LOGIN>>\",\"password\":\"<PASSWORD>>\",\"issueId\":\"ABC-12474\"}" --output e:/tmp/outpost.xls
```

<h4>При использовании в кубе.</h4>
Lagom. Параметры в конфиге и configMap.
После изменений `configMap` удалить существующую `replicaSet` приложения для того, чтобы подтянулись новые конфиги.

```
curl -X GET http://todo-kubernetes-test-stand-enviroment/api/jira-manager/report?issueId=ABC-12474 --output c:\temp\file
```

K8s.config-map, в разделе `conf`

```
jira.userName=<LOGIN>
jira.password=<PASSWORD>
```

<h3> Настройки `applicaion.conf` (аналогично можно в configMap).</h3>
<h4>Таймауты</h4>
<ul><li> Если `ReadTimeoutException` из-за долгого отвепта Jira - можно увеличить значения:

```
play.server.http.idleTimeout = 30m
play.server.akka.requestTimeout = 30m
lagom.circuit-breaker.default.call-timeout = 30m
```

Есть возможность указания аннотации ингресса: `nginx.ingress.kubernetes.io/proxy-read-timeout: 10min`.
</li><li> клиент jira настраивается так (указаны значения по умолчанию):

```
jira {
  maxResultCount = 10000
  leaseTimeout = 600000
  connectionTimeout = 5s
  socketTimeout = 20s
  requestTimeout = 90s
}
```

</li></ul>
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
jira {
     userName = "<LOGIN>"
     password = "<PASSWORD>"
     maxResultCount = 10000
   }
```


<strong>Заменить настройки репо, прокси, название проекта (группы), хост куба и стенда, хост жиры, начинающиеся с TODO</strong>
