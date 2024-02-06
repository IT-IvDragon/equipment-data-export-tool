package com.example.e7tools.converter;

import com.example.e7tools.model.Stat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * todo:仅实现正常的功能，代码还需要优化
 */
public class Converter {

    private static Map<String, String> statByIngameStat = new HashMap<>();
    private static Map<String, Integer> countByRank = new HashMap<>();
    private static Map<String, Integer> offsetByRank = new HashMap<>();
    private static Map<String, String> gearByIngameType = new HashMap<>();
    private static Map<String, String> gearByGearLetter = new HashMap<>();
    private static Map<String, String> setsByIngameSet = new HashMap<>();
    private static final List<String> rankByIngameGrade = new ArrayList<>();

    static {
        rankByIngameGrade.add("Unknown");
        rankByIngameGrade.add("Normal");
        rankByIngameGrade.add("Good");
        rankByIngameGrade.add("Rare");
        rankByIngameGrade.add("Heroic");
        rankByIngameGrade.add("Epic");

        statByIngameStat.put("att_rate", "AttackPercent");
        statByIngameStat.put("max_hp_rate", "HealthPercent");
        statByIngameStat.put("def_rate", "DefensePercent");
        statByIngameStat.put("att", "Attack");
        statByIngameStat.put("max_hp", "Health");
        statByIngameStat.put("def", "Defense");
        statByIngameStat.put("speed", "Speed");
        statByIngameStat.put("res", "EffectResistancePercent");
        statByIngameStat.put("cri", "CriticalHitChancePercent");
        statByIngameStat.put("cri_dmg", "CriticalHitDamagePercent");
        statByIngameStat.put("acc", "EffectivenessPercent");
        statByIngameStat.put("coop", "DualAttackChancePercent");

        countByRank.put("Normal", 5);
        countByRank.put("Good", 6);
        countByRank.put("Rare", 7);
        countByRank.put("Heroic", 8);
        countByRank.put("Epic", 9);

        offsetByRank.put("Normal", 0);
        offsetByRank.put("Good", 1);
        offsetByRank.put("Rare", 2);
        offsetByRank.put("Heroic", 3);
        offsetByRank.put("Epic", 4);

        gearByIngameType.put("weapon", "Weapon");
        gearByIngameType.put("helm", "Helmet");
        gearByIngameType.put("armor", "Armor");
        gearByIngameType.put("neck", "Necklace");
        gearByIngameType.put("ring", "Ring");
        gearByIngameType.put("boot", "Boots");

        gearByGearLetter.put("w", "Weapon");
        gearByGearLetter.put("h", "Helmet");
        gearByGearLetter.put("a", "Armor");
        gearByGearLetter.put("n", "Necklace");
        gearByGearLetter.put("r", "Ring");
        gearByGearLetter.put("b", "Boots");

        setsByIngameSet.put("set_acc", "HitSet");
        setsByIngameSet.put("set_att", "AttackSet");
        setsByIngameSet.put("set_coop", "UnitySet");
        setsByIngameSet.put("set_counter", "CounterSet");
        setsByIngameSet.put("set_cri_dmg", "DestructionSet");
        setsByIngameSet.put("set_cri", "CriticalSet");
        setsByIngameSet.put("set_def", "DefenseSet");
        setsByIngameSet.put("set_immune", "ImmunitySet");
        setsByIngameSet.put("set_max_hp", "HealthSet");
        setsByIngameSet.put("set_penetrate", "PenetrationSet");
        setsByIngameSet.put("set_rage", "RageSet");
        setsByIngameSet.put("set_res", "ResistSet");
        setsByIngameSet.put("set_revenge", "RevengeSet");
        setsByIngameSet.put("set_scar", "InjurySet");
        setsByIngameSet.put("set_speed", "SpeedSet");
        setsByIngameSet.put("set_vampire", "LifestealSet");
        setsByIngameSet.put("set_shield", "ProtectionSet");
        setsByIngameSet.put("set_torrent", "TorrentSet");
    }

    public static List<Map<String, Object>> convertUnits(List<List<Map<String, Object>>> rawUnits, String scanType) {
        List<Map<String, Object>> lastRawUnit = rawUnits.get(rawUnits.size() - 1);

        for (Map<String, Object> rawUnit : lastRawUnit) {
            if (rawUnit.getOrDefault("name", null) == null || rawUnit.getOrDefault("id", null) == null) {
                continue;
            }

            rawUnit.put("stars", rawUnit.getOrDefault("g", 0));
            rawUnit.put("awaken", rawUnit.getOrDefault("z", 0));

        }

        String filterType = "optimizer";
        if (scanType.equals("heroes")) {
            filterType = "sixstar";
        }
        return lastRawUnit.stream().filter(x -> x.containsKey("name")).collect(Collectors.toList());
    }

    public static List<Map<String, Object>> convertItems(List<Map<String, Object>> rawItems, String scanType) {
        List<Map<String, Object>> filteredItems = rawItems.stream()
                .filter(item -> item.containsKey("f"))
                .collect(Collectors.toList());
        for (Map<String, Object> rawItem : filteredItems) {
            convertGear(rawItem);
            convertRank(rawItem);
            convertSet(rawItem);
            convertName(rawItem);
            convertLevel(rawItem);
            convertEnhance(rawItem);
            convertMainStat(rawItem);
            convertSubStats(rawItem);
            convertId(rawItem);
            convertEquippedId(rawItem);
        }

        return filterItems(filteredItems, scanType);
    }

    private static void convertId(Map<String, Object> item) {
        item.put("ingameId", item.get("id"));
    }

    private static void convertEquippedId(Map<String, Object> item) {
        item.put("ingameEquippedId", item.containsKey("p") ? String.valueOf(item.get("p")) : null);
    }

    private static List<Map<String, Object>> filterItems(List<Map<String, Object>> rawItems, String scanType) {
        int enhanceLimit = 6;
        if (scanType.equals("heroes")) {
            enhanceLimit = 15;
        } else if (scanType.equals("items")) {
            enhanceLimit = 6;
        }

        List<Map<String, Object>> filteredItems = new ArrayList<>();
        for (Map<String, Object> item : rawItems) {
            if ((int) item.get("enhance") >= enhanceLimit) {
                filteredItems.add(item);
            }
        }

        return filteredItems;
    }

    private static void convertSubStats(Map<String, Object> item) {
        Map<String, Map<String, Object>> statAcc = new HashMap<>();

        List<List<Object>> ops = (List<List<Object>>) item.get("op");
        for (int i = 1; i < ops.size(); i++) {
            List<Object> op = ops.get(i);
            String opType = null;
            Number opValue = 0;
            String annotation = null;
            Boolean modification = null;

            if (op.size() >= 1) {
                opType = (String) op.get(0);
            }
            if (op.size() >= 2) {
                opValue = (Number) op.get(1);
            }
            if (op.size() >= 3) {
                annotation = (String) op.get(2);
            }
//            if (op.size() >= 4) {
//                modification = (Boolean) op.get(3);
//            }

            String type = statByIngameStat.get(opType);
            double value = isFlat(opType) ? opValue.doubleValue() : Math.round(opValue.doubleValue() * 100);

            if (statAcc.containsKey(type)) {
                Map<String, Object> acc = statAcc.get(type);
                acc.put("value", (double) acc.get("value") + value);
                if (annotation != null) {
                    if (annotation.equals("u")) {

                    } else if (annotation.equals("c")) {
                        acc.put("modified", true);
                    } else {
                        acc.put("rolls", (int) acc.get("rolls") + 1);
                        acc.put("ingameRolls", (int) acc.get("ingameRolls") + 1);
                    }
                }

            } else {
                Map<String, Object> acc = new HashMap<>();
                acc.put("value", value);
                acc.put("rolls", 1);
                acc.put("ingameRolls", 1);
                statAcc.put(type, acc);
            }
        }

        List<Stat> substats = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : statAcc.entrySet()) {
            String key = entry.getKey();
            Map<String, Object> acc = entry.getValue();
            double value = (double) acc.get("value");
            Integer rolls = (int) acc.get("rolls");
            Boolean modified = (Boolean) acc.get("modified");
            substats.add(new Stat(key, value, rolls, modified));
        }

        item.put("substats", substats);
    }

    private static void convertMainStat(Map<String, Object> item) {
        List<List> op = (List<List>) item.get("op");
        List<Object> mainOp = op.get(0);
        String mainOpType = (String) mainOp.get(0);

        Number mainOpValue = (Number) item.getOrDefault("mainStatValue", 0);
        String mainType = statByIngameStat.get(mainOpType);
        double fixedMainValue = isFlat(mainOpType) ? mainOpValue.doubleValue() : Math.round(mainOpValue.doubleValue() * 10) / 10;
        item.put("main", new Stat(mainType, fixedMainValue));
    }

    private static void convertEnhance(Map<String, Object> item) {
        String rank = (String) item.get("rank");
        List subs = (List) item.get("op");
        int count = Math.min(subs.toArray().length - 1, countByRank.get(rank));
        int offset = offsetByRank.get(rank);

        item.put("enhance", Math.max((count - offset) * 3, 0));

    }

    private static void convertLevel(Map<String, Object> item) {
        if (!item.containsKey("level")) {
            item.put("level", 0);
        }
    }

    private static void convertName(Map<String, Object> item) {
        if (!item.containsKey("name")) {
            item.put("name", "Unknown");
        }
    }

    private static void convertRank(Map<String, Object> item) {
        item.put("rank", rankByIngameGrade.get(Integer.valueOf(item.get("g").toString())));
    }

    private static void convertGear(Map<String, Object> item) {
        if (!item.containsKey("type")) {
            String baseCode = item.get("code").toString().split("_")[0];
            String gearLetter = String.valueOf(baseCode.charAt(baseCode.length() - 1));
            item.put("gear", gearByGearLetter.get(gearLetter));
        } else {
            item.put("gear", gearByIngameType.get((String) item.get("type")));
        }
    }

    private static void convertSet(Map<String, Object> item) {
        item.put("set", setsByIngameSet.get(item.get("f")));
    }

    private static boolean isFlat(String text) {
        return text.equals("max_hp") || text.equals("speed") || text.equals("att") || text.equals("def");
    }
}