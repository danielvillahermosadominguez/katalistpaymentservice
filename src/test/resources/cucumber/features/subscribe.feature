Feature: As an user interested in Katalyst courses
         I want to give my personal data to subscribe to the course
         so that I can access to the learning platform

  Scenario: The user is not a company and subscribes to the course. He/She has not been enrolled to other courses
    in the past.
    Given An customer who has chosen a course
    And the user has filled the following data
      | email            | Name | Surname | Company  | Dni/CIF   | isCompany | Address                 | Phone          |
      | john@example.com | John | Doe     | Randstad | 46842041D | No        | Avd. Yellowstone 45, 2B | +34 6367373373 |
    When the user request the subscription to the course
    And the user pay the subscription
    Then the subscription is successful
    And the user has received an invoice
    And the user has received the access to the platform

  Scenario: The user is not a company and subscribes to the course. He/She has been enrolled to other courses
  in the past but not in this one.
    Given An customer who has chosen a course
    And he/she has been subscribed to other courses in the past with the following data
      | email            | Name | Surname | Company  | Dni/CIF   | isCompany | Address                 | Phone          |
      | john@example.com | John | Doe     | Randstad | 46842041D | No        | Avd. Yellowstone 45, 2B | +34 6367373373 |

    And the user has filled the following data
      | email            | Name | Surname | Company  | Dni/CIF   | isCompany | Address                 | Phone          |
      | john@example.com | John | Doe     | Randstad | 46842041D | No        | Avd. Yellowstone 45, 2B | +34 6367373373 |
    When the user request the subscription to the course
    And the user pay the subscription
    Then the subscription is successful
    And the user has received an invoice
    And the user has received the access to the platform

  Scenario: The user is not a company and subscribes to the course. He/She is enrolled to this course now.
    Given An customer who has chosen a course
    And he/she has been subscribed to the same course in the past with the following data
      | email            | Name | Surname | Company  | Dni/CIF   | isCompany | Address                 | Phone          |
      | john@example.com | John | Doe     | Randstad | 46842041D | No        | Avd. Yellowstone 45, 2B | +34 6367373373 |

    And the user has filled the following data
      | email            | Name | Surname | Company  | Dni/CIF   | isCompany | Address                 | Phone          |
      | john@example.com | John | Doe     | Randstad | 46842041D | No        | Avd. Yellowstone 45, 2B | +34 6367373373 |
    When the user request the subscription to the course
    And the user pay the subscription
    Then the subscription is successful
    And the user has received an invoice
    And the user has received the access to the platform

  Scenario: The user is not a company and subscribes to a course which is not in the course catalog
    Given An customer who has chosen a course which is not in the catalog
    And the user has filled the following data
      | email            | Name | Surname | Company  | Dni/CIF   | isCompany | Address                 | Phone          |
      | john@example.com | John | Doe     | Randstad | 46842041D | No        | Avd. Yellowstone 45, 2B | +34 6367373373 |
    When the user request the subscription to the course
    And the user pay the subscription
    Then the subscription is not successful because the course is not in the catalog