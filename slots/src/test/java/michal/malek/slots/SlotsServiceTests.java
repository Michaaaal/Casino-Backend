package michal.malek.slots;

import michal.malek.slots.models.SlotsGameEntity;
import michal.malek.slots.repositories.SlotsGameRepository;
import michal.malek.slots.services.SlotsService;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.HashMap;

public class SlotsServiceTests {

    private SlotsService slotsService;
    private SlotsGameRepository slotsGameRepository;

    @BeforeEach
    void setUp() {
        slotsService = new SlotsService(slotsGameRepository);
    }

    @Test
    void testSpinReturnsCorrectEntity() {
        int stake = 10;
        int userId = 1;

        SlotsGameEntity result = slotsService.spin(stake, userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(stake, result.getStake());
        assertTrue(result.getWinAmount() >= 0);
        assertTrue(result.getMultiplier() >= 1);
        assertNotNull(result.getSlotMatrix());
    }

    @Test
    void testSpinWithNoWinningCombination() {
        slotsService = spy(slotsService);
        doReturn(generateNoWinningSlotsMatrix()).when(slotsService).getRandomSlotsMatrix();

        int stake = 10;
        int userId = 1;

        SlotsGameEntity result = slotsService.spin(stake, userId);

        assertEquals(0, result.getWinAmount());
        assertEquals(1, result.getMultiplier());
    }

    private HashMap<Integer, Integer> generateNoWinningSlotsMatrix() {
        HashMap<Integer, Integer> slotsMatrix = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            slotsMatrix.put(i, i);
        }
        return slotsMatrix;
    }


    @Test
    void testRandomSlotsMatrixSize() {
        SlotsService yourClass = slotsService;
        HashMap<Integer, Integer> matrix = yourClass.getRandomSlotsMatrix();
        assertEquals(12, matrix.size(), "Map should have exactly 12 entries");
    }

    @Test
    void testRandomSlotsMatrixRange() {
        SlotsService yourClass = slotsService;
        HashMap<Integer, Integer> matrix = yourClass.getRandomSlotsMatrix();
        boolean allInRange = matrix.values().stream().allMatch(value -> value >= 1 && value <= 8);
        assertTrue(allInRange, "All values should be between 1 and 8");
    }

    @Test
    void testRandomSlotsMatrixRandomness() {
        SlotsService yourClass = slotsService;
        HashMap<Integer, Integer> firstMatrix = yourClass.getRandomSlotsMatrix();
        HashMap<Integer, Integer> secondMatrix = yourClass.getRandomSlotsMatrix();
        assertFalse(firstMatrix.equals(secondMatrix), "It is unlikely that two consecutive calls to getRandomSlotsMatrix return the same mapping");
    }
}
