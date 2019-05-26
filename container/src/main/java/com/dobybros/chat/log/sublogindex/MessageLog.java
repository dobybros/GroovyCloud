package com.dobybros.chat.log.sublogindex;

import com.dobybros.chat.log.LogIndex;
import com.dobybros.chat.open.data.Message;

/**
 * This class provides common fields of log of need indexing
 * 
 * @author Aplomb
 * 
 */
public class MessageLog extends LogIndex {
	public static final String FIELD_TOPIDLOG_MESSAGE = "msg";
	public static final String FIELD_TOPIDLOG_TERMINAL = "t";
	private Integer terminal;
//
	private Message message;
	
	public MessageLog() {
		setLogType(LogType.Message);
		setIndexType(LogType.Message.getName());
	}

//	@Override
//	public void fromDocument(Document dbObj) {
//		super.fromDocument(dbObj);
//		Document topicObj = (Document) dbObj.get(FIELD_TOPIDLOG_MESSAGE);
//		if(topicObj != null) {
//			message = new Message();
//			message.fromDocument(topicObj);
//		}
//		Number terminalNum = (Number) dbObj.get(FIELD_TOPIDLOG_TERMINAL);
//		if(terminalNum != null)
//			terminal = terminalNum.byteValue();
//	}
//
//	@Override
//	public Document toDocument() {
//		Document dbObj = super.toDocument();
//		dbObj.put(FIELD_TOPIDLOG_MESSAGE, message.toDocument());
//		dbObj.put(FIELD_TOPIDLOG_TERMINAL, terminal);
//		return dbObj;
//	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public Integer getTerminal() {
		return terminal;
	}

	public void setTerminal(Integer terminal) {
		this.terminal = terminal;
	}
}
