package yhb.chorus.entity;

public class LirBean implements Comparable<LirBean>{
    private int time;
    private String liric;

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getLiric() {
        return liric;
    }

    public void setLiric(String liric) {
        this.liric = liric;
    }


    @Override
    public int compareTo(LirBean another) {
        if (this.getTime()>another.getTime()){
            return another.getTime();
        }else{
            return this.getTime();
        }
    }
}
