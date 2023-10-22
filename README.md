# How to start with this service
## Clone the repository
```
git clone <this repository>
```
## Configure the environment
You will use to develop in local a .env file in the root of the repository. The main variables are to work on local.
```
FLYWAY_URL=jdbc:postgresql://localhost:5432/katalist?user=postgres&password=password
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/katalist
SPRING_DATASOURCE_PASSWORD=password
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_HOSTNAME=localhost
DATABASE_HOST=localhost
DATABASE_NAME=katalist
MOODLE_URL_BASE=https://<your moodle domain>.moodlecloud.com/webservice/rest/server.php?
MOODLE_TOKEN=<TOKEN FOR MOODLE>
PAYCOMET_URL_BASE=https://rest.paycomet.com
PAYCOMET_APYKEY=<api key for paycomet>
PAYCOMET_TERMINAL=<terminal for paycomet>
PAYMENTS_RETRY_ACTIVE = <True if you want the retry process active>
PAYMENTS_RETRY_INITIAL_DELAY = <Initial delay for the retry process active>
PAYMENTS_RETRY_FIXED_RATE = <delay for the retry process active>
#IPCATCHER_DEV_IP=<Your external IP - This is only for development> 
#IPCATCHER_DEV_MODE=<TRUE if you want to fix your external IP>
```
You will need to have a file ".env" with this variables in the root of the solution.

### Environment variables are in Bitwarden
You have all the data to create your .env file in Bitwaren https://bitwarden.com/.
Look for in Bitwarden the name: "[Katalyst.payment] katalistpaymentservice - environment variables"

You have here the production and development environments

### Environment variables in production
The environment variables for production are in Bitwarden https://bitwarden.com/.

To configure them manually you will need to follow the following steps:
- Go to App Services and select katalistpaymentservice
- Go to configuration-> Application settings
- Add the variable

## Configure local database
You will need to install docker.
https://docs.docker.com/desktop/install/linux-install/
Once you have installed it, you only need to write in the root folder:
```
docker-compose up -d
```
You will need a database client. We recommend to use dbeaver (https://dbeaver.io/)
To test the database you will need to create a new connection for postgree and include these data:
- Conected by : Host
- Host: localhost
- Port: 5432
- Database: katalist
- User name: postgres
- Password: password
You can see the data in the docker-compose.yml

## Run the service
Run the application:

```
mvn clean spring-boot:run
```

Check if the service is working

```
curl http://localhost:8080/healthcheck
```

It should return "OK!"
### Swagger (OpenAPI)

You can access to the OpenAPI in local:
```
http://localhost:8080/swagger-ui/index.html#/
```
# Database configuration and deploy

## Database in local docker-composer for development
You can run the database in local. This is explained in the section "How to start with this service."
## The production Database in Azure
We have created a database in production. Now it is in Azure and we have created the database here, but surely
we will have this database in AWS. Here we expain how you could create a new one (if you need):

If you need to create the database, you can follow this guide:
- Create new resource -> Select azure Database for PostgreSQL
- Select Flexible server(Recommended) -> Better cost
- resource group: katalistpayment_group
- server name: katalistdb
- postgreSQL version: 15
- Workload type: Development 
- Authentication method: PostgreSQL authentication only
- Admin username: katalist
- Password: see in Bitwarden
- Create-> Create server without firewall rules

- When you have the database you will need to configure the networking. Go to the database and choose
the Networking in the menu.
- Public access
- Add current client IP address 0.0.0.0 - 255.255.255.255
And save. 

Go to Databases and create a new one: the name is katalist. 

You can try to connect with your database client
You can see how to connect in the section "Connect". Remember the password is in Bitwarden.
## How to connect to the production database
You will need a database client. We recommend to use dbeaver (https://dbeaver.io/)
To test the database you will need to create a new connection for postgree and include these data:
- Conected by : Host
- Host: katalistdb.postgres.database.azure.com
- Port: 5432
- Database: katalist
- User name: katalist
- Password: <see it in Bitwarden>

The data is in Bitwarden:"[Katalyst.payment] katalistpaymentservice - environment variables"

We recommend to configure the type of connection to avoid mistakes:
- Edit connection->General->Type of connection: Production
You will see in dbeaver this connection in red color.

# Create manually the docker image

Our deployment follow the following steps:
- clone the repository, compile and test the project
- create a docker image and deploy it to the container register in azure
- the app service get the image when it restarts

This section explain how you could create the docker image in local.

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
# Integration with Moodle
## Access to Moodle
We are using moodle cloud and the production account is: https://codurance.moodlecloud.com/
For development purpose we need a trial account. The trial account in Moodle are about 45 days
of duration. Surely you will need to create one account and configure it.

The current development account is: https://exampleforcodurance2.moodlecloud.com/

The information to access to the accounts (production and development) are in bitwarden.
You can find this information in:"[Katalyst.payment] Moodle keys, password, mails"

In addition, you have some enviroments variables related to Moodle in your ".env", so you
should make a look to the section "How to start with this service".

## Configuration of Moodle

## Some considerations for Moodle integration

# Integration with Holded
## Access to Holded
The production account is: https://www.holded.com/
For development purpose we need a trial account. The trial account in Moodle are about 15 days
of duration.

To renew the time we have a contact in Holded who help us to reset the trial. This contact email is 
in Bitwarden.

The current development account is https://www.holded.com/ too. And the difference is the 
user we are using.

You have all the information in: "[Katalyst.payment] Moodle keys, password, mails"

## Configuration of Holded


## Some considerations for Holded integration
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

# Integration with Paycomet
## Access to Paycomet
We are using Paycomet with the Sabadell bank. We can login here:
https://dashboard.paycomet.com/cp_control/bsabadell-login

All the information to connect is in Bitwarden: "[Katalyst.payment] Paycomet keys, password, mails" 
You will need multifactor to connect.

You will have the production account and the Sandbox account. You will use a different
email and password to login. For example if you email to connect to production is
"daniel.villahermosa@codurance.es" you email to connect to the sandbox could be
"daniel.villahermosa_sandbox_4pwxs32e@paycomet.com"

## Support

We have support from Paycomet and from Banco Sabadell. We can ask question both of
them. 

- Banco Sabadell: send emails to "TPVVirtual, SS.CC." <TPVVirtual@bancsabadell.com>" with 
writing in the email what is our commercial number: 354180143
- Paycomet support: In paycommet you can create support tickets and ask questions.

## Configuration of Paycomet
## Some considerations for Paycomet integration

# CI/CD with Github actions
# The pipeline
# Secrets and variables

# Azure
We recommend to review the plans we are using in Azure if you want to use as "Production".
In case to migrate to AWS (one of the objectives), you should study the best optimization
for the infrastructure.

We haven't take into account any optimization but we explain in this section some
considerations.

## Azure playground
We have the infrastructure in the Azure playground. If you need to acces to the portal 
you will need to ask access in the slack channel of Azure to "Lee Anderson"

Our infrastructure is in the resource group: katalistpayment_group
Here we have the following elements:
- Katalist - container register
- Katalistdb - Azure Database for PostgreSQL flexible server
- katalistpayment - Static Web App
- katalistpaymentservice - App Service
- workspacekatalistpaymentgroup - Log Analytics workspace (Do we need it?)

## Katalist - container register
Here we have the last docker image. The github create the docker image and
it is deployed in this repository.
The url is: https://katalist.azurecr.io

You can access to the password and username in "Access keys" in the resource.
You can access to the repository in "Repositories"

We are using a Standard pricing plan for this resource:
https://azure.microsoft.com/en-us/pricing/details/container-registry/#pricing

Were we have at least 100G to store our docker images. We could study to use a Basic
pricing plan but we should remove the old docker images because we have a limit of
10G.
## Katalistdb - Azure Database for PostgreSQL flexible server
We need a postgree database. We are using  an Azure Database of PostgreeSQL flexible
server which is the recommendation from Microsoft.

Our configuyration is the standard B1 (minimum configuration):
- 1 virtual core
- 2 Gb of RAM

The storage size is 32 GiB (minimum)

And it is not enable High availability.

The estimated costs are 13.06 GBP per month

https://azure.microsoft.com/en-us/pricing/details/postgresql/flexible-server/

## katalistpayment - Static Web App
We are using an static web app for the front. It is the recomendation of microsft when you want to
deploy html application in a storage.

The hosting plan is : free

https://azure.microsoft.com/en-us/pricing/details/app-service/static/

The objective of this plan is for hobbies/personal projects and we have certain limitations in storage
But for general purpose production app we should use the Standard plan with 9GBP pero month.

## App Service
For the service we are using an APP service where we have all the environment variables in Configuration.
In "Deployment center" you can see the configuration:
- Source: Container register (see the section Katalist - container register)
- Container type : Single container. 
- Autentication: Admin creditials
- Registry: katalist
- Image: katalist
- Tag: latest

The operative system is linux and we don't have redundance zone.

the way to deploy is to restart the service after deploying the new image in the container register.
This Web app is related to the App Service plan.

### The Katalyst service Plan : "ASP-katalistpaymentgroup-839e"
We are using a pricing plan B1. Location in East US.
The B1 is the most basic feature and have a cost. The Free option didn't work very well and had
a limitation of 60 minutes per day. Usually not enought for development.

The cost is 0,028GBP per instance (we have 1 instance) 10,081 per month per instance.

The hardware is the minimum:
- 1 virtual CPU
- 1.75 GB of memory
- Remote storage 10G
With this hardware, the deployments are very slow (5-10 minutes)

https://azure.microsoft.com/en-us/pricing/details/app-service/windows/?ef_id=_k_CjwKCAjwkNOpBhBEEiwAb3MvvaZPp0XJevSpR2F5aeZApmEr_8oF_o8CZ4GIRAjmuW5p-Jx2MAl3xhoC-XQQAvD_BwE_k_&OCID=AIDcmm68ejnsa0_SEM__k_CjwKCAjwkNOpBhBEEiwAb3MvvaZPp0XJevSpR2F5aeZApmEr_8oF_o8CZ4GIRAjmuW5p-Jx2MAl3xhoC-XQQAvD_BwE_k_&gclid=CjwKCAjwkNOpBhBEEiwAb3MvvaZPp0XJevSpR2F5aeZApmEr_8oF_o8CZ4GIRAjmuW5p-Jx2MAl3xhoC-XQQAvD_BwE#pricing

For a B1, the price is 0.094/hour in $
### Logs
The service has some configured logs. In "Logs" you can see the "AppServiceConsoleLogs" from spring.

If you create a new app service you will need to configure it in "Diagnostic settings" creating a new
"diagnostic settings" (We have created AppPlatformLogForKatalyst) with the following:
- Logs: App Service console logs and App service application logs
- Subscription: Codurance Playground(Pay as you go)
- Log analystics workspace (Default workspace....)

Also, the App service logs are enabled: "File system", so you can see the output in the "Log stream" too.

## Some considerations for the migration to AWS


