# Bayes Java Dota Challlenge

This is the [task](TASK.md).

Any additional information about your solution goes here.

_**Dependencies**_
I've added org.modelmapper:modelmapper to simplify model to entity conversion

_**Considerations**_

main focus during implementation - performance and none blocking processing
1. gg.bayes.challenge.rest.controller.MatchController.ingestCombatLog signature was changed to 
support streaming of uploaded file: lines can be processed before the whole file is uploaded 
2. service can process many log files in parallel
3. all I/O operation (storing in DB) executed in dedicated threads, so response provided at once 
the whole uploaded and parsed 

_**Further improvements**_
1. batch insert into database
2. integration test with Testcontainers

