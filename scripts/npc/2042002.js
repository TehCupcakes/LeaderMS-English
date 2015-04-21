/*
  [Monster Carnival PQ] [By Haiku01]
  Update 1: Fixed bracket errors..
*/
importPackage(Packages.server.maps);

var status = 0;
var rnk = -1;

function start() {
    status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (status >= 0 && mode == 0) {
			cm.sendOk("Alright then, I hope we can chat later next time.");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		 if (cm.getPlayer().getMapId() == 980000010) {
			if (status == 0) {
				cm.sendNext("I hope you had fun in Monster Carnival. See you later!");
			} else if (status > 0) {
				cm.warp(980000000);
				cm.dispose();
			}
		} else if (cm.getChar().getMap().isCPQLoserMap()) {
	        if (status == 0) {
	          if (cm.getChar().getParty() != null) {
	            var shiu = "Sorry for the loss... Yet, you have put up quite an good show! Your team work and battle skills were amazing! I'll be expecting the same effort next time!\r\n\r\nMonster Carnival Rank: ";
	            switch (cm.calculateCPQRanking()) {
	              case 0:
	                shiu += "#rA#k";
					 rnk = 1;
	                cm.sendOk(shiu);
	                break;
	              case 1:
	                shiu += "#rB#k";
					 rnk = 2;
	                cm.sendOk(shiu);
	                break;
	              case 2:
	                shiu += "#rC#k";
					 rnk = 3;
	                cm.sendOk(shiu);
	                break;
	              case 3:
	                shiu += "#rD#k";
					 rnk = 4;
	                cm.sendOk(shiu);
	                break;
	              default:
	                cm.sendOk("Even though you lost here is your exp.");
					cm.dispose();
					break;
	            }
	          } else {
	            cm.warp(980000000, 0);
	            cm.dispose();
	          }
	        } else if (status == 1) {
		        switch (rnk) {
		            case 1:
						cm.warp(980000000, 0);
						cm.gainExp(10000);
						cm.dispose();
						break;
					case 2:
						cm.warp(980000000, 0);
						cm.gainExp(8500);
						cm.dispose();
						break;
					case 3:
						cm.warp(980000000, 0);
						cm.gainExp(7000);
						cm.dispose();
						break;
					case 4:
						cm.warp(980000000, 0);
						cm.gainExp(4550);
						cm.dispose();
						break;
					default:
						cm.playerMessage("Wow you lost but still got 300 points?");
						cm.warp(980000000, 0);
						cm.gainExp(10000);
						cm.dispose();
						break;
				}
			}
        } else if (cm.getChar().getMap().isCPQWinnerMap()) {
	        if (status == 0) {
	            if (cm.getChar().getParty() != null) {
	                var shi = "Congratulations on your victory!!! That was an amazing performance! The opposing party couldn't do a thing! I'll be expecting the same effort next time!\r\n\r\nMonster Carnival Rank: ";
	                switch (cm.calculateCPQRanking()) {
	                    case 1:
	                        shi += "#rA#k";
	                        rnk = 1;
	                        cm.sendOk(shi);
	                        break;
	                    case 2:
	                        shi += "#rB#k";
	                        rnk = 2;
	                        cm.sendOk(shi);
	                        break;
	                    case 3:
	                        shi += "#rC#k";
	                        rnk = 3;
	                        cm.sendOk(shi);
	                        break;
	                    case 4:
	                        shi += "#rD#k";
	                        rnk = 4;
	                        cm.sendOk(shi);
	                        break;
	                    default:
	                        cm.sendOk("Nice gaining all those points!");
	                }
	            } else {
	                cm.warp(980000000, 0);
	                cm.dispose();
				}
			} else if (status == 1) {
	            switch (rnk) {
	                case 1:
	                    cm.warp(980000000, 0);
	                    cm.gainExp(30000);
	                    cm.dispose();
	                    break;
	                case 2:
	                    cm.warp(980000000, 0);
	                    cm.gainExp(25500);
	                    cm.dispose();
	                    break;
	                case 3:
	                    cm.warp(980000000, 0);
	                    cm.gainExp(21000);
	                    cm.dispose();
	                    break;
	                case 4:
	                    cm.warp(980000000, 0);
	                    cm.gainExp(19550);
	                    cm.dispose();
	                    break;
					default:
						cm.playerMessage("You have gained over 300 points, nice!");
						cm.warp(980000000, 0);
	                    cm.gainExp(30000);  // added the exp on this
	                    cm.dispose();
						break;
	            }
	        }
		} else {
	        if (status == 0) {
	            cm.sendSimple("Hello there, my name is Spieglemann, and I host the Monster Carnival. I am trying to find players who are willing to fight for there lives against each other in a fair competition, and that is why I host the Monster Carnival PQ.\r\n\r\n#L0#I would like to participate#l\r\n#L1#What is Monster Carnival?#l\r\n#L2#Detailed Information#l\r\n#L3#Trade Maple Coins for Equipment#l");
	        } else if (status == 1) {
	            if (selection == 0) {
	                if ((cm.getLevel() > 29 && cm.getLevel() < 51) || cm.getPlayer().isGM()) {
	                    cm.getChar().saveLocation(SavedLocationType.MONSTER_CARNIVAL);
	                    cm.warp(980000000, 0);
	                    cm.dispose();
	                    return;
	                } else {
	                    cm.sendOk("I'm sorry, but only the users within Level 30~50 may\r\n participate in Monster Carnival.");
	                    cm.dispose();
	                    return;
	                }
	            } else if (selection == 1) {
	                cm.sendOk("Monster Carnival is unlike other PQs, in that this is a competitive party quest pitting your party against another party. Your goal is to collect the most amount of Carnival Points (CP) by killing monsters from the opposing party, and these CP, in turn, can be used to perform 3 tasks to distract the opposing party: Summon a Monster, Skill, and Protector. There are also designated hotkeys that can be used when selecting one of these three tabs to activate it. Inside you can find #bMaple Coins#k, which later can be exchanged for some items!");
	                cm.dispose();
		        } else if (selection == 2) {
		            cm.sendOk("Well it's simple, all you have to do is enter #bMonster Carnival#k and create a party. Once you created a party you could talk to me inside and you could choose a room to wait a receive fights against another party. Once your inside and fighting, you will be able to pick up potions to heal you or even pick up curse potions to curse the other team! You will receive #rCP#k when you fight and you can exchange those to spawn monsters for the other team or spawn curses on them or you could just buff yourself! But overall, your main goal is to achieve the most CP and be the winner. Enjoy!");
		            cm.dispose();
		        } else if (selection == 3) {
		            cm.openNpc(9100002);
		        }
			}
		}
	}
}
