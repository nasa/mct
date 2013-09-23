package gov.nasa.arc.mct.fastplot.scatter;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestImplicitTimeAxis {
	private ImplicitTimeAxis testAxis;
	
	private static final int    TEST_COUNT  = 10;
	
	private static final long[] TEST_STARTS = new long[TEST_COUNT];
	private static final long[] TEST_ENDS   = new long[TEST_COUNT];
	private static final long[] TEST_SHIFTS = new long[TEST_COUNT];

	private static final boolean[] BOOLEANS = { false, true };
	private static final double EPSILON = 0.999999;
	
	@BeforeMethod
	public void setup() {
		testAxis = new ImplicitTimeAxis();
		for (int i = 0; i < TEST_COUNT; i++) {
			TEST_STARTS[i] = 1000L * i * i * i;
			TEST_ENDS  [i] = 2000L * i * i * i * i;
			TEST_SHIFTS[i] = -50000L + 1000L* i * i * i;
		}
	}
	
	
	@Test
	public void testShift() {
		for (int i = 0; i < TEST_COUNT; i++) {
			for (int s = 0; s < TEST_COUNT; s++) {
				for (boolean longStart : BOOLEANS) {
					for (boolean longEnd : BOOLEANS) {
						if (longStart) testAxis.setStart(         TEST_STARTS[i]);
						else           testAxis.setStart((double) TEST_STARTS[i]);
						if (longEnd)   testAxis.setEnd  (         TEST_ENDS  [i]);
						else           testAxis.setEnd  ((double) TEST_ENDS  [i]);
						
						testAxis.shift(TEST_SHIFTS[s]);
						
						Assert.assertEquals(testAxis.getStartAsLong(), TEST_STARTS[i] + TEST_SHIFTS[s]);
						Assert.assertEquals(testAxis.getEndAsLong()  , TEST_ENDS  [i] + TEST_SHIFTS[s]);
						Assert.assertEquals(testAxis.getStart()      , (double) (TEST_STARTS[i]+ TEST_SHIFTS[s]), EPSILON);
						Assert.assertEquals(testAxis.getEnd()        , (double) (TEST_ENDS  [i]+ TEST_SHIFTS[s]), EPSILON);
					}
				}
			}
		}
	}
	
	
	@Test
	public void testGetterSetters() {
		for (int i = 0; i < TEST_COUNT; i++) {
			for (boolean longStart : BOOLEANS) {
				for (boolean longEnd : BOOLEANS) {
					if (longStart) testAxis.setStart(         TEST_STARTS[i]);
					else           testAxis.setStart((double) TEST_STARTS[i]);
					if (longEnd)   testAxis.setEnd  (         TEST_ENDS  [i]);
					else           testAxis.setEnd  ((double) TEST_ENDS  [i]);
					
					Assert.assertEquals(testAxis.getStartAsLong(), TEST_STARTS[i]);
					Assert.assertEquals(testAxis.getEndAsLong()  , TEST_ENDS  [i]);
					Assert.assertEquals(testAxis.getStart()      , (double) TEST_STARTS[i], EPSILON);
					Assert.assertEquals(testAxis.getEnd()        , (double) TEST_ENDS  [i], EPSILON);
				}
			}

		}
	}
}
