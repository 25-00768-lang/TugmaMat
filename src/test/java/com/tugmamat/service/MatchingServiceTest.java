package com.tugmamat.service;

import static org.junit.jupiter.api.Assertions.*;

import com.tugmamat.model.Enums.Category;
import com.tugmamat.model.Enums.Condition;
import com.tugmamat.model.Material;
import com.tugmamat.model.MaterialRequest;
import org.junit.jupiter.api.Test;

/** Tests the compatibility score using the demo data (no database needed). */
class MatchingServiceTest {

    private final MatchingService service = new MatchingService(null);

    private MaterialRequest request() {
        MaterialRequest r = new MaterialRequest();
        r.name = "2x4 lumber"; r.category = Category.KAHOY;
        r.lengthCm = 240.0; r.widthCm = 5.0; r.quantity = 5;
        r.desiredCondition = Condition.GOOD; r.location = "Batangas";
        return r;
    }

    private Material material(String name, Category cat, String desc, double len, double wid,
                              int qty, Condition cond, String loc) {
        Material m = new Material();
        m.name = name; m.category = cat; m.description = desc;
        m.lengthCm = len; m.widthCm = wid; m.quantity = qty; m.unit = "pcs";
        m.matCondition = cond; m.location = loc;
        return m;
    }

    @Test
    void closeMatchScoresHigh() {
        Material m = material("Wood 2x4 lumber", Category.KAHOY, "Still dry and straight.", 244, 5, 3, Condition.GOOD, "Batangas City");
        MatchResult res = service.score(m, request());
        assertEquals(92, res.score);
        assertTrue(res.isExact());
        assertFalse(res.unmet.isEmpty()); // not enough quantity (3 of 5)
    }

    @Test
    void lowerConditionAndShorterLengthLowerTheScore() {
        Material m = material("Wood 2x3 lumber", Category.KAHOY, "A bit old but sturdy.", 200, 3, 6, Condition.FAIR, "Batangas City");
        assertEquals(80, service.score(m, request()).score);
    }

    @Test
    void differentCategoryIsOnlyAnAlternative() {
        Material m = material("Marine plywood 1/2 inch", Category.PLYWOOD, "One full sheet.", 244, 122, 1, Condition.GOOD, "Batangas City");
        MatchResult res = service.score(m, request());
        assertEquals(44, res.score);
        assertFalse(res.isExact());
        assertTrue(res.score >= MatchingService.MIN_THRESHOLD);
    }

    @Test
    void scoreNeverExceeds100() {
        Material m = material("2x4 lumber", Category.KAHOY, "", 300, 10, 50, Condition.NEW, "Batangas");
        assertEquals(100, service.score(m, request()).score);
    }
}
