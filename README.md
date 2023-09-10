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
# Environment variables
You will use to develop in local a .env file in the root of the repository. The main variables are to work on local.

FLYWAY_URL=jdbc:postgresql://localhost:5432/katalist?user=postgres&password=password
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/katalist
SPRING_DATASOURCE_PASSWORD=password
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_HOSTNAME=localhost
DATABASE_HOST=localhost
DATABASE_NAME=katalist
## Environemnt variables in production
The environment variables to production are in Bitlocker.
To configurate them manually you will need to follow the following steps:
- Go to App Services and select katalistpaymentservice
- Go to configuration
- 

# Database configuration and deploy

## Run the database in local docker-composer for development

Interesting commands:

```
docker-compose up -d
```
## The production Database in Azure
We have created a database in production. Now it is in Azure and we have created the database here.

If you need to create the database, you can follow this guide:
- Create new resource -> Select azure Database for PostgreSQL
- Select Flexible server(Recommended) -> Better cost
- resource group: katalistpayment_group
- server name: katalistdb
- postgreSQL version: 15
- Workload type: Development 
- Authentication method: PostgreSQL authentication only
- Admin username: katalist
- Password: see in bitlocker
- Create-> Create server without firewall rules

When you have the database you will need to configurate the networking. Go to the database and choose
the Networking in the menu.
- Public access
- Add current client IP address 0.0.0.0 - 255.255.255.255
And save. 

Go to Databases and create a new one: the name is katalist. 

You can try to connect with your database client
You can see how to connect in the section "Connect". Remember the password is in Bitlocker.

# Create manually the docker image
The docker image is based on the Dockerfile in the root of the repository.
To create this step by step in local you should:
- You need to have a database in local. Based on "docker-composer.yml"
```
docker-compose up -d
```
- Create the package in the target folder
```j
mvn clean -e -B package
```
- build the docker image
```
docker build . -t katalist
docker image list
C:\workspace\bench\katalistpaymentservice>docker image list
REPOSITORY   TAG       IMAGE ID       CREATED         SIZE
katalist     latest    df26454a4829   7 seconds ago   379MB
postgres     15.4      69e765e8cdbe   2 days ago      412MB
```
- To test it, you should execute
```java
docker run -p 8080:8080 --net=host -it df26454a4829 # if you want to run only
# Notice that --net=host is to access to local host with the 127.0.0.1 -> local database
```

# Doubts
- [ ] How is going to be the full use case for the user. The courses have an ID, and now it is necessary
      for the subscription. We could have the courses in the form or maybe we could call from the website with
      this Id. 
- [ ] Problems of security because we don't have user autentication. Some ideas:
  - inject a token to the form
  - Using a cookie with the URL to call
  - block access to the endpoint in the infrastructure.
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
  - Prize of courses. Where is the source of information. Options
    - Database and manually fill this information?
    - Moodle. I haven't found any way to find it
    - The same prize for every course. Hardcode, constant

  
# Task related with the POC

- [x] static website with a basic form in Azure (playground). Github repository + deployment
- [x] Basic service in Java without security in the playground. Github repository + deployement
- [x] Basic Integration between website form and service
- [x] Access to Moodle and configuration for interaction with the API
- [x] Moodle interaction - basic uses cases
- [ ] TPV and Sandbox (Paycomet) access
- [ ] TPV interaction - basic uses cases
- [x] Access to holded
- [x] holded interaction - basic uses cases
- [ ] Workaround. Create a new account with other email in holded to test the api
- [ ] Environments variables in HTML5. How not to harcode the url
- [ ] Enviroment variables in Service
- [ ] Refactor of the service and some test coverage after the poc
- [ ] Validation the correct input in the service. surely with tests
- [ ] Send a mail with an account. The idea it could be neccesary, at least to the edge cases
- [x] Basic integration of in azure with Moodle. Tests
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
- [ ] Form with token and remove the courseID (this could be a parameter)
- [ ] Restrict connection with the service, only for the static form (cookies?)

# Detected BUGS
- [ ] When an email contains "+" character. For example daniel.villahermosa+user@codurance.com. We receive an error with the following data:
  - Course ID : 9
  - Email: daniel.villahermosa+user@codurance.com
  - Name: Daniel
  - Surname: Garcia
  - Company Name: Su empresa
  - DNI/CIF: 339939393F
  - Only invoice with Holded