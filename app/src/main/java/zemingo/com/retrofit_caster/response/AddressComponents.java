package zemingo.com.retrofit_caster.response;

import com.google.gson.annotations.SerializedName;

public class AddressComponents
{
    @SerializedName("long_name")
    private String mLongName;

    @SerializedName("short_name")
    private String mShortName;

    public String getLongName()
    {
        return mLongName;
    }

    public String getShortName()
    {
        return mShortName;
    }
}
