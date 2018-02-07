package yhb.chorus.entity.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by yhb on 18-2-6.
 */

public class GankResponse {
    @SerializedName("error")
    @Expose
    private Boolean error;
    @SerializedName("results")
    @Expose
    private List<GanHuo> mGanHuos = null;

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public List<GanHuo> getGanHuos() {
        return mGanHuos;
    }

    public void setGanHuos(List<GanHuo> ganHuos) {
        this.mGanHuos = ganHuos;
    }
}
