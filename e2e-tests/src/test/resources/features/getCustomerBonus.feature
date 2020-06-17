Feature: Get Customer Bonus
  As a user of video rental store, I want to be able to get a registered customer's total bonus

  Scenario:
    Given we have a registered customer
    When we select to see registered customer's total bonus
    Then we get a detailed response of customer's total bonus
