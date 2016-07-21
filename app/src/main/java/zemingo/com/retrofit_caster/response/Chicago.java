package zemingo.com.retrofit_caster.response;

import com.google.gson.annotations.SerializedName;

public class Chicago
{
    @SerializedName("results")
    private Result mResult;

    public Result getResult()
    {
        return mResult;
    }
}
