# AGENTS.md – Guidelines for the Calorie Tracker

This file defines mandatory working rules for all agents in this repository.
The goal is **readable, maintainable, and well-structured** Android code aligned with **"Clean Code"** (Robert C. Martin).

## 1) Project Goal
- This project builds an Android calorie tracker.
- Every change should optimize for **readability**, **maintainability**, and **domain clarity**.
- Preferred communication language in project artifacts: **English**.
- This repository is **AI-first**: code is expected to be written almost entirely by agents.

## 1.1) Mandatory Git workflow
- `main` must remain **stable** at all times.
- `development` is the integration branch for ongoing work.
- Agents must **never** implement work directly on `main`.
- Agents must **never** implement work directly on `development`.
- Every code or documentation change must start from a dedicated topic branch created from the latest `development` branch.
- Allowed topic branch prefixes are:
  - `feature/<short-description>` for user-facing functionality
  - `bugfix/<short-description>` for defect fixes
  - `chore/<short-description>` for maintenance, tooling, CI, or documentation work
- Every topic branch must target **`development`** in its pull request.
- `main` may only be updated by intentionally merging `development` into `main` after enough validated changes are ready for release.
- If an agent notices it is on `main` or `development` before making changes, it must stop and create the correct topic branch first.
- If the current branch does not match the task type, the agent must create a new correctly named branch before editing files.

## 2) Core Principle: Clean Code is mandatory
The following rules apply to every commit:

1. **Intention-revealing names**
   - Names must clearly describe what something does.
   - Avoid cryptic abbreviations (`x`, `tmp`, `mgr`) unless they are widely accepted.

2. **Small, focused functions**
   - A function should do **one** thing.
   - Split long functions when responsibilities are mixed.

3. **Single Responsibility Principle (SRP)**
   - Classes, files, and functions should have one clear reason to change.

4. **No duplication (DRY)**
   - Extract duplicated logic (e.g., helper functions or shared components).

5. **Clear layers and dependencies**
   - Keep UI, domain logic, and data access cleanly separated.
   - Do not leak Android-specific details into business logic unnecessarily.

6. **Intentional error handling**
   - Handle error paths explicitly (e.g., Result types, clear failure states).
   - No silent failures and no "catch and ignore".

7. **Comments only when they add value**
   - Prefer self-explanatory code.
   - Comments should explain **why**, not obvious **what**.
   - Outdated comments must be updated or removed.

8. **Formatting and consistency**
   - Follow a consistent style (Kotlin conventions, IDE formatting).
   - Avoid arbitrary stylistic deviations without strong reason.

## 3) Kotlin/Android-specific guidelines
- Use Kotlin idioms appropriately (e.g., `data class`, `sealed` classes for state, prefer immutability).
- Prefer `val` over `var` whenever possible.
- Keep Composables small and reusable.
- Model state explicitly (e.g., dedicated UI state structures).
- Avoid hardcoded UI values; use resources for reusable strings/colors/dimensions.
- Use `ViewModel` as the default owner for UI state and side-effect orchestration.

## 4) Architecture principles (practical)
- Place new features in clearly separated packages/modules where possible.
- Domain logic must not depend on UI details.
- Keep external dependencies minimal; briefly justify each new dependency.
- Keep public APIs (classes/functions) small and stable.
- Prefer unidirectional data flow from data/domain → state → UI.

## 5) AI-first delivery requirements
Because agents produce most code, every change must be understandable without oral context.

- **Always leave a traceable rationale** in PR description:
  - problem,
  - chosen approach,
  - rejected alternatives (short),
  - validation performed.
- **No hidden assumptions**: encode assumptions in code/docs/tests.
- **Refactor while touching**: if code is unclear and directly related, improve it within scope.
- **Deterministic behavior first**: avoid flaky logic and time-dependent hidden behavior.
- **No silent TODO debt**:
  - if adding TODO/FIXME, include owner, reason, and completion condition.
- **Explicit contracts**:
  - define expected inputs/outputs and failure behavior at boundaries (repository, use case, API).

## 6) Testing and quality assurance
- New or changed business logic should be covered by unit tests.
- Bug fixes should include a regression test when feasible.
- For state-heavy UI behavior, test state reducers/transformers where possible.
- Before finishing:
  - Build should succeed.
  - Relevant tests should pass.
  - Obvious warnings/dead code should be cleaned up.
  - New behavior should be reproducible from commit + PR notes.

## 7) Definition of Done (DoD)
A task is only done when:
- code is understandable and consistent,
- no unnecessary complexity was introduced,
- architecture quality did not regress,
- relevant tests are added/updated,
- local build/tests have been executed successfully,
- PR includes enough context for another agent to continue work safely.

## 8) Pull Request quality
Each PR should include:
- **What** changed?
- **Why** it changed?
- **How** it was validated? (tests, manual checks)
- **Risks / follow-ups** (if any)
- If relevant: screenshots for UI changes.

## 9) Decision rule for trade-offs
When multiple solutions are possible, prioritize in this order:
1. Higher readability
2. Lower complexity
3. Better testability
4. Lower coupling
5. Easier extensibility
6. Lower long-term maintenance cost

## 10) Anti-patterns to avoid
- God classes / utility dumping grounds
- Boolean parameters that hide behavior switches
- Deep nesting instead of early returns
- Magic numbers/strings without naming
- "Quick fix" workarounds without TODO + context
- Mixing feature work with unrelated broad refactors in one PR

## 11) Agent workflow
- Create a brief plan first for larger changes.
- Before editing files, verify the current branch and target branch against the mandatory Git workflow above.
- Prefer small, traceable commits.
- Keep each topic branch focused on one concern and open a pull request to `development` when the change is ready.
- Avoid silent side-refactorings unrelated to the task.
- Respect existing structure; refactor only with clear value.
- When uncertain, choose the option that is easiest for the next agent to understand and extend.

## 12) Documentation minimum for AI-maintained code
For non-trivial new logic, include at least one of:
- concise KDoc on public interfaces, or
- targeted README section in the relevant module/package.

Documentation must cover:
- purpose,
- key invariants,
- failure behavior,
- how to test/verify.

---

**Guiding statement:**
> "Clean Code is not an end in itself. It reduces defects, accelerates change, and keeps long-term delivery sustainable."
