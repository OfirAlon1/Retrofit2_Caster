package zemingo.com.retrofit_caster.utils;

import com.google.gson.annotations.SerializedName;

public class ApiError
{
    /**
     * statusCode : 401
     * message : Something
     */

    @SerializedName("statusCode")
    private int mStatusCode;
    @SerializedName("message")
    private String mMessage;

    public int getStatusCode()
    {
        return mStatusCode;
    }

    public void setStatusCode(int statusCode)
    {
        mStatusCode = statusCode;
    }

    public String getMessage()
    {
        return mMessage;
    }

    public void setMessage(String message)
    {
        mMessage = message;
    }
}