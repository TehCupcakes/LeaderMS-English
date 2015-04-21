/*
* @autor Java
* LeaderMS MapleStory Private Server
* Trocas
*/

var status;
function start() {
status = -1;//sets status to -1
action( 1, 0, 0);
}
function action (mode, type , selection) {
if (mode == 1) { 
     status++; 
 }else{ 
       status--; 
}
if (status == 0) { 
cm.sendSimple("Ola #e#h ##n, eu sou Tia a auxiliar do LeaderMS.\r\nSe voce tem alguns pontos de cash e deseja trocar, basta prosseguir com nossa conversa. \r\n\r\nSe voce ja #epossui#n estes pontos, clique em trocar, caso nao tenha, volte novamente mais tarde.\r\n\r\nVoce possui (#e" + cm.getPlayer().getCashPoints() + "#n) CashPoints.\r\n\#L0#Trocar CashPoints#l\r\n\#L1#Trocar Trofeus#l\r\n\#L2##r#eComo obter pontos?#k#l#n");
}else if (status == 1){
if (selection == 0) {
cm.sendSimple("                                #e#r<CashPoints - Troca>#k#n\r\n\r\n#L20#Trocar - 1.000 (CashPoints) por 2.000 de Cash#l\r\n#L21#Trocar - 1.500 (CashPoints) por 3.000 de Cash#l\r\n#L22#Trocar - 2.000 (CashPoints) por 5.000 de Cash#l");
}else if (selection == 1){
cm.sendSimple("                                #e#r<CashPoints - Troca>#k#n\r\n\r\n#L23#Trocar - 1 (Trofeu) por 5.000 de Cash#l\r\n#L24#Trocar - 2 (Trofeu(s)) por 12.000 de Cash#l");
} else if (selection == 2){
cm.sendSimple("                                #e#r<CashPoints - Infos>#k#n\r\n\r\nOs CashPoints podem ser obtidos atraves da Quest que se localiza em algum lugar de Kerning. Voce deve procurar NPC \"Cashiro\" e verificar se esta no horario correto para poder participar da Quest e poder pegar alguns pontos.");
}
} else if (status == 2){
if (selection == 20){
if (cm.getPlayer().getCashPoints() >= 1000){
cm.getPlayer().modifyCSPoints(1, 2000);
cm.getPlayer().gainCashPoints(-1000);
cm.getPlayer().dropMessage("Voce ganhou 2.000 de Cash!")
cm.dispose();
}else{
cm.sendOk("Voce nao possui CashPoints suficientes para efutuar a troca!");
cm.dispose();
}
}else if (selection == 21){
if (cm.getPlayer().getCashPoints() >= 1500){
cm.getPlayer().modifyCSPoints(1, 3000);
cm.getPlayer().gainCashPoints(-1500);
cm.getPlayer().dropMessage("Voce ganhou 3.000 de Cash!")
cm.dispose();
}else{
cm.sendOk("Voce nao possui CashPoints suficientes para efutuar a troca!");
cm.dispose();
}
}else if (selection == 22){
if (cm.getPlayer().getCashPoints() >= 2000){
cm.getPlayer().modifyCSPoints(1, 5000);
cm.getPlayer().gainCashPoints(-2000);
cm.getPlayer().dropMessage("Voce ganhou 5.000 de Cash!")
cm.dispose();
}else{
cm.sendOk("Voce nao possui CashPoints suficientes para efutuar a troca!");
cm.dispose();
}
}else if (selection  == 23){
if (cm.getPlayer().haveItem(4000038)){
cm.gainItem(4000038, -1);
cm.getPlayer().modifyCSPoints(1, 5000);
cm.getPlayer().dropMessage("Voce ganhou 5.000 de Cash!")
cm.dispose();
}else{
cm.sendOk("Desculpe, mais voce nao tem o Trofeu para troca!");
cm.dispose();
}
}else if (selection  == 24){
if (cm.haveItem(4000038, 2)){
cm.gainItem(4000038, -2);
cm.getPlayer().modifyCSPoints(1, 12000);
cm.getPlayer().dropMessage("Voce ganhou 12.000 de Cash!")
cm.dispose();
}else{
cm.sendOk("Desculpe, mais voce nao tem o Trofeu para troca!");
cm.dispose();
}
}
}
}
 
