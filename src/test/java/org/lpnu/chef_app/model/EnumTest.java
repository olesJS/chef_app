package org.lpnu.chef_app.model.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnumTest {

    @Test
    void testAllEnums() {
        for (Allergen a : Allergen.values()) {
            assertNotNull(a.getDisplayName());
            assertEquals(a.getDisplayName(), a.toString());
            assertNotNull(Allergen.valueOf(a.name()));
        }

        for (ProcessingState state : ProcessingState.values()) {
            assertNotNull(state.getDisplayName());
            assertTrue(state.getMassFactor() >= 0.8);
            assertTrue(state.getKcalFactor() >= 0.9);
            assertEquals(state.getDisplayName(), state.toString());
        }

        for (ProductType type : ProductType.values()) {
            assertNotNull(type.getDisplayName());
            assertEquals(type.getDisplayName(), type.toString());
        }
    }
}