/*  Cliff - Happy Village NPC
 */
importPackage(Packages.client);
importPackage(Packages.server);

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
    if (status == 0) {
        cm.sendNext("                                  <#e"+cm.getServerName()+" Halloween#n>             \r\n\r\nHello #e#h ##n, I am Malady, a "+cm.getServerName()+" assistant.\r\nEstou needs your help to #bcollect#k an item from another world. If you collect this item for me, I will help you grow.\r\nAll right, the item I need is:\r\n\r\n#i4000524# #t4000524# - Qnty. 500\r\n\r\nYou currently have (#e" + cm.getPlayer().countItem(4000524) + "#n) #t4000524#.\r\n\r\nIf you already #epossess#n these items, click continue. If not, come back again later.");
    } else if (status == 1) {
        cm.sendSimple("Which item would you like to exchange? \r\n\r\n#L0#Hat Speaker - Random Attributes#l");
        
    } else if (selection == 1000){
                 cm.sendSimple("#eGetting LeaderPoints - Mini Tutorial#n\r\nLeaderPoints are acquired through monsters. Each monster you defeat has a chance to earn an amount of LeaderPoints, ranging from 1 to 3.\r\n\r\n#eGetting Occupation - Mini Tutorial#n\r\nYou should attend the vast majority of missions around "+cm.getServerName()+", from which you can accumulate points in order to get an occupation! ");
    } else if(selection == 0) { /* HERO */
      if(cm.haveItem(4000524, 1)) {
            var ii = MapleItemInformationProvider.getInstance();
            var newItem = ii.randomizeStatsMalady(ii.getEquipById(1000027));
            MapleInventoryManipulator.addFromDrop(cm.getC(), newItem, "");
            cm.gainItem(4000524, -1);
            cm.sendOk("Thank you! Enjoy your new item!");
            cm.dispose();
        } else {
        cm.sendOk("Too bad, you do not have the required items.");
        cm.dispose();
   } 
 }
} 
