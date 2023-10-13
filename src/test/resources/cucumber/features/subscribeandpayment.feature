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