package handling.login.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.MaplePacketHandler;
import handling.login.LoginWorker;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.KoreanDateUtil;
import java.util.Calendar;
import handling.channel.ChannelServer;


public class LoginPasswordHandler implements MaplePacketHandler {

	@Override
	public boolean validateState(MapleClient c) {
		return !c.isLoggedIn();
	}

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		String login = slea.readMapleAsciiString();
		String pwd = slea.readMapleAsciiString();
		
		if (login.startsWith("_")) {
			String loginFix = login.replaceFirst("_", "");
			int ok = c.fix(loginFix, pwd);
			if (ok != 0)
				c.getSession().write(MaplePacketCreator.getLoginFailed(ok));
			else
				c.getSession().write(MaplePacketCreator.getLoginFailed(7));
			return;
		}

		c.setAccountName(login);

		int loginok = 0;
		boolean ipBan = c.hasBannedIP();
		boolean macBan = c.hasBannedMac();

		loginok = c.login(login, pwd, ipBan || macBan);

		Calendar tempbannedTill = c.getTempBanCalendar();
		if (loginok == 0 && (ipBan || macBan)) {
			loginok = 3;

			if (macBan) {
				String[] ipSplit = c.getSession().getRemoteAddress().toString().split(":");
				MapleCharacter.ban(ipSplit[0], "Enforcing account ban, account " + login, false);
			}
		}

		if (loginok == 7) { 
			for (ChannelServer cs : ChannelServer.getAllInstances()) {
				for (MapleCharacter mc : cs.getPlayerStorage().getAllCharacters()) {
					if (mc.getClient() != null) { 
						if (mc.getClient().getAccountName().equalsIgnoreCase(login)) {
							if (mc.getClient().getSession() != null) { 
								if (!mc.getClient().getSession().isConnected()) {
									mc.getClient().disconnect(); 
									loginok = 0; 
								}
							}
						}
					}
				}
			}
		}
                if (loginok == 3) {
                        c.getSession().write(MaplePacketCreator.getPermBan((byte) 1));
                        return;
                } 
		if (loginok != 0) {
			c.getSession().write(MaplePacketCreator.getLoginFailed(loginok));
			return;
		}else if (tempbannedTill.getTimeInMillis() != 0) {
			long tempban = KoreanDateUtil.getTempBanTimestamp(tempbannedTill.getTimeInMillis());
			byte reason = c.getBanReason();
			c.getSession().write(MaplePacketCreator.getTempBan(tempban, reason));
			return;
		}
		if (c.isGm()) {
			LoginWorker.getInstance().registerGMClient(c);
		} else {
			LoginWorker.getInstance().registerClient(c);
		}
	}
}
