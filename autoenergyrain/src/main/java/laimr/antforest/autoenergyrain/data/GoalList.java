package laimr.antforest.autoenergyrain.data;

/**
 * @author laiyulong
 * @date 2021/11/21 23:32
 */
public enum GoalList {
    FinishEnergyRain3times("FinishEnergyRain3times");

    private String value;
    GoalList(String e) {
        this.value = e;
    }
    public String getValue(){
        return this.value;
    }
}
