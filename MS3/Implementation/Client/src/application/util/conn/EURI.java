package application.util.conn;

/**
 * URIs.
 * @author Leonid Vilents
 */
public enum EURI {
	
	SIGNUP ("%ssignup"),
	SIGNOUT ("%ssignout"),
	LOGIN ("%slogin"),
	LOGOUT ("%slogout"),
	DASHBOARD ("%sdashboard"),
	SEEKSTATUS ("%sseek"),
	PROFILE_CM ("%sprofile/casemodder/%d"),
	PROFILE_SP ("%sprofile/sponsor/%d"),
	MESSAGES ("%smessages/"),
	MESSAGE ("%smessages/%d"),
	PROJECTS ("%sprojects/"),
	PROJECT ("%sprojects/%d"),
	PROJECTUPDATES ("%sprojectupdates/"),
	PROJECTUPDATE ("%sprojectupdates/%d"),
	SPONSORING ("%ssponsoring/"),
	TEAM ("%ssponsoring/%d"),
	COMMENT_PROJECT ("%scomment/project/%d"),
	COMMENT_PROJECTUPDATE ("%scomment/projectupdate/%d"),
	UPVOTE_COMMENT ("%supvote/comment/%d"),
	UPVOTE_PROJECT ("%supvote/project/%d"),
	UPVOTE_PROJECTUPDATE ("%supvote/projectupdate/%d");
	
	private final String uri;
	
	EURI(String uri) { this.uri = uri; }
	
	public String uri() { return uri; }
}
