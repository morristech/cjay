package com.cloudjay.cjay.events;

import java.util.ArrayList;
import java.util.List;

import com.cloudjay.cjay.model.ContainerSession;

public class ContainerSessionAddedEvent {

	private final List<ContainerSession> listContainerSessions;

	public ContainerSessionAddedEvent(List<ContainerSession> uploads) {
		listContainerSessions = uploads;
	}

	public ContainerSessionAddedEvent(ContainerSession upload) {
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