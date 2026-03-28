package com.example.kalorientracker.testutil

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

fun fixedTestClock(): Clock = Clock.fixed(
    Instant.parse("2026-03-28T10:15:30Z"),
    ZoneOffset.UTC
)
