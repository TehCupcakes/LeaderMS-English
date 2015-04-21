/*
 * LeaderMS Private Server
 * Servidor Brasileiro 2011-2014
 * JavaScriptz <javascriptz@leaderms.com.br>
 * Troca de LeaderPoints
 */

var status;
function start() {
status = -1;
action( 1, 0, 0);
}
function action (mode, type , selection) {
if (mode == 1) { 
     status++; 
 }else{ 
       status--; 
}
if(cm.getChar().getMapId() == 910000000){
if (status == 0) { 
cm.sendSimple("Ola #e#h ##n, eu sou Ria a auxiliar do LeaderMS.\r\nSe voce tem alguns pontos de cash e deseja trocar, basta prosseguir com nossa conversa. \r\n\r\nSe voce ja #epossui#n estes pontos, clique em trocar, caso nao tenha, volte novamente mais tarde.\r\n\r\nVoce possui (#e" + cm.getPlayer().getCSPoints(4) + "#n) Cash / LeaderPoints (#e" + cm.getPlayer().getCSPoints(2) + "#n).\r\n\#L0#Trocar Cash#l\r\n\#L1#Trocar LeaderPoints#l\r\n\#L3#Trocar Mesos#l\r\n\#L2##r#eComo obter pontos?#k#l#n");
}else if (status == 1){
if (selection == 0) {
cm.sendSimple("                                #e#r<Cash - Troca>#k#n\r\n\r\n#L20#Trocar - 1.500 (Cash) por (1) Item Megaphone#l");
}else if (selection == 1){
cm.sendSimple("                                #e#r<LeaderPoints - Troca>#k#n\r\n\r\n#L23#Trocar - 4 (LeaderPoints) por (1) Incubadora#l");
} else if (selection == 3){
cm.sendSimple("                                #e#r<Mesos - Troca>#k#n\r\n\r\n#L24#Trocar - 100,000 (Mesos) por (1) Ovo de Pigmeu#l");
} else if (selection == 2){
cm.sendSimple("#e#r<Cash - Infos>#k#n\r\nO Cash pode ser obtido atraves dos monstros dentro do jogo, com chances aleatorias de cair um cartao de NX.#e#r\r\n\r\n<LeaderPoints - Infos>#k#n\r\nSao pontos de doacao, que tambem podem ser obtidos pelo site #bwww.leaderms.com.br/leadermalls#n");
}
} else if (status == 2){
if (selection == 20){
if (cm.getPlayer().getCSPoints(4) >= 1500){
cm.getPlayer().modifyCSPoints(4, -1500);
cm.gainItem(5076000, 1);
cm.getPlayer().dropMessage("[Cash] Voce perdeu (1.500) de Cash!")
cm.getPlayer().dropMessage("Voce ganhou um Item Megaphone!")
cm.dispose();
}else{
cm.sendOk("Voce nao possui Cash suficiente para efutuar a troca!");
cm.dispose();
}
}else if (selection == 23){
if (cm.getPlayer().getCSPoints(2) >= 4) {
cm.getPlayer().modifyCSPoints(2, -4);
cm.gainItem(5060002, 1)
cm.getPlayer().dropMessage("[LeaderPoints] Voce perdeu (-4) LeaderPoints!")
cm.sendOk("Obrigado, voce ja pode usar seu novo item!");
cm.dispose();
}else{
cm.sendOk("Voce nao possui LeaderPoints suficientes para efutuar a troca!");
cm.dispose();
}
}else if (selection == 24){
if (cm.getPlayer().getMeso() > 100000) {
cm.gainMeso(-100000);
cm.gainItem(4170000, 1)
cm.sendOk("Obrigado, voce ja pode usar seu novo item!");
cm.dispose();
}else{
cm.sendOk("Voce nao possui Mesos suficientes para efutuar a troca!");
cm.dispose();
}
}
}
}
}
 