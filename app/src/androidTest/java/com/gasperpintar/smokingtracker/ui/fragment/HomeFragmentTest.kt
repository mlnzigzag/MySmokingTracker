package com.gasperpintar.smokingtracker.ui.fragment

import android.content.Context
import android.content.pm.ActivityInfo
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.activity.MainActivity
import com.gasperpintar.smokingtracker.database.Provider
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.database.repository.HistoryRepository
import com.gasperpintar.smokingtracker.database.repository.SettingsRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class HomeFragmentTest {
    private lateinit var context: Context
    private lateinit var historyRepository: HistoryRepository

    @Before
    fun setup() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        val database = Provider.getDatabase(context)
        historyRepository = HistoryRepository(database.historyDao())
        historyRepository.deleteAll()
        val settingsRepository = SettingsRepository(database.settingsDao())
        settingsRepository.get()?.let {
            settingsRepository.update(it.copy(dayEndMinutes = 0))
        } ?: settingsRepository.insert(SettingsEntity.default().copy(dayEndMinutes = 0))
    }

    @Test
    fun oneTapAddsOneSmokedNowAndUpdatesMinimalHome() {
        val before = LocalDateTime.now().minusSeconds(2)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
            onView(withId(R.id.today_count)).check(matches(withText("0")))
            onView(withId(R.id.button_add_cigarette)).perform(click())
            val entries = awaitEntries()
            assertEquals(1, entries.size)
            assertEquals(0, entries.single().lent)
            assertTrue(entries.single().createdAt >= before)
            assertTrue(entries.single().createdAt <= LocalDateTime.now().plusSeconds(2))
            onView(withId(R.id.today_count)).check(matches(withText("1")))
            onView(withId(R.id.timer_label)).check { view, _ ->
                assertEquals(ContextCompat.getColor(context, R.color.home_timer_warning), (view as TextView).currentTextColor)
            }
            scenario.onActivity { activity ->
                val counter = activity.findViewById<android.view.View>(R.id.today_counter_region)
                val pager = activity.findViewById<android.view.View>(R.id.main_view_pager)
                assertTrue(counter.height * 5 >= pager.height)
            }
        }
    }

    private fun awaitEntries(): List<HistoryEntity> {
        repeat(50) {
            val entries = runBlocking { historyRepository.getAll() }
            if (entries.size == 1) return entries
            Thread.sleep(100)
        }
        return runBlocking { historyRepository.getAll() }
    }
}
