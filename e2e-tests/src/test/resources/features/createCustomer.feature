Feature: Create Customer Account
  As a user of video rental store I want to create and persist a customer account

  Scenario:
    Given we have created a customer account
    When we submit it to the system
    Then the customer account has been successfully stored in the system
