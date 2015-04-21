/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package server.maps;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import tools.FilePrinter;

// This is here temporary, to test out if the spawn bug is with map object

public class MapTimer {

    private static MapTimer instance = new MapTimer();
    private ScheduledThreadPoolExecutor ses;

    public static MapTimer getInstance() {
	return instance;
    }

    public void start() {
	if (ses != null && !ses.isShutdown() && !ses.isTerminated()) {
	    return; //starting the same timermanager twice is no - op
	}

	final ThreadFactory thread = new ThreadFactory() {

	    private final AtomicInteger threadNumber = new AtomicInteger(1);

	    @Override
	    public Thread newThread(Runnable r) {
		final Thread t = new Thread(r);
		t.setName("Timermanager-Worker-" + threadNumber.getAndIncrement());
		return t;
	    }
	};

	final ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(3, thread);
	stpe.setKeepAliveTime(10, TimeUnit.MINUTES);
	stpe.allowCoreThreadTimeOut(true);
	stpe.setCorePoolSize(3);
	stpe.setMaximumPoolSize(5);
	stpe.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
	ses = stpe;
    }

    public void stop() {
	ses.shutdown();
    }

    public ScheduledFuture<?> register(Runnable r, long repeatTime, long delay) {
	return ses.scheduleAtFixedRate(new LoggingSaveRunnable(r), delay, repeatTime, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> register(Runnable r, long repeatTime) {
	return ses.scheduleAtFixedRate(new LoggingSaveRunnable(r), 0, repeatTime, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> schedule(Runnable r, long delay) {
	return ses.schedule(new LoggingSaveRunnable(r), delay, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> scheduleAtTimestamp(Runnable r, long timestamp) {
	return schedule(r, timestamp - System.currentTimeMillis());
    }

    private class LoggingSaveRunnable implements Runnable {

	Runnable r;

	public LoggingSaveRunnable(final Runnable r) {
	    this.r = r;
	}

	@Override
	public void run() {
	    try {
		r.run();
	    } catch (Throwable t) {
		FilePrinter.printError(FilePrinter.Map_Log, t);
	    }
	}
    }
}