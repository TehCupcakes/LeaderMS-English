/* Cadeiras
Made By bahadirtje Ragezone- YarakMS
Pf, best nomes de cadeiras by Ank 
*/
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
            cm.sendSimple("                          #e<LeaderMS - Cadeiras>#n            \r\n\r\nOla #e#h ##n.\r\nAqui voce pode adquirir diversas Cadeiras.\r\n\Voce tem algum presente para mim?#b\r\n\#L1#Trocar Presentes");
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
            cm.sendSimple("Quantos presentes voce tem?#b\r\n\#L4# #v 4031442 # Trocar 5 Presentes por cadeiras  \r\n\#L5# Trocar 10 Presentes por cadeiras \r\n\#L6# Trocar 20 Presentes por cadeiras");
        }  else if (selection == 4) {
        cm.sendSimple("Escolha a sua cadeira, sao  5 Presentes#b cada!\r\n\#L7# #v 3010004 # Yellow Relaxer\r\n\#L8# #v 3010005 # Red Relaxer\r\n\#L9# #v 3010011 # Amorian Relaxer \r\n\ #L10# #v 3010006 # Yellow Chair \r\n\ #L11# #v 3010002 # Green Chair \r\n\ #L12# #v 3010003 # Red Chair \r\n\ #L13# #v 3010012 # Warrior Throne \r\n\. ");
        }  else if (selection == 5) {
        cm.sendSimple("Escolha a sua cadeira, sao  10 Presentes#b cada!\r\n\#L14# #v 3010008 # Cadeira de Foca Azul \r\n\#L15# #v 3010007 # Cadeira de Foca Rosa\r\n\#L16##v 3010017 # Cadeira de Foca Douorada\r\n\ #L17# #v 3010016 # Cadeira de Foca Cinza \r\n\ #L18# #v 3010010 # Cadeira de Foca Branca \r\n\ #L19# #v 3010013 # Cadeira de Praia com Palmeiras  \r\n\ #L20# #v 3010018 # Cadeira de Praia Rosa \r\n\ #L21# #v 3011000 # Cadeira de Pesca\r\n\. ");
        }  else if (selection == 6) {
        cm.sendSimple
("Escolha a sua cadeira, sao  20 Presentes#b cada!\r\n\#L22# #v 3010022 # Urso Polar Branco (Coca Cola) \r\n\#L23# #v 3010023 # Urso Polar Marrom (Coca Cola)\r\n\#L24# #v 3010025 # Cadeira Arvore Maple\r\n\ #L25# #v 3010009 # Cadeira Rosa Redonda \r\n\ #L26# #v 3010040 # Cadeira de Morcego \r\n\ #L27# #v 3010026 # Cadeira de Urso Zumbi -_- \r\n\ #L28# #v 3010028 # Cadeira do Lorde Pirata \r\n\ #L29# #v 3010041 # Trono de Cranio \r\n\. ");
        } else if (selection == 7) {
      if (cm.haveItem(4031442, 5)) {
                      cm.gainItem(4031442, -5);
                      cm.gainItem(3010004, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        } else if (selection == 8) {
      if (cm.haveItem(4031442, 5)) {
                      cm.gainItem(4031442, -5);
                      cm.gainItem(3010005, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        } else if (selection == 9) {
      if (cm.haveItem(4031442, 5)) {
                      cm.gainItem(4031442, -5);
                      cm.gainItem(3010011, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        } else if (selection == 10) {
      if (cm.haveItem(4031442, 5)) {
                      cm.gainItem(4031442, -5);
                      cm.gainItem(3010006, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        } else if (selection == 11) {
      if (cm.haveItem(4031442, 5)) {
                      cm.gainItem(4031442, -5);
                      cm.gainItem(3010002, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        } else if (selection == 12) {
      if (cm.haveItem(4031442, 5)) {
                      cm.gainItem(4031442, -5);
                      cm.gainItem(3010003, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        } else if (selection == 13) {
      if (cm.haveItem(4031442, 5)) {
                      cm.gainItem(4031442, -5);
                      cm.gainItem(3010012, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        } else if (selection == 14) {
      if (cm.haveItem(4031442, 10)) {
                      cm.gainItem(4031442, -10);
                      cm.gainItem(3010008, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        } else if (selection == 15) {
      if (cm.haveItem(4031442, 10)) {
                      cm.gainItem(4031442, -10);
                      cm.gainItem(3010007, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        } else if (selection == 16) {
      if (cm.haveItem(4031442, 10)) {
                      cm.gainItem(4031442, -10);
                      cm.gainItem(3010017, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        } else if (selection == 17) {
      if (cm.haveItem(4031442, 10)) {
                      cm.gainItem(4031442, -10);
                      cm.gainItem(3010016, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        } else if (selection == 18) {
      if (cm.haveItem(4031442, 10)) {
                      cm.gainItem(4031442, -10);
                      cm.gainItem(3010010, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        } else if (selection == 19) {
      if (cm.haveItem(4031442, 10)) {
                      cm.gainItem(4031442, -10);
                      cm.gainItem(3010013, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        } else if (selection == 20) {
      if (cm.haveItem(4031442, 10)) {
                      cm.gainItem(4031442, -10);
                      cm.gainItem(3010018, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        } else if (selection == 21) {
      if (cm.haveItem(4031442, 10)) {
                      cm.gainItem(4031442, -10);
                      cm.gainItem(3011000, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        } else if (selection == 22) {
      if (cm.haveItem(4031442, 20)) {
                      cm.gainItem(4031442, -20);
                      cm.gainItem(3010022, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        } else if (selection == 23) {
      if (cm.haveItem(4031442, 20)) {
                      cm.gainItem(4031442, -20);
                      cm.gainItem(3010023, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        } else if (selection == 24) {
      if (cm.haveItem(4031442, 20)) {
                      cm.gainItem(4031442, -20);
                      cm.gainItem(3010025, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        } else if (selection == 25) {
      if (cm.haveItem(4031442, 20)) {
                      cm.gainItem(4031442, -20);
                      cm.gainItem(3010009, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        } else if (selection == 26) {
      if (cm.haveItem(4031442, 20)) {
                      cm.gainItem(4031442, -20);
                      cm.gainItem(3010040, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        } else if (selection == 27) {
      if (cm.haveItem(4031442, 20)) {
                      cm.gainItem(4031442, -20);
                      cm.gainItem(3010026, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        } else if (selection == 28) {
      if (cm.haveItem(4031442, 20)) {
                      cm.gainItem(4031442, -20);
                      cm.gainItem(3010028, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        } else if (selection == 29) {
      if (cm.haveItem(4031442, 20)) {
                      cm.gainItem(4031442, -20);
                      cm.gainItem(3010041, 1);
                      cm.sendOk("Obrigado, aproveite sua nova cadeira!");
                      cm.dispose();
            } else {
                cm.sendOk("Desculpe, mais voce nao tem presentes suficiente!");
                cm.dispose();
        }
        

    }
}