Feature: Return Rented Film Copy
  As a user of video rental store, I want to be able perform return operation for a rented film copy by a registered customer

  Scenario:
    Given we have a registered customer, which has rented a film copy
    When this customer returns the rented film copy
    Then a return film copy entry has been successfully created to the system, and bonus awarded to customer
