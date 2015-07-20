package dash.scheduling;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

@WebListener
public class QuartzInitServletContextListener implements ServletContextListener {

	private static Scheduler scheduler = null;
	
	private Timer dailyTimer;
	
	private Timer timeoutTimer;
	
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		scheduleDailyInitJob(sce);
		scheduleTimeoutJob(sce);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		try {
			scheduler.shutdown();
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dailyTimer.cancel();
		dailyTimer.purge();
		timeoutTimer.cancel();
		timeoutTimer.purge();
		//Sleep the thread in order to allow the scheduler and timers
		//to finish. Prevents errors and memory leaks
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	private void scheduleTimeoutJob(ServletContextEvent sce) {
		timeoutTimer = new Timer();// The timer thread needs to be a
											 // daemon
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, cal.get(Calendar.MINUTE) % 5);
		Date start = cal.getTime();
		timeoutTimer.scheduleAtFixedRate(new TimeoutTask(sce), start,
				TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES));
	}

	private void scheduleDailyInitJob(ServletContextEvent sce) {
		dailyTimer = new Timer();// The timer thread needs to be a
											// daemon
		try {
			scheduler = new StdSchedulerFactory().getScheduler();
			scheduler.start();
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Calendar cal = Calendar.getInstance();
		/*cal.add(Calendar.DATE, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 5);// Scheduling at 12:05am removes midnight
									// ambiguity
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);*/
		cal.add(Calendar.SECOND, 60);
		Date midnightDate = cal.getTime();
		System.out.println("Scheduling DailyTask");
		dailyTimer.scheduleAtFixedRate(new DailyInitTask(sce, scheduler),
				midnightDate, TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)); // Executes
																				// daily
	}

}
