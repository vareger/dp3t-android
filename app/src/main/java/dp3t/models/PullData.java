
package dp3t.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PullData {

    @SerializedName("create_date")
    private String createDate;
    @Expose
    private int id;
    @Expose
    private int status;
    @Expose
    private String token;
    @SerializedName("user_id")
    private int userId;

    public long getCreateDate() {
        //2020-05-05T10:35:57.927Z
        long milliseconds = 0l;
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss a");
        try {
            Date d = f.parse(createDate);
            milliseconds = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return milliseconds;
        }
        return milliseconds;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public PullData(String createDate, int id, int status, String token, int userId) {
        this.createDate = createDate;
        this.id = id;
        this.status = status;
        this.token = token;
        this.userId = userId;
    }
}
