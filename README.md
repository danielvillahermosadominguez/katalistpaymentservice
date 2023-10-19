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
MOODLE_URL_BASE=https://<your moodle domain>.moodlecloud.com/webservice/rest/server.php?
MOODLE_TOKEN=<TOKEN FOR MOODLE>
## Environemnt variables in production
The environment variables to production are in Bitlocker.
To configurate them manually you will need to follow the following steps:
- Go to App Services and select katalistpaymentservice
- Go to configuration-> Application settings
- Add the variable

NOTE: Update this information for a container

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
# Integration with Moodle - Considerations

# Integration with Holded - Considerations
In Holded we need to follow the following steps:
- We receive the following data :
  - name
  - surname
  - email 
  - NIF/CIF 
  - isCIF (pending) :
  - Address (pending) :
- The process look for if the contact exists with the custom_id = <NIF/CIF><email> encoding in utf8 
- if the contact doesn't exist we create a new contact with this data
- we create an invoice
- we send the invoice
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
# Restrictions and Rules
- [12.09.2023 ] We don't override data in Moodle or in Holded. 
- [12.09.2023 ] The quantity is initially 1. One per user. We will evolve by the time
- [21.09.2023 ] Respositories must be privates. Ask to Jose H, these repositories.
- [21.09.2023 ] MVP - First productive version. We think we could use 7-10 natural days to finish the process and payment (Basic).
In parallel we could see the the review of the messages and text, the review of the UI, and translations
Out of this we will need to do the migration to AWS and migration of reposistories with the help of a Platform engineer
In parallel we will need to activation of the TPV with Paycomet.
Tasks:
  - Basic - Paycomet integration - finish the integration, including configuration of Paycomet and database.
  - Basic - Save in holded new fields and change the behaviour of company/CIF or person/NIF - in the form
  - Basic - Country and holdem. See the requirement
  - Basic - Validation of NIF and CIF at least in a form level
  - Basic - Logs and repeat in case of error
  - Basic - Connect and customize Moodle pro
  - Basic - Connect and customize Holded pro
  - Basic - Connect and customize Paycomet pro
  - Tests - Test in real environment with PO
  - Documentation - Secrets, bitlocker and environment variables, documentation of integrations, etc
  - Infrastructure - AWS migration and migration of repos. 
  - Other people: review UI and translations.
  - Activation of TPV with Paycomet and the bank
  - Optional: IP request, deployment in azure and create a backlog with user stories
NOTE: see comments in PaymentCometApiClientAdapter->payment to continue.
- [5.10.2023 ]  We started again with the objective to do a demo and a handover the next 24/10/2023

# Task related with the POC
- [In progress ] Remove gson as a dependency and use object mapper for serialization
- [ ] Manual tests - description of regression tests
- [ ] Course with Zero price => we should allow to the user subscribe
- [ ] Web: refactor paths in the tests
- [ ] Web: refactor of code js - to many ifs for fields
- [ ] Connection holded PRO
- [ ] Connection moodle PRO + configuration of security
- [ ] Deploy the bundle and avoid to use embebed library i18n
- [ ] Validation of the format for NIF and CIF in web page
- [ ] Review if we need all the countries that are shown in the selector in the web page
- [ ] Confirmation page - Codurance confirmation and KO page
- [ ] Messages in front for compliance with Paycomet
- [ ] Review of the separation of classes between layers
- [ ] Business Rules taxes, account, etc. See bussiness rules in miro
- [ ] Check the IP confirmation from Paycomet servers
- [ ] CSS styles in the payment web page
- [ ] Mail and format in the website. Message in spanish
- [ ] Send a email in case of error with the information (or monitoring, think about it). Surely we need to process it manually and correct the bug. Se cases in the test cases
- [ ] Send a mail with an account. The idea it could be neccesary, at least to the edge cases
- [ ] Azure Web App, delay when start after a long time. Something related to the configuration surely.
- [ ] Default data in creation of contact
- [ ] Problem with the automatic deployment of the service. User in azure to deploy.
- [ ] Private Data Policy in the form - People of Paycomet will help us to cover it
- [ ] Repository migration to Codurance organization
- [ ] Service security in the backend
- [ ] Documentation about the integrations.
  - [ ] Integration with Moodle and user, token, etc
      - Some importants topics:
        - Creation of contacts and trial. Point of contact in holded
        - Configuration in Moodle
        - Paycomet procedures
        - Paycomet and configuration the router for the IP and configuration of the IP to develop
        - Languages in holded and strange behaviours
  - [ ] Decisions in Holded integration + configuration
  - [ ] Deployment in Azure and improvements
  - [ ] Secrets in vaults and Bitlocker
  - [ ] The solution and structure - IDD
- [ ] Review the infrastructure and dockerization - use a docker container for the service
- [ ] Transfer repositories to codurance organization in Github
- [ ] Migration to AWS
- [x] BDD and retry for learning and financial
  - [x] Resend in case of error + to be sure the mail has been sent (invoice)
- [x] Test the main changes of the refactor (split the two calls from paycomet) in Azure.
- [x] IP, get the ip from the request and prepare for development with a fixed IP
- [x] Builders o fixtures for the tests. Equals when it is necessary.
- [x] Handover approach - think about it
- [x] BDD and edge cases: error cases
- [x] Sometimes the pipeline fail in bdd but github doesn't detect the fail. strange.
- [x] Errors from Payment review and include the CODE ERROR form backend ERROR_PAYMENT_PLATFORM_CANNOT_TO_PROCESS_THIS_CREDIT_CARD
- [x] WireMock - clean the code and avoid complexity
- [x] Logs and traceability
- [x] Paycomet adapter -> test and refactor
- [x] Integration test for getUserByUserName
- [x] BDD with database - it should be good to have it.
- [x] New data in the forms: address, phone
- [x] TPV interaction - basic uses cases
- [x] In the form, we should show name and surname if it is NIF, and company name if it is CIF
- [x] Create in holded person and company depending on the form. Include a checkbox "Company"
- [x]  Uppercase in values
- [x] To use a hash for the key in holded
- [x] Environments variables in HTML5. How not to harcode the url
- [x] Validation the correct input in the service. surely with tests
- [x] Restrict connection with the service, only for the static form (cookies?) or with infrastructure
- [x] Evaluate to use MongoDB - Deprecated. Create an ADR for the PostgreeSQL decision
- [x] TPV and Sandbox (Paycomet) access
- [x] Emulation of the TPV (temporary) - Optional and to evaluate
- [x] Basic integration in azure with TPV. Test
- [x] Improve the look and feel of the basic form - use codurance styles
- [x] Review the format of the invoice - review with product owner
- [x] Review the error messages
- [x] Form must have localization
- [x] Refactor of the service and some test coverage after the poc
- [x] static website with a basic form in Azure (playground). Github repository + deployment
- [x] Basic service in Java without security in the playground. Github repository + deployement
- [x] Basic Integration between website form and service
- [x] Access to Moodle and configuration for interaction with the API
- [x] Moodle interaction - basic uses cases
- [x] Access to holded
- [x] holded interaction - basic uses cases
- [x] Workaround. Create a new account with other email in holded to test the api
- [x] Enviroment variables in Service
- [x] Basic integration of in azure with Moodle. Tests
- [x] Basic integration in azure with Holded. Test
- [x] Database to persist information in the playground. Postgree? We could delay the decision with SQLite or file.
- [x] Review if the playground is the best place for it.-> Final in AWS
- [x] Form with token and remove the courseID (this could be a parameter)

# Detected BUGS
