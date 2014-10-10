package com.cloudjay.cjay.event;

import com.cloudjay.cjay.model.Session;
;import java.util.List;

public class ContainerSearchedEvent {

	public List<Session> getSessions() {
		return sessions;
	}

	public List<Session> getTargets() {
		return sessions;
	}

	public Session getTarget() {
		if (isSingleChange()) {
			return sessions.get(0);
		} else {
			throw new IllegalStateException("Can only call this when isSingleChange returns true");
		}
	}

	private List<Session> sessions;

	public ContainerSearchedEvent(List<Session> sessions) {
		this.sessions = sessions;
	}

	public boolean isSingleChange() {
		return sessions.size() == 1;
	}
}
