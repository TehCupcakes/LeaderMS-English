importPackage(Packages.tools.packet);
importPackage(Packages.server.life);
importPackage(java.awt);

var status;
var curMap;
var playerStatus;
var chatState;
var questions = Array("Esta e a tarefa. Recolher o mesmo numero de cupons, com o nivel minimo necessario para fazer o avanco para o primeiro trabalho como Guerreiro.",
			"Esta e a tarefa. Recolher o mesmo numero de cupons, com a quantidade minima de STR ​​necessaria para fazer o avanco para o primeiro trabalho como um Guerreiro.",
			"Esta e a tarefa. Recolher o mesmo numero de cupons, com a quantidade minima de INT necessaria para fazer o avanco para o primeiro trabalho como um Bruxo.",
			"Esta e a tarefa. Recolher o mesmo numero de cupons, com a quantidade minima de DEX necessaria para fazer o avanco para o primeiro trabalho como um Arqueiro.",
			"Esta e a tarefa. Recolher o mesmo numero de cupons, com a quantidade minima de DEX necessaria para fazer o avanco para o primeiro trabalho como um Gatuno.",
			"Esta e a tarefa. Recolher o mesmo numero de cupons, com o nivel minimo necessario para avancar para a Segunda Classe.");
var qanswers = Array(10, 35, 20, 25, 25, 30);
var party;
var preamble;
var stage2rects = Array(Rectangle(-770,-132,28,178),Rectangle(-733,-337,26,105),Rectangle(-601,-328,29,105),Rectangle(-495,-125,24,165));
var stage2combos = Array(Array(0,1,1,1),Array(1,0,1,1),Array(1,1,0,1),Array(1,1,1,0));
var stage3rects = Array(Rectangle(608,-180,140,50),Rectangle(791,-117,140,45),Rectangle(958,-180,140,50),Rectangle(876,-238,140,45),Rectangle(702,-238,140,45));
var stage3combos = Array(Array(0,0,1,1,1),Array(0,1,0,1,1),Array(0,1,1,0,1),Array(0,1,1,1,0),Array(1,0,0,1,1),Array(1,0,1,0,1),Array(1,0,1,1,0),Array(1,1,0,0,1),Array(1,1,0,1,0),Array(1,1,1,0,0));
var stage4rects = Array(Rectangle(910,-236,35,5),Rectangle(877,-184,35,5),Rectangle(946,-184,35,5),Rectangle(845,-132,35,5),Rectangle(910,-132,35,5),Rectangle(981,-132,35,5));
var stage4combos = Array(Array(0,0,0,1,1,1),Array(0,0,1,0,1,1),Array(0,0,1,1,0,1),Array(0,0,1,1,1,0),Array(0,1,0,0,1,1),Array(0,1,0,1,0,1),Array(0,1,0,1,1,0),Array(0,1,1,0,0,1),Array(0,1,1,0,1,0),Array(0,1,1,1,0,0),Array(1,0,0,0,1,1),Array(1,0,0,1,0,1),Array(1,0,0,1,1,0),Array(1,0,1,0,0,1),Array(1,0,1,0,1,0),Array(1,0,1,1,0,0),Array(1,1,0,0,0,1),Array(1,1,0,0,1,0),Array(1,1,0,1,0,0),Array(1,1,1,0,0,0));
var eye = 9300002;
var necki = 9300000;
var slime = 9300003;
var monsterIds = Array(eye, eye, eye, necki, necki, necki, necki, necki, necki, slime);
var prizeIdScroll = Array(2040502, 2040505,					// Overall DEX and DEF
			2040802,										// Gloves for DEX 
			2040002, 2040402, 2040602);						// Helmet, Topwear and Bottomwear for DEF
var prizeIdUse = Array(2000001, 2000002, 2000003, 2000006,	// Orange, White and Blue Potions and Mana Elixir
			2000004, 2022000, 2022003);						// Elixir, Pure Water and Unagi
var prizeQtyUse = Array(80, 80, 80, 50,
			5, 15, 15);
var prizeIdEquip = Array(1032004, 1032005, 1032009,			// Level 20-25 Earrings
			1032006, 1032007, 1032010,						// Level 30 Earrings
			1032002,										// Level 35 Earring
			1002026, 1002089, 1002090);						// Bamboo Hats
var prizeIdEtc = Array(4010000, 4010001, 4010002, 4010003,	// Mineral Ores
			4010004, 4010005, 4010006,						// Mineral Ores
			4020000, 4020001, 4020002, 4020003,				// Jewel Ores
			4020004, 4020005, 4020006,						// Jewel Ores
			4020007, 4020008, 4003000);						// Diamond and Black Crystal Ores and Screws	
var prizeQtyEtc = Array(15, 15, 15, 15,
			8, 8, 8,
			8, 8, 8, 8,
			8, 8, 8,
			3, 3, 30);
			
function start() {
	status = -1;
	mapId = cm.getPlayer().getMapId();
	if (mapId == 103000800)
		curMap = 1;
	else if (mapId == 103000801)
		curMap = 2;
	else if (mapId == 103000802)
		curMap = 3;
	else if (mapId == 103000803)
		curMap = 4;
	else if (mapId == 103000804)
		curMap = 5;
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
	if (curMap == 1) { 
		if (playerStatus) { 
			if (status == 0) {
				var eim = cm.getPlayer().getEventInstance();
				party = eim.getPlayers();
				preamble = eim.getProperty("leader1stpreamble");
				if (preamble == null) {
					cm.sendNext("Ola! Bem-vindo(a) ao 1 estagio. Ande pelo mapa e voce vera Jacares ao seu redor. Quando voce derrota-los, eles irao te dar um #bcupon#k. Cada membro do grupo que nao seja o lider deve conversar comigo, e reunir-se o mesmo numero de #bcupons#k como a resposta para a pergunta que eu vou dar para eles.\r\nSe voce juntar a quantidade correta de #bcupons#k, eu vou lhe dar o #bpasse#k. Uma vez que todos os outros membros do grupo acabarem, devem entregar ao lider os #bpasses#k, o lider vai me entregar os #bpasses#k, concluindo a fase. Quanto mais rapido voce terminar os estagios, mais estagios voce vai ser capaz de desafiar. Entao eu sugiro que voce tome o cuidado. Boa sorte!");
					eim.setProperty("leader1stpreamble","done");
					cm.dispose();
				} else {
					var complete = eim.getProperty(curMap.toString() + "stageclear");
					if (complete != null) {
						cm.sendNext("Por favor, nos apressarmos para a proxima fase, o portal esta aberto!");
						cm.dispose();
					} else {
						var numpasses = party.size()-1;
						var passes = cm.haveItem(4001008,numpasses);
						var strpasses = "#b" + numpasses.toString() + " passes#k";
						if (!passes) {
							cm.sendNext("Voce precisa coletar o numero de cupons sugerido pela resposta. Nem mais nem menos. Verifique se voce tem mesmo os cupons.");
							cm.dispose();
						} else {
							cm.sendNext("Voce reuniu " + strpasses + "! Parabens por concluir este estagio. Eu vou liberar o portal e envia-lo para a proxima fase. Ha um limite de tempo para chegar la, entao por favor apresse. Boa sorte para todos voces!");
							clear(1,eim,cm);
							cm.givePartyExp(100, party);
							cm.gainItem(4001008, -numpasses);
							cm.dispose();
						}
					}
				}
			}
		} else { 
			var eim = cm.getPlayer().getEventInstance();
			pstring = "member1stpreamble" + cm.getPlayer().getId().toString();
			preamble = eim.getProperty(pstring);
			if (status == 0 && preamble == null) {
				var qstring = "member1st" + cm.getPlayer().getId().toString();
				var question = eim.getProperty(qstring);
				if (question == null) {
					var questionNum = Math.floor(Math.random() * questions.length);
					eim.setProperty(qstring, questionNum.toString());
				}
					cm.sendNext("Ola! Bem-vindo(a) ao 1 estagio. Ande pelo mapa e voce vera Jacares ao seu redor. Quando voce derrota-los, eles irao te dar um #bcupon#k. Cada membro do grupo que nao seja o lider deve conversar comigo, e reunir-se o mesmo numero de #bcupons#k como a resposta para a pergunta que eu vou dar para eles.\r\nSe voce juntar a quantidade correta de #bcupons#k, eu vou lhe dar o #bpasse#k. Uma vez que todos os outros membros do grupo acabarem, devem entregar ao lider os #bpasses#k, o lider vai me entregar os #bpasses#k, concluindo a fase. Quanto mais rapido voce terminar os estagios, mais estagios voce vai ser capaz de desafiar. Entao eu sugiro que voce tome o cuidado. Boa sorte!");
			} else if (status == 0) { 
				var complete = eim.getProperty(curMap.toString() + "stageclear");
				if (complete != null) {
					cm.sendNext("Por favor, nos apressarmos para a proxima fase, o portal esta aberto!");
					cm.dispose();
				} else {
					var qstring = "member1st" + cm.getPlayer().getId().toString();
					var numcoupons = qanswers[parseInt(eim.getProperty(qstring))];
					var qcorr = cm.haveItem(4001007,(numcoupons+1));
					var enough = false;
					if (!qcorr) { 
						qcorr = cm.haveItem(4001007,numcoupons);
						if (qcorr) { 
							cm.sendNext("Resposta correta! Voce acaba de ganhar um #bpasse#k. Por favor, entregue-o para o lider do seu grupo.");
							cm.gainItem(4001007, -numcoupons);
							cm.gainItem(4001008, 1);
							enough = true;
						}
					}
					if (!enough) {
						cm.sendNext("Resposta incorreta. So posso entregar o passe se voce coletar o numero de #bcupons#k sugerido pela resposta a pergunta.");
					}
					cm.dispose();
				}
			} else if (status == 1) {
				if (preamble == null) {
					var qstring = "member1st" + cm.getPlayer().getId().toString();
					var question = parseInt(eim.getProperty(qstring));
					cm.sendNextPrev(questions[question]);
				} else { 
					cm.dispose();
				}
			} else if (status == 2) { 
				eim.setProperty(pstring,"done");
				cm.dispose();
			} else { 
				eim.setProperty(pstring,"done");
				cm.dispose();
			}
		} 
	} else if (2 <= curMap && 4 >= curMap) {
		rectanglestages(cm);
	} else if (curMap == 5) {
		var eim = cm.getPlayer().getEventInstance();
		var stage5done = eim.getProperty("5stageclear");
		if (stage5done == null) {
			if (playerStatus) { 
				var map = eim.getMapInstance(cm.getPlayer().getMapId());
				var passes = cm.haveItem(4001008,10);
				if (passes) {
					cm.sendNext("Aqui esta o portal que leva ao ultimo estagio de bonus. E um estagio que permite derrotar monstros comuns um pouco mais facilmente. Voce tera um tempo limite para derrotar o maximo possivel deles, mas podera sair do estagio quando quiser falando com o NPC. Mais uma vez, parabens por completar todos os estagios. Cuidado...");
					party = eim.getPlayers();
					cm.gainItem(4001008, -10);
					clear(5,eim,cm);
					cm.givePartyExp(1500, party);
                                      //  cm.givePartyQPoints(20, party);
					cm.dispose();
				} else { 
					cm.sendNext("Ola! Bem-vindo(a) ao 5 estagio final. Ande pelo mapa e voce podera ver alguns Monstros Chefes. Derrote todos e junte 10 #bpasses#k para mim. Obtido o seu passe, o lider do seu grupo vai junta-los e me entregar quanto tiver todos os 10. Os monstros podem parecer familiares, mas eles sao muito mais fortes do que voce pensa. Por isso, tenha cuidado. Boa sorte!");
				}
				cm.dispose();
			} else { 
					cm.sendNext("Ola! Bem-vindo(a) ao 5 estagio final. Ande pelo mapa e voce podera ver alguns Monstros Chefes. Derrote todos e junte 10 #bpasses#k para mim. Obtido o seu passe, o lider do seu grupo vai junta-los e me entregar quanto tiver todos os 10. Os monstros podem parecer familiares, mas eles sao muito mais fortes do que voce pensa. Por isso, tenha cuidado. Boa sorte!");
				cm.dispose();
			}
		} else { 
			if (status == 0) {
				cm.sendNext("Incrivel! Voce completou todos os estágios para chegar até aqui. Aqui esta uma pequena recompensa pelo trabalho bem-feito. Mas, antes de aceitar, verifique se voce possui slots disponiveis nos inventarios de uso e etc.");
			}
			if (status == 1) {
				getPrize(eim,cm);
				cm.dispose();
			}
		}
	} else { 
		cm.sendNext("Mapa invalido, isso significa que o estagio esta incompleto.");
		cm.dispose();
		}
	}
}

function clear(stage, eim, cm) {
	eim.setProperty(stage.toString() + "stageclear","true");
	var packetef = MaplePacketCreator.showEffect("quest/party/clear");
	var packetsnd = MaplePacketCreator.playSound("Party1/Clear");
	var packetglow = MaplePacketCreator.environmentChange("gate",2);
	var map = eim.getMapInstance(cm.getPlayer().getMapId());
	map.broadcastMessage(packetef);
	map.broadcastMessage(packetsnd);
	map.broadcastMessage(packetglow);
	var mf = eim.getMapFactory();
	map = mf.getMap(103000800 + stage);
	var nextStage = eim.getMapInstance(103000800 + stage);
	var portal = nextStage.getPortal("next00");
	if (portal != null) {
		portal.setScriptName("kpq" + (stage+1).toString());
	}
}

function failstage(eim, cm) {
	var packetef = MaplePacketCreator.showEffect("quest/party/wrong_kor");
	var packetsnd = MaplePacketCreator.playSound("Party1/Failed");
	var map = eim.getMapInstance(cm.getPlayer().getMapId());
	map.broadcastMessage(packetef);
	map.broadcastMessage(packetsnd);
}

function rectanglestages (cm) {
	var debug = false;
	var eim = cm.getPlayer().getEventInstance();
	if (curMap == 2) {
		var nthtext = "2";
		var nthobj = "cordas";
		var nthverb = "pendurar";
		var nthpos = "pendurar nas cordas muito baixas";
		var curcombo = stage2combos;
		var currect = stage2rects;
		var objset = [0,0,0,0];
	} else if (curMap == 3) {
		var nthtext = "3";
		var nthobj = "plataformas";
		var nthverb = "suporte";
		var nthpos = "ficar muito perto das bordas";
		var curcombo = stage3combos;
		var currect = stage3rects;
		var objset = [0,0,0,0,0];
	} else if (curMap == 4) {
		var nthtext = "4";
		var nthobj = "barris";
		var nthverb = "suporte";
		var nthpos = "ficar muito perto das bordas";
		var curcombo = stage4combos;
		var currect = stage4rects;
		var objset = [0,0,0,0,0,0];
	}
	if (playerStatus) { 
		if (status == 0) {
			party = eim.getPlayers();
			preamble = eim.getProperty("leader" + nthtext + "preamble");
			if (preamble == null) {
				cm.sendNext("Ola! Bem-vindo(a) ao " + nthtext + " estagio. . Voce vera alguns barris por perto. 3 desses barris estarao conectados ao portal que leva ao proximo estagio. #b3 membros do grupo precisam encontrar os barris corretos e ficar em cima deles#k para completar o estagio. MAS, para a resposta contar, e preciso ficar bem firme no centro do barril, nao na beira. E apenas 3 membros do seu grupo podem ficar em cima dos barris. Quando os membros estiverem em cima, o lider do grupo devera #bclicar duas vezes em mim para saber se a resposta esta correta ou nao#k. Agora, encontre os barris corretos!");
				eim.setProperty("leader" + nthtext + "preamble","done");
				var sequenceNum = Math.floor(Math.random() * curcombo.length);
				eim.setProperty("stage" + nthtext + "combo",sequenceNum.toString());
				cm.dispose();
			} else {
				var complete = eim.getProperty(curMap.toString() + "stageclear");
				if (complete != null) {	
					var mapClear = curMap.toString() + "stageclear";
					eim.setProperty(mapClear,"true"); 
					cm.sendNext("Por favor, nos apressarmos para a proxima fase, o portal esta aberto!");
				} else { 
					var totplayers = 0;
					for (i = 0; i < objset.length; i++) {
						for (j = 0; j < party.size(); j++) {
							var present = currect[i].contains(party.get(j).getPosition());
							if (present) {
								objset[i] = objset[i] + 1;
								totplayers = totplayers + 1;
							}
						}
					}
			if (totplayers == 3 || debug) {
				var combo = curcombo[parseInt(eim.getProperty("stage" + nthtext + "combo"))];
				var testcombo = true;
				for (i = 0; i < objset.length; i++) {
					if (combo[i] != objset[i])
						testcombo = false;
				}
			if (testcombo || debug) {
				clear(curMap,eim,cm);
				var exp = (Math.pow(2,curMap) * 50);
				cm.givePartyExp(exp, party);
				cm.dispose();
			} else { 
				failstage(eim,cm);
				cm.dispose();
				}
			} else {
				if (debug) {
					var outstring = "Objetos contem:"
					for (i = 0; i < objset.length; i++) {
						outstring += "\r\n" + (i+1).toString() + ". " + objset[i].toString();
					}
					cm.sendNext(outstring); 
				} else
					cm.sendNext("Parece que voce ainda nao encontrou os 3 barris. Pense numa combinacao diferente dos barris. E nao esqueca que apenas 3 membros podem ficar em cima dos barris, firmes no centro para que a resposta conte como correta. Continue!");
					cm.dispose();
					}
				}
			}
		} else {
			var complete = eim.getProperty(curMap.toString() + "stageclear");
			if (complete != null) {
				var target = eim.getMapInstance(103000800 + curMap);
				var targetPortal = target.getPortal("st00");
				cm.getPlayer().changeMap(target, targetPortal);
			}
			cm.dispose();
		}
	} else { 
		if (status == 0) {
			var complete = eim.getProperty(curMap.toString() + "stageclear");
			if (complete != null) {
				cm.sendNext("Por favor, nos apressarmos para a proxima fase, o portal esta aberto!");
			} else {
				cm.sendNext("Por favor, peca o lider do grupo para falar comigo.");
				cm.dispose();
			}
		} else {
			var complete = eim.getProperty(curMap.toString() + "stageclear");
			if (complete != null) {	
				var target = eim.getMapInstance(103000800 + curMap);
				var targetPortal = target.getPortal("st00");
				cm.getPlayer().changeMap(target, targetPortal);
			}
			cm.dispose();
		}
	}
}

function getPrize(eim,cm) {
	var itemSetSel = Math.random();
	var itemSet;
	var itemSetQty;
	var hasQty = false;
	if (itemSetSel < 0.3)
		itemSet = prizeIdScroll;
	else if (itemSetSel < 0.6)
		itemSet = prizeIdEquip;
	else if (itemSetSel < 0.9) {
		itemSet = prizeIdUse;
		itemSetQty = prizeQtyUse;
		hasQty = true;
	} else { 
		itemSet = prizeIdEtc;
		itemSetQty = prizeQtyEtc;
		hasQty = true;
	}
	var sel = Math.floor(Math.random()*itemSet.length);
	var qty = 1;
	if (hasQty)
	qty = itemSetQty[sel];
	cm.gainItem(itemSet[sel], qty);
	var map = eim.getMapInstance(103000805);
	var portal = map.getPortal("sp");
	cm.getPlayer().changeMap(map,portal);
}