Feature: As an user interested in Katalyst courses
         I want to give my personal data to subscribe to the course
         so that I can access to the learning platform

  Scenario: The user is not a company and subscribes to the course
    Given An customer who has choosen a course
    And the user has filled the following data
      | email            | Name | Surname | Company  | Dni/CIF   | isCompany | Address                 | Phone          |  
      | john@example.com | John | Doe     | Randstad | 46842041D | No        | Avd. Yellowstone 45, 2B | +34 6367373373 |  
    When the user request the subscription to the course
    And the user pay the subscription
    Then the subscription is successful
    And the user has received an invoice
    And the user has received the access to the platform

 # Scenario: The user is a company and subscribes to the course
 #  Given A company who has choosen a course
 #   And the user has filled the following data
 #     | email            | Name | Surname | Company  | Dni/CIF   | isCompany | Address                 | Phone          |
 #     | john@example.com | John | Doe     | Randstad | 46842041D | No        | Avd. Yellowstone 45, 2B | +34 6367373373 |
 #   When the user request the subscription
 #   And the user pay the subscription
 #   Then the subscription is successful
 #   And the user has received an invoice
 #   And the user has received the access to the platform

 # Scenario: The user has been subscribed before to the course
 #   Given A company who has choosen a course
 #   And the user has filled the following data
 #     | email            | Name | Surname | Company  | Dni/CIF   | isCompany | Address                 | Phone          |
 #     | john@example.com | John | Doe     | Randstad | 46842041D | No        | Avd. Yellowstone 45, 2B | +34 6367373373 |
 #   When the user request the subscription
 #   Then the user is informed he/she is already subscribed to this course


