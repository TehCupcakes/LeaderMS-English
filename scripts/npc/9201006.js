var status;
var x;

	
	function start() { 
	    status = -1; 
	    action(1, 0, 0); 
	} 
	
	function action(mode, type, selection) { 
	     if (mode == -1 || mode == 0) {
	        cm.sendOk("Bye");
	            cm.dispose();
	                        return;
	    } else if (mode == 1) {
	            status++;
	        } else {
	            status--;
	        }
		if (cm.getPlayer().getMapId() == 680000210){
	    if (status == 0) {
	                var text = "";
	                var choice = new Array("When does the wedding begin?", "I want out!");
	                for (x = 0; x < choice.length; x++) {
	                        text += "\r\n#L" + x + "##b" + choice[x] + "#l";
	                }
	                cm.sendSimple(text);
	        } else if (status == 1) {
	                switch(selection) {
	                        case 0:
	                                cm.sendOk("We will wait until the bride and groom are ready. Please wait a few minutes!");
	                                cm.dispose();
	                                break;
	                        case 1:
								   cm.removeAll(5251100);
	                               cm.warp("680000000");
								   cm.dispose();
	                                break;
	                        }                
					}		
			} else if (cm.getPlayer().getMapId() == 680000200){
			cm.sendOk("Uhh, sorry for the delay. Father John went to do something very quickly. It should not be long; please wait for us to start.");
			cm.dispose();
			}
	}
			