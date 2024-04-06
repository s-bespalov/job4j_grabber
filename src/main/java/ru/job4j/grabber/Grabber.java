package ru.job4j.grabber;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {
    private final Parse parse;
    private final Store store;
    private final Scheduler scheduler;
    private final int time;
    private final int port;

    public Grabber(Parse parse, Store store, Scheduler scheduler, int time, int port) {
        this.parse = parse;
        this.store = store;
        this.scheduler = scheduler;
        this.time = time;
        this.port = port;
    }

    @Override
    public void init() throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(time)
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public void web(Store store) {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(port)) {
                while (!server.isClosed()) {
                    Socket socket = server.accept();
                    try (OutputStream out = socket.getOutputStream()) {
                        out.write(("HTTP/1.1 200 OK\r\n"
                                + "Content-type: text/html; charset=utf-8\r\n"
                                + "\r\n\r\n").getBytes());
                        out.write("<html><head><title>Posts</title></head><body>".getBytes());
                        for (Post post : store.getAll()) {
                            out.write(("<p>" + post.toString() + "</p>").getBytes());
                        }
                        out.write("</body></html>".getBytes());
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static class GrabJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            var map = context.getJobDetail().getJobDataMap();
            var store = (Store) map.get("store");
            var parse = (Parse) map.get("parse");
            var vacancies = parse.list(HabrCareerParse.SOURCE_LINK);
            vacancies.forEach(store::save);
        }
    }

    public static void main(String[] args) throws Exception {
        var config = new Properties();
        try (InputStream input = Grabber.class.getClassLoader()
                .getResourceAsStream("app.properties")) {
            config.load(input);
        }
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        var parse = new HabrCareerParse(new HabrCareerDateTimeParser());
        var store = new PsqlStore(config);
        var time = Integer.parseInt(config.getProperty("time"));
        var port = Integer.parseInt(config.getProperty("port"));
        var grab = new Grabber(parse, store, scheduler, time, port);
        grab.init();
        grab.web(store);
    }
}