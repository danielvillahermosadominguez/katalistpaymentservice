Feature: As an user interested in Katalyst courses
  I want to see the course name and the price previously to subscribe in a course

  Scenario: The customer can visualize the course name and price. We use an identifier
    for the course because the user has clicked in Katalyst and it send the identifier to
    the payment system
    Given a set of courses availables in the learning platform
      | ID | NAME               | PRICE                |
      | 1  | TDD in depth       | 49.9                 |
      | 2  | Technical Coaching | 45.39                |
      | 3  | CPC                | <NO PRICE AVAILABLE> |
    When the customer open the payment system with the id 1 for the course
    Then the customer can see the course name is "TDD in depth" and the price is 49.9 euros

  Scenario: The customer cannot visualize the course name and price. And they should use the payment system.
    It could be because an user could manipulate the parameters or there is an error in the integration with
    Katalyst
    Given a set of courses availables in the learning platform
      | ID | NAME               | PRICE                |
      | 1  | TDD in depth       | 49.9                 |
      | 2  | Technical Coaching | 45.39                |
      | 3  | CPC                | <NO PRICE AVAILABLE> |
    When the customer open the payment system with the id 5 for the course
    Then the customer can see the course is not available because doesn't exist

  Scenario: The course selected by the user has not a price. This is because it is not
    configurated in the e-learning platform.
    Given a set of courses availables in the learning platform
      | ID | NAME               | PRICE                |
      | 1  | TDD in depth       | 49.9                 |
      | 2  | Technical Coaching | 45.39                |
      | 3  | CPC                | <NO PRICE AVAILABLE> |
    When the customer open the payment system with the id 3 for the course
    Then the customer can see the course is not available because the course has not a price

  Scenario: The user cannot see the course because the elearning platform is not available for some reason.
    In this case, the user can see a message with the error and can try again later
    Given a set of courses availables in the learning platform
      | ID | NAME               | PRICE                |
      | 1  | TDD in depth       | 49.9                 |
      | 2  | Technical Coaching | 45.39                |
      | 3  | CPC                | <NO PRICE AVAILABLE> |
    And the e-learning platform is not available
    When the customer open the payment system with the id 3 for the course
    Then the customer can see the course is not available because the payment platform is not available