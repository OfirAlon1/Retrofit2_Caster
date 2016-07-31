package zemingo.com.retrofit_caster.utils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by yanivsos on 20/06/16.
 * Zemingo Ltd.
 */
@SuppressWarnings("unused")
public class ServerError extends ArrayList<ServerError.ServerErrorInternal>
{
    static class ServerErrorInternal
    {

        /**
         * ErrorMessage : Option does not exist
         * ReturnCode : 0
         * ErrorNumber : 900
         */

        @SerializedName("ErrorMessage")
        private String mErrorMessage;
        @SerializedName("ReturnCode")
        private int mReturnCode;
        @SerializedName("ErrorNumber")
        private int mErrorNumber;

        public String getErrorMessage()
        {
            return mErrorMessage;
        }

        public int getReturnCode()
        {
            return mReturnCode;
        }

        public int getErrorNumber()
        {
            return mErrorNumber;
        }

    }

    public String getErrorMessage()
    {
        return get(0).getErrorMessage();
    }

    public int getReturnCode()
    {
        return get(0).getReturnCode();
    }

    public int getErrorNumber()
    {
        return get(0).getErrorNumber();
    }
}
