/*
* @autor Java
* LeaderMS MapleStory Private Server
* Armas Maple
*/

importPackage(Packages.config.configuration);

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1)
        status++;
    else {
        cm.sendOk("Tudo bem, ate a proxima!");
        cm.dispose();
        return;
    }
    if (status == 0) {
            cm.sendSimple("                          #e<"+Configuration.Server_Name+" - Armas Maple>#n            \r\n\r\nOla #e#h ##n.\r\nAqui voce pode adquirir Armas Maple.\r\n\Voce tem algumas folhas Maple?#b\r\n\#L1#Trocar Folhas");
        }  else if (selection == 0) {
            cm.sendSimple("So you want to hunt for them? Okay I'll warp you to the map.#b\r\n#L2#Warp me pl0x :D#b\r\n#L3#No Ty I changed my mind");
        }  else if (selection == 3) {
            cm.sendOk("Okay nub");
            cm.dispose();
        }  else if (selection == 2) {
            cm.warp(6830000);
        cm.sendOk("Good Luck!");
            cm.dispose();
        }  else if (selection == 1) {
            cm.sendSimple("O que voce quer trocar?#b\r\n\#L4#Lv. 35 Armas Maple \r\n\#L5#Lv. 43 Armas Maple \r\n\#L6#Lv. 64 Armas Maple ");
        }  else if (selection == 4) {
        cm.sendSimple("Escolha a sua arma, sao #r1.200 Folhas Maple#b cada!\r\n\#L7#Maple Sword\r\n\#L8#Maple Staff\r\n\#L9#Maple Bow\r\n\#L10#Maple Crow\r\n\#L11#Maple Claw\r\n\#L12#Maple Knuckle\r\n\#L13#Maple Gun#l\r\n\ .");
        }  else if (selection == 5) {
        cm.sendSimple("Escolha a sua arma, sao #r1.400 Folhas Maple#b cada!\r\n\#L14#Maple Lama Staff\r\n\#L15#Maple Soul Singer\r\n\#L16#Maple Wagner\r\n\#L17#Maple Dragon Axe\r\n\#L18#Maple Doom Singer\r\n\#L19#Maple Impaler\r\n\#L20#Maple Scorpio\r\n\#L21#Maple Soul Searcher\r\n\#L22#Maple Crossbow\r\n\#L23#Maple Kandayo\r\n\#L24#Maple Storm Pistol\r\n\#L25#Maple Storm Finger#l\r\n\ .");
        }  else if (selection == 6) {
        cm.sendSimple("Escolha a sua arma, sao #r1.600 Folhas Maple#b cada!\r\n\#L26#Maple Glory Sword\r\n\#L27#Maple Steel Axe\r\n\#L28#Maple Havoc Hammer\r\n\#L29#Maple Dark Mate\r\n\#L30#Maple Asura Dagger\r\n\#L31#Maple Shine Wand\r\n\#L32#Maple Wisdom Staff\r\n\#L33#Maple Soul Rohen\r\n\#L34#Maple Demon Axe\r\n\#L35#Maple Belzet\r\n\#L36#Maple Soul Spear\r\n\#L37#Maple Karstan\r\n\#L38#Maple Kandiva Bow\r\n\#L39#Maple Nishada\r\n\#L40#Maple Skanda\r\n\#L41#Maple Cannon Shooter\r\n\#L42#Maple Golden Claw#l\r\n\.");
        } else if (selection == 7) {
      if (cm.haveItem(4001126, 1200)) {
                      cm.gainItem(4001126, -1200);
                      cm.gainItem(1302020, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 8) {
      if (cm.haveItem(4001126, 1200)) {
                      cm.gainItem(4001126, -1200);
                      cm.gainItem(1382009, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 9) {
      if (cm.haveItem(4001126, 1200)) {
                      cm.gainItem(4001126, -1200);
                      cm.gainItem(1452016, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 10) {
      if (cm.haveItem(4001126, 1400)) {
                      cm.gainItem(4001126, -1200);
                      cm.gainItem(1462014, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 11) {
      if (cm.haveItem(4001126, 1200)) {
                      cm.gainItem(4001126, -1200);
                      cm.gainItem(1472030, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 12) {
      if (cm.haveItem(4001126, 1200)) {
                      cm.gainItem(4001126, -1200);
                      cm.gainItem(1482020, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 13) {
      if (cm.haveItem(4001126, 1400)) {
                      cm.gainItem(4001126, -1400);
                      cm.gainItem(1492020, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 14) {
      if (cm.haveItem(4001126, 1400)) {
                      cm.gainItem(4001126, -1400);
                      cm.gainItem(1382012, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 15) {
      if (cm.haveItem(4001126, 1400)) {
                      cm.gainItem(4001126, -1400);
                      cm.gainItem(1302030, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 16) {
      if (cm.haveItem(4001126, 1400)) {
                      cm.gainItem(4001126, -1400);
                      cm.gainItem(1332025, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 17) {
      if (cm.haveItem(4001126, 1400)) {
                      cm.gainItem(4001126, -1400);
                      cm.gainItem(1412011, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 18) {
      if (cm.haveItem(4001126, 1400)) {
                      cm.gainItem(4001126, -1400);
                      cm.gainItem(1422014, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 19) {
      if (cm.haveItem(4001126, 1400)) {
                      cm.gainItem(4001126, -1400);
                      cm.gainItem(1432012, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 20) {
      if (cm.haveItem(4001126, 1400)) {
                      cm.gainItem(4001126, -1400);
                      cm.gainItem(1442024, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 21) {
      if (cm.haveItem(4001126, 1400)) {
                      cm.gainItem(4001126, -1400);
                      cm.gainItem(1452022, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 22) {
      if (cm.haveItem(4001126, 1400)) {
                      cm.gainItem(4001126, -1400);
                      cm.gainItem(1462019, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 23) {
      if (cm.haveItem(4001126, 1400)) {
                      cm.gainItem(4001126, -1400);
                      cm.gainItem(1472032);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 24) {
      if (cm.haveItem(4001126, 1200)) {
                      cm.gainItem(4001126, -1400);
                      cm.gainItem(1492021, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 25) {
      if (cm.haveItem(4001126, 1400)) {
                      cm.gainItem(4001126, -1400);
                      cm.gainItem(1482021, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 26) {
      if (cm.haveItem(4001126, 1600)) {
                      cm.gainItem(4001126, -1600);
                      cm.gainItem(1302064, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 27) {
      if (cm.haveItem(4001126, 1600)) {
                      cm.gainItem(4001126, -1600);
                      cm.gainItem(1312032, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 28) {
      if (cm.haveItem(4001126, 1600)) {
                      cm.gainItem(4001126, -1600);
                      cm.gainItem(1322054, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 29) {
      if (cm.haveItem(4001126, 1600)) {
                      cm.gainItem(4001126, -1600);
                      cm.gainItem(1332055, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 30) {
      if (cm.haveItem(4001126, 1600)) {
                      cm.gainItem(4001126, -1600);
                      cm.gainItem(1332056, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 31) {
      if (cm.haveItem(4001126, 1600)) {
                      cm.gainItem(4001126, -1600);
                      cm.gainItem(1372034, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 32) {
      if (cm.haveItem(4001126, 1600)) {
                      cm.gainItem(4001126, -1600);
                      cm.gainItem(1382039, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 33) {
      if (cm.haveItem(4001126, 1600)) {
                      cm.gainItem(4001126, -1600);
                      cm.gainItem(1402039, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 34) {
      if (cm.haveItem(4001126, 1600)) {
                      cm.gainItem(4001126, -1600);
                      cm.gainItem(1412027, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 35) {
      if (cm.haveItem(4001126, 1600)) {
                      cm.gainItem(4001126, -1600);
                      cm.gainItem(1422029, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 36) {
      if (cm.haveItem(4001126, 1600)) {
                      cm.gainItem(4001126, -1600);
                      cm.gainItem(1432040, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 37) {
      if (cm.haveItem(4001126, 1600)) {
                      cm.gainItem(4001126, -1600);
                      cm.gainItem(1442051, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 38) {
      if (cm.haveItem(4001126, 1600)) {
                      cm.gainItem(4001126, -1600);
                      cm.gainItem(1452045, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 39) {
      if (cm.haveItem(4001126, 1600)) {
                      cm.gainItem(4001126, -1600);
                      cm.gainItem(1462040, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 40) {
      if (cm.haveItem(4001126, 1600)) {
                      cm.gainItem(4001126, -1600);
                      cm.gainItem(1472055, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 41) {
      if (cm.haveItem(4001126, 1600)) {
                      cm.gainItem(4001126, -1600);
                      cm.gainItem(1492022, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }
        } else if (selection == 42) {
      if (cm.haveItem(4001126, 1600)) {
                      cm.gainItem(4001126, -1600);
                      cm.gainItem(1482022, 1);
                      cm.sendOk("Obrigado, aproveite seu novo item!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem folhas suficiente!");
                cm.dispose();
        }

    }
}