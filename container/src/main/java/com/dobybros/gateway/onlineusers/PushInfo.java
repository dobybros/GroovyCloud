package com.dobybros.gateway.onlineusers;

import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.open.data.Message;

public class PushInfo {
	private Message event;
	private Data data;
	private Integer excludeTerminal;
	private Integer toTerminal;

	private SpecialHandler handler;
	
	public interface SpecialHandler {
		public void handle();
	}
	
	public PushInfo(SpecialHandler handler) {
		this.handler = handler;
	}

	public PushInfo(Message event, Integer excludeTerminal, Integer toTerminal) {
		this.event = event;
		this.excludeTerminal = excludeTerminal;
		this.toTerminal = toTerminal;
	}

	public PushInfo(Message event, Integer excludeTerminal) {
		this(event, excludeTerminal, null);
	}
	
	public PushInfo(Data data, Integer excludeTerminal, Integer toTerminal) {
		this.data = data;
		this.excludeTerminal = excludeTerminal;
		this.toTerminal = toTerminal;
	}

	public PushInfo(Data data, Integer excludeTerminal) {
		this(data, excludeTerminal, null);
	}
	
	/*@Override
	public int compareTo(PushInfo pushInfo) {
		Long seq = null;
		Long thisSeq = null;
		if(pushInfo.event != null) {
			seq = pushInfo.event.getSequence();
			thisSeq = this.event.getSequence();
			if(seq == null && pushInfo.event instanceof Event) {
				Event e = (Event) pushInfo.event;
				seq = e.getTime();
			}
			if(thisSeq == null && event instanceof Event) {
				thisSeq = ((Event)event).getTime();
			}
		}
		if(thisSeq == null)
			thisSeq = 0L;
		if(seq == null)
			seq = 0L;
		return (int) (thisSeq - seq);
	}*/

	public Message getEvent() {
		return event;
	}

	public void setEvent(Message event) {
		this.event = event;
	}

	public String getId() {
		String id = null;
		if(event != null) 
			id = event.getId();
		if(data != null)
			id = data.getId();
		return id;
	}

	public SpecialHandler getHandler() {
		return handler;
	}

	public void setHandler(SpecialHandler handler) {
		this.handler = handler;
	}

	public Integer getExcludeTerminal() {
		return excludeTerminal;
	}

	public void setExcludeTerminal(Integer excludeTerminal) {
		this.excludeTerminal = excludeTerminal;
	}

	public Integer getToTerminal() {
		return toTerminal;
	}

	public void setToTerminal(Integer toTerminal) {
		this.toTerminal = toTerminal;
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}
}