package application.util;

/**
 * URIs.
 * @author Leonid Vilents
 */
public enum EURI {
	
	SIGNUP ("%ssignup"),
	SIGNOUT ("%signout"),
	LOGIN ("%slogin"),
	LOGOUT ("%slogout"),
	DASHBOARD ("%sdashboard"),
	SEEKSTATUS ("%sseek"),
	PROFILE_CM ("%sprofile/casemodder/%d"),
	PROFILE_SP ("%sprofile/sponsor/%d"),
	MESSAGES ("%smessages/index"),
	MESSAGE_NEW ("%smessages/new"),
	MESSAGE ("%smessages/%d"),
	PROJECTS ("%sprojects/index"),
	PROJECT_NEW ("%sprojects/new"),
	PROJECT ("%sprojects/%d"),
	PROJECTUPDATE_NEW ("%sprojectupdates/new"),
	PROJECTUPDATE ("%sprojectupdates/%d"),
	SPONSORING ("%ssponsoring/index"),
	TEAM_NEW ("%ssponsoring/new"),
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
