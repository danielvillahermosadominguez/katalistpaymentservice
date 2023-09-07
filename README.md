# How to start with this service

Run the application:

```
mvn clean spring-boot:run
```

Check if the service is working

```
curl http://localhost:8080/healthcheck
```

It should return "OK!"
# Swagger (OpenAPI)

You can access to the OpenAPI in local:
```
http://localhost:8080/swagger-ui/index.html#/
```
# Doubts
- [ ] How is going to be the full use case for the user. The courses have an ID, and now it is necessary
      for the subscription. We could have the courses in the form or maybe we could call from the website with
      this Id. 
- [ ] What kind of edge paths we have in the uses cases?:
  - The user try to subscribe to a course where he is subscribed yet
    - Decision: We can return an error and show a message in the form.
  - The mail is wrong. We don't have registered users, everyone can request
    - Proposition: Sending an email with the payment, we are sure the mail exists.
  - There is a problem in on of the integrations. Should we retry?, send an email to manually correct it?.
    - Proposition: first version send an email to the support with the problem and do the process manually 
  - How are we going to control the time of the subscription and deny the access when the subscription has finalized
    - Proposition: first version manually. With a mail of support. Next versions, automatize this process 
  - How are we going to allow the cancellation of the course by the user.
    - Proposition: first version manually. With a mail of support. Next versions, automatize this process
  - How to calculate the user (extract from the mail for example) and what happen if the user exists (use numbers at the end for example)
    - Decision: use like username the email until the "@", in lowercase, and remove special characters " ! # $ % & ' * + - / = ? ^ _ ` { |" and " ( ) , : ; < > @ [ \ ]"
    - In case we have a user with the same name we will include numbers at the end
    - https://knowledge.validity.com/hc/en-us/articles/220560587-What-are-the-rules-for-email-address-syntax-#:~:text=The%20most%20commonly%20used%20special,(%2D)%20and%20plus%20sign%20(%2B).&text=These%20alternative%20special%20characters%20may,a%20sending%20or%20receiving%20server.

  
# Task related with the POC

- [x] static website with a basic form in Azure (playground). Github repository + deployment
- [x] Basic service in Java without security in the playground. Github repository + deployement
- [x] Basic Integration between website form and service
- [x] Access to Moodle and configuration for interaction with the API
- [ ] Moodle interaction - basic uses cases
- [ ] TPV and Sandbox (Paycomet) access
- [ ] TPV interaction - basic uses cases
- [ ] Access to holded
- [ ] holded interaction - basic uses cases
- [ ] Workaround. Create a new account with other email in holded to test the api
- [ ] Environments variables in HTML5. How not to harcode the url
- [ ] Enviroment variables in Service
- [ ] Refactor of the service and some test coverage after the poc
- [ ] Validation the correct input in the service. surely with tests
- [ ] Send a mail with an account. The idea it could be neccesary, at least to the edge cases
- [ ] Basic integration of in azure with Moodle. Tests
- [ ] Basic integration in azure with Holded. Test
- [ ] Basic integration in azure with TPV. Test
- [ ] Service security in the backend
- [ ] Database to persist information in the playground. Postgree? We could delay the decision with SQLite or file.
- [ ] Optional if we don't have TPV interaction - Stripe interaction. Review it in the demo
- [ ] Improve the look and feel of the basic form
- [ ] Emulation of the TPV (temporary) - Optional and to evaluate
- [ ] Documentation about the integrations.
  - [ ] Integration with Moodle and user, token, etc
  - [ ] Deployment in Azure and improvements
- [ ] Review the infrastructure and dockerization. 
- [ ] Review if the playground is the best place for it.
- [ ] Transfer repositories to codurance organization in Github
- [ ] Problem with the automatic deployment of the service. User in azure to deploy.
- [ ] Migration to AWS