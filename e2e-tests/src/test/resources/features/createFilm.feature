Feature: Create Film
  As a user of video rental store I want to create and persist a film

  Scenario:
    Given we have a film which we want to store it in the system
    When we submit the film
    Then the film has successfully stored in the system
