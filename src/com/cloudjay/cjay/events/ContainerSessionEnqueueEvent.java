package com.cloudjay.cjay.events;

import java.util.ArrayList;
import java.util.List;

import com.cloudjay.cjay.model.ContainerSession;

/**
 * 
 * Trigger when user add container session to queue (upload confirmation = true)
 * 
 * @author tieubao
 * 
 */

public class ContainerSessionEnqueueEvent {

	private final List<ContainerSession> listContainerSessions;

	public ContainerSessionEnqueueEvent(List<ContainerSession> uploads) {
		listContainerSessions = uploads;
	}

	public ContainerSessionEnqueueEvent(ContainerSession upload) {
		listContainerSessions = new ArrayList<ContainerSession>();
		listContainerSessions.add(upload);
	}

	public List<ContainerSession> getTargets() {
		return listContainerSessions;
	}

	public ContainerSession getTarget() {
		if (isSingleChange()) {
			return listContainerSessions.get(0);
		} else {
			throw new IllegalStateException(
					"Can only call this when isSingleChange returns true");
		}
	}

	public boolean isSingleChange() {
		return listContainerSessions.size() == 1;
	}

}