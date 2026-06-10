package sum3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Greenfield Telemetry System Tests")
class TelemetrySystemTest {

    @Nested
    @DisplayName("TelemetryEvent Subclasses")
    class EventTests {

        @Test
        @DisplayName("MovementEvent returns correct summary and inherits fields")
        void testMovementEvent() {
            MovementEvent move = new MovementEvent("RobotAlpha", 1622500000L, 10, 25);
            assertEquals("RobotAlpha", move.getRobotId());
            assertEquals(1622500000L, move.getTimestamp());
            assertEquals("Moved to [10, 25]", move.getEventSummary());
        }

        @Test
        @DisplayName("CombatEvent returns correct summary")
        void testCombatEvent() {
            CombatEvent combat = new CombatEvent("RobotBeta", 1622500100L, 50);
            assertEquals("Engaged in combat, dealt 50 damage", combat.getEventSummary());
        }

        @Test
        @DisplayName("CombatEvent throws exception on negative damage")
        void testCombatEventNegativeDamage() {
            assertThrows(IllegalArgumentException.class, () -> {
                new CombatEvent("RobotBeta", 1622500100L, -10);
            }, "Should throw IllegalArgumentException for negative damage");
        }
    }

    @Nested
    @DisplayName("TelemetryTracker Functionality")
    class TrackerTests {

        private TelemetryTracker tracker;

        @BeforeEach
        void setUp() {
            tracker = new TelemetryTracker();
        }

        @Test
        @DisplayName("Tracker correctly records and retrieves events for a robot")
        void testRecordAndRetrieveEvents() {
            MovementEvent move1 = new MovementEvent("R1", 100L, 0, 1);
            MovementEvent move2 = new MovementEvent("R1", 200L, 0, 2);
            CombatEvent combat = new CombatEvent("R2", 300L, 10);

            tracker.recordEvent(move1);
            tracker.recordEvent(move2);
            tracker.recordEvent(combat);

            List<TelemetryEvent> r1Events = tracker.getRobotEvents("R1");
            List<TelemetryEvent> r2Events = tracker.getRobotEvents("R2");

            assertEquals(2, r1Events.size(), "R1 should have 2 events recorded");
            assertEquals(1, r2Events.size(), "R2 should have 1 event recorded");
        }

        @Test
        @DisplayName("getRobotEvents returns empty list for unknown robot")
        void testRetrieveUnknownRobot() {
            List<TelemetryEvent> events = tracker.getRobotEvents("Unknown");
            assertNotNull(events, "Should return an empty list, not null");
            assertTrue(events.isEmpty(), "List should be empty");
        }

        @Test
        @DisplayName("getTotalDamageDealt correctly sums only CombatEvents")
        void testTotalDamageCalculation() {
            tracker.recordEvent(new MovementEvent("Hunter", 10L, 5, 5));
            tracker.recordEvent(new CombatEvent("Hunter", 20L, 40));
            tracker.recordEvent(new CombatEvent("Hunter", 30L, 60));
            tracker.recordEvent(new CombatEvent("Scout", 40L, 15)); // Different robot

            int hunterDamage = tracker.getTotalDamageDealt("Hunter");
            int scoutDamage = tracker.getTotalDamageDealt("Scout");
            int unknownDamage = tracker.getTotalDamageDealt("Unknown");

            assertEquals(100, hunterDamage, "Hunter should have 100 total damage (40 + 60)");
            assertEquals(15, scoutDamage, "Scout should have 15 total damage");
            assertEquals(0, unknownDamage, "Unknown robot should have 0 damage");
        }
    }
}