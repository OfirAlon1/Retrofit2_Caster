package zemingo.com.retrofit_caster.utils;

public class NetworkError<S>
{
    public NetworkError(S error)
    {
        mError = error;
    }

    private S mError;

    public S getSpecificError()
    {
        return mError;
    }

}
