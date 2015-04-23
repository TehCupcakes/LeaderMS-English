/*
 *Sky Blue Ballon - Stage 7 of LPQ =D
  *@author Jvlaple
  */

importPackage(Packages.tools);
importPackage(Packages.server.life);
importPackage(java.awt);

var status;
var partyLdr;
var chatState;
var party;
var preamble;

var stage8combos = Array(Array(0, 0, 0, 0, 1, 1, 1, 1, 1), 
						Array(0, 0, 0, 1, 0, 1, 1, 1, 1),
						Array(0, 0, 0, 1, 1, 0, 1, 1, 1),
						Array(0, 0, 0, 1, 1, 1, 0, 1, 1),
						Array(0, 0, 0, 1, 1, 1, 1, 0, 1),
						Array(0, 0, 0, 1, 1, 1, 1, 1, 0),
						Array(0, 0, 1, 0, 0, 1, 1, 1, 1),
						Array(0, 0, 1, 0, 1, 0, 1, 1, 1),
						Array(0, 0, 1, 0, 1, 1, 0, 1, 1),
						Array(0, 0, 1, 0, 1, 1, 1, 0, 1),
						Array(0, 0, 1, 0, 1, 1, 1, 1, 0),
						Array(0, 0, 1, 1, 0, 0, 1, 1, 1),
						Array(0, 0, 1, 1, 0, 1, 0, 1, 1),
						Array(0, 0, 1, 1, 0, 1, 1, 0, 1),
						Array(0, 0, 1, 1, 0, 1, 1, 1, 0),
						Array(0, 0, 1, 1, 1, 0, 0, 1, 1),
						Array(0, 0, 1, 1, 1, 0, 1, 0, 1),
						Array(0, 0, 1, 1, 1, 0, 1, 1, 0),
						Array(0, 0, 1, 1, 1, 1, 0, 0, 1),
						Array(0, 0, 1, 1, 1, 1, 0, 1, 0),
						Array(0, 0, 1, 1, 1, 1, 1, 0, 0),
						Array(0, 1, 0, 0, 0, 1, 1, 1, 1),
						Array(0, 1, 0, 0, 1, 0, 1, 1, 1),
						Array(0, 1, 0, 0, 1, 1, 0, 1, 1),
						Array(0, 1, 0, 0, 1, 1, 1, 0, 1),
						Array(0, 1, 0, 0, 1, 1, 1, 1, 0),
						Array(0, 1, 0, 1, 0, 0, 1, 1, 1),
						Array(0, 1, 0, 1, 0, 1, 0, 1, 1),
						Array(0, 1, 0, 1, 0, 1, 1, 0, 1),
						Array(0, 1, 0, 1, 0, 1, 1, 1, 0),
						Array(0, 1, 0, 1, 1, 0, 0, 1, 1),
						Array(0, 1, 0, 1, 1, 0, 1, 0, 1),
						Array(0, 1, 0, 1, 1, 0, 1, 1, 0),
						Array(0, 1, 0, 1, 1, 1, 0, 0, 1),
						Array(0, 1, 0, 1, 1, 1, 0, 1, 0),
						Array(0, 1, 0, 1, 1, 1, 1, 0, 0),
						Array(0, 1, 1, 0, 0, 0, 1, 1, 1),
						Array(0, 1, 1, 0, 0, 1, 0, 1, 1),
						Array(0, 1, 1, 0, 0, 1, 1, 0, 1),
						Array(0, 1, 1, 0, 0, 1, 1, 1, 0),
						Array(0, 1, 1, 0, 1, 0, 0, 1, 1),
						Array(0, 1, 1, 0, 1, 0, 1, 0, 1),
						Array(0, 1, 1, 0, 1, 0, 1, 1, 0),
						Array(0, 1, 1, 0, 1, 1, 0, 0, 1),
						Array(0, 1, 1, 0, 1, 1, 0, 1, 0),
						Array(0, 1, 1, 0, 1, 1, 1, 0, 0),
						Array(0, 1, 1, 1, 0, 0, 0, 1, 1),
						Array(0, 1, 1, 1, 0, 0, 1, 0, 1),
						Array(0, 1, 1, 1, 0, 0, 1, 1, 0),
						Array(0, 1, 1, 1, 0, 1, 0, 0, 1),
						Array(0, 1, 1, 1, 0, 1, 0, 1, 0),
						Array(0, 1, 1, 1, 0, 1, 1, 0, 0),
						Array(0, 1, 1, 1, 1, 0, 0, 0, 1),
						Array(0, 1, 1, 1, 1, 0, 0, 1, 0),
						Array(0, 1, 1, 1, 1, 0, 1, 0, 0),
						Array(0, 1, 1, 1, 1, 1, 0, 0, 0),
						Array(1, 0, 0, 0, 0, 1, 1, 1, 1),
						Array(1, 0, 0, 0, 1, 0, 1, 1, 1),
						Array(1, 0, 0, 0, 1, 1, 0, 1, 1),
						Array(1, 0, 0, 0, 1, 1, 1, 0, 1),
						Array(1, 0, 0, 0, 1, 1, 1, 1, 0),
						Array(1, 0, 0, 1, 0, 0, 1, 1, 1),
						Array(1, 0, 0, 1, 0, 1, 0, 1, 1),
						Array(1, 0, 0, 1, 0, 1, 1, 0, 1),
						Array(1, 0, 0, 1, 0, 1, 1, 1, 0),
						Array(1, 0, 0, 1, 1, 0, 0, 1, 1),
						Array(1, 0, 0, 1, 1, 0, 1, 0, 1),
						Array(1, 0, 0, 1, 1, 0, 1, 1, 0),
						Array(1, 0, 0, 1, 1, 1, 0, 0, 1),
						Array(1, 0, 0, 1, 1, 1, 0, 1, 0),
						Array(1, 0, 0, 1, 1, 1, 1, 0, 0),
						Array(1, 0, 1, 0, 0, 0, 1, 1, 1),
						Array(1, 0, 1, 0, 0, 1, 0, 1, 1),
						Array(1, 0, 1, 0, 0, 1, 1, 0, 1),
						Array(1, 0, 1, 0, 0, 1, 1, 1, 0),
						Array(1, 0, 1, 0, 1, 0, 0, 1, 1),
						Array(1, 0, 1, 0, 1, 0, 1, 0, 1),
						Array(1, 0, 1, 0, 1, 0, 1, 1, 0),
						Array(1, 0, 1, 0, 1, 1, 0, 0, 1),
						Array(1, 0, 1, 0, 1, 1, 0, 1, 0),
						Array(1, 0, 1, 0, 1, 1, 1, 0, 0),
						Array(1, 0, 1, 1, 0, 0, 0, 1, 1),
						Array(1, 0, 1, 1, 0, 0, 1, 0, 1),
						Array(1, 0, 1, 1, 0, 0, 1, 1, 0),
						Array(1, 0, 1, 1, 0, 1, 0, 0, 1),
						Array(1, 0, 1, 1, 0, 1, 0, 1, 0),
						Array(1, 0, 1, 1, 0, 1, 1, 0, 0),
						Array(1, 0, 1, 1, 1, 0, 0, 0, 1),
						Array(1, 0, 1, 1, 1, 0, 0, 1, 0),
						Array(1, 0, 1, 1, 1, 0, 1, 0, 0),
						Array(1, 0, 1, 1, 1, 1, 0, 0, 0),
						Array(1, 1, 0, 0, 0, 0, 1, 1, 1),
						Array(1, 1, 0, 0, 0, 1, 0, 1, 1),
						Array(1, 1, 0, 0, 0, 1, 1, 0, 1),
						Array(1, 1, 0, 0, 0, 1, 1, 1, 0),
						Array(1, 1, 0, 0, 1, 0, 0, 1, 1),
						Array(1, 1, 0, 0, 1, 0, 1, 0, 1),
						Array(1, 1, 0, 0, 1, 0, 1, 1, 0),
						Array(1, 1, 0, 0, 1, 1, 0, 0, 1),
						Array(1, 1, 0, 0, 1, 1, 0, 1, 0),
						Array(1, 1, 0, 0, 1, 1, 1, 0, 0),
						Array(1, 1, 0, 1, 0, 0, 0, 1, 1),
						Array(1, 1, 0, 1, 0, 0, 1, 0, 1),
						Array(1, 1, 0, 1, 0, 0, 1, 1, 0),
						Array(1, 1, 0, 1, 0, 1, 0, 0, 1),
						Array(1, 1, 0, 1, 0, 1, 0, 1, 0),
						Array(1, 1, 0, 1, 0, 1, 1, 0, 0),
						Array(1, 1, 0, 1, 1, 0, 0, 0, 1),
						Array(1, 1, 0, 1, 1, 0, 0, 1, 0),
						Array(1, 1, 0, 1, 1, 0, 1, 0, 0),
						Array(1, 1, 0, 1, 1, 1, 0, 0, 0),
						Array(1, 1, 1, 0, 0, 0, 0, 1, 1),
						Array(1, 1, 1, 0, 0, 0, 1, 0, 1),
						Array(1, 1, 1, 0, 0, 0, 1, 1, 0),
						Array(1, 1, 1, 0, 0, 1, 0, 0, 1),
						Array(1, 1, 1, 0, 0, 1, 0, 1, 0),
						Array(1, 1, 1, 0, 0, 1, 1, 0, 0),
						Array(1, 1, 1, 0, 1, 0, 0, 0, 1),
						Array(1, 1, 1, 0, 1, 0, 0, 1, 0),
						Array(1, 1, 1, 0, 1, 0, 1, 0, 0),
						Array(1, 1, 1, 0, 1, 1, 0, 0, 0),
						Array(1, 1, 1, 1, 0, 0, 0, 0, 1),
						Array(1, 1, 1, 1, 0, 0, 0, 1, 0),
						Array(1, 1, 1, 1, 0, 0, 1, 0, 0),
						Array(1, 1, 1, 1, 0, 1, 0, 0, 0),
						Array(1, 1, 1, 1, 1, 0, 0, 0, 0));

function start() {
	status = -1;
	playerStatus = cm.isLeader();
	preamble = null;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0 && status == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
			boxStage(cm);
		}
	}
			
function clear(stage, eim, cm) {
eim.setProperty("8stageclear","true");
var packetef = MaplePacketCreator.showEffect("quest/party/clear");
var packetsnd = MaplePacketCreator.playSound("Party1/Clear");
var packetglow = MaplePacketCreator.environmentChange("gate",2);
var map = eim.getMapInstance(cm.getChar().getMapId());
map.broadcastMessage(packetef);
map.broadcastMessage(packetsnd);
map.broadcastMessage(packetglow);
var mf = eim.getMapFactory();
map = mf.getMap(922010100 + stage * 100);
cm.givePartyExp(300, party);
cm.mapMessage("["+cm.getServerName()+" Quest] O portal que leva para o proximo estagio esta aberto.");
}

function failstage(eim, cm) {
	var packetef = MaplePacketCreator.showEffect("quest/party/wrong_kor");
	var packetsnd = MaplePacketCreator.playSound("Party1/Failed");
	var map = eim.getMapInstance(cm.getChar().getMapId());
	map.broadcastMessage(packetef);
	map.broadcastMessage(packetsnd);
}

function boxStage(cm) {
	var debug = false;
	var eim = cm.getChar().getEventInstance();
	var nthtext = "eighth";
	var nthobj = "caixas";
	var nthverb = "ficar";
	var nthpos = "ficar muito perto das bordas";
	var curcombo = stage8combos;
	var currect = cm.getChar().getMap().getAreas();
	var objset = [0,0,0,0,0,0,0,0,0];
		if (playerStatus) { 
			if (status == 0) {
					party = eim.getPlayers();
					preamble = eim.getProperty("leader" + nthtext + "preamble");
					if (preamble == null) {
							cm.sendNext("Ola! Bem-vindo(a) ao 8 estagio. Aqui voce vai encontrar muitas plataformas para subir. #b5#k delas estarao conectadas ao #bportal que leva ao proximo estagio#k. Para passar, coloque #b5 dos membros do seu grupo na plataforma correta#k.\r\nUm aviso: Voce vai precisar ficar firme no centro da plataforma para a resposta contar como correta. Lembre-se também de que apenas 5 membros podem ficar na plataforma. Quando isto acontecer, o lider do grupo devera #bclicar duas vezes em mim para saber se a resposta esta correta ou nao#k. Boa sorte!");
							eim.setProperty("leader" + nthtext + "preamble","done");
							var sequenceNum = Math.floor(Math.random() * curcombo.length);
							eim.setProperty("stage" + nthtext + "combo",sequenceNum.toString());
							cm.dispose();
					}
					else {
						var complete = eim.getProperty("8stageclear");
						if (complete != null) {	
							var mapClear = "8stageclear";
							eim.setProperty(mapClear,"true"); 
							cm.sendNext("Por favor completar todos processos, para abrir o portal.");
						}
						else { 
								var totplayers = 0;
								for (i = 0; i < objset.length; i++) {
										for (j = 0; j < party.size(); j++) {
												var present = currect.get(i).contains(party.get(j).getPosition());
													if (present) {
														objset[i] = objset[i] + 1;
														totplayers = totplayers + 1;
												}
										}
								}
								var numSpawn = 5;
								if (totplayers == 5 || debug) {
										var combo = curcombo[parseInt(eim.getProperty("stage" + nthtext + "combo"))];
										var testcombo = true;
										for (i = 0; i < objset.length; i++) {
											if (combo[i] != objset[i]){
												testcombo = false;
												}
										}
										if (testcombo || debug) {
												clear(1,eim,cm);
												var exp = (3000);
												cm.givePartyExp(exp, party);
												cm.dispose();
										}
										else {
												failstage(eim,cm);
												cm.dispose();
												
										}
								}
								else {
										if (debug) {
											var outstring = "Contem:"
											for (i = 0; i < objset.length; i++) {
												outstring += "\r\n" + (i+1).toString() + ". " + objset[i].toString();
											}
											cm.sendNext(outstring); 
											var combo = curcombo[parseInt(eim.getProperty("stage" + nthtext + "combo"))];
										}
										else
											cm.sendNext("Parece que voce nao tenha encontrado a 5 " + nthobj + " ainda. Por favor, pense uma combinacao diferente de " + nthobj + ". Apenas 5 e permitido " + nthverb + " em cima das " + nthobj + ", e se voce " + nthpos + " nao pode contar como uma resposta, por isso, tente manter isso em mente. Continue indo!");
											cm.dispose();
								}
						}
					}
			}
			else {
				var complete = eim.getProperty("8stageclear");
					if (complete != null) {	
					var target = eim.getMapInstance(103000800 + curMap);
			var targetPortal = target.getPortal("st00");
					cm.getChar().changeMap(target, targetPortal);
				}
				cm.dispose();
			}
	}
	else { 
		if (status == 0) {
				var complete = eim.getProperty("8stageclear");
				if (complete != null) {
					cm.sendNext("Por favor completar todos processos, para abrir o portal!");
					cm.dispose();
				}
				else {
					cm.sendNext("Por favor, peca o lider para falar comigo.");
					cm.dispose();
				}
		}
	else {
				var complete = eim.getProperty("8stageclear");
			if (complete != null) {	
			cm.sendNext("Por favor completar todos processos, para abrir o portal!");
			cm.dispose();
		}
				cm.dispose();
			}
	}
}