/**
 * 
 */
package com.uit.upload;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * @author John
 *
 */
public class MySessionCounter implements HttpSessionListener {

	private static int activeSessions = 0;

	public void sessionCreated(HttpSessionEvent se) {
		activeSessions++;
	}

	public void sessionDestroyed(HttpSessionEvent se) {
		if (activeSessions > 0)
			activeSessions--;
	}

	public static int getActiveSessions() {
		return activeSessions;
	}

}
