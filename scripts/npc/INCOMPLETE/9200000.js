///*
// * LeaderMS Private Server
// * Servidor Brasileiro 2011-2014
// * JavaScriptz <javascriptz@leaderms.com.br>
// * Troca de LeaderPoints
// */
//
//var status;
//function start() {
//status = -1;
//action( 1, 0, 0);
//}
//
//function action (mode, type , selection) {
//if (mode == 1) { 
//     status++; 
// }else{ 
//       status--; 
//}
//if (status == 0) { 
//cm.sendSimple("Ola #e#h ##n, eu sou Cody o auxiliar do LeaderMS.\r\nEstou precisando de sua ajuda para #bcoletar#k alguns pontos, me ajudando eu te darei items especiais, pode me ajudar? Tudo bem, os pontos podem ser obtidos atraves do jogo (LeaderPoints).\r\nLembrando que e necessario ter a ocupacao #e#rrequerida#n#k. \r\n\r\nSe voce ja #epossui#n estes pontos, clique em continuar, caso nao tenha, volte novamente mais tarde.\r\n\#L0#Pergaminhos (Raros)#l\r\n\#L206#Trocar 3.000 LeaderPoints por Trofeu (1)#l\r\n\#L205#Trocar (1) #t4031442# por 1.000 LeaderPoints#l\r\n\#L204#Trocar 1.000 Maple Leaf por 300 LeaderPoints#l\r\n\#L3##r#eComo obter pontos?#k#l#n");
//} else if (status == 1){
//if (selection == 0) {
//cm.sendSimple("                                     #e#r<Pergaminhos>#k#n\r\n\r\n\r\n\r\n#e#z2340000##n - Qntd. 1\r\n#eLeaderPoints#n - 7.000\r\n#eOcupacao#n - Platinium\r\n#L35#Trocar este item!#l\r\n\r\n\r\n#e#z2049100##n - Qntd. 1\r\n#eLeaderPoints#n - 4.000\r\n#eOcupacao#n - Platinium\r\n#L36#Trocar este item!#l");
// }  else if (selection == 3){
//                 cm.sendSimple("#eConseguindo LeaderPoints - Mini Tuto#n\r\nOs LeaderPoints sao adquiridos atraves de monstros, cada monstro derrotado voce tem uma porcetagem de ganhar uma quantia de LeaderPoints, eles variam de 1 entre 3.\r\n\r\n#eConseguindo Ocupacao - Mini Tuto#n\r\nVoce devera participar de grande maioria das missoes espalhadas pelo Leader, apos o termino delas voce acumula pontos para poder obter uma ocupacao! ");
// }    else if (selection == 204){
//            if(cm.haveItem(4001126, 1000)) {
//            cm.gainItem(4001126, -1000)
//            cm.gainLeaderPoints(300);
//            cm.getPlayer().saveToDB(true, true);
//            cm.sendOk("Obrigado, seus pontos foram adquiridos!");
//            cm.dispose();
//        } else {
//        cm.sendOk("Que pena, voce ainda nao tem folhas suficientes para este tipo de troca.");
//        cm.dispose();
//    } 
//} else if(selection == 205) {
//     if(cm.haveItem(4031442, 1)) {
//            cm.gainItem(4031442, -1)
//            cm.gainLeaderPoints(1000);
//            cm.getPlayer().saveToDB(true, true);
//            cm.sendOk("Obrigado, seus pontos foram adquiridos!");
//            cm.dispose();
//        } else {
//        cm.sendOk("Que pena, voce ainda nao tem presente(s) suficiente(s) para este tipo de troca.");
//        cm.dispose();
//  } 
//  }  else if(selection == 206) {
//     if(cm.getLeaderPoints() >= 3000) {
//            cm.gainItem(4000038, 1)
//            cm.gainLeaderPoints(-3000);
//            cm.getPlayer().saveToDB(true, true);
//            cm.sendOk("Obrigado, voce ganhou um Trofeu!");
//            cm.dispose();
//        } else {
//        cm.sendOk("Que pena, voce ainda nao tem pontos suficientes para este tipo de troca.");
//        cm.dispose();
//  }
//  }
//  }  else if (status == 2){
//     if (selection == 35){
//        if(cm.getLeaderPoints() >= 7000) {
//            cm.gainLeaderPoints(-7000);
//            cm.getPlayer().saveToDB(true, true);
//            cm.gainItem(2340000, 1);
//            cm.sendOk("Obrigado, ja pode desfrutar de seu novo item!");
//            cm.dispose();
//        } else {
//        cm.sendOk("Que pena, voce ainda nao tem os #epontos#n ou #eocupacao#n suficiente para este tipo de troca.");
//        cm.dispose();
//     }
//    } else if (selection == 36){
//        if(cm.getLeaderPoints() >= 4000) {
//            cm.gainLeaderPoints(-4000);
//            cm.getPlayer().saveToDB(true, true);
//            cm.gainItem(2049100, 1);
//            cm.sendOk("Obrigado, ja pode desfrutar de seu novo item!");
//            cm.dispose();
//        } else {
//        cm.sendOk("Que pena, voce ainda nao tem os #epontos#n ou #eocupacao#n suficiente para este tipo de troca.");
//        cm.dispose();
//     }
//    }
//   }   
// }

var status;
function start() {
status = -1;
action( 1, 0, 0);
}

function action (mode, type , selection) {
cm.sendOk("Ola, seja bem-vindo ao LeaderMS!\r\nEm breve teremos novidades por aqui, aguarde!");
cm.dispose();
}