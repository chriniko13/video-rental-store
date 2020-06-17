Feature: Create Film Copy
  As a user of video rental store I want to create and persist a film copy for an existing registered film

  Scenario:
    Given we have an already registered film
    When we submit the film copy
    Then the film copy has successfully stored in the system
