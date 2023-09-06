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

# Doubts
- [ ] How is going to be the full use case for the user. The courses have an ID, and now it is necessary
      for the subscription. We could have the courses in the form or maybe we could call from the website with
      this Id. 
- [ ] What kind of edge paths we have in the uses cases?:
  - The user try to subscribe to a course where he is subscribed yet
  - The mail is wrong. We don't have registered users, everyone can request
  - There is a problem in on of the integrations. Should we retry?, send an email to manually correct it?.
  
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