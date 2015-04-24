/*
 This file is part of the OdinMS Maple Story Server
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
 @author Jvlaple
 This notice may not be removed, or it will invalidate the conditions of using this script, or any other script/source/program released by Jvlaple.
 */


var status = 0;
var minLevel = 10;
var maxLevel = 200;
var minPlayers = 2;
var maxPlayers = 2;
var mySelection = -1;
var rings = Array(1112001, 1112002, 1112003, 1112005, 1112006);

importPackage(Packages.client);
importPackage(Packages.tools.packet);
importPackage(Packages.server);

function start() {
    status = -1;
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
        if (cm.getPlayer().getMapId() == 680000000) {
            if (status == 0) {
                cm.sendSimple("Ola #h #,\r\n#bVoce gostaria de se casar, ou visitar seus amigos em um casamento?\r\n#L0#Eu quero me casar!#l\r\n#L1#Eu quero ver meus amigos se casar!#l\r\n#L2#Comprar bilhete de casamento por 25.000,000 Mesos!#l\r\n#L3#Gostaria de obter uma licenca de casamento.#l\r\n");
            } else if (status == 1 && selection == 0) {
                if (cm.getParty() == null) {
                    cm.sendOk("Voce quer se casar? Entre em um grupo com sua/seu parceira(o)!");
                    cm.dispose();
                    return;
                }
                if (!cm.isLeader()) { 
                    cm.sendOk("Por favor, peca ao seu parceiro para falar comigo.");
                    cm.dispose();
                } else {
                    var party = cm.getParty().getMembers();
                    var mapId = cm.getPlayer().getMapId();
                    var next = true;
                    var levelValid = 0;
                    var inMap = 0;
                    var genderRight = 0;
                    var alreadyMarried = 0;
                    if (party.size() < minPlayers || party.size() > maxPlayers)
                        next = false;
                    else {
                        for (var i = 0; i < party.size() && next; i++) {
                            if ((party.get(i).getLevel() >= minLevel) && (party.get(i).getLevel() <= maxLevel))
                                levelValid += 1;
                            if (party.get(i).getMapid() == mapId)
                                inMap += 1;
                            if (party.get(i).getGender() == 0) {
                                genderRight += 1;
                            } else if (party.get(i).getGender() == 1) {
                                genderRight += 2;
                            }
                            if (party.get(i).isMarried() == 1) {
                                alreadyMarried += 1;
                            }
                        }
                        if (party.get(0).getGender() == 1) {
                            next = false;
                        }
                        if (levelValid < minPlayers || inMap < minPlayers || genderRight != 3 || alreadyMarried != 0)
                            next = false;
                    }
                    if (!cm.haveItem(4031374, 1)) {
                        next = false;
                    }
                    if (!cm.haveItem(5251003, 1)) {
                        next = false;
                    }
                    if (next) {
                        var em = cm.getEventManager("CathedralWedding");
                        if (em == null) {
                            cm.sendOk("Evento nao disponivel!");
                            cm.dispose();
                        }
                        else {
                            em.startInstance(cm.getParty(), cm.getPlayer().getMap());
                            party = cm.getPlayer().getEventInstance().getPlayers();
                            var hname = party.get(0).getName();
                            var wname = party.get(1).getName();
                            var hidd = party.get(0).getId();
                            var widd = party.get(1).getId();
                            var StringLine = hname + " e " + wname + "'s vao se casar na Catedral no Canal (" + cm.getC().getChannel() + ").";
                            cm.worldMessage(5, StringLine);
                            cm.gainItem(5251003, -1);
                            var eimi = cm.getPlayer().getEventInstance();
                            eimi.setProperty("husband", hname);
                            eimi.setProperty("wife", wname);
                            eimi.setProperty("husid", hidd);
                            eimi.setProperty("wifeid", widd);

                        }
                        cm.dispose();
                    }
                    else {
                        cm.sendOk("Voce nao esta no mesmo mapa com a sua parceira, ou voces dois sao do mesmo sexo, ou um de voce ja esta casado, ou o lider do grupo e a noiva, ou voce nao tem permissao suficiente. Lembre-se, voce tem que ser nivel 10(+) para se casar.");
                        cm.dispose();
                    }
                }

            }
            else if (status == 1 && selection == 1) {
                cm.sendGetText("Por favor, insira o nome de um dos membros do casamento.");
            } else if (status == 1 && selection == 2) {
                if (cm.getPlayer().getMeso() >= 25000000) {
                    cm.gainMeso(-25000000)
                    cm.gainItem(5251003, 1);
                    cm.dispose();
                } else {
                    cm.sendOk("Voce nao tem mesos suficientes.");
                    cm.dispose();
                }
            }
            else if (status == 1 && selection == 3) {
                if (cm.getPlayer().getMarriageQuestLevel() == 50) {
                    cm.sendNext("Por favor, va e visitar a Mom e Dad em sua casa. Eles vivem em algum lugar em  - Henesys Hunting Ground II");
                    cm.getPlayer().addMarriageQuestLevel();
                    cm.dispose();
                } else if (cm.getPlayer().getMarriageQuestLevel() == 53) {
                    if (cm.haveItem(4031373, 1)) {
                        cm.sendNext("Grande, voce tem a minha permissao.");
                        cm.removeAll(4031373);
                        cm.gainItem(4031374, 1);
                        cm.getPlayer().setMarriageQuestLevel(100);
                        cm.dispose();
                    } else {
                        cm.sendNext("Voce nao tem a bencao de Mom e Dad!");
                        cm.dispose();
                    }
                } else {
                    cm.sendNext("Eu nao sei o que esta falando.");
                    cm.dispose();
                }
            } else if (status == 2) {
                var mapid = 99;
                var txt = cm.getText();
                var chrr = cm.getCharByName(txt);
                if (chrr != null)
                    mapid = chrr.getMapId();
                if (mapid == 680000200) {
                    var eim = chrr.getEventInstance();
                    eim.registerPlayer(cm.getPlayer());
                    cm.dispose();
                } else {
                    cm.sendOk("O casamento que voce gostaria de participar nao foi iniciado.");
                    cm.dispose();
                }
                cm.dispose();
            }
        } else if (cm.getPlayer().getMapId() == 680000210) {
            //Vows
            var eimii = cm.getPlayer().getEventInstance();
            var hiidii = eimii.getProperty("husid");
            var wiidii = eimii.getProperty("wifeid");
            var partyi = cm.getParty().getMembers();
            var alrediClicked = eimii.getProperty("alreadyClicked");
            if (status == 0) {
                if (cm.getPlayer().getId() != hiidii && cm.getPlayer().getId() != wiidii) {
                    cm.sendOk("Voce nao esta se casando!");
                    cm.dispose();
                    cm.dispose();
                } else if (cm.getPlayer().isMarried() != 0) {
                    cm.sendOk("Voce ja foi casado.");
                    cm.dispose();
                } else {
                    cm.sendYesNo("Voce gostaria de se casar com seu parceiro(a)?\r\n\r\n");
                }
            } else if (status == 1) {
                if (alrediClicked == 0) {
                    eimii.setProperty("alreadyClicked", 1);
                    kkk = Math.floor(Math.random() * rings.length);
                    cm.createMarriage(partyi.get(0), partyi.get(1));//rings[1]);
                    if (cm.getPlayer().getGender() == 0) {
                        cm.getPlayer().setPartnerId(eimii.getProperty("wifeid")); //Force Save to DB
                        cm.getPlayer().saveToDB(true, true);
                    } else {
                        cm.getPlayer().setPartnerId(eimii.getProperty("husid"));
                        cm.getPlayer().saveToDB(true, true);
                    }
                } else {
                    cm.getPlayer().setMarried(1);
                    if (cm.getPlayer().getGender() == 0) {
                        cm.getPlayer().setPartnerId(eimii.getProperty("wifeid")); //Force Save to DB
                        cm.getPlayer().saveToDB(true, true);
                    } else {
                        cm.getPlayer().setPartnerId(eimii.getProperty("husid"));
                        cm.getPlayer().saveToDB(true, true);
                    }
                }
                cm.sendOk("Voce se casou com sucesso, parabens!");
                cm.gainItem(4031424, 1);
                cm.removeAll(4031374);
                cm.dispose();
            }
        }
    }
}