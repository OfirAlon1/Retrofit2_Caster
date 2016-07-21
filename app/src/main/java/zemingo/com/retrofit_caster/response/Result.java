package zemingo.com.retrofit_caster.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Result
{
//    @SerializedName("address_components")
//    private ArrayList<AddressComponents> mAddressComponents;
//
//    public ArrayList<AddressComponents> getAddressComponents()
//    {
//        return mAddressComponents;
//    }

    @SerializedName("status")
    private String mStatus;

    public String getStatus()
    {
        return mStatus;
    }
}
