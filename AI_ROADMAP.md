# AI Integration Roadmap – Calorie Tracker

This document outlines the strategic milestones for integrating AI capabilities into the Android Calorie Tracker application.

## Overview
The goal of AI integration is to reduce user friction (manual entry) and provide actionable insights from collected data, moving the app from a simple log to a personalized health coach.

---

## Phase 1: Smart Log (NLP – Natural Language Processing)
**Goal:** Enable users to log meals using natural language text or voice.

### Core Components
- **Domain Layer:** `AiMealParser` interface defining the contract for converting text strings into structured `CalorieEntry` objects.
- **Data Layer:** Implementation of the parser using **Gemini Pro/Flash** (Google AI SDK). Structured JSON output is mandatory.
- **UI Layer:** A "Magic Input" field in `TrackerCaptureScreen` where users can type sentences like "A large apple and a whole grain bread with cheese."

### Key Logic
- Prompt Gemini to return a structured list of food items, amounts (calories), and types (intake/burned).
- Validate the AI's JSON output before displaying it for user confirmation.

---

## Phase 2: Visual Log (Computer Vision)
**Goal:** Automatically estimate calories and ingredients from meal photos.

### Core Components
- **Technology:** **Gemini 1.5 Flash** for multi-modal analysis (image + text context).
- **Camera Integration:** `androidx.camera:camera-view` for in-app photo capture.
- **Workflow:** Capture Image → AI analysis → UI review list with checkboxes for identified items.

### Key Logic
- Multimodal prompts: "Analyze this photo, identify components, and estimate portion sizes based on volume."
- Graceful fallback for failed recognition (manual adjustment by user).

---

## Phase 3: Personal Nutrition Coach (Insights & Predictions)
**Goal:** Analyze historical data to provide proactive, personalized health advice.

### Core Components
- **Data Extraction:** A `GetUserTrendsUseCase` aggregating 14–30 days of data from the Room database.
- **AI Analytics:** Sending aggregated trends (intake, activity, goal progress) to the AI as a context window.
- **Dashboard Integration:** An "AI Insights" section in the `TrackerOverviewScreen`.

### Key Logic
- System prompts as a professional nutritionist.
- Identifying patterns (e.g., "Weekend surplus," "Low energy post-workout").
- Providing three concise, motivational, and actionable recommendations.

---

## Implementation Status
- [ ] Phase 1: Smart Log (Next Priority)
- [ ] Phase 2: Visual Log
- [ ] Phase 3: Personal Nutrition Coach
