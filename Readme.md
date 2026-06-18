



```shell
curl -X POST http://localhost:8080/api/email/access-review-reminder \
  -H "Content-Type: application/json" \
  -d '{
    "to": "xxx@hotmail.com",
    "username": "J",
    "expireDate": "2028-06-30",
    "persona": "Data Analyst"
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
