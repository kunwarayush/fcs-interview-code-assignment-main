# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```
Why not active record (Store):
- Couples domain model tightly to persistence
- Makes unit testing harder
- Domain entities become aware of database concerns

Why not  Direct Panache repository (Product):=
- No clear ports/adapters separation


Yes, I'd standardize on the Repository pattern used in Warehouse.

After 7 years, I've learned that Active Record (Store) creates tight coupling between domain and persistence, making it painful to test and evolve. The direct Panache approach (Product) works fine for simple CRUD but lacks the architectural boundaries needed when business rules grow.

The Repository pattern gives us proper separation of concerns, makes mocking trivial in tests, and keeps domain models clean. The upfront cost pays off when requirements inevitably get more complex. I've seen too many projects start simple with Active Record and then struggle to refactor when they need transaction control, caching, or complex queries.

For a fulfillment system handling warehouses, stores, and products, consistency matters. One pattern, easier onboarding, less cognitive overhead.

```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```
Warehouse's OpenAPI approach is appropriate for its complex validations and stable requirements. Product/Store can remain direct-coded if they change frequently, but migrate to OpenAPI once requirements stabilize for production use.


Open Api Pros:
 Contract-first design - API spec is single source of truth
 Auto-documentation (Swagger UI)
 Type safety with generated beans
 Breaking changes caught during build

Open Api Cpns:
 Build complexity (Maven plugin, regeneration)
 Less flexible for edge cases

Direct coding pros:
 Faster initial development
 Full flexibility
 Simpler build configuration

Direct coding Cons :
 Documentation can drift from code
 No contract enforcement
 Manual validation logic


I'd use OpenAPI for stable, external-facing APIs and direct coding for internal APIs during rapid iteration.

OpenAPI shines when multiple teams consume your API or when you need strong contracts. The auto-generated docs and type safety save debugging time, and the contract-first approach catches breaking changes at build time instead of production. I've used it successfully on microservices architectures where frontend/backend teams work in parallel.

Direct coding wins for speed during prototyping or when requirements change daily. I have used it during initial  iterations for some of the products that i have developed.

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