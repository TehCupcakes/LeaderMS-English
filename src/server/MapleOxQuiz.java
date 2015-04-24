/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import client.MapleCharacter;
import client.MapleStat;
import server.maps.MapleMap;
import tools.packet.MaplePacketCreator;

/**
 *
 * @author Jay Estrella
 */
public class MapleOxQuiz {

    private int round = 1;
    private int question = 1;
    private MapleMap map = null;
    private long delay = 5 * 1000; // Default delay
    private int expGain = 400; // WITHOUT Exp Rate.

    public MapleOxQuiz(MapleMap map, int round, int question) {
        this.map = map;
        this.round = round;
        this.question = question;
		map.setOx(this);
    }

    public void checkAnswers() {
        for (MapleCharacter chr : map.getCharacters()) {
            double x = chr.getPosition().getX();
            double y = chr.getPosition().getY();
            int answer = MapleOxQuizFactory.getOXAnswer(round, question);
            boolean correct = false;
            if (x > -234 && y > -26) { // False
                if (answer == 0) {
                    chr.dropMessage("[OXQuiz] Correct!");
                    correct = true;
                } else {
                    chr.dropMessage("[OXQuiz] Incorrect!");
                }
            } else if (x < -234 && y > -26) { // True
                if (answer == 1) {
                    chr.dropMessage("[OXQuiz] Correct!");
                    correct = true;
                } else {
                    chr.dropMessage("[OXQuiz] Incorrect!");
                }
            }
            if (correct) {
                chr.gainExp(expGain * chr.getClient().getChannelServer().getExpRate(), true, false);
            } else {
                chr.setHp(0);
                chr.updateSingleStat(MapleStat.HP, 0);
            }
        }
    }

    public void scheduleOx() {
        TimerManager.getInstance().schedule(new Runnable() {

            public void run() {
                map.broadcastMessage(MaplePacketCreator.serverNotice(6, MapleOxQuizFactory.getOXQuestion(round, question)));
                TimerManager.getInstance().schedule(new Runnable() {

                    public void run() {
                        checkAnswers();
                        scheduleAnswer(map);
                    }
                }, 15 * 1000); // 15 Seconds to respond
            }
        }, delay);
    }

    public void scheduleAnswer(final MapleMap map) {
        TimerManager.getInstance().schedule(new Runnable() {

            public void run() {
                map.broadcastMessage(MaplePacketCreator.serverNotice(6, MapleOxQuizFactory.getOXExplain(round, question)));
                if (map.getOx() != null) { // Set next one if Ox Quiz is still active.
                    scheduleOx();
                } else {
                    map.broadcastMessage(MaplePacketCreator.serverNotice(6, "Ox Quiz Deactivated"));
                }
            }
        }, 1 * 1000);
        doQuestion(); // After we give the response, next question
    }

    public void doQuestion() {
        if (round == 1 && question == 29) { // Weird case, it jumps to 100
            question = 100; // Set it to 100, even if the inc is higher.
        } else if (round == 2 && question == 17) {
            question = 100;
        } else if (round == 3 && question == 17) {
            question = 100;
        } else if (round == 4 && question == 12) {
            question = 100;
        } else if (round == 5 && question == 26) {
            question = 100;
        } else if (round == 6 && question == 16) {
            question = 100;
        } else if (round == 7 && question == 16) {
            question = 100;
        } else if (round == 8 && question == 12) {
            question = 100;
        } else if (round == 9 && question == 44) {
            question = 100;
        } else {
            question++;
        }
    }

    public int getRound() {
        return round;
    }

    public int getQuestion() {
        return question;
    }

    public MapleMap getMap() {
        return map;
    }
}
