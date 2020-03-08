import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;



@RunWith(Suite.class)
@SuiteClasses({ 
	AuswertungTest.class, 
	GegnerTest.class, 
	KonfigurationTest.class,
	RsmainTest.class,
	TrainerTest.class,
	WettkampfTest.class //,
//    JStravaV3.class
})
public class AllTests {

}
