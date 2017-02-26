package com.esloq.esloqapp.locklist;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.esloq.esloqapp.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Tests for the lock list screen, the main screen which contains a list of all locks.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LockListScreenTest {

    @Rule
    public ActivityTestRule<LockListActivity> mActivityRule = new ActivityTestRule<>(
            LockListActivity.class);

    @Test
    public void testClickAddLockButton_opensScanUi(){
        // Click on the add note button
        onView(withId(R.id.fab_add_lock)).perform(click());

        // Check if the scan screen is displayed
        onView(withId(R.id.scan_list)).check(matches(isDisplayed()));
    }

}
