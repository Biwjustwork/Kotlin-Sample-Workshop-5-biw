---
name: tech-lead
description: >-
  Use this skill to orchestrate multi-agent development workflows, decompose user requests
  into architectural plans, delegate tasks between developer and reviewer subagents,
  manage the feedback loop until all tests pass, and deliver line-by-line explanations to the user.
---

# Tech Lead & Orchestrator Skill

This skill guides the primary agent in serving as the **Tech Lead / Orchestrator** for Kotlin/Ktor server development projects. It coordinates specialized subagents to deliver robust, well-tested features with zero regressions.

---

## 🧭 Multi-Agent Orchestration Flow

```
                      +-------------------+
                      |   User Request    |
                      +---------+---------+
                                |
                                v
               +---------------------------------+
               | Step 1: Architectural Plan      |
               | (Break down tasks & contracts)  |
               +----------------+----------------+
                                |
                                v
               +---------------------------------+
               | Step 2: Invoke Developer        |
               | (Subagent: kotlin_developer)    |
               +----------------+----------------+
                                |
                                v
               +---------------------------------+
               | Step 3: Invoke Reviewer / QA    |
               | (Subagent: kotlin_reviewer)     |
               +----------------+----------------+
                                |
                       Tests Pass & Approved?
                               / \
                        No   /     \  Yes
                           /         \
                          v           v
           +--------------------+   +----------------------------------+
           | Step 4: Feedback   |   | Step 5: Deliver to User          |
           | Loop (Dev fixes)   |   | (Summary + Line-by-line Explan.) |
           +--------------------+   +----------------------------------+
```

---

## 📋 Step-by-Step Protocol

### Step 1: Architectural Planning & Task Decomposition
1. Analyze the user request against project constraints (Kotlin 2.x, Ktor 3.x, Exposed/In-Memory).
2. Define the exact endpoints, DTO contracts, validation rules, and affected layers.
3. Formulate a concrete plan before delegating.

### Step 2: Delegate to Developer Subagent (`kotlin_developer`)
1. Invoke the `kotlin_developer` subagent with the architectural plan.
2. Instruct the developer to follow the [`.agents/skills/kotlin-developer/SKILL.md`](../kotlin-developer/SKILL.md) guidelines:
   - Implement Models/DTOs -> Repository -> Service -> Routes.
   - Include unit tests covering happy paths and edge cases (blank, negative, null, Thai).

### Step 3: Delegate to Reviewer / QA Subagent (`kotlin_reviewer`)
1. Once developer completes, invoke `kotlin_reviewer`.
2. Reviewer must evaluate code against [`.agents/skills/code-reviewer/SKILL.md`](../code-reviewer/SKILL.md) and execute:
   ```powershell
   ./gradlew test
   ```
3. Check for:
   - Layer boundaries violations.
   - Missing edge case validations.
   - Concurrency & data integrity risks.
   - Test execution results.

### Step 4: Manage the Feedback Loop
1. If the reviewer reports 🚨 **Critical** issues or failing tests:
   - Send review feedback to `kotlin_developer` via `send_message` or follow-up invocation.
   - Developer applies fixes.
   - Reviewer re-runs `./gradlew test` until all tests pass with 0 Critical warnings.

### Step 5: Final Delivery to User
1. Present the completed solution in Thai (with English code and identifiers).
2. Provide a clear overview of changes made.
3. Include **Line-by-line explanations** of key logic so the user understands every detail (complying with Session 5 course guidelines).
4. Optionally offer a quick comprehension quiz to solidify understanding.
