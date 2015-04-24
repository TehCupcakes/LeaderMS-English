package handling.login;

public interface LoginServerMBean {
	int getNumberOfSessions();
	int getLoginInterval();

	String getEventMessage();
	int getFlag();

	void setEventMessage(String newMessage);
	void setFlag(int flag);

	int getUserLimit();
	void setUserLimit(int newLimit);
}
