 /* 
 * @author Soulfist 
 * LeaderMS 2014
 */ 

importPackage(Packages.tools); 

var jqpoints = 3;

function start() { 
    cm.sendSimple("It seems that you managed to get to the top of the mission. Congratulations. #h #... \r\n\r\n\t#b#L0#Yes, I did it!#l"); 
} 

function action(m,t,s) { 
    cm.dispose(); 
    if(m > 0){ 
         cm.warp(105040300);
cm.dispose();
    } 
}  