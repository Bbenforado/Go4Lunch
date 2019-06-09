package com.example.blanche.go4lunch;

import com.example.blanche.go4lunch.adapters.MessageViewHolder;

import org.junit.Test;

import static com.example.blanche.go4lunch.utils.Utils.getDistance;
import static com.example.blanche.go4lunch.utils.Utils.getFormattedOpeningHours;
import static com.example.blanche.go4lunch.utils.Utils.meterDistanceBetweenPoints;
import static com.example.blanche.go4lunch.utils.Utils.verifyUsernameLength;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }


    @Test
    public void calculateDistanceBetweenTwoPointsInMeters() {
        float latA = (float)48.870300;
        float lngA = (float)2.351114;

        float latB = (float)48.870610;
        float lngB = (float)2.348239;

        double result = meterDistanceBetweenPoints(latA, lngA, latB, lngB);
        assertEquals(212.87552253627214, result, 0.0);
    }

    @Test
    public void testGetDistance() {
        String position = "48.870610,2.348239";
        double lat = 48.870300;
        double lng = 2.351114;
        String result = getDistance(lat, lng, position);
        assertEquals("212 m", result);
    }

    @Test
    public void testGetFormattedOpeningHours() {
        String openingHours = "Monday: open 24h";
        assertEquals("open 24h", getFormattedOpeningHours(openingHours, "Monday: "));
        assertEquals("Monday: open 24h", getFormattedOpeningHours(openingHours, "Friday: "));
        assertNotEquals("Monday: open 24h", getFormattedOpeningHours(openingHours, "Monday: "));
    }

    @Test
    public void testVerifyUsernameLength() {
        String username = "henri jacques";
        assertEquals("henri j.", verifyUsernameLength(username));
        String shortName = "henri";
        assertEquals("henri", verifyUsernameLength(shortName));
    }

}