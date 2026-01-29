# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**
Scenario 1: Cost Allocation and Tracking
Key Information & Considerations:
First question: What decisions does this data drive? At Arcesium, our P&L calculations needed 99.98% accuracy because portfolio managers made million-dollar trading decisions on those numbers. At ESP Solutions serving 50+ districts, enrollment data needed perfection for compliance, but operational metrics could tolerate 95% accuracy. For warehouse costs, if you're deciding which warehouse to close, you need audit-grade precision. If it's quarterly trend analysis for executives, 90% accuracy using simple square-footage allocation is fine. The business impact determines technical complexity—the last 10% accuracy costs 50% more engineering effort.
Second: Get stakeholder agreement on allocation rules upfront. At ESP, we built a flexible cost allocation engine but couldn't deploy it for six months because out CEO disagreed on infrastructure costs. The system is easy; business rule consensus is hard.Scope Phase 1 as direct costs only (labor hours, inventory at location), defer shared costs (overhead, cross-warehouse managers) to Phase 2 after proving reliability with the easy stuff.

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**
We can't optimize what you can't measure. Before reducing ESP's data processing SLA by 40%, I profiled where time actually went—database queries 45%, ETL 30%, API calls 15%. For warehouses, instrument cost tracking first: cost-per-order by warehouse, labor %, inventory turnover. Run 3 months to establish baseline, then build benchmarking for warehouse costs.
Deliver in phases: visibility (dashboards), recommendations (ROI-scored opportunities), partial automation (route optimization), full automation (auto-scale labor)—each phase earns trust for the next. At ESP, our 40% improvement was 10 smaller optimizations over 3 months, each measured with rollback capability.
Present options scored by effort and ROI: quick wins (better scheduling) vs structural changes (automation). Technology surfaces opportunities; business decides based on strategic context.

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**
Integration establishes one source of truth, not two systems that occasionally agree. At ESP Solutions, I integrated Stripe payment processing with our internal billing where mismatches between Stripe transactions and our records caused finance reconciliation nightmares. At Arcesium processing billions in hedge fund assets, our P&L system integrated with accounting where even 0.01% discrepancy triggered regulatory alerts. The benefit isn't "moving data faster"—it's that Operations and Finance see identical numbers, so when warehouse costs spike, both teams investigate the same reality instead of debating whose numbers are right.
Build reconciliation before assuming integration is "done." At Arcesium, we didn't declare integration successful when data flowed—we declared success when reconciliation reports showed 99.98% match rates over 30 days.For warehouses, deliver variance reports comparing operational vs financial data, surface mismatches for investigation, and only after 3 months of clean reconciliation trust the integration enough to automate decisions on it.

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**
Forecasting systems need business assumptions, not just technical models. At ESP, we predicted data processing loads for capacity planning but learned our technical model (historical averages + 15% growth) missed reality because it ignored business context—two large migrations at school year end. At Snapdeal, we followed similar progression: first gathered behavioral data about shopping habits creating buyer profiles, then used that foundation to predict drop-off points in the buyer journey and intervene with targeted nudges (cart reminders, personalized recommendations).

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**

Preserving History - Audit vs. Operations:Preserving history serves two distinct masters: Audit and Operation. Audit needs immutable records—what happened, when, and at what cost. Operational Budgeting needs context—distinguishing between "startup burn" of the new facility vs "steady state" of the old one. If we naively mix the data under one ID without segmentation, we corrupt our baseline.

Technical Implementation - Logical vs. Physical IDs:We likely need a "Logical Warehouse ID" vs "Physical Warehouse ID" separation.
The Business Unit Code (BUC) stays the same (Logical ID).We introduce a versioning or epoch system (Physical ID/Effective Date) in the underlying data.Old data remains accessible via (BUC + Version 1).New data accumulates under (BUC + Version 2).

Budget Validation & Normalization:We can't just compare "Old Warehouse Apr 2023" vs "New Warehouse Apr 2024" directly because the new one has migration costs.We need to tag costs in the new facility as "One-time Migration" vs "Recurring Operational". Our cost control dashboard should show "Normalized Run Rate" to truly see if the new warehouse is performing within budget, stripping out the noise of the move.


## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.
