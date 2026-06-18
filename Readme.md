



```shell
curl -X POST http://localhost:8080/api/email/access-review-reminder \
  -H "Content-Type: application/json" \
  -d '{
    "to": "xxx@dbs.com",
    "username": "J",
    "expireDate": "2029-06-30",
    "persona": "Data Analyst(test-v5)",
    "reviewUrl": "http://xxxxxx/api/v1/review"
  }'
```


```shell
curl -X POST http://localhost:8080/api/email/send \
  -H "Content-Type: application/json" \
  -d '{
    "to": "xxx@icloud.com",
    "subject": "Welcome",
    "templatePath": "templates/welcome.html",
    "recipientName": "Alice"
  }'
```
