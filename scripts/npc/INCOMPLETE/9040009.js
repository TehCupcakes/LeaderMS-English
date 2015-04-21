/* 
 * This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/* 
 * @Author Lerk
 * 
 * Gatekeeper, Sharenian: Door to the Sharenian Castle (990000300)
 * 
 * Guild Quest - stage 1
 */

importPackage(Packages.server.maps);

var status;
var stage;

function start() {
	status = -1;
        action (1, 0, 0);
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
                var eim = cm.getPlayer().getEventInstance();
                if (eim == null) {
                        cm.warp(990001100);
                } else {
                        if (eim.getProperty("leader").equals(cm.getPlayer().getName())) {
                                if (cm.getPlayer().getMap().getReactorByName("statuegate").getState() > 0){
                                        cm.sendOk("Prosseguir.");
                                        cm.dispose();
                                } else {
                                        if (status == 0) {
                                                if (eim.getProperty("stage1status") == null || eim.getProperty("stage1status").equals("waiting")) {
                                                        if (eim.getProperty("stage1phase") == null) {
                                                                stage = 1;
                                                                eim.setProperty("stage1phase",stage);
                                                        } else {
                                                                stage = parseInt(eim.getProperty("stage1phase"));
                                                        }
                                                        if (stage == 1) {
                                                                cm.sendOk("Neste desafio, vou mostrar um padrao nas estatuas em torno de mim. Quando eu der a palavra, repetir o padrao para poder continuar.");
                                                        }
                                                        else {
                                                                cm.sendOk("Vou agora apresentar um quebra-cabeca mais dificil para voce.")
                                                        }
                                                }
                                                else if (eim.getProperty("stage1status").equals("active")) {
                                                        stage = parseInt(eim.getProperty("stage1phase"));
                                                        if (eim.getProperty("stage1combo").equals(eim.getProperty("stage1guess"))) {
                                                                if (stage == 3) {
                                                                        cm.getPlayer().getMap().getReactorByName("statuegate").hitReactor(cm.getC());
                                                                        cm.sendOk("Excelente trabalho. Por favor, prossiga para a proxima fase.");
                                                                        cm.showEffect("quest/party/clear");
                                                                        cm.playSound("Party1/Clear");
                                                                        var prev = eim.setProperty("stage1clear","true",true);
                                                                        if (prev == null) {
                                                                                cm.getGuild().gainGP(15);
                                                                        }
                                                                } else {
                                                                        
                                                                        cm.sendOk("Muito bom. Voce ainda tem mais trabalho a concluir. Fale comigo novamente quando estiver pronto.");
                                                                        eim.setProperty("stage1phase", stage + 1);
                                                                        cm.mapMessage("Voce concluiu a parte (" + stage + ") Teste do Gatekeeper.");
                                                                }
                                                                
                                                        } else {
                                                                cm.sendOk("Voce falhou neste teste.");
                                                                cm.mapMessage("Voce falhou no Teste do Gatekeeper.");
                                                                eim.setProperty("stage1phase","1")
                                                        }
                                                        eim.setProperty("stage1status", "waiting");
                                                        cm.dispose();
                                                }
                                                else {
                                                        cm.sendOk("Por favor, aguarde.");
                                                        cm.dispose();
                                                }
                                        }
                                        else if (status == 1) {
                                                //only applicable for "waiting"
                                                var reactors = getReactors();
                                                var combo = makeCombo(reactors);
                                                /*/var reactorString = "Debug: Reactors in map: ";
                                                for (var i = 0; i < reactors.length; i++) {
                                                        reactorString += reactors[i] + " ";
                                                }
                                                cm.playerMessage(reactorString);
                                                reactorString = "Debug: Reactors in combo: ";
                                                for (var i = 0; i < combo.length; i++) {
                                                        reactorString += combo[i] + " ";
                                                }
                                                cm.playerMessage(reactorString);*/
                                                cm.mapMessage("Por favor, aguarde enquanto a combinacao e revelada.");
                                                
                                                var delay = 5000;
                                                for (var i = 0; i < combo.length; i++) {
                                                        cm.getPlayer().getMap().getReactorByOid(combo[i]).delayedHitReactor(cm.getC(), delay + 3500*i);
                                                }
                                                eim.setProperty("stage1status", "display");
                                                eim.setProperty("stage1combo","");       
                                                cm.dispose();
                                        }
                                }

                        } else {
                                cm.sendOk("Eu preciso que o lider de seu partido fale comigo, mais ninguem.");
                                cm.dispose();
                        }
                }
        }
}

//method for getting the statue reactors on the map by oid
function getReactors() {
        var reactors = new Array();
        
        var iter = cm.getPlayer().getMap().getMapObjects().iterator();
        while (iter.hasNext()) {
                var mo = iter.next();
                if (mo.getType() == MapleMapObjectType.REACTOR && !mo.getName().equals("statuegate")) {
                        reactors.push(mo.getObjectId());
                }
        }
        
        return reactors;
}

function makeCombo(reactors) {
        var combo = new Array();
        
        while (combo.length < (stage + 3)) {
                var chosenReactor = reactors[Math.floor(Math.random() * reactors.length)];
                //cm.log("Debug: Chosen Reactor " + chosenReactor)
                var repeat = false;
                
                if (combo.length > 0) {
                        for (var i = 0; i < combo.length; i++) {
                                if (combo[i] == chosenReactor) {
                                        repeat = true;
                                        //cm.log("Debug: repeat reactor: " + chosenReactor);
                                        break;
                                }
                        }
                }
                
                if (!repeat) {
                        //cm.log("Debug: unique reactor: " + chosenReactor);
                        combo.push(chosenReactor);
                }
        }
        
        return combo;
}