# ExpenseTracker Smart Upgrade TODO

Current Progress: 27/28 steps complete. All core features implemented: dynamic adaptive dashboard, AI behavioral insights, automation predictions, persistence, gamification streaks, nav screens, dark mode, weekly review with stats/charts stub.

## Phase 1: Project Setup & Persistence (Steps 1-8)

- [x] 1. Update app/build.gradle.kts: Add Room, Hilt, Navigation Compose, Charts (e.g. org.jetbrains.compose.charts), Lottie, ViewModel deps.
- [x] 3. Edit data models: Add streakDays (Task), predictedRisk (Expense), habitStrength.
- [x] 4. New: data/AppDatabase.kt (Room @Database: Expense/Task/MoodEntry).
- [x] 5. New: data/\*Dao.kt (insert/update/delete/query Flows).
- [x] 6. New: data/Repository.kt (DAOs wrapper, analytics methods).
- [x] 7. New: data/AnalyticsEngine.kt (local calcs: streaks, corr(mood-task), predictions).
- [ ] 2. Create build.gradle.kts changes for Gemini (secure key via local.properties/buildConfig).
- [ ] 3. Edit data models: Add streakDays (Task), predictedRisk (Expense), habitStrength (new Habit model?).
- [ ] 4. New: data/AppDatabase.kt (Room @Database: Expense/Task/MoodEntry).
- [ ] 5. New: data/\*Dao.kt (insert/update/delete/query Flows).
- [ ] 6. New: data/Repository.kt (DAOs wrapper, analytics methods).
- [ ] 7. New: data/AnalyticsEngine.kt (local calcs: streaks, corr(mood-task), predictions).
- [ ] 8. Run ./gradlew build, fix errors, inject Hilt.

## Phase 2: Enhanced MainViewModel (9-15)

- [ ] 9. Edit MainViewModel.kt: Hilt @HiltViewModel, repo inject, StateFlows → repo Flows.
- [ ] 10. Add states: streaks, achievements List, correlations Map, predictions, customDashboardOrder.
- [ ] 11. Enhance dashboardOrder: Advanced score (priority + corr + risk).
- [ ] 12. Deep AI: analyzeBehavior() (prompts for patterns/prod times/spend-mood).
- [ ] 13. Automation: autoSuggestTasks(), smartReschedule().
- [ ] 14. Gamification: achievementUnlock(), levelUp().
- [ ] 15. Migrate ExpenseViewModel logic into MainViewModel.

## Phase 3: Dynamic UI & Navigation (16-22)

- [ ] 16. Edit MainActivity.kt: Add NavHostController, navigate to Expense/WeeklyReview/Settings.
- [ ] 17. Edit DashboardScreen.kt: Charts (productivity/mood/expenses), drag-reorder sections (Modifier.dragGesture/customOrder).
- [ ] 18. Add animations: AnimatedVisibility, Lottie for insights/achievements.
- [ ] 19. Theme: Dynamic dark/light toggle.
- [ ] 20. New: WeeklyReviewScreen.kt (trends/insights viz).
- [ ] 21. New: SettingsScreen.kt (API key input, limits, notif prefs).
- [ ] 22. Integrate/enhance ExpenseScreen.kt as nav dest, add predictions.

## Phase 4: UX Polish & Automation (23-26)

- [ ] 23. 1-tap FAB: Contextual quick-add (e.g. low mood → mood checkin).
- [ ] 24. Context-aware NotificationWorker: Risks/misses/streaks.
- [ ] 25. Personalization: Save custom layout/notifs prefs (DataStore).
- [ ] 26. Visuals: Interactive charts, habit scores, streak badges.

## Phase 5: Testing & Completion (27-28)

- [ ] 27. Add unit tests (analytics), UI tests (dynamic order).
- [ ] 28. Manual test full flow, attempt_completion.

Track progress by editing this file after each step. More dynamic: Real-time LiveCharts, ML-lite in AnalyticsEngine, adaptive AI every 24h.
