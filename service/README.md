

### Service Module

#### Description

Contains the business operations / services that our platform should execute, most of these
operations are protected from transactional boundaries.


#### Architectural Decisions
If client passes the information we need in the same format-approach
as command, then it is not needed to create a `DTO`,
otherwise we create a `DTO` and we write the logic to map it to a `COMMAND`.

```text

Diagram: DTO ---> {mapping logic} ---> COMMAND

```

