/*
 * @Author JavaScriptz
 * LeaderMS 2014
 * Skills p/ 4 Classe
 */

importPackage(Packages.client);
importPackage(Packages.config.configuration);

var status = -1;

function start() {
    action(1, 0, 0);
 }

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        if (status > 0) {
            status--;
        } else {
            cm.dispose();
            return;
        }
    } 
    var p = cm.getPlayer();
    if (status == 0) {
        cm.sendNext("                                  <#e"+Configuration.Server_Name+" 4.JOB#n>             \r\n\r\nOla #e#h ##n, eu sou a Carta a auxiliar do "+Configuration.Server_Name+".\r\nEstou precisando de sua ajuda para #bcoletar#k alguns items preciosos, me ajudando eu te ajudarei a crescer, pode me ajudar? Tudo bem, os items que preciso sao:\r\n\r\n#i4005000# #t4005000# - Qntd. 5\r\n#i4005001# #t4005001# - Qntd. 5\r\n#i4005002# #t4005002# - Qntd. 5\r\n#i4005003# #t4005003# - Qntd. 5\r\n#i4005004# #t4005004# - Qntd. 5 \r\n#i4001126# #t4001126# - Qntd. 5.000\r\n#i4000238# #t4000238# - Qntd. 200\r\n#i4000243# #t4000243# - Qntd. 1\r\n#i4000235# #t4000235# - Qntd. 1\r\n\r\nQuantia em Mesos - #e25m#n  \r\n\r\nSe voce ja #epossui#n estes items, clique em continuar, caso nao tenha, volte novamente mais tarde.");
    } else if (status == 1) {
        cm.sendSimple("Em qual classe gostaria de liberar as skills:" 
                + "\r\n\r\n#L0#Skills nv. 10 para #eHERO#l#n" //dn
                + "\r\n\r\n#L1#Skills nv. 10 para #ePALADIN#l#n" //dn
                + "\r\n\r\n#L2#Skills nv. 10 para #eDRAGON KNIGHT#l#n" //dn
                + "\r\n\r\n#L3#Skills nv. 10 para #eFP/ARCHMAGE#l#n" //dn
                + "\r\n\r\n#L4#Skills nv. 10 para #eIL/ARCHMAGE#l#n" //dn
                + "\r\n\r\n#L5#Skills nv. 10 para #eBISHOP#l#n" //dn
                + "\r\n\r\n#L6#Skills nv. 10 para #eBOWMASTER#l#n" //dn
                + "\r\n\r\n#L7#Skills nv. 10 para #eCROSSBOWMASTER#l#n" //dn
                + "\r\n\r\n#L8#Skills nv. 10 para #eNIGHTLORD#l#n" //dn
                + "\r\n\r\n#L9#Skills nv. 10 para #eSHADOWER#l#n" //dn
                + "\r\n\r\n#L10#Skills nv. 10 para #eBUCCANEER#l#n" //dn
                + "\r\n\r\n#L11#Skills nv. 10 para #eCORSAIR#l#n");
        
    }  else if(selection == 0) { /* HERO */
      if(cm.haveItem(4005000, 5) && cm.haveItem(4005001, 5) && cm.haveItem(4005002, 5) && cm.haveItem(4005003, 5) && cm.haveItem(4005004, 5) && cm.haveItem(4001126, 5000) && cm.haveItem(4000238, 200) &&  cm.haveItem(4000243, 1) && cm.haveItem(4000235, 1) && cm.getPlayer().getMeso() > 25000000 && cm.getJob().equals(MapleJob.HERO)) { 
                                     /* Remover items */ 
                                    cm.gainItem(4005000, -5);
                                    cm.gainItem(4005001, -5);
                                    cm.gainItem(4005002, -5);
                                    cm.gainItem(4005003, -5);
                                    cm.gainItem(4005004, -5);
                                    cm.gainItem(4001126, -5000);
                                    cm.gainItem(4000238, -200);
                                    cm.gainItem(4000243, -1);
                                    cm.gainItem(4000235, -1);
                                    cm.gainMeso(-25000000);
                                    /* Remover items */ 
                                    cm.teachSkill(1121002,0,10);
                                    cm.teachSkill(1120003,0,10);
                                    cm.teachSkill(1120005,0,10);
                                    cm.teachSkill(1121006,0,10);
                                    cm.teachSkill(1121010,0,10);
                                    cm.teachSkill(1121011,0,5);
                                    p.ganhaSp(0);                                    
                                    cm.getPlayer().updateSingleStat(MapleStat.AVAILABLESP, p.getRemainingSp());
            cm.sendOk("Obrigado, voce ja pode aproveitar suas novas habilidades!");
            cm.dispose();
        } else{
        cm.sendOk("Que pena, voce ainda nao tem os items suficientes.");
        cm.dispose();
    }
  } else if(selection == 1) { /* PALADIN  */
      if(cm.haveItem(4005000, 5) && cm.haveItem(4005001, 5) && cm.haveItem(4005002, 5) && cm.haveItem(4005003, 5) && cm.haveItem(4005004, 5) && cm.haveItem(4001126, 5000) && cm.haveItem(4000238, 200) &&  cm.haveItem(4000243, 1) && cm.haveItem(4000235, 1) && cm.getPlayer().getMeso() > 25000000 && cm.getJob().equals(MapleJob.PALADIN)) { 
                                     /* Remover items */ 
                                    cm.gainItem(4005000, -5);
                                    cm.gainItem(4005001, -5);
                                    cm.gainItem(4005002, -5);
                                    cm.gainItem(4005003, -5);
                                    cm.gainItem(4005004, -5);
                                    cm.gainItem(4001126, -5000);
                                    cm.gainItem(4000238, -200);
                                    cm.gainItem(4000243, -1);
                                    cm.gainItem(4000235, -1);
                                    cm.gainMeso(-25000000);
                                    /* Remover items */ 
                                    cm.teachSkill(1221002,0,10);
                                    cm.teachSkill(1221003,0,10);
                                    cm.teachSkill(1221004,0,10);
                                    cm.teachSkill(1220006,0,10);
                                    cm.teachSkill(1221007,0,10);
                                    cm.teachSkill(1220010,0,10);
                                    cm.teachSkill(1221011,0,10);
                                    cm.teachSkill(1221012,0,5);
                                    p.ganhaSp(0);                                    
                                    cm.getPlayer().updateSingleStat(MapleStat.AVAILABLESP, p.getRemainingSp());
            cm.sendOk("Obrigado, voce ja pode aproveitar suas novas habilidades!");
            cm.dispose();
        } else{
        cm.sendOk("Que pena, voce ainda nao tem os items suficientes.");
        cm.dispose();
    }
  } else if(selection == 2) { /* DARK KNIGHT  */
      if(cm.haveItem(4005000, 5) && cm.haveItem(4005001, 5) && cm.haveItem(4005002, 5) && cm.haveItem(4005003, 5) && cm.haveItem(4005004, 5) && cm.haveItem(4001126, 5000) && cm.haveItem(4000238, 200) &&  cm.haveItem(4000243, 1) && cm.haveItem(4000235, 1) && cm.getPlayer().getMeso() > 25000000 && cm.getJob().equals(MapleJob.DARKKNIGHT)) { 
                                     /* Remover items */ 
                                    cm.gainItem(4005000, -5);
                                    cm.gainItem(4005001, -5);
                                    cm.gainItem(4005002, -5);
                                    cm.gainItem(4005003, -5);
                                    cm.gainItem(4005004, -5);
                                    cm.gainItem(4001126, -5000);
                                    cm.gainItem(4000238, -200);
                                    cm.gainItem(4000243, -1);
                                    cm.gainItem(4000235, -1);
                                    cm.gainMeso(-25000000);
                                    /* Remover items */ 
                                    cm.teachSkill(1321002,0,10);
                                    cm.teachSkill(1321003,0,10);
                                    cm.teachSkill(1320006,0,10);
                                    cm.teachSkill(1320008,0,10);
                                    cm.teachSkill(1320009,0,10);
                                    cm.teachSkill(1321010,0,5);
                                    p.ganhaSp(0);
                                    cm.getPlayer().updateSingleStat(MapleStat.AVAILABLESP, p.getRemainingSp());
            cm.sendOk("Obrigado, voce ja pode aproveitar suas novas habilidades!");
            cm.dispose();
        } else{
        cm.sendOk("Que pena, voce ainda nao tem os items suficientes.");
        cm.dispose();
    }
  } else if(selection == 3) { /* FIRE_POISTON_MAGE    */
      if(cm.haveItem(4005000, 5) && cm.haveItem(4005001, 5) && cm.haveItem(4005002, 5) && cm.haveItem(4005003, 5) && cm.haveItem(4005004, 5) && cm.haveItem(4001126, 5000) && cm.haveItem(4000238, 200) &&  cm.haveItem(4000243, 1) && cm.haveItem(4000235, 1) && cm.getPlayer().getMeso() > 25000000 && cm.getJob().equals(MapleJob.FP_ARCHMAGE)) { 
                                     /* Remover items */ 
                                    cm.gainItem(4005000, -5);
                                    cm.gainItem(4005001, -5);
                                    cm.gainItem(4005002, -5);
                                    cm.gainItem(4005003, -5);
                                    cm.gainItem(4005004, -5);
                                    cm.gainItem(4001126, -5000);
                                    cm.gainItem(4000238, -200);
                                    cm.gainItem(4000243, -1);
                                    cm.gainItem(4000235, -1);
                                    cm.gainMeso(-25000000);
                                    /* Remover items */ 
                                    cm.teachSkill(2121003,0,10);
                                    cm.teachSkill(2121004,0,10);
                                    cm.teachSkill(2121005,0,10);
                                    cm.teachSkill(2121007,0,10);
                                    cm.teachSkill(2121008,0,5);
                                    p.ganhaSp(0);                                   
                                    cm.getPlayer().updateSingleStat(MapleStat.AVAILABLESP, p.getRemainingSp());
            cm.sendOk("Obrigado, voce ja pode aproveitar suas novas habilidades!");
            cm.dispose();
        } else{
        cm.sendOk("Que pena, voce ainda nao tem os items suficientes.");
        cm.dispose();
    }
  } else if(selection == 4) { /* IL_ARCHMAGE    */
      if(cm.haveItem(4005000, 5) && cm.haveItem(4005001, 5) && cm.haveItem(4005002, 5) && cm.haveItem(4005003, 5) && cm.haveItem(4005004, 5) && cm.haveItem(4001126, 5000) && cm.haveItem(4000238, 200) &&  cm.haveItem(4000243, 1) && cm.haveItem(4000235, 1) && cm.getPlayer().getMeso() > 25000000 && cm.getJob().equals(MapleJob.IL_ARCHMAGE)) { 
                                     /* Remover items */ 
                                    cm.gainItem(4005000, -5);
                                    cm.gainItem(4005001, -5);
                                    cm.gainItem(4005002, -5);
                                    cm.gainItem(4005003, -5);
                                    cm.gainItem(4005004, -5);
                                    cm.gainItem(4001126, -5000);
                                    cm.gainItem(4000238, -200);
                                    cm.gainItem(4000243, -1);
                                    cm.gainItem(4000235, -1);
                                    cm.gainMeso(-25000000);
                                    /* Remover items */ 
                                    cm.teachSkill(2221003,0,10);
                                    cm.teachSkill(2221004,0,10);
                                    cm.teachSkill(2221005,0,10);
                                    cm.teachSkill(2221007,0,10);
                                    cm.teachSkill(2221008,0,5);
                                    p.ganhaSp(0);                                    
                                    cm.getPlayer().updateSingleStat(MapleStat.AVAILABLESP, p.getRemainingSp());
            cm.sendOk("Obrigado, voce ja pode aproveitar suas novas habilidades!");
            cm.dispose();
        } else{
        cm.sendOk("Que pena, voce ainda nao tem os items suficientes.");
        cm.dispose();
    }
  } else if(selection == 5) { /* BISPO */
      if(cm.haveItem(4005000, 5) && cm.haveItem(4005001, 5) && cm.haveItem(4005002, 5) && cm.haveItem(4005003, 5) && cm.haveItem(4005004, 5) && cm.haveItem(4001126, 5000) && cm.haveItem(4000238, 200) &&  cm.haveItem(4000243, 1) && cm.haveItem(4000235, 1) && cm.getPlayer().getMeso() > 25000000 && cm.getJob().equals(MapleJob.BISHOP)) { 
                                     /* Remover items */ 
                                    cm.gainItem(4005000, -5);
                                    cm.gainItem(4005001, -5);
                                    cm.gainItem(4005002, -5);
                                    cm.gainItem(4005003, -5);
                                    cm.gainItem(4005004, -5);
                                    cm.gainItem(4001126, -5000);
                                    cm.gainItem(4000238, -200);
                                    cm.gainItem(4000243, -1);
                                    cm.gainItem(4000235, -1);
                                    cm.gainMeso(-25000000);
                                    /* Remover items */ 
                                    cm.teachSkill(2321003,0,10);
                                    cm.teachSkill(2321004,0,10);
                                    cm.teachSkill(2321006,0,10);
                                    cm.teachSkill(2321007,0,10);
                                    cm.teachSkill(2321008,0,10);
				    cm.teachSkill(2321009,0,5);
                                    p.ganhaSp(0);
                                    cm.getPlayer().updateSingleStat(MapleStat.AVAILABLESP, p.getRemainingSp());
            cm.sendOk("Obrigado, voce ja pode aproveitar suas novas habilidades!");
            cm.dispose();
        } else{
        cm.sendOk("Que pena, voce ainda nao tem os items suficientes.");
        cm.dispose();
    }
 } else if(selection == 6) { /* BOWMASTER */
      if(cm.haveItem(4005000, 5) && cm.haveItem(4005001, 5) && cm.haveItem(4005002, 5) && cm.haveItem(4005003, 5) && cm.haveItem(4005004, 5) && cm.haveItem(4001126, 5000) && cm.haveItem(4000238, 200) &&  cm.haveItem(4000243, 1) && cm.haveItem(4000235, 1) && cm.getPlayer().getMeso() > 25000000 && cm.getJob().equals(MapleJob.BOWMASTER)) { 
                                     /* Remover items */ 
                                    cm.gainItem(4005000, -5);
                                    cm.gainItem(4005001, -5);
                                    cm.gainItem(4005002, -5);
                                    cm.gainItem(4005003, -5);
                                    cm.gainItem(4005004, -5);
                                    cm.gainItem(4001126, -5000);
                                    cm.gainItem(4000238, -200);
                                    cm.gainItem(4000243, -1);
                                    cm.gainItem(4000235, -1);
                                    cm.gainMeso(-25000000);
                                    /* Remover items */ 
                                    cm.teachSkill(3121003,0,10);
                                    cm.teachSkill(3121004,0,10);
                                    cm.teachSkill(3121006,0,10);
                                    cm.teachSkill(3121008,0,10);
                                    cm.teachSkill(3121009,0,5);
                                    p.ganhaSp(0); 
                                    cm.getPlayer().updateSingleStat(MapleStat.AVAILABLESP, p.getRemainingSp());
            cm.sendOk("Obrigado, voce ja pode aproveitar suas novas habilidades!");
            cm.dispose();
        } else{
        cm.sendOk("Que pena, voce ainda nao tem os items suficientes.");
        cm.dispose();
    }
 } else if(selection == 7) { /* CROSSBOWMASTER */
      if(cm.haveItem(4005000, 5) && cm.haveItem(4005001, 5) && cm.haveItem(4005002, 5) && cm.haveItem(4005003, 5) && cm.haveItem(4005004, 5) && cm.haveItem(4001126, 5000) && cm.haveItem(4000238, 200) &&  cm.haveItem(4000243, 1) && cm.haveItem(4000235, 1) && cm.getPlayer().getMeso() > 25000000 && cm.getJob().equals(MapleJob.CROSSBOWMASTER)) { 
                                     /* Remover items */ 
                                    cm.gainItem(4005000, -5);
                                    cm.gainItem(4005001, -5);
                                    cm.gainItem(4005002, -5);
                                    cm.gainItem(4005003, -5);
                                    cm.gainItem(4005004, -5);
                                    cm.gainItem(4001126, -5000);
                                    cm.gainItem(4000238, -200);
                                    cm.gainItem(4000243, -1);
                                    cm.gainItem(4000235, -1);
                                    cm.gainMeso(-25000000);
                                    /* Remover items */ 
                                    cm.teachSkill(3221001,0,10);
                                    cm.teachSkill(3221003,0,10);
                                    cm.teachSkill(3221005,0,10);
                                    cm.teachSkill(3221007,0,10);
                                    cm.teachSkill(3221008,0,5);
                                    p.ganhaSp(0); 
                                    cm.getPlayer().updateSingleStat(MapleStat.AVAILABLESP, p.getRemainingSp());
            cm.sendOk("Obrigado, voce ja pode aproveitar suas novas habilidades!");
            cm.dispose();
        } else{
        cm.sendOk("Que pena, voce ainda nao tem os items suficientes.");
        cm.dispose();
    }
 }  else if(selection == 8) { /*NIGHTLORD*/
      if(cm.haveItem(4005000, 5) && cm.haveItem(4005001, 5) && cm.haveItem(4005002, 5) && cm.haveItem(4005003, 5) && cm.haveItem(4005004, 5) && cm.haveItem(4001126, 5000) && cm.haveItem(4000238, 200) &&  cm.haveItem(4000243, 1) && cm.haveItem(4000235, 1) && cm.getPlayer().getMeso() > 25000000 && cm.getJob().equals(MapleJob.NIGHTLORD)) { 
                                     /* Remover items */ 
                                    cm.gainItem(4005000, -5);
                                    cm.gainItem(4005001, -5);
                                    cm.gainItem(4005002, -5);
                                    cm.gainItem(4005003, -5);
                                    cm.gainItem(4005004, -5);
                                    cm.gainItem(4001126, -5000);
                                    cm.gainItem(4000238, -200);
                                    cm.gainItem(4000243, -1);
                                    cm.gainItem(4000235, -1);
                                    cm.gainMeso(-25000000);
                                    /* Remover items */ 
                                    cm.teachSkill(4121003,0,10);
                                    cm.teachSkill(4121004,0,10);
                                    cm.teachSkill(4121007,0,10);
                                    cm.teachSkill(4121008,0,10);
                                    cm.teachSkill(4121009,0,5);
                                    p.ganhaSp(0);  
                                    cm.getPlayer().updateSingleStat(MapleStat.AVAILABLESP, p.getRemainingSp());
            cm.sendOk("Obrigado, voce ja pode aproveitar suas novas habilidades!");
            cm.dispose();
        } else{
        cm.sendOk("Que pena, voce ainda nao tem os items suficientes.");
        cm.dispose();
    }
 } else if(selection == 9) { /*Shadower  */
      if(cm.haveItem(4005000, 5) && cm.haveItem(4005001, 5) && cm.haveItem(4005002, 5) && cm.haveItem(4005003, 5) && cm.haveItem(4005004, 5) && cm.haveItem(4001126, 5000) && cm.haveItem(4000238, 200) &&  cm.haveItem(4000243, 1) && cm.haveItem(4000235, 1) && cm.getPlayer().getMeso() > 25000000 && cm.getJob().equals(MapleJob.SHADOWER)) { 
                                     /* Remover items */ 
                                    cm.gainItem(4005000, -5);
                                    cm.gainItem(4005001, -5);
                                    cm.gainItem(4005002, -5);
                                    cm.gainItem(4005003, -5);
                                    cm.gainItem(4005004, -5);
                                    cm.gainItem(4001126, -5000);
                                    cm.gainItem(4000238, -200);
                                    cm.gainItem(4000243, -1);
                                    cm.gainItem(4000235, -1);
                                    cm.gainMeso(-25000000);
                                    /* Remover items */ 
                                    cm.teachSkill(4221001,0,10);
                                    cm.teachSkill(4221003,0,10);
                                    cm.teachSkill(4221004,0,10);
                                    cm.teachSkill(4221006,0,10);
                                    cm.teachSkill(4221008,0,5);
                                    p.ganhaSp(0);
                                    cm.getPlayer().updateSingleStat(MapleStat.AVAILABLESP, p.getRemainingSp());
            cm.sendOk("Obrigado, voce ja pode aproveitar suas novas habilidades!");
            cm.dispose();
        } else{
        cm.sendOk("Que pena, voce ainda nao tem os items suficientes.");
        cm.dispose();
    }
 } else if(selection == 10) { /*Buccaneer    */
      if(cm.haveItem(4005000, 5) && cm.haveItem(4005001, 5) && cm.haveItem(4005002, 5) && cm.haveItem(4005003, 5) && cm.haveItem(4005004, 5) && cm.haveItem(4001126, 5000) && cm.haveItem(4000238, 200) &&  cm.haveItem(4000243, 1) && cm.haveItem(4000235, 1) && cm.getPlayer().getMeso() > 25000000 && cm.getJob().equals(MapleJob.BUCCANEER)) { 
                                     /* Remover items */ 
                                    cm.gainItem(4005000, -5);
                                    cm.gainItem(4005001, -5);
                                    cm.gainItem(4005002, -5);
                                    cm.gainItem(4005003, -5);
                                    cm.gainItem(4005004, -5);
                                    cm.gainItem(4001126, -5000);
                                    cm.gainItem(4000238, -200);
                                    cm.gainItem(4000243, -1);
                                    cm.gainItem(4000235, -1);
                                    cm.gainMeso(-25000000);
                                    /* Remover items */ 
                                    cm.teachSkill(5121002,0,10);
                                    cm.teachSkill(5121005,0,10);
                                    cm.teachSkill(5121007,0,10);
                                    cm.teachSkill(5121008,0,5);
                                    cm.teachSkill(5121009,0,10);
                                    cm.teachSkill(5121010,0,10);
                                    p.ganhaSp(0);
                                    cm.getPlayer().updateSingleStat(MapleStat.AVAILABLESP, p.getRemainingSp());
            cm.sendOk("Obrigado, voce ja pode aproveitar suas novas habilidades!");
            cm.dispose();
        } else{
        cm.sendOk("Que pena, voce ainda nao tem os items suficientes.");
        cm.dispose();
    }
 } else if(selection == 11) { /*Corsair    */
      if(cm.haveItem(4005000, 5) && cm.haveItem(4005001, 5) && cm.haveItem(4005002, 5) && cm.haveItem(4005003, 5) && cm.haveItem(4005004, 5) && cm.haveItem(4001126, 5000) && cm.haveItem(4000238, 200) &&  cm.haveItem(4000243, 1) && cm.haveItem(4000235, 1) && cm.getPlayer().getMeso() > 25000000 && cm.getJob().equals(MapleJob.CORSAIR)) { 
                                     /* Remover items */ 
                                    cm.gainItem(4005000, -5);
                                    cm.gainItem(4005001, -5);
                                    cm.gainItem(4005002, -5);
                                    cm.gainItem(4005003, -5);
                                    cm.gainItem(4005004, -5);
                                    cm.gainItem(4001126, -5000);
                                    cm.gainItem(4000238, -200);
                                    cm.gainItem(4000243, -1);
                                    cm.gainItem(4000235, -1);
                                    cm.gainMeso(-25000000);
                                    /* Remover items */ 
                                    cm.teachSkill(5220001,0,10);
                                    cm.teachSkill(5220002,0,10);
                                    cm.teachSkill(5221006,0,10);
                                    cm.teachSkill(5221007,0,10);
                                    cm.teachSkill(5221008,0,10);
                                    cm.teachSkill(5221009,0,10);
                                    p.ganhaSp(0);
                                    cm.getPlayer().updateSingleStat(MapleStat.AVAILABLESP, p.getRemainingSp());
            cm.sendOk("Obrigado, voce ja pode aproveitar suas novas habilidades!");
            cm.dispose();
        } else{
        cm.sendOk("Que pena, voce ainda nao tem os items suficientes.");
        cm.dispose();
    }
 }
} 
