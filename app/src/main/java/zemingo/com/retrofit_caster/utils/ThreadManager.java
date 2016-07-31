package zemingo.com.retrofit_caster.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


public class ThreadManager
{
	private static ExecutorService msExecutor;
	private static ExecutorService msNetworkExecuters;

	static
	{
		msExecutor = Executors.newFixedThreadPool(5, new NamedThreadFactory());
		msNetworkExecuters = Executors.newFixedThreadPool(2, new NamedThreadFactory());
	}

	public static void execute(Runnable runnable)
	{
		msExecutor.execute(runnable);
	}

	public static ExecutorService getNetworkExecutors()
	{
		return msNetworkExecuters;
	}

	private static class NamedThreadFactory implements ThreadFactory
	{
		private static int msCounter = 0;

		public Thread newThread(Runnable r)
		{
			return new Thread(r, "Worker thread " + String.valueOf(msCounter++));
		}
	}
}
