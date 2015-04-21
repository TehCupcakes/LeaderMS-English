///*
//	This file is part of the OdinMS Maple Story Server
//    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
//					   Matthias Butz <matze@odinms.de>
//					   Jan Christian Meyer <vimes@odinms.de>
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU Affero General Public License as
//    published by the Free Software Foundation version 3 as published by
//    the Free Software Foundation. You may not use, modify or distribute
//    this program under any other version of the GNU Affero General Public
//    License.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU Affero General Public License for more details.
//
//    You should have received a copy of the GNU Affero General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
//*/
//
///**
// Rini (Orbis Boat Loader) 2012001
//**/
//
//function start() {
//    if(cm.haveItem(4031047)){
//        var em = cm.getEventManager("Boats");
//        if (em.getProperty("entry") == "true" && cm.getPlayer().getMap().hasBoat() == 2) {
//            cm.sendYesNo("Me parece que ha muito espaco para esse passeio. Por favor, tenha o seu bilhete pronto para que eu possa deixa-lo entrar. A viagem pode ser longa, mas voce vai chegar ao seu destino muito bem. O que voce acha? Voce quer entrar nesta viagem?");
//        } else {
//            if (em.getProperty("entry") == "false" && em.getProperty("docked") == "true" && cm.getPlayer().getMap().hasBoat() == 2) {
//                cm.sendOk("Comecaremos o embarque 5 minutos antes da saida. Por favor, seja paciente e aguarde alguns minutos. Esteja certo de que o navio partira no horario e nao receberemos mais bilhetes 1 minuto antes dele partir, então, por favor, esteja aqui a tempo.");
//            } else {
//                if (cm.getPlayer().getMap().hasBoat() == 2) {
//                    cm.sendOk("O barco de Ellinia esta pronto para decolar, por favor, seja paciente e aguarde o proximo.");
//                    cm.dispose();
//                } else{
//                    cm.sendOk("O barco de Ellinia esta pronto para decolar, por favor, seja paciente e aguarde o proximo.");
//                    cm.dispose();
//                }
//            }
//            cm.dispose();
//        }
//    } else {
//        cm.sendOk("Certifique-se de que voce tem um bilhete de Ellinia para viajar neste barco.");
//        cm.dispose();
//    }
//}
//
//function action(mode, type, selection){
//    cm.gainItem(4031047, -1);
//    cm.warp(200000112);
//    cm.dispose();
//}
 /* Author: Xterminator
	NPC Name: 		Rini
	Map(s): 		Orbis: Station<To Ellinia> (200000111)
	Description: 		Orbis Ticketing Usher
*/
var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
	if (status >= 0 && mode == 0) {
		cm.sendNext("You must have some business to take care of here, right?");
		cm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;
	if (status == 0) {
		cm.sendYesNo("It seems like there is still room on this ride. Please have your ticket ready so you can get on. The journey may be long, but you will get to your destination safely. What do you think? Do you want to go on this trip?");
	} else if (status == 1) {
		if (cm.haveItem(4031047)) {
			cm.gainItem(4031047, -1);
			cm.warp(101000300, 0);
			cm.dispose();
		} else {
			cm.sendNext("Make sure you have a Ellinia ticket to travel on this boat.");
			cm.dispose();
			}		
		}
	}
}