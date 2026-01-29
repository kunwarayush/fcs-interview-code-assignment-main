# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt

```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt

```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```
Priority:
1. Integration tests (@QuarkusTest): Highest ROI. Tests the full stack (REST, validation, DB constraints) and catches real bugs unit tests miss.
2. Unit tests for Domain Logic: Fast feedback for complex rules (e.g., UseCases) without DB overhead.
3. Skip testing framework internals or generated code.

Strategy:
Start with integration tests for happy paths and main error cases. Add unit tests specifically for complex business logic branches that are hard to set up in data.

Sustainability:
Enforce the 80% coverage threshold in CI pipelines.
Treat coverage drops as build failures.
Focus on assertions that verify behavior, not just line execution.

```