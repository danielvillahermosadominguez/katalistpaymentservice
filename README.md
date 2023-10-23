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

## Creation of a new account
If you need to create a new account you will need to go to
https://moodlecloud.com/app/en/login
You will need to click in "Try for free" where you will have a 45 days account.

You could create a new account, but you can renew the trial choosen the same 
moodlecloud site dame. In our case is "exampleforcodurance". You can use the password for admin
which you can see in Bitwarden:"[Katalyst.payment] Moodle keys, password, mails"

you can choose a new name, for example "exampleforcodurance2" for the site name.

## Configuration of Moodle
You will need to configure Moodle to use the API. 
1. Create a new user katalyst which it will have rights to have the capability to create
   courses, enrole users, etc. Site administration->users->Add new user
   * username: katalist 
   * password: <see it in bitwarden>
   * an email, for example: daniel.villahermosa+katalist@codurance.com
2. Create a test course: Courses->Add a new course:
   * Course full name: TDD in depth
   * Course short name: TDD in depth
   * Course ID number: 9 (we use it for testing)
3. Create a custom field for a price. Site administration-> courses-> course custom fields-> Add a new category
   * change the name of the new category to "Purchase fields"
   * Click on: add a new custom field -> short text:
     * name: price
     * short name: price
     * default value: 0
4. Put a price to the TDD in depth course: My courses-> TDD in depth-> settings
   * in the group "Purchase fields" write a price, for example: 60.55
5. Create a new external service: Site administration->Server->Web services-> external services
   * Add a new "Custom services" name = katalyst and shortname = katalyst
   * Click -> Enabled = true
   * Click -> authorized users only
   * click on "add service" -> add functions
   * An now we are going to add all the functions we are using in the api. They are:
     * core_enrol_get_enrolled_users
     * core_user_get_users_by_field
     * core_user_create_users
     * enrol_manual_enrol_users
     * core_course_get_courses 
   * If you have added more functions to the MoodleAdapter, you will need to add them too.
   * When you finish to select, click on add functions.
   * You will see the functions, description and required capabilities. The capabilies are important
     to take into account for the step 6
For example:
```
* core_enrol_get_enrolled_users needs: moodle/course:view, moodle/course:update, moodle/course:viewhiddencourses
* core_user_get_users_by_field needs: moodle/user:viewdetails, moodle/user:viewhiddendetails, moodle/course:useremail, moodle/user:update, moodle/site:accessallgroups
* core_user_create_users nees: moodle/user:create
* enrol_manual_enrol_users: moodle/user:viewdetails, moodle/user:viewhiddendetails, moodle/course:useremail, moodle/user:update
* core_course_get_courses needs: enrol/manual:enrol
```
7. Assign the katalist user to the new service. In Site administration->Server->Web services-> External services
   * Go to External service -> katalyst and click on "Authorized users"
   * Asign the katalist user
8. Assign right to the katalist user. Site administration->Users->Permissions
   * In site administrators, include katalist from potential users to main administrators
   * Go to define roles and "add a new role". We are going to create a role with all the permissions
   * User role archetype: Manager
   * Continue
   * shortname: katalyst
   * custom full name: full control role (KATALYST API access)
   * Context type where this role may be assigned: System, Category, and course (values by default)
   * Allow role assigments: by default
   * Allow role overrides:by default
   * Allow role switches: by default
   * Allow role to view: by default
   * click on "show advanced" and click "allow" in all the capabilities of:
     * To user the REST API:
       * webservice/rest:use
       * moodle/site:viewparticipants 
       * moodle/site:viewuseridentity
       * moodle/course:managegroups
       * moodle/course:view
       * moodle/course:viewparticipants
       * moodle/user:viewdetails
       * moodle/user:viewhiddendetails
       * moodle/course:useremail
       * moodle/course:viewhiddencourses
       * moodle/site:accessallgroups
       * moodle/course:update
       * moodle/user:update
     * to use: core_enrol_get_enrolled_users needs: 
       * moodle/course:view, 
       * moodle/course:update, 
       * moodle/course:viewhiddencourses
     * to use core_user_get_users_by_field needs: 
       * moodle/user:viewdetails, 
       * moodle/user:viewhiddendetails, 
       * moodle/course:useremail, 
       * moodle/user:update, 
       * moodle/site:accessallgroups
     * to use core_user_create_users needs: moodle/user:create
     * to enrol_manual_enrol_users needs: 
       * moodle/user:viewdetails, 
       * moodle/user:viewhiddendetails, 
       * moodle/course:useremail, 
       * moodle/user:update
     * to core_course_get_courses needs: enrol/manual:enrol
   * Click on "Create this role"
9. Assign this role to the user: katalist
   * Go to Site administration->Users-> permissions-> assign system roles
   * Choose: full control role (KATALYST API access)
   * select katalist in potential users and "Add"

10. Activate the REST protocol. Site administration->Server->web services->Manage protocols-> Enable REST protocol
11. Enable web services: Site administration->Advanced features: enable web services.
12. Create a token: click on "create token":
    * token: KATALYST
    * User: katalist
    * Service: katalyst
    * IP restriction: empty
    * valid until: (not enabled = forever)
    * Click on "saves changes"

This token should be stored in bitwarden : "[Katalyst.payment] Moodle keys, password, mails"


Some useful link:
https://help.feedbackfruits.com/en/articles/4392969-configuring-the-api-for-moodle
In Site administration->Server-> web services->overview you have a set of steps fo enable the access.It could
be useful.

```
NOTE: We should review the permissions we need and assign only them. In the firsts versions we assign
all the permissions to this user.
```

To be sure every call to the api is going to work, try to test it with for example Postman:https://www.postman.com/
### Testing the api service of moodle
If the token is 06280b0c477e3fc6921a0e0066da2761

If we use postman for it, we could do the following tests

#### core_course_get_courses
```
   protocol: POST
   https://exampleforcodurance2.moodlecloud.com/webservice/rest/server.php?wstoken=06280b0c477e3fc6921a0e0066da2761&wsfunction=core_course_get_courses&moodlewsrestformat=json
   Body: x-www-form-unlencoded
   Key = options[ids][0]
   Value = 9
```

```json
[
    {
        "id": 9,
        "shortname": "TDD in depth",
        "categoryid": 1,
        "categorysortorder": 10001,
        "fullname": "TDD in depth",
        "displayname": "TDD in depth",
        "idnumber": "",
        "summary": "",
        "summaryformat": 1,
        "format": "topics",
        "showgrades": 1,
        "newsitems": 5,
        "startdate": 1698012000,
        "enddate": 1729548000,
        "numsections": 4,
        "maxbytes": 2097152,
        "showreports": 0,
        "visible": 1,
        "hiddensections": 1,
        "groupmode": 0,
        "groupmodeforce": 0,
        "defaultgroupingid": 0,
        "timecreated": 1697981114,
        "timemodified": 1697981114,
        "enablecompletion": 1,
        "completionnotify": 0,
        "lang": "",
        "forcetheme": "",
        "courseformatoptions": [
            {
                "name": "hiddensections",
                "value": 1
            },
            {
                "name": "coursedisplay",
                "value": 0
            }
        ],
        "showactivitydates": true,
        "showcompletionconditions": true,
        "customfields": [
            {
                "name": "price",
                "shortname": "price",
                "type": "text",
                "valueraw": "60.55",
                "value": "60.55"
            }
        ]
    }
]

```

#### core_enrol_get_enrolled_users
```
   protocol: POST
   https://exampleforcodurance2.moodlecloud.com/webservice/rest/server.php?wstoken=06280b0c477e3fc6921a0e0066da2761&wsfunction=core_enrol_get_enrolled_users&moodlewsrestformat=json
   Body: x-www-form-unlencoded
   Key = courseid
   Value = 9
```

```json
[
  {
    "id": 2,
    "username": "admin",
    "firstname": "Daniel",
    "lastname": "Villahermosa",
    "fullname": "Daniel Villahermosa",
    "email": "daniel.villahermosa+moodle@codurance.com",
    "department": "",
    "firstaccess": 1697980675,
    "lastaccess": 1697989005,
    "lastcourseaccess": 1697989023,
    "description": "",
    "descriptionformat": 1,
    "country": "ES",
    "profileimageurlsmall": "https://secure.gravatar.com/avatar/05d73ca8d7409e9e02731aee37cb45d6?s=35&d=mm",
    "profileimageurl": "https://secure.gravatar.com/avatar/05d73ca8d7409e9e02731aee37cb45d6?s=100&d=mm",
    "roles": [
      {
        "roleid": 3,
        "name": "",
        "shortname": "editingteacher",
        "sortorder": 0
      }
    ],
    "enrolledcourses": [
      {
        "id": 9,
        "fullname": "TDD in depth",
        "shortname": "TDD in depth"
      },
      {
        "id": 8,
        "fullname": "Starting with Moodle",
        "shortname": "Starting with Moodle"
      }
    ]
  }
]
```
#### core_user_get_users_by_field
```
   protocol: POST
   https://exampleforcodurance2.moodlecloud.com/webservice/rest/server.php?wstoken=06280b0c477e3fc6921a0e0066da2761&wsfunction=core_user_get_users_by_field&moodlewsrestformat=json
   Body: x-www-form-unlencoded
   Key1 = field
   Value1 = username
   Key2 = values[0]
   Value2 = admin
```

#### core_course_get_courses
```
   protocol: POST
   https://exampleforcodurance2.moodlecloud.com/webservice/rest/server.php?wstoken=06280b0c477e3fc6921a0e0066da2761&wsfunction=core_course_get_courses&moodlewsrestformat=json
   Body: x-www-form-unlencoded
   Key = options[ids][0]
   Value = 9   
```

#### core_user_create_users
```
   protocol: POST
   https://exampleforcodurance2.moodlecloud.com/webservice/rest/server.php?wstoken=06280b0c477e3fc6921a0e0066da2761&wsfunction=core_user_create_users&moodlewsrestformat=json
   Body: x-www-form-unlencoded
   Key1 = users[0][username]
   Value1 = prueba   
   
   Key2 = users[0][createpassword]   
   Value2 = 1
   
   Key3 = users[0][email]
   Value3 = daniel.villahermosa+student@codurance.com
   
   Key4 = users[0][firstname]
   Value4 = Ramon
   
   Key5 = users[0][lastname]
   Value5 = Garcia   
```
```json
[
    {
        "id": 5,
        "username": "prueba"
    }
]
```

#### enrol_manual_enrol_users
```
   protocol: POST
   https://exampleforcodurance2.moodlecloud.com/webservice/rest/server.php?wstoken=06280b0c477e3fc6921a0e0066da2761&wsfunction=enrol_manual_enrol_users&moodlewsrestformat=json
   Body: x-www-form-unlencoded
   Key1 = enrolments[0][roleid]
   Value1 = 5   
   
   Key2 = enrolments[0][userid] 
   Value2 = id for the created user
   
   Key3 = enrolments[0][courseid]
   Value3 = 9
         
```
```json
   null
```

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
To use Holded you will need to create an API-key.
Go to Your profile -> Settings -> More -> Developers -> New Api Key

You will create an api key.

This api key must be stored in Bitwardem in "[Katalyst.payment] Moodle keys, password, mails".

## Some considerations for Holded integration
In holded we are using Contacts and invoices. The invoices are in Sales -> Invoices.

To identify the contact, from the point of view of the Service, is using the CustomId. This is
calculated applying a SHA-256 to the [CIF/NIF]+[email]. 

Te custom-id is stored in: Contact->Edit contact -> Preferences->Reference.

Be careful with the field country. When you use the API you will need to fill both fields: country and country
code. In the code there are some explainations about this strange behaviour of the API.

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
In Configure Produce you can edit the TPV produce. This is valid for sandbox and real accounts.

You can here to change:
- Name of product
- Commercial name of the product

In this window you will have information you need for the payment as for example:
- Password
- Terminal number
- client code
- JET ID => you will use in the front to integrate with Paycomet

In addition, here you can change other interesting things:

- Type of notification. To receive the notification in the service you will need include here the url to notify.
For example: https://katalistpaymentservice.azurewebsites.net//confirmation
Take into account that if you are testing in local, you can receive this notification but you will need to 
establish here you external IP, to have the service running and also, to map your router port to your computer
If you are using a movistar router this information could be useful:
  https://www.movistar.es/blog/router/abrir-puertos-router-movistar/
- URL OK: your ok html page. By default Paycomet has www.paycomet.com/url-bs-ok
- URL KO: your ok html page. By default Paycomet has www.paycomet.com/url-bs-ko

In addition, if you are using for the front integration IFRAME you could configure some of the styles.

To connect with the API, you will need to create the API key. It is in "my products-> API keys"

You only need to click on "create new api key". Remember you will need to store this api key in Bitwarden: "[Katalyst.payment] Paycomet keys, password, mails"


## Operations historical
To see your operations you will need to click : Operation Historical-> Search in history:
* click on select all my products
* click on www.codurace.com (name of product)
* Search

You will se here all the operations and the state and other relevant data. Remember:
- Fail if it has been cancelled or failed
- Pending if it has been only authoriced but not confirmed by the user
- Accepted if it has been confirmed by the user

## Support

In Support you can create tikets you can use "Support-> Creation of tiket"

## Some considerations with Paycomet
* Remember how is the workflow.
```json

  FRONT                         PAYCOMET                     BACKEND
    │                              │                            │
    │                              │                            │
    │    ask token with jetid      │                            │
    ├─────────────────────────────►│                            │
    │                              │                            │
    │           token              │                            │
    │◄─────────────────────────────┤                            │
    │                              │                            │
    │   subscribe(data, token..)   │                            │
    ├──────────────────────────────┼───────────────────────────►│
    │                              │                            │
    │                              │  authorize payment with    │
    │                              ◄────────────────────────────┤
    │                              │   token, passord, order... │
    │                              │                            │
    │                              │ok and urlchallenge         │
    │                              ├───────────────────────────►│
    │   url challenge              │                            │
    ◄──────────────────────────────┼────────────────────────────┤
    │     ok or cancel             │                            │
    ├─────────────────────────────►│  notification ok or cancel │
    │                              └───────────────────────────┐►
                                                               └┘
```
* The IP is something which is important, the External IP of the client, because paycomet use it
  to authorize the payment and other parameters (password, order, etc)
* We calculate the order code for each payment. Currently, the code is "PAYyyyyMMddHHmmssSSS" where:
  * yyyy = year, for example 2023
  * MM = month, for example 12 (December)
  * dd = day of the month, for example 3
  * HH = hour, for example 15
  * mm = minute, for example 55
  * ss = seconds, for example 12
  * SSS = milliseconds, for example 345
* To test the payment, you will need to use https://docs.paycomet.com/en/recursos/testcards

# CI/CD with Github actions
# The pipeline
The pipeline:
- Get the code
- Prepare a local database with docker
- compile, test and create the artifact with maven
- create the docker file
- deploy the docker file in the azure container register
- remove the database
- Restart the app service (To be developed)
# Secrets and variables
To do these things the pipeline has several secrets. They are in Settings-> Secrets and variables-> Actions

* ACR_ENDPOINT => container register end point (you can see it in the Azure container service)
* ACR_PASSWORD => container register password (you can see it in the Azure container service)
* ACR_USERNAME => container register username (you can see it in the Azure container service)

# Automatic deployment

For the deployment, you will need right to assign a user to the App service. Currently, I don't have
rights with my account.

You should login in Azure:

```
az login
```

And create 

```
az ad sp create-for-rbac --name "katalystcicd" --role contributor --scopes /subscriptions/edb907a0-34b8-47cc-a65b-9ae69c0b6398/resourceGroups/katalistpayment_group  --sdk-auth
```
and you should copy the json which should be result. An no real example could be:
```json
{
  "clientId": "efbfad19-6742-441c-9e9e-8803b0ab8397",
  "clientSecret": "fOA8Q~Uacpm6fNMAkOoWhvloYzCH_dC5EMWjBidN",
  "subscriptionId": "5f983a34-8f24-40dc-338e-35d5a138adf3",
  "tenantId": "e68bd738-9e3b-4e35-b5b3-337b38db205c",
  "activeDirectoryEndpointUrl": "https://login.microsoftonline.com",
  "resourceManagerEndpointUrl": "https://management.azure.com/",
  "activeDirectoryGraphResourceId": "https://graph.windows.net/",
  "sqlManagementEndpointUrl": "https://management.core.windows.net:8443/",
  "galleryEndpointUrl": "https://gallery.azure.com/",
  "managementEndpointUrl": "https://management.core.windows.net/"
}
```
You will need to save this and put in a secret in Github "AZ_CREDENTIALS"
In addition, you will need to include "AZURE_APP_SERVICE_NAME" which will be the name of the App service:katalistpaymentservice

```json
``` yaml
  deploy:
    runs-on: ubuntu-latest
    needs:  test-and-build    
    permissions:
      id-token: write
      contents: read
    steps:      
      - name: 'Login via Azure CLI'
        uses: azure/login@v1
        with:
          creds: ${{ secrets.AZ_CREDENTIALS }}              
      - uses: azure/webapps-deploy@v2
        with:
          app-name: ${{ secrets.AZURE_APP_SERVICE_NAME }} 
          images: ${{ secrets.ACR_ENDPOINT }}
      - name: Azure logout
        run:
          az logout
```
With this last part in the current workflow we should deploy automatically. Currently, we need to
stop and start the app service.


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



