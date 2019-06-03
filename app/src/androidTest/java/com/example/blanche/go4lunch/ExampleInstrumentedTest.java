package com.example.blanche.go4lunch;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;

import com.example.blanche.go4lunch.activities.MainActivity;
import com.example.blanche.go4lunch.fragments.PageFragment;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.example.blanche.go4lunch", appContext.getPackageName());
    }

    @Rule
    public ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Before
    public void init() throws Throwable {
        Context targetContext = getInstrumentation().getTargetContext();
    }

    @Test
    public void openAndCloseSearchFieldTest() {
        onView(withId(R.id.search_item)).perform(click());
        onView(withId(R.id.idCardView)).check(matches(isDisplayed()));
        pressBack();
        onView(withId(R.id.idCardView)).check(matches(not(isDisplayed())));
    }

    @Test
    public void userCanTypeInSearchFieldTest() {
        onView(withId(R.id.search_item)).perform(click());
        onView(withId(R.id.autocomplete_textview)).perform(typeText("something"));
    }

    @Test
    public void userCanTypeAndClearTextInSearchFieldTest() {
        onView(withId(R.id.search_item)).perform(click());
        onView(withId(R.id.autocomplete_textview)).perform(typeText("something"));
        onView(withId(R.id.clear_text_button)).perform(click());

    }

}
