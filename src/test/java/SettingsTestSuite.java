import org.example.Settings;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class SettingsTestSuite {

    private Settings settings;

    @Before
    public void setUp() {
        settings = new Settings();
    }

    @Test
    public void testAddReceipt() {
        settings.addReceipt("taxi", 50.0);
        double total = settings.calculateTotalReimbursement(1, 0);
        assertEquals(65.0, total, 0.01);
    }

    @Test
    public void testCalculateTotalReimbursement() {
        double total = settings.calculateTotalReimbursement(2, 10);
        assertEquals(33.0, total, 0.01);
    }
}