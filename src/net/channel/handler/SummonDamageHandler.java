package net.channel.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import client.ISkill;
import client.MapleCharacter;
import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import client.SummonSkillEntry;
import client.anticheat.CheatingOffense;
import client.status.MonsterStatusEffect;
import net.AbstractMaplePacketHandler;
import server.AutobanManager;
import server.MapleStatEffect;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleSummon;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;


public class SummonDamageHandler extends AbstractMaplePacketHandler {

    public class SummonAttackEntry {

        private int monsterOid;
        private int damage;

        public SummonAttackEntry(int monsterOid, int damage) {
            super();
            this.monsterOid = monsterOid;
            this.damage = damage;
        }

        public int getMonsterOid() {
            return monsterOid;
        }

        public int getDamage() {
            return damage;
        }
    }

      @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int oid = slea.readInt();
        MapleCharacter player = c.getPlayer();
        Collection<MapleSummon> summons = player.getSummons().values();
        MapleSummon summon = null;
        for (MapleSummon sum : summons)
            if (sum.getObjectId() == oid)
                summon = sum;
        if (summon == null) {
            System.out.println(MapleClient.getLogMessage(c, "Using summon attack without a summon"));
            return;
        }
        ISkill summonSkill = SkillFactory.getSkill(summon.getSkill());
        MapleStatEffect summonEffect = summonSkill.getEffect(summon.getSkillLevel());
        slea.skip(5);
        List<SummonAttackEntry> allDamage = new ArrayList<SummonAttackEntry>();
        int numAttacked = slea.readByte();
        player.getCheatTracker().checkSummonAttack();
        for (int x = 0; x < numAttacked; x++) {
            int monsterOid = slea.readInt(); // attacked oid
            slea.skip(14); // who knows
            int damage = slea.readInt();
            allDamage.add(new SummonAttackEntry(monsterOid, damage));
        }
        if (!player.isAlive()) {
            player.getCheatTracker().registerOffense(CheatingOffense.ATTACKING_WHILE_DEAD);
            return;
        }
        player.getMap().broadcastMessage(player, MaplePacketCreator.summonAttack(player.getId(), summon.getSkill(), 4, allDamage), summon.getPosition());
        for (SummonAttackEntry attackEntry : allDamage) {
            int damage = attackEntry.getDamage();
            MapleMonster target = player.getMap().getMonsterByOid(attackEntry.getMonsterOid());
            if (target != null) {
                if (damage > 0 && summonEffect.getMonsterStati().size() > 0)
                    if (summonEffect.makeChanceResult()) {
                        MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(summonEffect.getMonsterStati(), summonSkill, false);
                        target.applyStatus(player, monsterStatusEffect, summonEffect.isPoison(), 4000);
                    }
                player.getMap().damageMonster(player, target, damage);
            }
        }
    }
}