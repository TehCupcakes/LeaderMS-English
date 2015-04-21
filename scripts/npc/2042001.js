/*
  [Monster Carnival PQ] [By Haiku01]
*/
importPackage(Packages.server.maps);
 
var status = 0;
var rnk = 0;
 
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
    if (cm.getChar().getMap().getId() == 220000000) {
        if (status == 0) {
            cm.sendSimple("Hello there, my name is Spieglemann, and I host the Monster Carnival. I am trying to find players who are willing to fight for there lives against each other in a fair competition, and that is why I host the Monster Carnival PQ.\r\n\r\n#L0#I would like to participate#l\r\n#L1#What is Monster Carnival?#l\r\n#L2#Detailed Information#l\r\n#L3#Trade Maple Coins for Equipment#l");
        } else if (status == 1) {
            if (selection == 0) {
                if (cm.getLevel() > 29 && cm.getLevel() < 51) {
                    cm.getChar().saveLocation(SavedLocationType.MONSTER_CARNIVAL);
                    cm.warp(980000000, 0);
                    cm.dispose();
                    return;
                } else {
                    cm.sendOk("Im sorry, but only the users within Level 30~50 may\r\n participate in Monster Carnival.");
                    cm.dispose();
                    return;
                }
            } else if (selection == 1) {
                cm.sendOk("What's a Monster Carnival? Hahaha! Let's just say that it's an experience you will never forget! It's a battle against other travelers like yourself! I know that it is way too dangerous for you to fight one another using real weapons; nor would I suggest such an barbaric act. No my friend, what I offer is competition. The thrill of battle and excitement against people just as strong and motivated as yourself. I offer the premise that your party and the opposing party both sum monsters, and defeat the monsters summoned by the opposing party. That's the essemce of the Monster Carnival. Also, you can use Maple Coins earned during the Monster Carnival to obtain new items and weapons! Of course, it's not as simple as that. There are different ways to prevent the other party from hunting monsters, and it's up to you to figure out how. What do you think? Interested in a little friendly (or not-so-friendly) competition?");
                cm.dispose();
                status = 0;
        } else if (selection == 2) {}
            cm.sendOk("No coding for this yet.");
            cm.dispose();
            status = 0;
        } else if (selection == 3) {
            cm.sendOk("I have not yet been coded to trade Maple Coins.");
            cm.dispose();
            status = 0;
        }
    } else if (cm.getChar().getMap().isCPQWinnerMap()) {
        if (status == 0) {
            if (cm.getChar().getParty() != null) {
                var shi = "Congratulations on your victory!!! That was an amazing performance! The opposing party couldn't do a thing! I'll be expecting the same effort next time!\r\n\r\nMonster Carnival Rank: ";
                switch (cm.calculateCPQRanking()) {
                    case 1:
                        shi += "A";
                        rnk = 1;
                        cm.sendOk(shi);
                        break;
                    case 2:
                        shi += "B";
                        rnk = 2;
                        cm.sendOk(shi);
                        break;
                    case 3:
                        shi += "C";
                        rnk = 3;
                        cm.sendOk(shi);
                        break;
                    case 4:
                        shi += "D";
                        rnk = 4;
                        cm.sendOk(shi);
                        break;
                    default:
                        cm.sendOk("There has been an error with Monster Carnival.");
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
            }
        }
      } else if (cm.getChar().getMap().isCPQLoserMap()) {
        if (status == 0) {
          if (cm.getChar().getParty() != null) {
            var shiu = "Sorry for the loss... Yet, you have put up quite an good show! Your team work and battle skills were amazing! I'll be expecting the same effort next time!\r\n\r\nMonster Carnival Rank: ";
            switch (cm.calculateCPQRanking()) {
              case 10:
                shiu += "A";
				rnk = 1;
                cm.sendOk(shiu);
                break;
              case 20:
                shiu += "B";
				rnk = 2;
                cm.sendOk(shiu);
                break;
              case 30:
                shiu += "C";
				rnk = 3;
                cm.sendOk(shiu);
                break;
              case 40:
                shiu += "D";
				rnk = 4;
                cm.sendOk(shiu);
                break;
              default:
                cm.sendOk("There has been an error with Monster Carnival.");
            }
          } else {
            cm.warp(980000000, 0);
            cm.dispose();
          }
        }
    } else if (status == 1) {
        switch (rnk) {
            case 10:
                cm.warp(980000000, 0);
                cm.gainExp(10000);
                    cm.dispose();
                    break;
                case 20:
                    cm.warp(980000000, 0);
                    cm.gainExp(8500);
                    cm.dispose();
                    break;
                case 30:
                    cm.warp(980000000, 0);
                    cm.gainExp(7000);
                    cm.dispose();
                    break;
                case 40:
                    cm.warp(980000000, 0);
                    cm.gainExp(4550);
                    cm.dispose();
                    break;
            }
        }
      }
      }

