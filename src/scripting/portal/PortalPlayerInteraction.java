package scripting.portal;

import client.MapleClient;
import scripting.AbstractPlayerInteraction;
import server.MaplePortal;

public class PortalPlayerInteraction extends AbstractPlayerInteraction {
    private MaplePortal portal;
	
    public PortalPlayerInteraction(MapleClient c, MaplePortal portal) {
	super (c);
	this.portal = portal;
    }
	
    public MaplePortal getPortal() {
	return portal;
    }
}
