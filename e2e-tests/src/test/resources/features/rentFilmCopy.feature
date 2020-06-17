Feature: Rent Film Copy
  As a user of video rental store I want to rent a film copy to an existing customer

  Scenario:
    Given we have an already registered film, registered film copy and a registered customer to the system
    When we rent the selected film copy to the customer
    Then a film copy entry has been successfully created to the system
