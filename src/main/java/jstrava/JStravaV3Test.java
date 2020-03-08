package jstrava;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class JStravaV3Test {

    String accessToken;
    int athleteId;
    long activityId;
    int updateActivityId;
    int clubId;
    String gearId;
    long segmentId;
    String secret;

    @Before
    public void setUp() throws Exception {

        /* TODO Test-Token verwenden!!!*/

        accessToken ="606945d..."; // hier vorab ein gültiges Zugriffstoken einbauen
        athleteId=13826;		// Bruno
        activityId=new Long("3222445021");
        clubId=228597;
        gearId="1665277";
        segmentId=0L;
        updateActivityId=222445021;
        secret = "<<secret>>";
    }

    @After
    public void tearDown() throws Exception {

    }


    @Test(expected = RuntimeException.class)
    public void testFailedConnection()
    {

            JStravaV3 strava= new JStravaV3("xxxxxxxx");
            System.out.println(strava.toString());
    }


    
	@Test
    public void testJStravaV3() throws Exception {

        JStravaV3 strava= new JStravaV3(accessToken);

        Athlete athlete=strava.getCurrentAthlete();
        assertNotNull(athlete);
    }


    
	@Test
    public void testFindAthlete() throws Exception {

        JStravaV3 strava= new JStravaV3(accessToken);

        Athlete athlete=strava.findAthlete(athleteId);
        assertNotNull(athlete);
        assertFalse(athlete.getBikes().isEmpty());
        assertFalse(athlete.getShoes().isEmpty());
        assertTrue(athlete.getClubs().isEmpty());
    }


    
	@Test
    public void testUpdateAthlete() throws Exception {

        JStravaV3 strava= new JStravaV3(accessToken);

        HashMap<String, String> optionalParameters= new HashMap<String, String>();

        String weight = "88";
        optionalParameters.put("weight",weight);
        Athlete athlete=strava.updateAthlete(optionalParameters);
        assertNotNull(athlete);
    }


    
	@Test
    public void testFindAthleteKOMs(){

        JStravaV3 strava= new JStravaV3(accessToken);
        List<SegmentEffort> efforts= strava.findAthleteKOMs(athleteId);

        assertFalse(efforts.isEmpty());
        for (SegmentEffort effort:efforts)
        {
            System.out.println("Segment Effort KOM " + effort.toString());

        }

    }


    
	@Test
    public void testGetCurrentAthleteFriends() throws Exception{

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Athlete> athletes= strava.getCurrentAthleteFriends();
        assertFalse(athletes.isEmpty());
        for (Athlete athlete:athletes)
        {
            System.out.println("Current Athlete Friends "+athlete.toString());
        }

    }

    
	@Test
    public void testGetCurrentAthleteFriendsWithPagination() throws Exception{

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Athlete> athletes= strava.getCurrentAthleteFriends(2,1);
        assertFalse(athletes.isEmpty());
        assertTrue(athletes.size()==1);
        for (Athlete athlete:athletes)
        {
            System.out.println("Current Athlete Friends "+athlete.toString());
        }

    }

    
	@Test
    public void testFindAthleteFriends() throws Exception{

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Athlete> athletes= strava.findAthleteFriends(athleteId);
        assertFalse(athletes.isEmpty());
        for (Athlete athlete:athletes)
        {
            System.out.println("Athlete Friends "+athlete.toString());
        }
    }

    
	@Test
    public void testFindAthleteFriendsWithPagination() throws Exception{

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Athlete> athletes= strava.findAthleteFriends(athleteId,2,1);
        assertFalse(athletes.isEmpty());
        assertTrue(athletes.size()==1);
        for (Athlete athlete:athletes)
        {
            System.out.println("Athlete Friends with pagination "+athlete.toString());
        }
    }



    
	@Test
    public void testGetCurrentAthleteFollowers() throws Exception{

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Athlete> athletes= strava.getCurrentAthleteFollowers();
        assertFalse(athletes.isEmpty());
        for (Athlete athlete:athletes)
        {
            System.out.println("Athlete Followers "+athlete.toString());
        }
    }

    
	@Test
    public void testGetCurrentAthleteFollowersWithPagination() throws Exception{

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Athlete> athletes= strava.getCurrentAthleteFollowers(2,1);
        assertTrue(athletes.size()==1);
        assertFalse(athletes.isEmpty());
        for (Athlete athlete:athletes)
        {
            System.out.println("Athlete Followers "+athlete.toString());
        }
    }

    
	@Test
    public void testFindAthleteFollowers() throws Exception{

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Athlete> athletes= strava.findAthleteFollowers(athleteId);
        assertFalse(athletes.isEmpty());
        for (Athlete athlete:athletes)
        {
            System.out.println("Athlete Followers "+athlete.toString());
        }
    }

    
	@Test
    public void testFindAthleteFollowersWithPagination() throws Exception{

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Athlete> athletes= strava.findAthleteFollowers(athleteId,2,1);
        assertTrue(athletes.size()==1);
        assertFalse(athletes.isEmpty());
        for (Athlete athlete:athletes)
        {
            System.out.println("Athlete Followers "+athlete.toString());
        }
    }

    
	@Test
    public void testFindAthleteBothFollowing() throws Exception{

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Athlete> athletes= strava.findAthleteBothFollowing(athleteId);
        assertFalse(athletes.isEmpty());
        for (Athlete athlete:athletes)
        {
            System.out.println("Athletes Both Following "+athlete.toString());
        }
    }

    
	@Test
    public void testFindAthleteBothFollowingWithPagination() throws Exception{

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Athlete> athletes= strava.findAthleteBothFollowing(athleteId,2,1);
        assertTrue(athletes.size()==1);
        assertFalse(athletes.isEmpty());
        for (Athlete athlete:athletes)
        {
            System.out.println("Athletes Both Following "+athlete.toString());
        }
    }

    
	@Test
    public void testCreateAndDeleteActivity() throws Exception {

        JStravaV3 strava= new JStravaV3(accessToken);

        Activity activity= strava.createActivity("Test Manual Activity", "ride", "2014-03-14T09:00:00Z", 10);
        assertNotNull(activity);
        System.out.println("Activity Name "+activity.toString());
        Activity activityExtra= strava.createActivity("Test Manual Activity","ride","2014-03-14T09:00:00Z",10,"Testing manual creation",100);
        assertNotNull(activityExtra);
        System.out.println("Activity Name "+activityExtra.toString());
        strava.deleteActivity(activity.getId());
        strava.deleteActivity(activityExtra.getId());
    }

    
	@Test
    public void testFindActivity() throws Exception {

        JStravaV3 strava= new JStravaV3(accessToken);

        Activity activity= strava.findActivity(activityId);
        assertNotNull(activity);
        System.out.println("Activity Name "+activity.toString());
        assertNotNull(activity.getAthlete());
        System.out.println("Athlete "+activity.getAthlete().getId());
        System.out.println("MAP"+activity.getMap().toString());

        assertFalse(activity.getSegment_efforts().isEmpty());
        for (SegmentEffort segmentEffort: activity.getSegment_efforts())
        {
            System.out.println("Segment Effort "+segmentEffort.toString());
            System.out.println("  Segment Effort Athlete"+segmentEffort.getAthlete().getId());
            assertNotNull(segmentEffort.getSegment());
            System.out.println("        Matching Segment "+segmentEffort.getSegment().toString());
        }
    }

    
	@Test
    public void testUpdateActivity() throws Exception {

        JStravaV3 strava= new JStravaV3(accessToken);

        HashMap<String, String> optionalParameters= new HashMap<String, String>();

        String description="Biebergrund Bike Marathon am 23.11.2014";
        String name="Biebergrund Bike Marathon";
        optionalParameters.put("description",description);
        optionalParameters.put("name",name);
        Activity activity=strava.updateActivity(updateActivityId,optionalParameters);
        assertNotNull(activity);
    }



    
	@Test
	public void testGetCurrentAthleteActivities()
    {
    	JStravaV3 strava= new JStravaV3(accessToken);
    	List<Activity> activities= strava.getCurrentAthleteActivities();
    	assertFalse(activities.isEmpty());
    	for (Activity activity:activities)
    	{
    		System.out.println("Current Athlete Activity "+activity.toString());
    	}
    }


    
	@Test
    public void testGetCurrentAthleteActivitiesWithPagination()
    {
        JStravaV3 strava= new JStravaV3(accessToken);
        List<Activity> activities= strava.getCurrentAthleteActivities(2,1);
        assertTrue(activities.size()==1);
        assertFalse(activities.isEmpty());
        for (Activity activity:activities)
        {
            System.out.println("Current Athlete Activity With Pagination "+activity.toString());
        }
    }

    
	@Test
    public void testGetCurrentFriendsActivities()
    {
        JStravaV3 strava= new JStravaV3(accessToken);
        List<Activity> activities= strava.getCurrentFriendsActivities();
        assertFalse(activities.isEmpty());
        for (Activity activity:activities)
        {
            System.out.println("Friend Activity "+activity.toString());
        }
    }

    
	@Test
    public void testGetCurrentFriendsActivitiesWithPagination()
    {
        JStravaV3 strava= new JStravaV3(accessToken);
        List<Activity> activities= strava.getCurrentFriendsActivities(2, 1);
        assertTrue(activities.size()==1);
        assertFalse(activities.isEmpty());
        for (Activity activity:activities)
        {
            System.out.println("Friend Activity "+activity.toString());
        }
    }

    
	@Test
    public void testFindActivityLaps() throws Exception{

        JStravaV3 strava= new JStravaV3(accessToken);
        List<LapEffort>laps=strava.findActivityLaps(activityId);

        assertFalse(laps.isEmpty());

        for (LapEffort lap:laps)
        {
            System.out.println("Lap "+ lap.toString());
        }
    }

    
	@Test
    public void testFindActivityComments() throws Exception{

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Comment> comments= strava.findActivityComments(activityId);
        assertFalse(comments.isEmpty());
        for (Comment comment:comments)
        {
            System.out.println(comment.getText());
        }
    }

    
	@Test
    public void testFindActivityCommentsWithPagination() throws Exception{

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Comment> comments= strava.findActivityComments(activityId,false,2,1);
        assertTrue(comments.size()==1);
        assertFalse(comments.isEmpty());
        for (Comment comment:comments)
        {
            System.out.println(comment.getText());
        }
    }

    
	@Test
    public void testFindActivityKudos() throws Exception{

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Athlete> athletes= strava.findActivityKudos(activityId);
        assertFalse(athletes.isEmpty());
        for (Athlete athlete:athletes)
        {
            System.out.println(athlete.toString());
        }
    }

    
	@Test
    public void testFindActivityKudosWithPagination() throws Exception{

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Athlete> athletes= strava.findActivityKudos(activityId,2,1);
        assertTrue(athletes.size()==1);
        assertFalse(athletes.isEmpty());
        for (Athlete athlete:athletes)
        {
            System.out.println(athlete.toString());
        }
    }


    
	@Test(expected = RuntimeException.class)
    public void testFindActivityPhotos(){

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Photo> photos= strava.findActivityPhotos(activityId);

        assertFalse(photos.isEmpty());
        for (Photo photo: photos)
        {
            System.out.println("Photo " + photo.toString());
        }
    }

    
	@Test
    public void testFindClubMembers() throws Exception{

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Athlete> athletes= strava.findClubMembers(clubId);
        assertFalse(athletes.isEmpty());
        for (Athlete athlete:athletes)
        {
            System.out.println("Club Member "+athlete.toString());
        }
    }

    
	@Test
    public void testFindClubMembersWithPagination() throws Exception{

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Athlete> athletes= strava.findClubMembers(clubId,2,1);
        assertTrue(athletes.size()==1);
        assertFalse(athletes.isEmpty());
        for (Athlete athlete:athletes)
        {
            System.out.println("Club Member "+athlete.toString());
        }
    }


    
	@Test(expected=RuntimeException.class)
    public void testFindClubActivities(){

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Activity> activities= strava.findClubActivities(clubId);
        assertFalse(activities.isEmpty());
        for (Activity activity:activities)
        {
            System.out.println("Club Activity Name "+activity.toString());
        }
    }


    
	@Test(expected=RuntimeException.class)
    public void testFindClubActivitiesWithPagination(){

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Activity> activities= strava.findClubActivities(clubId,2,1);
        assertTrue(activities.size()==1);
        assertFalse(activities.isEmpty());
        for (Activity activity:activities)
        {
            System.out.println("Club Activity Name "+activity.toString());
        }
    }

    
	@Test
    public void testFindClub() throws Exception {

        JStravaV3 strava= new JStravaV3(accessToken);

        Club club= strava.findClub(clubId);
        assertNotNull(club);
        System.out.println("Club Name " + club.toString());
    }


    
	@Test
    public void testFindGear() throws Exception {

        JStravaV3 strava= new JStravaV3(accessToken);

        Gear gear= strava.findGear(gearId);
        assertNotNull(gear);
        System.out.println("Gear Name " + gear.toString());
    }

    
    
	@Test
    public void testFindActivityStreams() throws Exception{

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Stream> streams= strava.findActivityStreams(activityId,new String[]{"latlng","time","distance"});
        assertNotNull(streams);

        for (Stream stream:streams)
        {
            System.out.println("STREAM TYPE "+stream.getType());
             for (int i=0;i<stream.getData().size();i++)
             {
//                 System.out.println("STREAM "+stream.getData().get(i));
             }
        }
    }

    
	@Test
    public void testFindActivityStreamsWithResolution() throws Exception{

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Stream> streams= strava.findActivityStreams(activityId,new String[]{"latlng","time","distance"},"low",null);
        assertNotNull(streams);

        for (Stream stream:streams)
        {
            System.out.println("STREAM TYPE "+stream.getType());
            for (int i=0;i<stream.getData().size();i++)
            {
                assertEquals("low",stream.getResolution());
//                System.out.println("STREAM " + stream.getData().get(i));
            }
        }
    }


    
	@Test
    public void testFindEffortStreams() throws Exception{

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Stream> streams= strava.findEffortStreams(activityId,new String[]{"latlng","time","distance"});
        assertNotNull(streams);

        for (Stream stream:streams)
        {
            System.out.println("STREAM TYPE "+stream.getType());
            for (int i=0;i<stream.getData().size();i++)
            {
//                System.out.println("STREAM "+stream.getData().get(i));
            }
        }
    }


    
	@Test
    public void testFindEffortStreamsWithResolution() throws Exception{

        JStravaV3 strava= new JStravaV3(accessToken);
        List<Stream> streams= strava.findEffortStreams(activityId,new String[]{"latlng","time","distance"},"low",null);
        assertNotNull(streams);

        for (Stream stream:streams)
        {
            System.out.println("STREAM TYPE "+stream.getType());
            for (int i=0;i<stream.getData().size();i++)
            {
                assertEquals("low",stream.getResolution());
//                System.out.println("STREAM " + stream.getData().get(i));
            }
        }
    }


    
	@Test
    public void testStravaAuthenticator() throws Exception {
    	String authurl = "";
    	StravaAuthenticator auth = new StravaAuthenticator(athleteId, "http://localhost", secret);
        assertNotNull(auth);  
        authurl = auth.getRequestAccessUrl("force", false, true, "mystate");
        System.out.println("Auth: " + authurl);
    }
    
    
	@Test
    public void testgetToken() throws Exception {
    	//String authurl = "";
    	StravaAuthenticator auth = new StravaAuthenticator(athleteId, "http://localhost", secret);
        assertNotNull(auth);  
    	AuthResponse authresp = auth.getToken("1a5d0e...");
        assertNotNull(authresp);  
        System.out.println("Token: " + authresp.getAccess_token());
    } 
    
    
	@Test
    public void testuploadActivity() throws Exception {
    	int cnt = 0;
    	long newactid = 0;
        JStravaV3 strava= new JStravaV3(accessToken);
    	File uploadfile = new File(System.getProperty("user.home") + "/roomsports/" + "test/RioMarina.tcx");
    	assertTrue(uploadfile.isFile());
    	
    	UploadStatus uploadstat = strava.uploadActivity("tcx", uploadfile); 
    	assertNotNull(uploadstat);
    	String id = uploadstat.getId();
    	System.out.println("Id: " + id);
    	System.out.println("Error: " + uploadstat.getError());
    	assertTrue(id.length() > 0);
    	while (uploadstat.getStatus().contains("processed") && cnt++ < 10) {
    		Thread.sleep(1000);
    		uploadstat = strava.checkUploadStatus(new Integer(id));
    	}
    	System.out.println("cnt: " + cnt);
    	newactid = uploadstat.getActivity_id();
    	System.out.println("Activity-Id: " + newactid);
    	assertTrue(cnt < 10);
    	
        HashMap<String, String> optionalParameters= new HashMap<String, String>();
        String description="RS-Test Training: RioMarina, eingefügt am 3.10.2016";
        String name="RioMarina";
        String acttype="virtualride";
        optionalParameters.put("description",description);
        optionalParameters.put("name",name);
        optionalParameters.put("activity_type", acttype);
        Activity activity=strava.updateActivity(newactid,optionalParameters);
        assertNotNull(activity);               
    }
}
