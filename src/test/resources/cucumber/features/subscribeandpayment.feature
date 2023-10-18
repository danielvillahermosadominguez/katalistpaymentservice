Feature: As an user interested in Katalyst courses
  I want to give my personal data to subscribe to the course
  so that I can access to the learning platform
  Scenario: The customer, which is not a company, subscribes to the course. He/She has not been enrolled to other courses
  in the past.
    Given Holded has no contacts
    And Moodle has not students
    And An customer who has chosen the following course the course "TDD in depth" with a price of "99.9"
    And the customer has filled the following data
      | FIRST NAME | SURNAME | EMAIL            | COMPANY NAME | IS COMPANY | NIF/CIF   | PHONE NUMBER  | ADDRESS                 | POSTAL CODE | REGION | COUNTRY | CITY               |
      | John       | Doe     | john@example.com | N/A          | NO         | 46842041D | +34 636737337 | Avd. Yellowstone 45, 2B | 28080       | Madrid | Spain   | Boadilla del Monte |
    When the customer pays the subscription with credit/debit card with the following data
      | NAME     | NUMBER           | MONTH | YEAR | CVV | RESULT |
      | John Doe | 4273682057894021 | 05    | 24   | 123 | OK     |
    And the customer receives a challenge URL and decide to "Accept" the payment
    Then the customer is informed about the success of the subscription
    Then Holded has the following contacts
      | NAME     | CONTACT NIF | THIS CONTACT IS | EMAIL            | ADDRESS                 | PHONE NUMBER  | POSTAL CODE | CITY               | PROVINCE | COUNTRY | PURCHASE ACCOUNT | CUSTOMER-ID                                                      |
      | JOHN DOE | 46842041D   | Person          | john@example.com | AVD. YELLOWSTONE 45, 2B | +34 636737337 | 28080       | BOADILLA DEL MONTE | MADRID   | SPAIN   | 70500000         | a9454f69cff66d9dd54b57369b9296096b28691e9878fb59da6992b1e3edafe8 |
    And the customer will receive an invoice to the recipients "john@example.com" with the following data
      | CONCEPT      | PRICE | UNITS | SUBTOTAL | TOTAL |
      | TDD IN DEPTH | 99.9  | 1     | 99.9     | 99.9  |
    And the customer will receive access to the platform in the email "john@example.com" with the user "john" and fullname "John" "Doe"

  Scenario: The customer, which is a company, subscribes to the course. The company has not been enrolled to other courses
  in the past.
    Given Holded has no contacts
    And Moodle has not students
    And An customer who has chosen the following course the course "TDD in depth" with a price of "99.9"
    And the customer has filled the following data
      | FIRST NAME | SURNAME | EMAIL                        | COMPANY NAME         | IS COMPANY | NIF/CIF   | PHONE NUMBER  | ADDRESS                 | POSTAL CODE | REGION | COUNTRY | CITY               |
      | N/A        | N/A     | company.business@example.com | Company Business S.A | YES        | 46842041D | +34 636737337 | Avd. Yellowstone 45, 2B | 28080       | Madrid | Spain   | Boadilla del Monte |
    When the customer pays the subscription with credit/debit card with the following data
      | NAME     | NUMBER           | MONTH | YEAR | CVV | RESULT |
      | John Doe | 4273682057894021 | 05    | 24   | 123 | OK     |
    And the customer receives a challenge URL and decide to "Accept" the payment
    Then the customer is informed about the success of the subscription
    Then Holded has the following contacts
      | NAME                 | CONTACT NIF | VAT NUMBER | THIS CONTACT IS | EMAIL                        | ADDRESS                 | PHONE NUMBER  | POSTAL CODE | CITY               | PROVINCE | COUNTRY | PURCHASE ACCOUNT | CUSTOMER-ID                                                      |
      | COMPANY BUSINESS S.A | N/A         | 46842041D  | Company         | company.business@example.com | AVD. YELLOWSTONE 45, 2B | +34 636737337 | 28080       | BOADILLA DEL MONTE | MADRID   | SPAIN   | 70500000         | b4ab6e9f5fb366130cc8cd84d0845308cce9e0a5486424f3a56c0fff115e927e |
    And the customer will receive an invoice to the recipients "company.business@example.com" with the following data
      | CONCEPT      | PRICE | UNITS | SUBTOTAL | TOTAL |
      | TDD IN DEPTH | 99.9  | 1     | 99.9     | 99.9  |
    And the customer will receive access to the platform in the email "company.business@example.com" with the user "companybusiness" and fullname "Company Business S.A" ""

  Scenario: The customer, which is not a company, subscribes to the course. He/She has been enrolled to other courses
  in the past but not in this course and he/she include in the subscription some different data in the address
    Given Holded which has these previous contacts
      | NAME     | CONTACT NIF | THIS CONTACT IS | EMAIL                | ADDRESS                 | PHONE NUMBER  | POSTAL CODE | CITY               | PROVINCE | COUNTRY | PURCHASE ACCOUNT | CUSTOMER-ID                                                      |
      | JOHN DOE | 46842041D   | Person          | john.doe@example.com | AVD. YELLOWSTONE 45, 2B | +34 636737337 | 28080       | BOADILLA DEL MONTE | MADRID   | SPAIN   | 70500000         | d640ef3f8b62ba0cfe2c8a8a35cdc6f469f2bc7429675e6246cac82929d4c878 |
      | JANE DOE | 46842041X   | Person          | jane.doe@example.com | AVD. YELLOWSTONE 45, 2B | +34 636737337 | 28080       | BOADILLA DEL MONTE | MADRID   | SPAIN   | 70500000         | d0b2e6cfdd64aed23e91362089620464ff874e5da81ca233cf12b20ac22a8088 |
    And Moodle which has these previous users
      | NAME | SURNAME | USERNAME | EMAIL                |
      | John | Doe     | johndoe  | john.doe@example.com |
      | Jane | Doe     | janedoe  | jane.doe@example.com |
    And a previous course called "TDD in depth" exists which has the following students
      | NAME | SURNAME | USERNAME | EMAIL                |
      | Jane | Doe     | janedoe  | jane.doe@example.com |
    And An customer who has chosen the following course the course "TDD in depth" with a price of "99.9"
    And the customer has filled the following data
      | FIRST NAME | SURNAME | EMAIL                | COMPANY NAME | IS COMPANY | NIF/CIF   | PHONE NUMBER  | ADDRESS              | POSTAL CODE | REGION | COUNTRY | CITY               |
      | John       | Doe     | john.doe@example.com | N/A          | NO         | 46842041D | +34 636737337 | Avd. RedStone 45, 2B | 28081       | Madrid | Spain   | Boadilla del Monte |
    When the customer pays the subscription with credit/debit card with the following data
      | NAME     | NUMBER           | MONTH | YEAR | CVV | RESULT |
      | John Doe | 4273682057894021 | 05    | 24   | 123 | OK     |
    And the customer receives a challenge URL and decide to "Accept" the payment
    Then the customer is informed about the success of the subscription
    Then Holded has the following contacts
      | NAME     | CONTACT NIF | THIS CONTACT IS | EMAIL                | ADDRESS                 | PHONE NUMBER  | POSTAL CODE | CITY               | PROVINCE | COUNTRY | PURCHASE ACCOUNT | CUSTOMER-ID                                                      |
      | JOHN DOE | 46842041D   | Person          | john.doe@example.com | AVD. YELLOWSTONE 45, 2B | +34 636737337 | 28080       | BOADILLA DEL MONTE | MADRID   | SPAIN   | 70500000         | d640ef3f8b62ba0cfe2c8a8a35cdc6f469f2bc7429675e6246cac82929d4c878 |
      | JANE DOE | 46842041X   | Person          | jane.doe@example.com | AVD. YELLOWSTONE 45, 2B | +34 636737337 | 28080       | BOADILLA DEL MONTE | MADRID   | SPAIN   | 70500000         | d0b2e6cfdd64aed23e91362089620464ff874e5da81ca233cf12b20ac22a8088 |
    And the customer will receive an invoice to the recipients "john.doe@example.com" with the following data
      | CONCEPT      | PRICE | UNITS | SUBTOTAL | TOTAL |
      | TDD IN DEPTH | 99.9  | 1     | 99.9     | 99.9  |
    And the customer will receive access to the platform in the email "john.doe@example.com" with the user "johndoe" and fullname "John" "Doe"

  Scenario: The customer, which is not a company, subscribes to the course. He/She has not been enrolled to other courses
  in the past but an user exists in moodle with the same username. It could be,for example, the same person but with other email domain.
  We consider both as different people and users for moodle. For us, a person is the combination of the NIF/CIF and email
    Given Holded which has these previous contacts
      | NAME     | CONTACT NIF | THIS CONTACT IS | EMAIL                | ADDRESS                 | PHONE NUMBER  | POSTAL CODE | CITY               | PROVINCE | COUNTRY | PURCHASE ACCOUNT | CUSTOMER-ID                                                      |
      | JOHN DOE | 46842041D   | Person          | john.doe@domain1.com | AVD. YELLOWSTONE 45, 2B | +34 636737337 | 28080       | BOADILLA DEL MONTE | MADRID   | SPAIN   | 70500000         | 53a9280853584aa525949e24a900c11a087cfda27446f3fe269c3b5f457c624b |
      | JANE DOE | 46842041X   | Person          | jane.doe@example.com | AVD. YELLOWSTONE 45, 2B | +34 636737337 | 28080       | BOADILLA DEL MONTE | MADRID   | SPAIN   | 70500000         | d0b2e6cfdd64aed23e91362089620464ff874e5da81ca233cf12b20ac22a8088 |
    And Moodle which has these previous users
      | NAME | SURNAME | USERNAME | EMAIL                |
      | John | Doe     | johndoe  | john.doe@domain1.com |
      | Jane | Doe     | janedoe  | jane.doe@example.com |
    And a previous course called "TDD in depth" exists which has the following students
      | NAME | SURNAME | USERNAME | EMAIL                |
      | Jane | Doe     | janedoe  | jane.doe@example.com |
    And An customer who has chosen the following course the course "TDD in depth" with a price of "99.9"
    And the customer has filled the following data
      | FIRST NAME | SURNAME | EMAIL                | COMPANY NAME | IS COMPANY | NIF/CIF   | PHONE NUMBER  | ADDRESS              | POSTAL CODE | REGION | COUNTRY | CITY               |
      | John       | Doe     | john.doe@domain2.com | N/A          | NO         | 46842041D | +34 636737337 | Avd. RedStone 45, 2B | 28081       | Madrid | Spain   | Boadilla del Monte |
    When the customer pays the subscription with credit/debit card with the following data
      | NAME     | NUMBER           | MONTH | YEAR | CVV | RESULT |
      | John Doe | 4273682057894021 | 05    | 24   | 123 | OK     |
    And the customer receives a challenge URL and decide to "Accept" the payment
    Then the customer is informed about the success of the subscription
    Then Holded has the following contacts
      | NAME     | CONTACT NIF | THIS CONTACT IS | EMAIL                | ADDRESS                 | PHONE NUMBER  | POSTAL CODE | CITY               | PROVINCE | COUNTRY | PURCHASE ACCOUNT | CUSTOMER-ID                                                      |
      | JOHN DOE | 46842041D   | Person          | john.doe@domain1.com | AVD. YELLOWSTONE 45, 2B | +34 636737337 | 28080       | BOADILLA DEL MONTE | MADRID   | SPAIN   | 70500000         | 53a9280853584aa525949e24a900c11a087cfda27446f3fe269c3b5f457c624b |
      | JANE DOE | 46842041X   | Person          | jane.doe@example.com | AVD. YELLOWSTONE 45, 2B | +34 636737337 | 28080       | BOADILLA DEL MONTE | MADRID   | SPAIN   | 70500000         | d0b2e6cfdd64aed23e91362089620464ff874e5da81ca233cf12b20ac22a8088 |
      | JOHN DOE | 46842041D   | Person          | john.doe@domain2.com | AVD. REDSTONE 45, 2B    | +34 636737337 | 28081       | BOADILLA DEL MONTE | MADRID   | SPAIN   | 70500000         | 4fa4abbf83a9fbf0be21884cabbadf68f0079cf62c74e930067ef175f42dae48 |
    And the customer will receive an invoice to the recipients "john.doe@domain2.com" with the following data
      | CONCEPT      | PRICE | UNITS | SUBTOTAL | TOTAL |
      | TDD IN DEPTH | 99.9  | 1     | 99.9     | 99.9  |
    And the customer will receive access to the platform in the email "john.doe@domain2.com" with the user "johndoe1" and fullname "John" "Doe"

  Scenario: The customer subscribes to a course, however, this customer is enrolled already in the course. He/She receives a message explaining it.
    Given Holded which has these previous contacts
      | NAME     | CONTACT NIF | THIS CONTACT IS | EMAIL                | ADDRESS                 | PHONE NUMBER  | POSTAL CODE | CITY               | PROVINCE | COUNTRY | PURCHASE ACCOUNT | CUSTOMER-ID                                                      |
      | JOHN DOE | 46842041D   | Person          | john.doe@example.com | AVD. YELLOWSTONE 45, 2B | +34 636737337 | 28080       | BOADILLA DEL MONTE | MADRID   | SPAIN   | 70500000         | d640ef3f8b62ba0cfe2c8a8a35cdc6f469f2bc7429675e6246cac82929d4c878 |
      | JANE DOE | 46842041X   | Person          | jane.doe@example.com | AVD. YELLOWSTONE 45, 2B | +34 636737337 | 28080       | BOADILLA DEL MONTE | MADRID   | SPAIN   | 70500000         | d0b2e6cfdd64aed23e91362089620464ff874e5da81ca233cf12b20ac22a8088 |
    And Moodle which has these previous users
      | NAME | SURNAME | USERNAME | EMAIL                |
      | John | Doe     | johndoe  | john.doe@example.com |
      | Jane | Doe     | janedoe  | jane.doe@example.com |
    And a previous course called "TDD in depth" exists which has the following students
      | NAME | SURNAME | USERNAME | EMAIL                |
      | Jane | Doe     | janedoe  | jane.doe@example.com |
      | John | Doe     | johndoe  | john.doe@example.com |
    And An customer who has chosen the following course the course "TDD in depth" with a price of "99.9"
    And the customer has filled the following data
      | FIRST NAME | SURNAME | EMAIL                | COMPANY NAME | IS COMPANY | NIF/CIF   | PHONE NUMBER  | ADDRESS              | POSTAL CODE | REGION | COUNTRY | CITY               |
      | John       | Doe     | john.doe@example.com | N/A          | NO         | 46842041D | +34 636737337 | Avd. RedStone 45, 2B | 28081       | Madrid | Spain   | Boadilla del Monte |
    When the customer pays the subscription with credit/debit card with the following data
      | NAME     | NUMBER           | MONTH | YEAR | CVV | RESULT |
      | John Doe | 4273682057894021 | 05    | 24   | 123 | OK     |
    Then the customer is informed about the fail of the subscription
      | ERROR CODE | ERROR MESSAGE                               |
      | 2          | The user has a subscription for this course |
    And There are not pending authorized payments

  Scenario: The customer has subscribed to a course and payed the price. The Payment platform confirmed the payment but it
  had some problems to comunicate with the financial platform. So, it should retry and finish the process.
    Given Holded has no contacts
    And Moodle has not students
    And the retry process is active
    And the customer made a purchase with the following data
      | COURSE ID | PRICE | DESCRIPTION  | CONCEPT | ORDER CODE     | FIRST NAME | SURNAME | EMAIL                | COMPANY NAME | IS COMPANY | NIF/CIF   | PHONE NUMBER  | ADDRESS                 | POSTAL CODE | REGION | COUNTRY | CITY               |
      | 32        | 79.9  | TDD in depth | course  | PS202310121032 | John       | Doe     | john.doe@example.com | N/A          | NO         | 46842041D | +34 636737337 | Avd. YellowStone 45, 2B | 28080       | Madrid | ES      | Boadilla del Monte |
    And during the payment notification process, the financial platform didn't respond, but now is available
    Then the retry process finishes the notification process with the following contacts in holded
      | NAME     | CONTACT NIF | THIS CONTACT IS | EMAIL                | ADDRESS                 | PHONE NUMBER  | POSTAL CODE | CITY               | PROVINCE | COUNTRY | PURCHASE ACCOUNT | CUSTOMER-ID                                                      |
      | JOHN DOE | 46842041D   | Person          | john.doe@example.com | AVD. YELLOWSTONE 45, 2B | +34 636737337 | 28080       | BOADILLA DEL MONTE | MADRID   | ES       | 70500000         | d640ef3f8b62ba0cfe2c8a8a35cdc6f469f2bc7429675e6246cac82929d4c878 |

  Scenario: The customer has subscribed to a course and payed the price. The Payment platform confirmed the payment but it
  had some problems to comunicate with the learning platform. So, it should retry and finish the process.
    Given Holded has no contacts
    And Moodle has not students
    And the retry process is active
    And the customer made a purchase with the following data
      | COURSE ID | PRICE | DESCRIPTION  | CONCEPT | ORDER CODE     | FIRST NAME | SURNAME | EMAIL                | COMPANY NAME | IS COMPANY | NIF/CIF   | PHONE NUMBER  | ADDRESS                 | POSTAL CODE | REGION | COUNTRY | CITY               |
      | 32        | 79.9  | TDD in depth | course  | PS202310121032 | John       | Doe     | john.doe@example.com | N/A          | NO         | 46842041D | +34 636737337 | Avd. YellowStone 45, 2B | 28080       | Madrid | ES      | Boadilla del Monte |
    And during the payment notification process, the learning platform didn't respond, but now is available
    Then the retry process finishes the notification process with the following users in Moodle
      | NAME | SURNAME | USERNAME | EMAIL                |
      | John | Doe     | johndoe  | john.doe@example.com |

  Scenario: The customer has subscribed to a course and payed the price. The Payment platform confirmed the payment but it
  had some problems to comunicate with both the learning platform and financial platform. So, it should retry and finish the process.
    Given Holded has no contacts
    And Moodle has not students
    And the retry process is active
    And the customer made a purchase with the following data
      | COURSE ID | PRICE | DESCRIPTION  | CONCEPT | ORDER CODE     | FIRST NAME | SURNAME | EMAIL                | COMPANY NAME | IS COMPANY | NIF/CIF   | PHONE NUMBER  | ADDRESS                 | POSTAL CODE | REGION | COUNTRY | CITY               |
      | 32        | 79.9  | TDD in depth | course  | PS202310121032 | John       | Doe     | john.doe@example.com | N/A          | NO         | 46842041D | +34 636737337 | Avd. YellowStone 45, 2B | 28080       | Madrid | ES      | Boadilla del Monte |
    And during the payment notification process, the financial platform didn't respond, but now is available
    And during the payment notification process, the learning platform didn't respond, but now is available
    Then the retry process finishes the notification process with the following users in Moodle
      | NAME | SURNAME | USERNAME | EMAIL                |
      | John | Doe     | johndoe  | john.doe@example.com |
    And the retry process finishes the notification process with the following contacts in holded
      | NAME     | CONTACT NIF | THIS CONTACT IS | EMAIL                | ADDRESS                 | PHONE NUMBER  | POSTAL CODE | CITY               | PROVINCE | COUNTRY | PURCHASE ACCOUNT | CUSTOMER-ID                                                      |
      | JOHN DOE | 46842041D   | Person          | john.doe@example.com | AVD. YELLOWSTONE 45, 2B | +34 636737337 | 28080       | BOADILLA DEL MONTE | MADRID   | ES      | 70500000         | d640ef3f8b62ba0cfe2c8a8a35cdc6f469f2bc7429675e6246cac82929d4c878 |
